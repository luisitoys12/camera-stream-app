package tech.estacionkus.camerastream.ui.screens.home;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.billing.StripeManager;
import tech.estacionkus.camerastream.domain.FeatureGate;

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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<FeatureGate> featureGateProvider;

  private final Provider<StripeManager> stripeManagerProvider;

  public HomeViewModel_Factory(Provider<FeatureGate> featureGateProvider,
      Provider<StripeManager> stripeManagerProvider) {
    this.featureGateProvider = featureGateProvider;
    this.stripeManagerProvider = stripeManagerProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(featureGateProvider.get(), stripeManagerProvider.get());
  }

  public static HomeViewModel_Factory create(javax.inject.Provider<FeatureGate> featureGateProvider,
      javax.inject.Provider<StripeManager> stripeManagerProvider) {
    return new HomeViewModel_Factory(Providers.asDaggerProvider(featureGateProvider), Providers.asDaggerProvider(stripeManagerProvider));
  }

  public static HomeViewModel_Factory create(Provider<FeatureGate> featureGateProvider,
      Provider<StripeManager> stripeManagerProvider) {
    return new HomeViewModel_Factory(featureGateProvider, stripeManagerProvider);
  }

  public static HomeViewModel newInstance(FeatureGate featureGate, StripeManager stripeManager) {
    return new HomeViewModel(featureGate, stripeManager);
  }
}
