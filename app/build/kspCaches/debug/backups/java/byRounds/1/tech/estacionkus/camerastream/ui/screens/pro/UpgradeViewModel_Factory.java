package tech.estacionkus.camerastream.ui.screens.pro;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.billing.StripeManager;

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
public final class UpgradeViewModel_Factory implements Factory<UpgradeViewModel> {
  private final Provider<StripeManager> stripeManagerProvider;

  public UpgradeViewModel_Factory(Provider<StripeManager> stripeManagerProvider) {
    this.stripeManagerProvider = stripeManagerProvider;
  }

  @Override
  public UpgradeViewModel get() {
    return newInstance(stripeManagerProvider.get());
  }

  public static UpgradeViewModel_Factory create(
      javax.inject.Provider<StripeManager> stripeManagerProvider) {
    return new UpgradeViewModel_Factory(Providers.asDaggerProvider(stripeManagerProvider));
  }

  public static UpgradeViewModel_Factory create(Provider<StripeManager> stripeManagerProvider) {
    return new UpgradeViewModel_Factory(stripeManagerProvider);
  }

  public static UpgradeViewModel newInstance(StripeManager stripeManager) {
    return new UpgradeViewModel(stripeManager);
  }
}
