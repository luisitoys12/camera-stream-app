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
public final class NativeVideoEngine_Factory implements Factory<NativeVideoEngine> {
  private final Provider<Context> contextProvider;

  public NativeVideoEngine_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NativeVideoEngine get() {
    return newInstance(contextProvider.get());
  }

  public static NativeVideoEngine_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new NativeVideoEngine_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static NativeVideoEngine_Factory create(Provider<Context> contextProvider) {
    return new NativeVideoEngine_Factory(contextProvider);
  }

  public static NativeVideoEngine newInstance(Context context) {
    return new NativeVideoEngine(context);
  }
}
