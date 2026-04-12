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
public final class MultiStreamManager_Factory implements Factory<MultiStreamManager> {
  private final Provider<Context> contextProvider;

  public MultiStreamManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MultiStreamManager get() {
    return newInstance(contextProvider.get());
  }

  public static MultiStreamManager_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new MultiStreamManager_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static MultiStreamManager_Factory create(Provider<Context> contextProvider) {
    return new MultiStreamManager_Factory(contextProvider);
  }

  public static MultiStreamManager newInstance(Context context) {
    return new MultiStreamManager(context);
  }
}
