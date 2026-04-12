package tech.estacionkus.camerastream.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.billing.BillingManager;
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
public final class AppModule_ProvideBillingFactory implements Factory<BillingManager> {
  private final Provider<Context> cProvider;

  private final Provider<FeatureGate> fgProvider;

  public AppModule_ProvideBillingFactory(Provider<Context> cProvider,
      Provider<FeatureGate> fgProvider) {
    this.cProvider = cProvider;
    this.fgProvider = fgProvider;
  }

  @Override
  public BillingManager get() {
    return provideBilling(cProvider.get(), fgProvider.get());
  }

  public static AppModule_ProvideBillingFactory create(javax.inject.Provider<Context> cProvider,
      javax.inject.Provider<FeatureGate> fgProvider) {
    return new AppModule_ProvideBillingFactory(Providers.asDaggerProvider(cProvider), Providers.asDaggerProvider(fgProvider));
  }

  public static AppModule_ProvideBillingFactory create(Provider<Context> cProvider,
      Provider<FeatureGate> fgProvider) {
    return new AppModule_ProvideBillingFactory(cProvider, fgProvider);
  }

  public static BillingManager provideBilling(Context c, FeatureGate fg) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBilling(c, fg));
  }
}
