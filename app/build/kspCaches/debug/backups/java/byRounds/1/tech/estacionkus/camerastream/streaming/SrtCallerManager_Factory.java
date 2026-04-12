package tech.estacionkus.camerastream.streaming;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
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
public final class SrtCallerManager_Factory implements Factory<SrtCallerManager> {
  private final Provider<SrtSocketWrapper> socketWrapperProvider;

  public SrtCallerManager_Factory(Provider<SrtSocketWrapper> socketWrapperProvider) {
    this.socketWrapperProvider = socketWrapperProvider;
  }

  @Override
  public SrtCallerManager get() {
    return newInstance(socketWrapperProvider.get());
  }

  public static SrtCallerManager_Factory create(
      javax.inject.Provider<SrtSocketWrapper> socketWrapperProvider) {
    return new SrtCallerManager_Factory(Providers.asDaggerProvider(socketWrapperProvider));
  }

  public static SrtCallerManager_Factory create(Provider<SrtSocketWrapper> socketWrapperProvider) {
    return new SrtCallerManager_Factory(socketWrapperProvider);
  }

  public static SrtCallerManager newInstance(SrtSocketWrapper socketWrapper) {
    return new SrtCallerManager(socketWrapper);
  }
}
