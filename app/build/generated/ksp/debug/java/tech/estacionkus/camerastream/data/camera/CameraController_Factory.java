package tech.estacionkus.camerastream.data.camera;

import android.content.Context;
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
public final class CameraController_Factory implements Factory<CameraController> {
  private final Provider<Context> contextProvider;

  public CameraController_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CameraController get() {
    return newInstance(contextProvider.get());
  }

  public static CameraController_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new CameraController_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static CameraController_Factory create(Provider<Context> contextProvider) {
    return new CameraController_Factory(contextProvider);
  }

  public static CameraController newInstance(Context context) {
    return new CameraController(context);
  }
}
