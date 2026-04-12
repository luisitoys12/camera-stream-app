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
import tech.estacionkus.camerastream.streaming.GuestModeManager;

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
public final class AppModule_ProvideGuestModeFactory implements Factory<GuestModeManager> {
  private final Provider<Context> cProvider;

  public AppModule_ProvideGuestModeFactory(Provider<Context> cProvider) {
    this.cProvider = cProvider;
  }

  @Override
  public GuestModeManager get() {
    return provideGuestMode(cProvider.get());
  }

  public static AppModule_ProvideGuestModeFactory create(javax.inject.Provider<Context> cProvider) {
    return new AppModule_ProvideGuestModeFactory(Providers.asDaggerProvider(cProvider));
  }

  public static AppModule_ProvideGuestModeFactory create(Provider<Context> cProvider) {
    return new AppModule_ProvideGuestModeFactory(cProvider);
  }

  public static GuestModeManager provideGuestMode(Context c) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGuestMode(c));
  }
}
