package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.domain.FeatureGate;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideFeatureGateFactory implements Factory<FeatureGate> {
  @Override
  public FeatureGate get() {
    return provideFeatureGate();
  }

  public static AppModule_ProvideFeatureGateFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FeatureGate provideFeatureGate() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFeatureGate());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideFeatureGateFactory INSTANCE = new AppModule_ProvideFeatureGateFactory();
  }
}
