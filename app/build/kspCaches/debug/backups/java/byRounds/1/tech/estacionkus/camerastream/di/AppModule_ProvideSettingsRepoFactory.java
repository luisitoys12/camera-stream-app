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
import tech.estacionkus.camerastream.data.settings.SettingsRepository;

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
public final class AppModule_ProvideSettingsRepoFactory implements Factory<SettingsRepository> {
  private final Provider<Context> cProvider;

  public AppModule_ProvideSettingsRepoFactory(Provider<Context> cProvider) {
    this.cProvider = cProvider;
  }

  @Override
  public SettingsRepository get() {
    return provideSettingsRepo(cProvider.get());
  }

  public static AppModule_ProvideSettingsRepoFactory create(
      javax.inject.Provider<Context> cProvider) {
    return new AppModule_ProvideSettingsRepoFactory(Providers.asDaggerProvider(cProvider));
  }

  public static AppModule_ProvideSettingsRepoFactory create(Provider<Context> cProvider) {
    return new AppModule_ProvideSettingsRepoFactory(cProvider);
  }

  public static SettingsRepository provideSettingsRepo(Context c) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSettingsRepo(c));
  }
}
