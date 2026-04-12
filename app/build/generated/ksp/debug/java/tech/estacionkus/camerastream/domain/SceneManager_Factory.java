package tech.estacionkus.camerastream.domain;

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
public final class SceneManager_Factory implements Factory<SceneManager> {
  @Override
  public SceneManager get() {
    return newInstance();
  }

  public static SceneManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SceneManager newInstance() {
    return new SceneManager();
  }

  private static final class InstanceHolder {
    static final SceneManager_Factory INSTANCE = new SceneManager_Factory();
  }
}
