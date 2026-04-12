package tech.estacionkus.camerastream.streaming;

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
public final class SportsStateManager_Factory implements Factory<SportsStateManager> {
  @Override
  public SportsStateManager get() {
    return newInstance();
  }

  public static SportsStateManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SportsStateManager newInstance() {
    return new SportsStateManager();
  }

  private static final class InstanceHolder {
    static final SportsStateManager_Factory INSTANCE = new SportsStateManager_Factory();
  }
}
