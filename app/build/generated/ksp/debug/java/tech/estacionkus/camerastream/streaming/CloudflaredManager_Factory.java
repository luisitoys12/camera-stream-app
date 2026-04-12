package tech.estacionkus.camerastream.streaming;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CloudflaredManager_Factory implements Factory<CloudflaredManager> {
  private final Provider<Context> contextProvider;

  public CloudflaredManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CloudflaredManager get() {
    return newInstance(contextProvider.get());
  }

  public static CloudflaredManager_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new CloudflaredManager_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static CloudflaredManager_Factory create(Provider<Context> contextProvider) {
    return new CloudflaredManager_Factory(contextProvider);
  }

  public static CloudflaredManager newInstance(Context context) {
    return new CloudflaredManager(context);
  }
}
