package tech.estacionkus.camerastream.domain.features

import tech.estacionkus.camerastream.data.auth.LicensePlan
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureManager @Inject constructor() {

    private var currentPlan: String? = null
    private var isCreator: Boolean = false

    fun updatePlan(plan: LicensePlan?, isCreator: Boolean) {
        this.currentPlan = plan?.id
        this.isCreator = isCreator
    }

    fun isEnabled(feature: Feature): Boolean {
        if (feature.tier == Tier.FREE) return true
        return when (currentPlan) {
            "starter" -> feature.tier == Tier.PAID
            "pro"     -> feature.tier in listOf(Tier.PAID, Tier.PRO) || isCreator
            "agency"  -> true
            else      -> false
        }.let { allowed ->
            // Creators with CREADOR100 get full PRO access
            if (isCreator && feature.tier in listOf(Tier.PAID, Tier.PRO)) true
            else allowed
        }
    }

    fun requiresPlan(feature: Feature): String = when (feature.tier) {
        Tier.FREE   -> "Gratis"
        Tier.PAID   -> "Starter o superior"
        Tier.PRO    -> "Pro o Agency"
        Tier.AGENCY -> "Agency"
    }
}
