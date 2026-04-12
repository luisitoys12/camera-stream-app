package tech.estacionkus.camerastream.streaming;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ChatManager_Factory implements Factory<ChatManager> {
  @Override
  public ChatManager get() {
    return newInstance();
  }

  public static ChatManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ChatManager newInstance() {
    return new ChatManager();
  }

  private static final class InstanceHolder {
    static final ChatManager_Factory INSTANCE = new ChatManager_Factory();
  }
}
