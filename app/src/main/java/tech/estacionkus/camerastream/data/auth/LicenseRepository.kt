package tech.estacionkus.camerastream.data.auth

import io.github.jan.supabase.functions.functions
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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
    private val functions = Supabase.client.functions

    suspend fun verifyLicense(): LicenseStatus {
        return try {
            val response = functions.invoke("verify-license")
            Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())
        } catch (e: Exception) {
            LicenseStatus(licensed = false, message = e.message)
        }
    }

    suspend fun redeemCoupon(code: String): CouponRedeemResult {
        return try {
            val response = functions.invoke("redeem-coupon") {
                body = "{\"code\":\"${code.trim().uppercase()}\"}"
            }
            Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())
        } catch (e: Exception) {
            CouponRedeemResult(error = e.message)
        }
    }
}
