package tech.estacionkus.camerastream.billing;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.domain.FeatureGate;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class BillingManager_Factory implements Factory<BillingManager> {
  private final Provider<Context> contextProvider;

  private final Provider<FeatureGate> featureGateProvider;

  public BillingManager_Factory(Provider<Context> contextProvider,
      Provider<FeatureGate> featureGateProvider) {
    this.contextProvider = contextProvider;
    this.featureGateProvider = featureGateProvider;
  }

  @Override
  public BillingManager get() {
    return newInstance(contextProvider.get(), featureGateProvider.get());
  }

  public static BillingManager_Factory create(javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<FeatureGate> featureGateProvider) {
    return new BillingManager_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(featureGateProvider));
  }

  public static BillingManager_Factory create(Provider<Context> contextProvider,
      Provider<FeatureGate> featureGateProvider) {
    return new BillingManager_Factory(contextProvider, featureGateProvider);
  }

  public static BillingManager newInstance(Context context, FeatureGate featureGate) {
    return new BillingManager(context, featureGate);
  }
}
