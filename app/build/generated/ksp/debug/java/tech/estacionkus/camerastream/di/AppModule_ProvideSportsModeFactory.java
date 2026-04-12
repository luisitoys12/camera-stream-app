package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.SportsModeManager;

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
public final class AppModule_ProvideSportsModeFactory implements Factory<SportsModeManager> {
  @Override
  public SportsModeManager get() {
    return provideSportsMode();
  }

  public static AppModule_ProvideSportsModeFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SportsModeManager provideSportsMode() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSportsMode());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideSportsModeFactory INSTANCE = new AppModule_ProvideSportsModeFactory();
  }
}
