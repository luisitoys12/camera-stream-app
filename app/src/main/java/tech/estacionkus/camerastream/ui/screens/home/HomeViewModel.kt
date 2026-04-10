package tech.estacionkus.camerastream.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.auth.LicenseRepository
import tech.estacionkus.camerastream.domain.model.PlanFeatures
import javax.inject.Inject

data class HomeUiState(
    val planId: String? = null,
    val isCreator: Boolean = false,
    val features: PlanFeatures = PlanFeatures.FREE,
    val lastSessionDuration: Long = 0L
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val licenseRepository: LicenseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val license = licenseRepository.verifyLicense()
            _uiState.value = _uiState.value.copy(
                planId = license.plan?.id,
                isCreator = license.isCreator,
                features = PlanFeatures.fromPlanId(license.plan?.id)
            )
        }
    }

    fun onAccountTap() {
        // Navigate to account/upgrade — handled by nav
    }
}
