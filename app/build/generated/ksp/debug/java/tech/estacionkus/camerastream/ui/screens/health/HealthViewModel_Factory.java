package tech.estacionkus.camerastream.ui.screens.health;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.domain.FeatureGate;
import tech.estacionkus.camerastream.streaming.StreamHealthMonitor;

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
public final class HealthViewModel_Factory implements Factory<HealthViewModel> {
  private final Provider<StreamHealthMonitor> healthMonitorProvider;

  private final Provider<FeatureGate> featureGateProvider;

  public HealthViewModel_Factory(Provider<StreamHealthMonitor> healthMonitorProvider,
      Provider<FeatureGate> featureGateProvider) {
    this.healthMonitorProvider = healthMonitorProvider;
    this.featureGateProvider = featureGateProvider;
  }

  @Override
  public HealthViewModel get() {
    return newInstance(healthMonitorProvider.get(), featureGateProvider.get());
  }

  public static HealthViewModel_Factory create(
      javax.inject.Provider<StreamHealthMonitor> healthMonitorProvider,
      javax.inject.Provider<FeatureGate> featureGateProvider) {
    return new HealthViewModel_Factory(Providers.asDaggerProvider(healthMonitorProvider), Providers.asDaggerProvider(featureGateProvider));
  }

  public static HealthViewModel_Factory create(Provider<StreamHealthMonitor> healthMonitorProvider,
      Provider<FeatureGate> featureGateProvider) {
    return new HealthViewModel_Factory(healthMonitorProvider, featureGateProvider);
  }

  public static HealthViewModel newInstance(StreamHealthMonitor healthMonitor,
      FeatureGate featureGate) {
    return new HealthViewModel(healthMonitor, featureGate);
  }
}
