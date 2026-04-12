package tech.estacionkus.camerastream.data.overlay;

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
public final class NativeOverlayRenderer_Factory implements Factory<NativeOverlayRenderer> {
  @Override
  public NativeOverlayRenderer get() {
    return newInstance();
  }

  public static NativeOverlayRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NativeOverlayRenderer newInstance() {
    return new NativeOverlayRenderer();
  }

  private static final class InstanceHolder {
    static final NativeOverlayRenderer_Factory INSTANCE = new NativeOverlayRenderer_Factory();
  }
}
