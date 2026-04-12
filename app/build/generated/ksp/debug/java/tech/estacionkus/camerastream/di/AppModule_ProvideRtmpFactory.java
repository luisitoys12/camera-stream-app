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
import tech.estacionkus.camerastream.streaming.RtmpStreamManager;

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
public final class AppModule_ProvideRtmpFactory implements Factory<RtmpStreamManager> {
  private final Provider<Context> cProvider;

  public AppModule_ProvideRtmpFactory(Provider<Context> cProvider) {
    this.cProvider = cProvider;
  }

  @Override
  public RtmpStreamManager get() {
    return provideRtmp(cProvider.get());
  }

  public static AppModule_ProvideRtmpFactory create(javax.inject.Provider<Context> cProvider) {
    return new AppModule_ProvideRtmpFactory(Providers.asDaggerProvider(cProvider));
  }

  public static AppModule_ProvideRtmpFactory create(Provider<Context> cProvider) {
    return new AppModule_ProvideRtmpFactory(cProvider);
  }

  public static RtmpStreamManager provideRtmp(Context c) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRtmp(c));
  }
}
