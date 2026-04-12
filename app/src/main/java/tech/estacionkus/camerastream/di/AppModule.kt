package tech.estacionkus.camerastream.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.estacionkus.camerastream.billing.StripeManager
import tech.estacionkus.camerastream.data.auth.AuthRepository
import tech.estacionkus.camerastream.data.auth.LicenseRepository
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.SceneManager
import tech.estacionkus.camerastream.streaming.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun provideRtmp(@ApplicationContext c: Context) = RtmpStreamManager(c)
    @Provides @Singleton fun provideMultiStream(@ApplicationContext c: Context) = MultiStreamManager(c)
    @Provides @Singleton fun provideSrtServer() = SrtServerManager()
    @Provides @Singleton fun provideSrtCaller() = SrtCallerManager()
    @Provides @Singleton fun provideSportsMode() = SportsModeManager()
    @Provides @Singleton fun provideCloudflared(@ApplicationContext c: Context) = CloudflaredManager(c)
    @Provides @Singleton fun provideMultiChat() = MultiChatManager()
    @Provides @Singleton fun provideSceneManager() = SceneManager()
    @Provides @Singleton fun provideFeatureGate() = FeatureGate()
    @Provides @Singleton fun provideChatManager() = ChatManager()
    @Provides @Singleton fun provideRecordingManager(@ApplicationContext c: Context) = RecordingManager(c)
    @Provides @Singleton fun provideAdaptiveBitrate(@ApplicationContext c: Context, rtmp: RtmpStreamManager) = AdaptiveBitrateController(c, rtmp)
    @Provides @Singleton fun provideSettingsRepo(@ApplicationContext c: Context) = SettingsRepository(c)
    @Provides @Singleton fun provideStripeManager(@ApplicationContext c: Context, fg: FeatureGate) = StripeManager(c, fg)
    @Provides @Singleton fun provideDisconnectProtection(@ApplicationContext c: Context) = DisconnectProtectionManager(c)
    @Provides @Singleton fun provideStreamHealth() = StreamHealthMonitor()
    @Provides @Singleton fun provideGuestMode(@ApplicationContext c: Context) = GuestModeManager(c)
    @Provides @Singleton fun provideSportsState() = SportsStateManager()
    @Provides @Singleton fun provideAuthRepository() = AuthRepository()
    @Provides @Singleton fun provideLicenseRepository(auth: AuthRepository) = LicenseRepository(auth)
}
