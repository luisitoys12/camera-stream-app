package tech.estacionkus.camerastream.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import tech.estacionkus.camerastream.billing.StripeManager
import tech.estacionkus.camerastream.domain.FeatureGate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val featureGate: FeatureGate,
    private val stripeManager: StripeManager
) : ViewModel() {
    val planName: StateFlow<String> = featureGate.planName
    val currentPlan = stripeManager.currentPlan
}
