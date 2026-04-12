package tech.estacionkus.camerastream.billing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tech.estacionkus.camerastream.data.auth.Supabase
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.model.PlanFeatures
import javax.inject.Inject
import javax.inject.Singleton

private val Context.planDataStore: DataStore<Preferences> by preferencesDataStore(name = "plan_prefs")

enum class PlanTier(val id: String, val displayName: String, val priceDisplay: String) {
    FREE("free", "Free", "Gratis"),
    PRO("pro", "Pro", "$4.99/mo"),
    AGENCY("agency", "Agency", "$14.99/mo")
}

@Serializable
data class LicenseRecord(
    val key: String = "",
    val plan: String = "",
    val user_id: String? = null,
    val active: Boolean = true
)

@Singleton
class StripeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureGate: FeatureGate
) {
    private val TAG = "StripeManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val KEY_PLAN = stringPreferencesKey("active_plan")
        private val KEY_LICENSE = stringPreferencesKey("license_key")
        private val KEY_STRIPE_PRO_URL = stringPreferencesKey("stripe_pro_url")
        private val KEY_STRIPE_AGENCY_URL = stringPreferencesKey("stripe_agency_url")
        // Default URLs — users must configure their own Stripe Checkout links in Settings
        private const val DEFAULT_STRIPE_PRO_URL = ""
        private const val DEFAULT_STRIPE_AGENCY_URL = ""
    }

    private val _currentPlan = MutableStateFlow(PlanTier.FREE)
    val currentPlan: StateFlow<PlanTier> = _currentPlan.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _licenseKey = MutableStateFlow<String?>(null)
    val licenseKey: StateFlow<String?> = _licenseKey.asStateFlow()

    private val _stripeProUrl = MutableStateFlow(DEFAULT_STRIPE_PRO_URL)
    val stripeProUrl: StateFlow<String> = _stripeProUrl.asStateFlow()

    private val _stripeAgencyUrl = MutableStateFlow(DEFAULT_STRIPE_AGENCY_URL)
    val stripeAgencyUrl: StateFlow<String> = _stripeAgencyUrl.asStateFlow()

    fun initialize() {
        scope.launch {
            context.planDataStore.data.first().let { prefs ->
                val planId = prefs[KEY_PLAN] ?: "free"
                val key = prefs[KEY_LICENSE]
                _licenseKey.value = key
                _stripeProUrl.value = prefs[KEY_STRIPE_PRO_URL] ?: DEFAULT_STRIPE_PRO_URL
                _stripeAgencyUrl.value = prefs[KEY_STRIPE_AGENCY_URL] ?: DEFAULT_STRIPE_AGENCY_URL
                val tier = PlanTier.entries.find { it.id == planId } ?: PlanTier.FREE
                updatePlan(tier)
            }
        }
    }

    fun setStripeUrls(proUrl: String, agencyUrl: String) {
        _stripeProUrl.value = proUrl
        _stripeAgencyUrl.value = agencyUrl
        scope.launch {
            context.planDataStore.edit { prefs ->
                prefs[KEY_STRIPE_PRO_URL] = proUrl
                prefs[KEY_STRIPE_AGENCY_URL] = agencyUrl
            }
        }
    }

    fun openStripeCheckout(tier: PlanTier) {
        val url = when (tier) {
            PlanTier.PRO -> _stripeProUrl.value
            PlanTier.AGENCY -> _stripeAgencyUrl.value
            PlanTier.FREE -> return
        }
        if (url.isBlank()) {
            _errorMessage.value = "Stripe checkout URL not configured. Go to Settings to add your Stripe payment link."
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _errorMessage.value = "No se pudo abrir el navegador"
        }
    }

    fun activateLicenseKey(key: String) {
        if (key.isBlank()) {
            _errorMessage.value = "Ingresa una license key"
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        scope.launch {
            try {
                val result = Supabase.client.postgrest["licenses"]
                    .select(Columns.ALL) {
                        filter {
                            eq("key", key.trim())
                            eq("active", true)
                        }
                    }

                val records: List<LicenseRecord> = json.decodeFromString(result.data)
                if (records.isEmpty()) {
                    _errorMessage.value = "Key no valida o ya fue usada"
                    _isLoading.value = false
                    return@launch
                }

                val record = records.first()
                if (record.user_id != null) {
                    _errorMessage.value = "Esta key ya fue activada por otro usuario"
                    _isLoading.value = false
                    return@launch
                }

                val tier = when (record.plan.lowercase()) {
                    "agency" -> PlanTier.AGENCY
                    "pro" -> PlanTier.PRO
                    else -> PlanTier.PRO
                }

                // Store locally
                savePlan(tier, key.trim())
                _successMessage.value = "Plan ${tier.displayName} activado"
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "License validation failed: ${e.message}")
                // Offline mode: validate format and store locally
                if (key.trim().length >= 8) {
                    savePlan(PlanTier.PRO, key.trim())
                    _successMessage.value = "Plan Pro activado (verificacion pendiente)"
                } else {
                    _errorMessage.value = "Error al validar: ${e.message}"
                }
                _isLoading.value = false
            }
        }
    }

    fun activateCoupon(code: String) {
        if (code.isBlank()) {
            _errorMessage.value = "Ingresa un codigo de cupon"
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        scope.launch {
            // Check hardcoded coupon first
            if (code.trim().lowercase() == "creador100") {
                savePlan(PlanTier.PRO, "coupon:creador100")
                _successMessage.value = "Cupon aplicado! Plan Pro activado"
                _isLoading.value = false
                return@launch
            }

            // Check Supabase coupons table
            try {
                val result = Supabase.client.postgrest["coupons"]
                    .select(Columns.ALL) {
                        filter {
                            eq("code", code.trim().uppercase())
                            eq("active", true)
                        }
                    }

                @Serializable
                data class CouponRecord(
                    val code: String = "",
                    val plan: String = "pro",
                    val discount_percent: Int = 0,
                    val max_uses: Int = 0,
                    val current_uses: Int = 0,
                    val active: Boolean = true
                )

                val coupons: List<CouponRecord> = json.decodeFromString(result.data)
                if (coupons.isEmpty()) {
                    _errorMessage.value = "Cupon no valido o expirado"
                    _isLoading.value = false
                    return@launch
                }

                val coupon = coupons.first()
                if (coupon.max_uses > 0 && coupon.current_uses >= coupon.max_uses) {
                    _errorMessage.value = "Este cupon ya alcanzo el limite de usos"
                    _isLoading.value = false
                    return@launch
                }

                if (coupon.discount_percent >= 100) {
                    val tier = when (coupon.plan.lowercase()) {
                        "agency" -> PlanTier.AGENCY
                        else -> PlanTier.PRO
                    }
                    savePlan(tier, "coupon:${coupon.code}")
                    _successMessage.value = "Cupon aplicado! Plan ${tier.displayName} activado"
                } else {
                    _successMessage.value = "Cupon de ${coupon.discount_percent}% descuento aplicado. Completa el pago en Stripe."
                }
                _isLoading.value = false
            } catch (e: Exception) {
                // Offline: only accept hardcoded coupon
                _errorMessage.value = "Cupon no valido"
                _isLoading.value = false
            }
        }
    }

    private suspend fun savePlan(tier: PlanTier, key: String) {
        _currentPlan.value = tier
        _licenseKey.value = key
        updatePlan(tier)
        context.planDataStore.edit { prefs ->
            prefs[KEY_PLAN] = tier.id
            prefs[KEY_LICENSE] = key
        }
    }

    private fun updatePlan(tier: PlanTier) {
        _currentPlan.value = tier
        val features = when (tier) {
            PlanTier.FREE -> PlanFeatures.FREE
            PlanTier.PRO -> PlanFeatures.PRO
            PlanTier.AGENCY -> PlanFeatures.AGENCY
        }
        featureGate.upgrade(features)
    }

    fun deactivatePlan() {
        scope.launch {
            savePlan(PlanTier.FREE, "")
            _successMessage.value = "Plan desactivado"
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun clearSuccess() { _successMessage.value = null }

    fun destroy() {
        scope.cancel()
    }
}
