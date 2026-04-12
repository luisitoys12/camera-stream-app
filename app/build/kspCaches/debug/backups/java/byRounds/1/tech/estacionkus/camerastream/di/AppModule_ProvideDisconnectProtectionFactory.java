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
import tech.estacionkus.camerastream.streaming.DisconnectProtectionManager;

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
public final class AppModule_ProvideDisconnectProtectionFactory implements Factory<DisconnectProtectionManager> {
  private final Provider<Context> cProvider;

  public AppModule_ProvideDisconnectProtectionFactory(Provider<Context> cProvider) {
    this.cProvider = cProvider;
  }

  @Override
  public DisconnectProtectionManager get() {
    return provideDisconnectProtection(cProvider.get());
  }

  public static AppModule_ProvideDisconnectProtectionFactory create(
      javax.inject.Provider<Context> cProvider) {
    return new AppModule_ProvideDisconnectProtectionFactory(Providers.asDaggerProvider(cProvider));
  }

  public static AppModule_ProvideDisconnectProtectionFactory create(Provider<Context> cProvider) {
    return new AppModule_ProvideDisconnectProtectionFactory(cProvider);
  }

  public static DisconnectProtectionManager provideDisconnectProtection(Context c) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDisconnectProtection(c));
  }
}
