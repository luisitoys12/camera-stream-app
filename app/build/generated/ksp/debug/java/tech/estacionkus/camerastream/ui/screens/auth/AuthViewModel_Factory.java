package tech.estacionkus.camerastream.ui.screens.auth;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.data.auth.AuthRepository;
import tech.estacionkus.camerastream.data.auth.LicenseRepository;

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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<LicenseRepository> licenseRepositoryProvider;

  public AuthViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<LicenseRepository> licenseRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.licenseRepositoryProvider = licenseRepositoryProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(authRepositoryProvider.get(), licenseRepositoryProvider.get());
  }

  public static AuthViewModel_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider,
      javax.inject.Provider<LicenseRepository> licenseRepositoryProvider) {
    return new AuthViewModel_Factory(Providers.asDaggerProvider(authRepositoryProvider), Providers.asDaggerProvider(licenseRepositoryProvider));
  }

  public static AuthViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<LicenseRepository> licenseRepositoryProvider) {
    return new AuthViewModel_Factory(authRepositoryProvider, licenseRepositoryProvider);
  }

  public static AuthViewModel newInstance(AuthRepository authRepository,
      LicenseRepository licenseRepository) {
    return new AuthViewModel(authRepository, licenseRepository);
  }
}
