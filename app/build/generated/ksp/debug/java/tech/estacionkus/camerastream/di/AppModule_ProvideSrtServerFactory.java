package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.SrtServerManager;

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
public final class AppModule_ProvideSrtServerFactory implements Factory<SrtServerManager> {
  @Override
  public SrtServerManager get() {
    return provideSrtServer();
  }

  public static AppModule_ProvideSrtServerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SrtServerManager provideSrtServer() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSrtServer());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideSrtServerFactory INSTANCE = new AppModule_ProvideSrtServerFactory();
  }
}
