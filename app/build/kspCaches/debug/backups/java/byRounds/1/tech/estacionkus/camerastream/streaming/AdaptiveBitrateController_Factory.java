package tech.estacionkus.camerastream.streaming;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AdaptiveBitrateController_Factory implements Factory<AdaptiveBitrateController> {
  private final Provider<Context> contextProvider;

  private final Provider<RtmpStreamManager> streamManagerProvider;

  public AdaptiveBitrateController_Factory(Provider<Context> contextProvider,
      Provider<RtmpStreamManager> streamManagerProvider) {
    this.contextProvider = contextProvider;
    this.streamManagerProvider = streamManagerProvider;
  }

  @Override
  public AdaptiveBitrateController get() {
    return newInstance(contextProvider.get(), streamManagerProvider.get());
  }

  public static AdaptiveBitrateController_Factory create(
      javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<RtmpStreamManager> streamManagerProvider) {
    return new AdaptiveBitrateController_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(streamManagerProvider));
  }

  public static AdaptiveBitrateController_Factory create(Provider<Context> contextProvider,
      Provider<RtmpStreamManager> streamManagerProvider) {
    return new AdaptiveBitrateController_Factory(contextProvider, streamManagerProvider);
  }

  public static AdaptiveBitrateController newInstance(Context context,
      RtmpStreamManager streamManager) {
    return new AdaptiveBitrateController(context, streamManager);
  }
}
