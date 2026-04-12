package tech.estacionkus.camerastream.ui.screens.pro;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ManualCameraViewModel_Factory implements Factory<ManualCameraViewModel> {
  @Override
  public ManualCameraViewModel get() {
    return newInstance();
  }

  public static ManualCameraViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ManualCameraViewModel newInstance() {
    return new ManualCameraViewModel();
  }

  private static final class InstanceHolder {
    static final ManualCameraViewModel_Factory INSTANCE = new ManualCameraViewModel_Factory();
  }
}
