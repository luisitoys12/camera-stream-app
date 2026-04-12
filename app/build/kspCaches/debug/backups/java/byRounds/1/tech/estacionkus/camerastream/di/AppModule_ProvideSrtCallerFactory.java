package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.SrtCallerManager;

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
public final class AppModule_ProvideSrtCallerFactory implements Factory<SrtCallerManager> {
  @Override
  public SrtCallerManager get() {
    return provideSrtCaller();
  }

  public static AppModule_ProvideSrtCallerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SrtCallerManager provideSrtCaller() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSrtCaller());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideSrtCallerFactory INSTANCE = new AppModule_ProvideSrtCallerFactory();
  }
}
