package tech.estacionkus.camerastream.data.streaming;

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
public final class SrtlaBondingManager_Factory implements Factory<SrtlaBondingManager> {
  private final Provider<Context> contextProvider;

  public SrtlaBondingManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SrtlaBondingManager get() {
    return newInstance(contextProvider.get());
  }

  public static SrtlaBondingManager_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new SrtlaBondingManager_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static SrtlaBondingManager_Factory create(Provider<Context> contextProvider) {
    return new SrtlaBondingManager_Factory(contextProvider);
  }

  public static SrtlaBondingManager newInstance(Context context) {
    return new SrtlaBondingManager(context);
  }
}
