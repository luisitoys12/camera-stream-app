package tech.estacionkus.camerastream.ui.screens.pro;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.CloudflaredManager;
import tech.estacionkus.camerastream.streaming.SrtServerManager;

@ScopeMetadata
@QualifierMetadata
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
public final class SrtServerViewModel_Factory implements Factory<SrtServerViewModel> {
  private final Provider<SrtServerManager> srtServerProvider;

  private final Provider<CloudflaredManager> cloudflaredProvider;

  public SrtServerViewModel_Factory(Provider<SrtServerManager> srtServerProvider,
      Provider<CloudflaredManager> cloudflaredProvider) {
    this.srtServerProvider = srtServerProvider;
    this.cloudflaredProvider = cloudflaredProvider;
  }

  @Override
  public SrtServerViewModel get() {
    return newInstance(srtServerProvider.get(), cloudflaredProvider.get());
  }

  public static SrtServerViewModel_Factory create(
      javax.inject.Provider<SrtServerManager> srtServerProvider,
      javax.inject.Provider<CloudflaredManager> cloudflaredProvider) {
    return new SrtServerViewModel_Factory(Providers.asDaggerProvider(srtServerProvider), Providers.asDaggerProvider(cloudflaredProvider));
  }

  public static SrtServerViewModel_Factory create(Provider<SrtServerManager> srtServerProvider,
      Provider<CloudflaredManager> cloudflaredProvider) {
    return new SrtServerViewModel_Factory(srtServerProvider, cloudflaredProvider);
  }

  public static SrtServerViewModel newInstance(SrtServerManager srtServer,
      CloudflaredManager cloudflared) {
    return new SrtServerViewModel(srtServer, cloudflared);
  }
}
