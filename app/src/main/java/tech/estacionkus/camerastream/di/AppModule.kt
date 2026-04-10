package tech.estacionkus.camerastream.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.estacionkus.camerastream.streaming.RtmpStreamManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideRtmpStreamManager(@ApplicationContext ctx: Context) = RtmpStreamManager(ctx)
}
