package tech.estacionkus.camerastream.data.overlay;

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
public final class OverlayRepository_Factory implements Factory<OverlayRepository> {
  private final Provider<Context> contextProvider;

  public OverlayRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OverlayRepository get() {
    return newInstance(contextProvider.get());
  }

  public static OverlayRepository_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new OverlayRepository_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static OverlayRepository_Factory create(Provider<Context> contextProvider) {
    return new OverlayRepository_Factory(contextProvider);
  }

  public static OverlayRepository newInstance(Context context) {
    return new OverlayRepository(context);
  }
}
