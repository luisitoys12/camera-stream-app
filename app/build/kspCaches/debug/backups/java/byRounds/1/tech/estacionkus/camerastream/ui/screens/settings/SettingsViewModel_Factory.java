package tech.estacionkus.camerastream.ui.screens.settings;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.data.settings.SettingsRepository;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> repoProvider;

  public SettingsViewModel_Factory(Provider<SettingsRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static SettingsViewModel_Factory create(
      javax.inject.Provider<SettingsRepository> repoProvider) {
    return new SettingsViewModel_Factory(Providers.asDaggerProvider(repoProvider));
  }

  public static SettingsViewModel_Factory create(Provider<SettingsRepository> repoProvider) {
    return new SettingsViewModel_Factory(repoProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository repo) {
    return new SettingsViewModel(repo);
  }
}
