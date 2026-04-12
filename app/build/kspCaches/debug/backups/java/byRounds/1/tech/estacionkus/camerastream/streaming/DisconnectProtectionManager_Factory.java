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
public final class DisconnectProtectionManager_Factory implements Factory<DisconnectProtectionManager> {
  private final Provider<Context> contextProvider;

  public DisconnectProtectionManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DisconnectProtectionManager get() {
    return newInstance(contextProvider.get());
  }

  public static DisconnectProtectionManager_Factory create(
      javax.inject.Provider<Context> contextProvider) {
    return new DisconnectProtectionManager_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static DisconnectProtectionManager_Factory create(Provider<Context> contextProvider) {
    return new DisconnectProtectionManager_Factory(contextProvider);
  }

  public static DisconnectProtectionManager newInstance(Context context) {
    return new DisconnectProtectionManager(context);
  }
}
