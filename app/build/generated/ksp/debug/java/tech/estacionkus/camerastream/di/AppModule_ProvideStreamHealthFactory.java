package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.StreamHealthMonitor;

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
public final class AppModule_ProvideStreamHealthFactory implements Factory<StreamHealthMonitor> {
  @Override
  public StreamHealthMonitor get() {
    return provideStreamHealth();
  }

  public static AppModule_ProvideStreamHealthFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static StreamHealthMonitor provideStreamHealth() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideStreamHealth());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideStreamHealthFactory INSTANCE = new AppModule_ProvideStreamHealthFactory();
  }
}
