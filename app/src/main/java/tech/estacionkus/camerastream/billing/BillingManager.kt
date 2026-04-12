package tech.estacionkus.camerastream.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.model.PlanFeatures
import javax.inject.Inject
import javax.inject.Singleton

enum class PlanTier(val productId: String, val displayName: String, val priceDisplay: String) {
    FREE("", "Free", "Free"),
    PRO("camerastream_pro_monthly", "Pro", "$4.99/mo"),
    AGENCY("camerastream_agency_monthly", "Agency", "$14.99/mo")
}

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val featureGate: FeatureGate
) {
    private val TAG = "BillingManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _currentPlan = MutableStateFlow(PlanTier.FREE)
    val currentPlan: StateFlow<PlanTier> = _currentPlan.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    private var billingClient: BillingClient? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User cancelled purchase")
        } else {
            _errorMessage.value = "Purchase error: ${billingResult.debugMessage}"
        }
    }

    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing connected")
                    queryProducts()
                    queryExistingPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing disconnected")
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PlanTier.PRO.productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PlanTier.AGENCY.productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val map = mutableMapOf<String, ProductDetails>()
                productDetailsList.forEach { map[it.productId] = it }
                _productDetails.value = map
                Log.d(TAG, "Products loaded: ${map.keys}")
            }
        }
    }

    private fun queryExistingPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var highestTier = PlanTier.FREE
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val tier = tierFromProducts(purchase.products)
                        if (tier.ordinal > highestTier.ordinal) highestTier = tier
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                    }
                }
                updatePlan(highestTier)
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, tier: PlanTier) {
        val details = _productDetails.value[tier.productId]
        if (details == null) {
            _errorMessage.value = "Product not available. Please try again later."
            return
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val tier = tierFromProducts(purchase.products)
            updatePlan(tier)
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        scope.launch {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                }
            }
        }
    }

    fun restorePurchases() {
        _isLoading.value = true
        queryExistingPurchases()
        _isLoading.value = false
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

    private fun tierFromProducts(products: List<String>): PlanTier {
        return when {
            products.contains(PlanTier.AGENCY.productId) -> PlanTier.AGENCY
            products.contains(PlanTier.PRO.productId) -> PlanTier.PRO
            else -> PlanTier.FREE
        }
    }

    fun clearError() { _errorMessage.value = null }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}
