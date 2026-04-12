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
public final class SportsModeManager_Factory implements Factory<SportsModeManager> {
  @Override
  public SportsModeManager get() {
    return newInstance();
  }

  public static SportsModeManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SportsModeManager newInstance() {
    return new SportsModeManager();
  }

  private static final class InstanceHolder {
    static final SportsModeManager_Factory INSTANCE = new SportsModeManager_Factory();
  }
}
