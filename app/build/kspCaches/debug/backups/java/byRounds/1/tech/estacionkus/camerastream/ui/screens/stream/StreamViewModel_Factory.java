package tech.estacionkus.camerastream.ui.screens.stream;

import android.app.Application;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.data.auth.LicenseRepository;
import tech.estacionkus.camerastream.data.settings.SettingsRepository;
import tech.estacionkus.camerastream.domain.FeatureGate;
import tech.estacionkus.camerastream.domain.SceneManager;
import tech.estacionkus.camerastream.streaming.ChatManager;
import tech.estacionkus.camerastream.streaming.DisconnectProtectionManager;
import tech.estacionkus.camerastream.streaming.MultiStreamManager;
import tech.estacionkus.camerastream.streaming.RecordingManager;
import tech.estacionkus.camerastream.streaming.RtmpStreamManager;
import tech.estacionkus.camerastream.streaming.StreamHealthMonitor;

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
public final class StreamViewModel_Factory implements Factory<StreamViewModel> {
  private final Provider<Application> appProvider;

  private final Provider<RtmpStreamManager> streamManagerProvider;

  private final Provider<MultiStreamManager> multiStreamManagerProvider;

  private final Provider<RecordingManager> recordingManagerProvider;

  private final Provider<ChatManager> chatManagerProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<LicenseRepository> licenseRepositoryProvider;

  private final Provider<FeatureGate> featureGateProvider;

  private final Provider<DisconnectProtectionManager> disconnectProtectionProvider;

  private final Provider<StreamHealthMonitor> healthMonitorProvider;

  private final Provider<SceneManager> sceneManagerProvider;

  public StreamViewModel_Factory(Provider<Application> appProvider,
      Provider<RtmpStreamManager> streamManagerProvider,
      Provider<MultiStreamManager> multiStreamManagerProvider,
      Provider<RecordingManager> recordingManagerProvider,
      Provider<ChatManager> chatManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<LicenseRepository> licenseRepositoryProvider,
      Provider<FeatureGate> featureGateProvider,
      Provider<DisconnectProtectionManager> disconnectProtectionProvider,
      Provider<StreamHealthMonitor> healthMonitorProvider,
      Provider<SceneManager> sceneManagerProvider) {
    this.appProvider = appProvider;
    this.streamManagerProvider = streamManagerProvider;
    this.multiStreamManagerProvider = multiStreamManagerProvider;
    this.recordingManagerProvider = recordingManagerProvider;
    this.chatManagerProvider = chatManagerProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.licenseRepositoryProvider = licenseRepositoryProvider;
    this.featureGateProvider = featureGateProvider;
    this.disconnectProtectionProvider = disconnectProtectionProvider;
    this.healthMonitorProvider = healthMonitorProvider;
    this.sceneManagerProvider = sceneManagerProvider;
  }

  @Override
  public StreamViewModel get() {
    return newInstance(appProvider.get(), streamManagerProvider.get(), multiStreamManagerProvider.get(), recordingManagerProvider.get(), chatManagerProvider.get(), settingsRepositoryProvider.get(), licenseRepositoryProvider.get(), featureGateProvider.get(), disconnectProtectionProvider.get(), healthMonitorProvider.get(), sceneManagerProvider.get());
  }

  public static StreamViewModel_Factory create(javax.inject.Provider<Application> appProvider,
      javax.inject.Provider<RtmpStreamManager> streamManagerProvider,
      javax.inject.Provider<MultiStreamManager> multiStreamManagerProvider,
      javax.inject.Provider<RecordingManager> recordingManagerProvider,
      javax.inject.Provider<ChatManager> chatManagerProvider,
      javax.inject.Provider<SettingsRepository> settingsRepositoryProvider,
      javax.inject.Provider<LicenseRepository> licenseRepositoryProvider,
      javax.inject.Provider<FeatureGate> featureGateProvider,
      javax.inject.Provider<DisconnectProtectionManager> disconnectProtectionProvider,
      javax.inject.Provider<StreamHealthMonitor> healthMonitorProvider,
      javax.inject.Provider<SceneManager> sceneManagerProvider) {
    return new StreamViewModel_Factory(Providers.asDaggerProvider(appProvider), Providers.asDaggerProvider(streamManagerProvider), Providers.asDaggerProvider(multiStreamManagerProvider), Providers.asDaggerProvider(recordingManagerProvider), Providers.asDaggerProvider(chatManagerProvider), Providers.asDaggerProvider(settingsRepositoryProvider), Providers.asDaggerProvider(licenseRepositoryProvider), Providers.asDaggerProvider(featureGateProvider), Providers.asDaggerProvider(disconnectProtectionProvider), Providers.asDaggerProvider(healthMonitorProvider), Providers.asDaggerProvider(sceneManagerProvider));
  }

  public static StreamViewModel_Factory create(Provider<Application> appProvider,
      Provider<RtmpStreamManager> streamManagerProvider,
      Provider<MultiStreamManager> multiStreamManagerProvider,
      Provider<RecordingManager> recordingManagerProvider,
      Provider<ChatManager> chatManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<LicenseRepository> licenseRepositoryProvider,
      Provider<FeatureGate> featureGateProvider,
      Provider<DisconnectProtectionManager> disconnectProtectionProvider,
      Provider<StreamHealthMonitor> healthMonitorProvider,
      Provider<SceneManager> sceneManagerProvider) {
    return new StreamViewModel_Factory(appProvider, streamManagerProvider, multiStreamManagerProvider, recordingManagerProvider, chatManagerProvider, settingsRepositoryProvider, licenseRepositoryProvider, featureGateProvider, disconnectProtectionProvider, healthMonitorProvider, sceneManagerProvider);
  }

  public static StreamViewModel newInstance(Application app, RtmpStreamManager streamManager,
      MultiStreamManager multiStreamManager, RecordingManager recordingManager,
      ChatManager chatManager, SettingsRepository settingsRepository,
      LicenseRepository licenseRepository, FeatureGate featureGate,
      DisconnectProtectionManager disconnectProtection, StreamHealthMonitor healthMonitor,
      SceneManager sceneManager) {
    return new StreamViewModel(app, streamManager, multiStreamManager, recordingManager, chatManager, settingsRepository, licenseRepository, featureGate, disconnectProtection, healthMonitor, sceneManager);
  }
}
