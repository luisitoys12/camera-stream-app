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
import tech.estacionkus.camerastream.streaming.RecordingManager;

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
public final class AppModule_ProvideRecordingManagerFactory implements Factory<RecordingManager> {
  private final Provider<Context> cProvider;

  public AppModule_ProvideRecordingManagerFactory(Provider<Context> cProvider) {
    this.cProvider = cProvider;
  }

  @Override
  public RecordingManager get() {
    return provideRecordingManager(cProvider.get());
  }

  public static AppModule_ProvideRecordingManagerFactory create(
      javax.inject.Provider<Context> cProvider) {
    return new AppModule_ProvideRecordingManagerFactory(Providers.asDaggerProvider(cProvider));
  }

  public static AppModule_ProvideRecordingManagerFactory create(Provider<Context> cProvider) {
    return new AppModule_ProvideRecordingManagerFactory(cProvider);
  }

  public static RecordingManager provideRecordingManager(Context c) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRecordingManager(c));
  }
}
