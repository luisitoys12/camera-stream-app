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
public final class StreamHealthMonitor_Factory implements Factory<StreamHealthMonitor> {
  @Override
  public StreamHealthMonitor get() {
    return newInstance();
  }

  public static StreamHealthMonitor_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static StreamHealthMonitor newInstance() {
    return new StreamHealthMonitor();
  }

  private static final class InstanceHolder {
    static final StreamHealthMonitor_Factory INSTANCE = new StreamHealthMonitor_Factory();
  }
}
