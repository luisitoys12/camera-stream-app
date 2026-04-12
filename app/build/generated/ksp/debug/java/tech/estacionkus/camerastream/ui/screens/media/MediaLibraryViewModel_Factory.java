package tech.estacionkus.camerastream.ui.screens.media;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.data.media.MediaRepository;

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
public final class MediaLibraryViewModel_Factory implements Factory<MediaLibraryViewModel> {
  private final Provider<MediaRepository> mediaRepositoryProvider;

  public MediaLibraryViewModel_Factory(Provider<MediaRepository> mediaRepositoryProvider) {
    this.mediaRepositoryProvider = mediaRepositoryProvider;
  }

  @Override
  public MediaLibraryViewModel get() {
    return newInstance(mediaRepositoryProvider.get());
  }

  public static MediaLibraryViewModel_Factory create(
      javax.inject.Provider<MediaRepository> mediaRepositoryProvider) {
    return new MediaLibraryViewModel_Factory(Providers.asDaggerProvider(mediaRepositoryProvider));
  }

  public static MediaLibraryViewModel_Factory create(
      Provider<MediaRepository> mediaRepositoryProvider) {
    return new MediaLibraryViewModel_Factory(mediaRepositoryProvider);
  }

  public static MediaLibraryViewModel newInstance(MediaRepository mediaRepository) {
    return new MediaLibraryViewModel(mediaRepository);
  }
}
