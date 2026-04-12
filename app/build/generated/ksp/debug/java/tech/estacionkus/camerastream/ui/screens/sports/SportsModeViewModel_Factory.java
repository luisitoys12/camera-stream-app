package tech.estacionkus.camerastream.ui.screens.sports;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.domain.FeatureGate;
import tech.estacionkus.camerastream.streaming.SportsStateManager;

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
public final class SportsModeViewModel_Factory implements Factory<SportsModeViewModel> {
  private final Provider<SportsStateManager> sportsStateManagerProvider;

  private final Provider<FeatureGate> featureGateProvider;

  public SportsModeViewModel_Factory(Provider<SportsStateManager> sportsStateManagerProvider,
      Provider<FeatureGate> featureGateProvider) {
    this.sportsStateManagerProvider = sportsStateManagerProvider;
    this.featureGateProvider = featureGateProvider;
  }

  @Override
  public SportsModeViewModel get() {
    return newInstance(sportsStateManagerProvider.get(), featureGateProvider.get());
  }

  public static SportsModeViewModel_Factory create(
      javax.inject.Provider<SportsStateManager> sportsStateManagerProvider,
      javax.inject.Provider<FeatureGate> featureGateProvider) {
    return new SportsModeViewModel_Factory(Providers.asDaggerProvider(sportsStateManagerProvider), Providers.asDaggerProvider(featureGateProvider));
  }

  public static SportsModeViewModel_Factory create(
      Provider<SportsStateManager> sportsStateManagerProvider,
      Provider<FeatureGate> featureGateProvider) {
    return new SportsModeViewModel_Factory(sportsStateManagerProvider, featureGateProvider);
  }

  public static SportsModeViewModel newInstance(SportsStateManager sportsStateManager,
      FeatureGate featureGate) {
    return new SportsModeViewModel(sportsStateManager, featureGate);
  }
}
