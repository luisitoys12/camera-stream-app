package tech.estacionkus.camerastream.ui.screens.studio;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.data.overlay.OverlayRepository;
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
public final class MyStudioViewModel_Factory implements Factory<MyStudioViewModel> {
  private final Provider<SceneManager> sceneManagerProvider;

  private final Provider<OverlayRepository> overlayRepositoryProvider;

  private final Provider<FeatureGate> featureGateProvider;

  public MyStudioViewModel_Factory(Provider<SceneManager> sceneManagerProvider,
      Provider<OverlayRepository> overlayRepositoryProvider,
      Provider<FeatureGate> featureGateProvider) {
    this.sceneManagerProvider = sceneManagerProvider;
    this.overlayRepositoryProvider = overlayRepositoryProvider;
    this.featureGateProvider = featureGateProvider;
  }

  @Override
  public MyStudioViewModel get() {
    return newInstance(sceneManagerProvider.get(), overlayRepositoryProvider.get(), featureGateProvider.get());
  }

  public static MyStudioViewModel_Factory create(
      javax.inject.Provider<SceneManager> sceneManagerProvider,
      javax.inject.Provider<OverlayRepository> overlayRepositoryProvider,
      javax.inject.Provider<FeatureGate> featureGateProvider) {
    return new MyStudioViewModel_Factory(Providers.asDaggerProvider(sceneManagerProvider), Providers.asDaggerProvider(overlayRepositoryProvider), Providers.asDaggerProvider(featureGateProvider));
  }

  public static MyStudioViewModel_Factory create(Provider<SceneManager> sceneManagerProvider,
      Provider<OverlayRepository> overlayRepositoryProvider,
      Provider<FeatureGate> featureGateProvider) {
    return new MyStudioViewModel_Factory(sceneManagerProvider, overlayRepositoryProvider, featureGateProvider);
  }

  public static MyStudioViewModel newInstance(SceneManager sceneManager,
      OverlayRepository overlayRepository, FeatureGate featureGate) {
    return new MyStudioViewModel(sceneManager, overlayRepository, featureGate);
  }
}
