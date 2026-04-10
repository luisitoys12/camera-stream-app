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
object AppModule {
    @Provides @Singleton fun provideRtmp(@ApplicationContext c: Context) = RtmpStreamManager(c)
    @Provides @Singleton fun provideMultiStream(@ApplicationContext c: Context) = MultiStreamManager(c)
    @Provides @Singleton fun provideSrtServer() = SrtServerManager()
    @Provides @Singleton fun provideCloudflared(@ApplicationContext c: Context) = CloudflaredManager(c)
    @Provides @Singleton fun provideMultiChat() = MultiChatManager()
    @Provides @Singleton fun provideSceneManager() = SceneManager()
    @Provides @Singleton fun provideFeatureGate() = FeatureGate()
}
