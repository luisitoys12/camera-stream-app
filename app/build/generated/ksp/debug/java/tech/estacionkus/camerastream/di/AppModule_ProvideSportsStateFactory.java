package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.SportsStateManager;

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
public final class AppModule_ProvideSportsStateFactory implements Factory<SportsStateManager> {
  @Override
  public SportsStateManager get() {
    return provideSportsState();
  }

  public static AppModule_ProvideSportsStateFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SportsStateManager provideSportsState() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSportsState());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideSportsStateFactory INSTANCE = new AppModule_ProvideSportsStateFactory();
  }
}
