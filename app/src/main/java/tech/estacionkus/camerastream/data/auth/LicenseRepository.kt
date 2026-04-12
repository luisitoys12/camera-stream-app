package tech.estacionkus.camerastream.data.auth

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class LicensePlan(
    val id: String,
    val name: String,
    val maxPlatforms: Int,
    val maxResolution: String,
    val allowSrt: Boolean,
    val allowOverlay: Boolean,
    val allowRecording: Boolean
)

@Serializable
data class LicenseStatus(
    val licensed: Boolean,
    val plan: LicensePlan? = null,
    val isCreator: Boolean = false,
    val validUntil: String? = null,
    val message: String? = null
)

@Serializable
data class CouponRedeemResult(
    val success: Boolean = false,
    val plan: String? = null,
    val message: String? = null,
    val error: String? = null
)

@Singleton
class LicenseRepository @Inject constructor(
    private val authRepository: AuthRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun verifyLicense(): LicenseStatus {
        return try {
            val result = Supabase.client.postgrest.rpc("verify_license")
            json.decodeFromString(result.data)
        } catch (e: Exception) {
            LicenseStatus(licensed = false, message = e.message)
        }
    }

    suspend fun redeemCoupon(code: String): CouponRedeemResult {
        return try {
            val params = buildJsonObject {
                put("code", code.trim().uppercase())
            }
            val result = Supabase.client.postgrest.rpc("redeem_coupon", params)
            json.decodeFromString(result.data)
        } catch (e: Exception) {
            CouponRedeemResult(error = e.message)
        }
    }
}
