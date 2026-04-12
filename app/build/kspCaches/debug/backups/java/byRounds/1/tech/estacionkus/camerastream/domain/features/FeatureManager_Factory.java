package tech.estacionkus.camerastream.domain.features;

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
public final class FeatureManager_Factory implements Factory<FeatureManager> {
  @Override
  public FeatureManager get() {
    return newInstance();
  }

  public static FeatureManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FeatureManager newInstance() {
    return new FeatureManager();
  }

  private static final class InstanceHolder {
    static final FeatureManager_Factory INSTANCE = new FeatureManager_Factory();
  }
}
