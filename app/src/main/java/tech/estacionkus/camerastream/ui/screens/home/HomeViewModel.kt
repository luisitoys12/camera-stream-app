package tech.estacionkus.camerastream.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.billing.BillingManager
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.model.PlanFeatures
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val featureGate: FeatureGate,
    private val billingManager: BillingManager
) : ViewModel() {
    val planName: StateFlow<String> = featureGate.planName
    val currentPlan = billingManager.currentPlan
}
