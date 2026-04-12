package tech.estacionkus.camerastream.ui.screens.pro

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import tech.estacionkus.camerastream.billing.PlanTier
import tech.estacionkus.camerastream.billing.StripeManager
import javax.inject.Inject

@HiltViewModel
class UpgradeViewModel @Inject constructor(
    private val stripeManager: StripeManager
) : ViewModel() {

    val currentPlan: StateFlow<PlanTier> = stripeManager.currentPlan
    val isLoading: StateFlow<Boolean> = stripeManager.isLoading
    val errorMessage: StateFlow<String?> = stripeManager.errorMessage
    val successMessage: StateFlow<String?> = stripeManager.successMessage
    val licenseKey: StateFlow<String?> = stripeManager.licenseKey

    init {
        stripeManager.initialize()
    }

    fun openStripeCheckout(tier: PlanTier) {
        stripeManager.openStripeCheckout(tier)
    }

    fun activateLicenseKey(key: String) {
        stripeManager.activateLicenseKey(key)
    }

    fun activateCoupon(code: String) {
        stripeManager.activateCoupon(code)
    }

    fun deactivatePlan() {
        stripeManager.deactivatePlan()
    }

    fun clearError() {
        stripeManager.clearError()
    }

    fun clearSuccess() {
        stripeManager.clearSuccess()
    }
}
