package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.MultiChatManager;

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
public final class AppModule_ProvideMultiChatFactory implements Factory<MultiChatManager> {
  @Override
  public MultiChatManager get() {
    return provideMultiChat();
  }

  public static AppModule_ProvideMultiChatFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MultiChatManager provideMultiChat() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMultiChat());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideMultiChatFactory INSTANCE = new AppModule_ProvideMultiChatFactory();
  }
}
