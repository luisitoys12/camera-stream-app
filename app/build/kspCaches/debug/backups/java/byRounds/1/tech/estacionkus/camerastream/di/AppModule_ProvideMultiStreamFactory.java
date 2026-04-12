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
import tech.estacionkus.camerastream.streaming.MultiStreamManager;

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
public final class AppModule_ProvideMultiStreamFactory implements Factory<MultiStreamManager> {
  private final Provider<Context> cProvider;

  public AppModule_ProvideMultiStreamFactory(Provider<Context> cProvider) {
    this.cProvider = cProvider;
  }

  @Override
  public MultiStreamManager get() {
    return provideMultiStream(cProvider.get());
  }

  public static AppModule_ProvideMultiStreamFactory create(
      javax.inject.Provider<Context> cProvider) {
    return new AppModule_ProvideMultiStreamFactory(Providers.asDaggerProvider(cProvider));
  }

  public static AppModule_ProvideMultiStreamFactory create(Provider<Context> cProvider) {
    return new AppModule_ProvideMultiStreamFactory(cProvider);
  }

  public static MultiStreamManager provideMultiStream(Context c) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMultiStream(c));
  }
}
