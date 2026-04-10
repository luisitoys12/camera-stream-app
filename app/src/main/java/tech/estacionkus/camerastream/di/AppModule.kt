package tech.estacionkus.camerastream.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.estacionkus.camerastream.data.auth.AuthRepository
import tech.estacionkus.camerastream.data.auth.LicenseRepository
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import tech.estacionkus.camerastream.streaming.ChatManager
import tech.estacionkus.camerastream.streaming.RecordingManager
import tech.estacionkus.camerastream.streaming.RtmpStreamManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideSettingsRepository(@ApplicationContext ctx: Context) = SettingsRepository(ctx)

    @Provides @Singleton
    fun provideAuthRepository() = AuthRepository()

    @Provides @Singleton
    fun provideLicenseRepository(auth: AuthRepository) = LicenseRepository(auth)

    @Provides @Singleton
    fun provideRtmpStreamManager(@ApplicationContext ctx: Context) = RtmpStreamManager(ctx)

    @Provides @Singleton
    fun provideRecordingManager(
        @ApplicationContext ctx: Context,
        rtmp: RtmpStreamManager
    ) = RecordingManager(ctx, rtmp)

    @Provides @Singleton
    fun provideChatManager() = ChatManager()
}
