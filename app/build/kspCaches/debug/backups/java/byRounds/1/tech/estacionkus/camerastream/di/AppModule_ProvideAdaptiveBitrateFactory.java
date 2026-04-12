package tech.estacionkus.camerastream.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.AdaptiveBitrateController;
import tech.estacionkus.camerastream.streaming.RtmpStreamManager;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AppModule_ProvideAdaptiveBitrateFactory implements Factory<AdaptiveBitrateController> {
  private final Provider<Context> cProvider;

  private final Provider<RtmpStreamManager> rtmpProvider;

  public AppModule_ProvideAdaptiveBitrateFactory(Provider<Context> cProvider,
      Provider<RtmpStreamManager> rtmpProvider) {
    this.cProvider = cProvider;
    this.rtmpProvider = rtmpProvider;
  }

  @Override
  public AdaptiveBitrateController get() {
    return provideAdaptiveBitrate(cProvider.get(), rtmpProvider.get());
  }

  public static AppModule_ProvideAdaptiveBitrateFactory create(
      javax.inject.Provider<Context> cProvider,
      javax.inject.Provider<RtmpStreamManager> rtmpProvider) {
    return new AppModule_ProvideAdaptiveBitrateFactory(Providers.asDaggerProvider(cProvider), Providers.asDaggerProvider(rtmpProvider));
  }

  public static AppModule_ProvideAdaptiveBitrateFactory create(Provider<Context> cProvider,
      Provider<RtmpStreamManager> rtmpProvider) {
    return new AppModule_ProvideAdaptiveBitrateFactory(cProvider, rtmpProvider);
  }

  public static AdaptiveBitrateController provideAdaptiveBitrate(Context c,
      RtmpStreamManager rtmp) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAdaptiveBitrate(c, rtmp));
  }
}
