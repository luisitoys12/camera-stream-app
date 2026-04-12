package tech.estacionkus.camerastream.ui.screens.home;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.billing.BillingManager;
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

  private final Provider<BillingManager> billingManagerProvider;

  public HomeViewModel_Factory(Provider<FeatureGate> featureGateProvider,
      Provider<BillingManager> billingManagerProvider) {
    this.featureGateProvider = featureGateProvider;
    this.billingManagerProvider = billingManagerProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(featureGateProvider.get(), billingManagerProvider.get());
  }

  public static HomeViewModel_Factory create(javax.inject.Provider<FeatureGate> featureGateProvider,
      javax.inject.Provider<BillingManager> billingManagerProvider) {
    return new HomeViewModel_Factory(Providers.asDaggerProvider(featureGateProvider), Providers.asDaggerProvider(billingManagerProvider));
  }

  public static HomeViewModel_Factory create(Provider<FeatureGate> featureGateProvider,
      Provider<BillingManager> billingManagerProvider) {
    return new HomeViewModel_Factory(featureGateProvider, billingManagerProvider);
  }

  public static HomeViewModel newInstance(FeatureGate featureGate, BillingManager billingManager) {
    return new HomeViewModel(featureGate, billingManager);
  }
}
