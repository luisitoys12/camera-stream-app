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
public final class SrtServerManager_Factory implements Factory<SrtServerManager> {
  @Override
  public SrtServerManager get() {
    return newInstance();
  }

  public static SrtServerManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SrtServerManager newInstance() {
    return new SrtServerManager();
  }

  private static final class InstanceHolder {
    static final SrtServerManager_Factory INSTANCE = new SrtServerManager_Factory();
  }
}
