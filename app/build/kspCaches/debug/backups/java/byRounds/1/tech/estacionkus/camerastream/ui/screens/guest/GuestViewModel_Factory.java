package tech.estacionkus.camerastream.ui.screens.guest;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.domain.FeatureGate;
import tech.estacionkus.camerastream.streaming.GuestModeManager;

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
public final class GuestViewModel_Factory implements Factory<GuestViewModel> {
  private final Provider<GuestModeManager> guestManagerProvider;

  private final Provider<FeatureGate> featureGateProvider;

  public GuestViewModel_Factory(Provider<GuestModeManager> guestManagerProvider,
      Provider<FeatureGate> featureGateProvider) {
    this.guestManagerProvider = guestManagerProvider;
    this.featureGateProvider = featureGateProvider;
  }

  @Override
  public GuestViewModel get() {
    return newInstance(guestManagerProvider.get(), featureGateProvider.get());
  }

  public static GuestViewModel_Factory create(
      javax.inject.Provider<GuestModeManager> guestManagerProvider,
      javax.inject.Provider<FeatureGate> featureGateProvider) {
    return new GuestViewModel_Factory(Providers.asDaggerProvider(guestManagerProvider), Providers.asDaggerProvider(featureGateProvider));
  }

  public static GuestViewModel_Factory create(Provider<GuestModeManager> guestManagerProvider,
      Provider<FeatureGate> featureGateProvider) {
    return new GuestViewModel_Factory(guestManagerProvider, featureGateProvider);
  }

  public static GuestViewModel newInstance(GuestModeManager guestManager, FeatureGate featureGate) {
    return new GuestViewModel(guestManager, featureGate);
  }
}
