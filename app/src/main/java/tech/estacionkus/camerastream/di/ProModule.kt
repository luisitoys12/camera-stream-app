package tech.estacionkus.camerastream.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.estacionkus.camerastream.domain.FeatureGate
import tech.estacionkus.camerastream.domain.SceneManager
import tech.estacionkus.camerastream.streaming.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProModule {
    @Provides @Singleton
    fun provideSrtServerManager() = SrtServerManager()

    @Provides @Singleton
    fun provideCloudflaredManager(@ApplicationContext ctx: Context) = CloudflaredManager(ctx)

    @Provides @Singleton
    fun provideAdaptiveBitrate(
        @ApplicationContext ctx: Context,
        rtmp: RtmpStreamManager
    ) = AdaptiveBitrateController(ctx, rtmp)

    @Provides @Singleton
    fun provideMultiStreamManager(@ApplicationContext ctx: Context) = MultiStreamManager(ctx)

    @Provides @Singleton
    fun provideSrtCallerManager() = SrtCallerManager()

    @Provides @Singleton
    fun provideMultiChatManager() = MultiChatManager()

    @Provides @Singleton
    fun provideSceneManager() = SceneManager()

    @Provides @Singleton
    fun provideFeatureGate() = FeatureGate()

    @Provides @Singleton
    fun provideOverlayRenderer() = tech.estacionkus.camerastream.streaming.OverlayRenderer
}
