package tech.estacionkus.camerastream.ui.screens.chat;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.domain.FeatureGate;
import tech.estacionkus.camerastream.streaming.MultiChatManager;

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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<MultiChatManager> chatManagerProvider;

  private final Provider<FeatureGate> featureGateProvider;

  public ChatViewModel_Factory(Provider<MultiChatManager> chatManagerProvider,
      Provider<FeatureGate> featureGateProvider) {
    this.chatManagerProvider = chatManagerProvider;
    this.featureGateProvider = featureGateProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(chatManagerProvider.get(), featureGateProvider.get());
  }

  public static ChatViewModel_Factory create(
      javax.inject.Provider<MultiChatManager> chatManagerProvider,
      javax.inject.Provider<FeatureGate> featureGateProvider) {
    return new ChatViewModel_Factory(Providers.asDaggerProvider(chatManagerProvider), Providers.asDaggerProvider(featureGateProvider));
  }

  public static ChatViewModel_Factory create(Provider<MultiChatManager> chatManagerProvider,
      Provider<FeatureGate> featureGateProvider) {
    return new ChatViewModel_Factory(chatManagerProvider, featureGateProvider);
  }

  public static ChatViewModel newInstance(MultiChatManager chatManager, FeatureGate featureGate) {
    return new ChatViewModel(chatManager, featureGate);
  }
}
