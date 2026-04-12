package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.domain.SceneManager;

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
public final class AppModule_ProvideSceneManagerFactory implements Factory<SceneManager> {
  @Override
  public SceneManager get() {
    return provideSceneManager();
  }

  public static AppModule_ProvideSceneManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SceneManager provideSceneManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSceneManager());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideSceneManagerFactory INSTANCE = new AppModule_ProvideSceneManagerFactory();
  }
}
