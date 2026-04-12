package tech.estacionkus.camerastream.data.auth;

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
public final class LicenseRepository_Factory implements Factory<LicenseRepository> {
  private final Provider<AuthRepository> authRepositoryProvider;

  public LicenseRepository_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public LicenseRepository get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static LicenseRepository_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new LicenseRepository_Factory(Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static LicenseRepository_Factory create(Provider<AuthRepository> authRepositoryProvider) {
    return new LicenseRepository_Factory(authRepositoryProvider);
  }

  public static LicenseRepository newInstance(AuthRepository authRepository) {
    return new LicenseRepository(authRepository);
  }
}
