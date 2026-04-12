package tech.estacionkus.camerastream.ui.screens.pro

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import tech.estacionkus.camerastream.billing.BillingManager
import tech.estacionkus.camerastream.billing.PlanTier
import javax.inject.Inject

@HiltViewModel
class UpgradeViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    val currentPlan: StateFlow<PlanTier> = billingManager.currentPlan

    val productDetails: StateFlow<Map<String, ProductDetails>> = billingManager.productDetails

    val isLoading: StateFlow<Boolean> = billingManager.isLoading

    val errorMessage: StateFlow<String?> = billingManager.errorMessage

    init {
        billingManager.initialize()
    }

    fun launchPurchase(activity: Activity, tier: PlanTier) {
        billingManager.launchPurchaseFlow(activity, tier)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun clearError() {
        billingManager.clearError()
    }
}
