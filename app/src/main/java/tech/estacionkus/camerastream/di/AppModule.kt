package tech.estacionkus.camerastream.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.estacionkus.camerastream.data.media.MediaRepository
import tech.estacionkus.camerastream.data.overlay.OverlayRepository
import tech.estacionkus.camerastream.data.settings.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideSettingsRepository(@ApplicationContext ctx: Context) = SettingsRepository(ctx)

    @Provides @Singleton
    fun provideMediaRepository(@ApplicationContext ctx: Context) = MediaRepository(ctx)

    @Provides @Singleton
    fun provideOverlayRepository(@ApplicationContext ctx: Context) = OverlayRepository(ctx)
}
