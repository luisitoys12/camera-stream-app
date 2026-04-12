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
public final class MultiChatManager_Factory implements Factory<MultiChatManager> {
  @Override
  public MultiChatManager get() {
    return newInstance();
  }

  public static MultiChatManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MultiChatManager newInstance() {
    return new MultiChatManager();
  }

  private static final class InstanceHolder {
    static final MultiChatManager_Factory INSTANCE = new MultiChatManager_Factory();
  }
}
