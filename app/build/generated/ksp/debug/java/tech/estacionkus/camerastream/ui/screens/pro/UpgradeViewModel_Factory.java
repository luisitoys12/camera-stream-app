package tech.estacionkus.camerastream.ui.screens.pro;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.billing.BillingManager;

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
  private final Provider<BillingManager> billingManagerProvider;

  public UpgradeViewModel_Factory(Provider<BillingManager> billingManagerProvider) {
    this.billingManagerProvider = billingManagerProvider;
  }

  @Override
  public UpgradeViewModel get() {
    return newInstance(billingManagerProvider.get());
  }

  public static UpgradeViewModel_Factory create(
      javax.inject.Provider<BillingManager> billingManagerProvider) {
    return new UpgradeViewModel_Factory(Providers.asDaggerProvider(billingManagerProvider));
  }

  public static UpgradeViewModel_Factory create(Provider<BillingManager> billingManagerProvider) {
    return new UpgradeViewModel_Factory(billingManagerProvider);
  }

  public static UpgradeViewModel newInstance(BillingManager billingManager) {
    return new UpgradeViewModel(billingManager);
  }
}
