package tech.estacionkus.camerastream.ui.screens.filters

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.estacionkus.camerastream.billing.PlanTier
import tech.estacionkus.camerastream.billing.StripeManager
import tech.estacionkus.camerastream.domain.model.CameraFilter
import tech.estacionkus.camerastream.domain.model.FilterTier
import javax.inject.Inject

@HiltViewModel
class CameraFiltersViewModel @Inject constructor(
    private val stripeManager: StripeManager
) : ViewModel() {

    val currentPlan: StateFlow<PlanTier> = stripeManager.currentPlan

    private val _currentFilter = MutableStateFlow<CameraFilter?>(null)
    val currentFilter: StateFlow<CameraFilter?> = _currentFilter.asStateFlow()

    fun selectFilter(filter: CameraFilter) {
        _currentFilter.value = filter
    }

    fun canUseFilter(filter: CameraFilter): Boolean {
        val plan = stripeManager.currentPlan.value
        return when (filter.tier) {
            FilterTier.FREE -> true
            FilterTier.PRO -> plan == PlanTier.PRO || plan == PlanTier.AGENCY
            FilterTier.AGENCY -> plan == PlanTier.AGENCY
        }
    }
}
