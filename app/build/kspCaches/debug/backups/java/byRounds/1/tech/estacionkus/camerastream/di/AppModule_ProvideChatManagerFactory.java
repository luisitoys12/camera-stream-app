package tech.estacionkus.camerastream.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.streaming.ChatManager;

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
public final class AppModule_ProvideChatManagerFactory implements Factory<ChatManager> {
  @Override
  public ChatManager get() {
    return provideChatManager();
  }

  public static AppModule_ProvideChatManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ChatManager provideChatManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideChatManager());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideChatManagerFactory INSTANCE = new AppModule_ProvideChatManagerFactory();
  }
}
