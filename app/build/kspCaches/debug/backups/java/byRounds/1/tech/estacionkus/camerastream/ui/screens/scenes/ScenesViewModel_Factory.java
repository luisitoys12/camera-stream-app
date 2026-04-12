package tech.estacionkus.camerastream.ui.screens.scenes;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.domain.FeatureGate;
import tech.estacionkus.camerastream.domain.SceneManager;

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
public final class ScenesViewModel_Factory implements Factory<ScenesViewModel> {
  private final Provider<SceneManager> sceneManagerProvider;

  private final Provider<FeatureGate> featureGateProvider;

  public ScenesViewModel_Factory(Provider<SceneManager> sceneManagerProvider,
      Provider<FeatureGate> featureGateProvider) {
    this.sceneManagerProvider = sceneManagerProvider;
    this.featureGateProvider = featureGateProvider;
  }

  @Override
  public ScenesViewModel get() {
    return newInstance(sceneManagerProvider.get(), featureGateProvider.get());
  }

  public static ScenesViewModel_Factory create(
      javax.inject.Provider<SceneManager> sceneManagerProvider,
      javax.inject.Provider<FeatureGate> featureGateProvider) {
    return new ScenesViewModel_Factory(Providers.asDaggerProvider(sceneManagerProvider), Providers.asDaggerProvider(featureGateProvider));
  }

  public static ScenesViewModel_Factory create(Provider<SceneManager> sceneManagerProvider,
      Provider<FeatureGate> featureGateProvider) {
    return new ScenesViewModel_Factory(sceneManagerProvider, featureGateProvider);
  }

  public static ScenesViewModel newInstance(SceneManager sceneManager, FeatureGate featureGate) {
    return new ScenesViewModel(sceneManager, featureGate);
  }
}
