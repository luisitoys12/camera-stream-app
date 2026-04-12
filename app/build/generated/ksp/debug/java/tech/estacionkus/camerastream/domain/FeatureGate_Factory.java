package tech.estacionkus.camerastream.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class FeatureGate_Factory implements Factory<FeatureGate> {
  @Override
  public FeatureGate get() {
    return newInstance();
  }

  public static FeatureGate_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FeatureGate newInstance() {
    return new FeatureGate();
  }

  private static final class InstanceHolder {
    static final FeatureGate_Factory INSTANCE = new FeatureGate_Factory();
  }
}
