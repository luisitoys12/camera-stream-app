package tech.estacionkus.camerastream.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.CloudflaredManager;

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
public final class AppModule_ProvideCloudflaredFactory implements Factory<CloudflaredManager> {
  private final Provider<Context> cProvider;

  public AppModule_ProvideCloudflaredFactory(Provider<Context> cProvider) {
    this.cProvider = cProvider;
  }

  @Override
  public CloudflaredManager get() {
    return provideCloudflared(cProvider.get());
  }

  public static AppModule_ProvideCloudflaredFactory create(
      javax.inject.Provider<Context> cProvider) {
    return new AppModule_ProvideCloudflaredFactory(Providers.asDaggerProvider(cProvider));
  }

  public static AppModule_ProvideCloudflaredFactory create(Provider<Context> cProvider) {
    return new AppModule_ProvideCloudflaredFactory(cProvider);
  }

  public static CloudflaredManager provideCloudflared(Context c) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideCloudflared(c));
  }
}
