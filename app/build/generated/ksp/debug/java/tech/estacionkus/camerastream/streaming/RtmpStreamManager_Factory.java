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
public final class RtmpStreamManager_Factory implements Factory<RtmpStreamManager> {
  private final Provider<Context> contextProvider;

  public RtmpStreamManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RtmpStreamManager get() {
    return newInstance(contextProvider.get());
  }

  public static RtmpStreamManager_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new RtmpStreamManager_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static RtmpStreamManager_Factory create(Provider<Context> contextProvider) {
    return new RtmpStreamManager_Factory(contextProvider);
  }

  public static RtmpStreamManager newInstance(Context context) {
    return new RtmpStreamManager(context);
  }
}
