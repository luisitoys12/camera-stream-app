package tech.estacionkus.camerastream;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import tech.estacionkus.camerastream.billing.StripeManager;
import tech.estacionkus.camerastream.data.auth.AuthRepository;
import tech.estacionkus.camerastream.data.auth.LicenseRepository;
import tech.estacionkus.camerastream.data.media.MediaRepository;
import tech.estacionkus.camerastream.data.overlay.OverlayRepository;
import tech.estacionkus.camerastream.data.settings.SettingsRepository;
import tech.estacionkus.camerastream.di.AppModule_ProvideAuthRepositoryFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideChatManagerFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideCloudflaredFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideDisconnectProtectionFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideFeatureGateFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideGuestModeFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideLicenseRepositoryFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideMultiChatFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideMultiStreamFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideRecordingManagerFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideRtmpFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideSceneManagerFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideSettingsRepoFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideSportsStateFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideSrtServerFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideStreamHealthFactory;
import tech.estacionkus.camerastream.di.AppModule_ProvideStripeManagerFactory;
import tech.estacionkus.camerastream.domain.FeatureGate;
import tech.estacionkus.camerastream.domain.SceneManager;
import tech.estacionkus.camerastream.streaming.ChatManager;
import tech.estacionkus.camerastream.streaming.CloudflaredManager;
import tech.estacionkus.camerastream.streaming.DisconnectProtectionManager;
import tech.estacionkus.camerastream.streaming.GuestModeManager;
import tech.estacionkus.camerastream.streaming.MultiChatManager;
import tech.estacionkus.camerastream.streaming.MultiStreamManager;
import tech.estacionkus.camerastream.streaming.RecordingManager;
import tech.estacionkus.camerastream.streaming.RtmpStreamManager;
import tech.estacionkus.camerastream.streaming.SportsStateManager;
import tech.estacionkus.camerastream.streaming.SrtServerManager;
import tech.estacionkus.camerastream.streaming.StreamHealthMonitor;
import tech.estacionkus.camerastream.ui.screens.auth.AuthViewModel;
import tech.estacionkus.camerastream.ui.screens.auth.AuthViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.auth.AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.auth.AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.chat.ChatViewModel;
import tech.estacionkus.camerastream.ui.screens.chat.ChatViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.chat.ChatViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.chat.ChatViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.filters.CameraFiltersViewModel;
import tech.estacionkus.camerastream.ui.screens.filters.CameraFiltersViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.filters.CameraFiltersViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.filters.CameraFiltersViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.guest.GuestViewModel;
import tech.estacionkus.camerastream.ui.screens.guest.GuestViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.guest.GuestViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.guest.GuestViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.health.HealthViewModel;
import tech.estacionkus.camerastream.ui.screens.health.HealthViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.health.HealthViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.health.HealthViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.home.HomeViewModel;
import tech.estacionkus.camerastream.ui.screens.home.HomeViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.home.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.home.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.media.MediaLibraryViewModel;
import tech.estacionkus.camerastream.ui.screens.media.MediaLibraryViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.media.MediaLibraryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.media.MediaLibraryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.pro.ManualCameraViewModel;
import tech.estacionkus.camerastream.ui.screens.pro.ManualCameraViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.pro.ManualCameraViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.pro.ManualCameraViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.pro.SrtServerViewModel;
import tech.estacionkus.camerastream.ui.screens.pro.SrtServerViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.pro.SrtServerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.pro.SrtServerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.pro.UpgradeViewModel;
import tech.estacionkus.camerastream.ui.screens.pro.UpgradeViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.pro.UpgradeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.pro.UpgradeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.radio.RadioBroadcastViewModel;
import tech.estacionkus.camerastream.ui.screens.radio.RadioBroadcastViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.radio.RadioBroadcastViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.radio.RadioBroadcastViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.scenes.ScenesViewModel;
import tech.estacionkus.camerastream.ui.screens.scenes.ScenesViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.scenes.ScenesViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.scenes.ScenesViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.settings.SettingsViewModel;
import tech.estacionkus.camerastream.ui.screens.settings.SettingsViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.sports.SportsModeViewModel;
import tech.estacionkus.camerastream.ui.screens.sports.SportsModeViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.sports.SportsModeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.sports.SportsModeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.stream.StreamViewModel;
import tech.estacionkus.camerastream.ui.screens.stream.StreamViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.stream.StreamViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.stream.StreamViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.studio.MyStudioViewModel;
import tech.estacionkus.camerastream.ui.screens.studio.MyStudioViewModel_HiltModules;
import tech.estacionkus.camerastream.ui.screens.studio.MyStudioViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import tech.estacionkus.camerastream.ui.screens.studio.MyStudioViewModel_HiltModules_KeyModule_Provide_LazyMapKey;

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
public final class DaggerCameraStreamApp_HiltComponents_SingletonC {
  private DaggerCameraStreamApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CameraStreamApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CameraStreamApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CameraStreamApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CameraStreamApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CameraStreamApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CameraStreamApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CameraStreamApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CameraStreamApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CameraStreamApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CameraStreamApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CameraStreamApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CameraStreamApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CameraStreamApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CameraStreamApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CameraStreamApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CameraStreamApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CameraStreamApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CameraStreamApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CameraStreamApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(16).put(AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AuthViewModel_HiltModules.KeyModule.provide()).put(CameraFiltersViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CameraFiltersViewModel_HiltModules.KeyModule.provide()).put(ChatViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChatViewModel_HiltModules.KeyModule.provide()).put(GuestViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, GuestViewModel_HiltModules.KeyModule.provide()).put(HealthViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HealthViewModel_HiltModules.KeyModule.provide()).put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide()).put(ManualCameraViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ManualCameraViewModel_HiltModules.KeyModule.provide()).put(MediaLibraryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MediaLibraryViewModel_HiltModules.KeyModule.provide()).put(MyStudioViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MyStudioViewModel_HiltModules.KeyModule.provide()).put(RadioBroadcastViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RadioBroadcastViewModel_HiltModules.KeyModule.provide()).put(ScenesViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ScenesViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(SportsModeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SportsModeViewModel_HiltModules.KeyModule.provide()).put(SrtServerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SrtServerViewModel_HiltModules.KeyModule.provide()).put(StreamViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, StreamViewModel_HiltModules.KeyModule.provide()).put(UpgradeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, UpgradeViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }
  }

  private static final class ViewModelCImpl extends CameraStreamApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<CameraFiltersViewModel> cameraFiltersViewModelProvider;

    private Provider<ChatViewModel> chatViewModelProvider;

    private Provider<GuestViewModel> guestViewModelProvider;

    private Provider<HealthViewModel> healthViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<ManualCameraViewModel> manualCameraViewModelProvider;

    private Provider<MediaLibraryViewModel> mediaLibraryViewModelProvider;

    private Provider<MyStudioViewModel> myStudioViewModelProvider;

    private Provider<RadioBroadcastViewModel> radioBroadcastViewModelProvider;

    private Provider<ScenesViewModel> scenesViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SportsModeViewModel> sportsModeViewModelProvider;

    private Provider<SrtServerViewModel> srtServerViewModelProvider;

    private Provider<StreamViewModel> streamViewModelProvider;

    private Provider<UpgradeViewModel> upgradeViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.cameraFiltersViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.chatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.guestViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.healthViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.manualCameraViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.mediaLibraryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.myStudioViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.radioBroadcastViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.scenesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.sportsModeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.srtServerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.streamViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.upgradeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(16).put(AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) authViewModelProvider)).put(CameraFiltersViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) cameraFiltersViewModelProvider)).put(ChatViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) chatViewModelProvider)).put(GuestViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) guestViewModelProvider)).put(HealthViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) healthViewModelProvider)).put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) homeViewModelProvider)).put(ManualCameraViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) manualCameraViewModelProvider)).put(MediaLibraryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) mediaLibraryViewModelProvider)).put(MyStudioViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) myStudioViewModelProvider)).put(RadioBroadcastViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) radioBroadcastViewModelProvider)).put(ScenesViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) scenesViewModelProvider)).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) settingsViewModelProvider)).put(SportsModeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) sportsModeViewModelProvider)).put(SrtServerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) srtServerViewModelProvider)).put(StreamViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) streamViewModelProvider)).put(UpgradeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) upgradeViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // tech.estacionkus.camerastream.ui.screens.auth.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.provideAuthRepositoryProvider.get(), singletonCImpl.provideLicenseRepositoryProvider.get());

          case 1: // tech.estacionkus.camerastream.ui.screens.filters.CameraFiltersViewModel 
          return (T) new CameraFiltersViewModel(singletonCImpl.provideStripeManagerProvider.get());

          case 2: // tech.estacionkus.camerastream.ui.screens.chat.ChatViewModel 
          return (T) new ChatViewModel(singletonCImpl.provideMultiChatProvider.get(), singletonCImpl.provideFeatureGateProvider.get());

          case 3: // tech.estacionkus.camerastream.ui.screens.guest.GuestViewModel 
          return (T) new GuestViewModel(singletonCImpl.provideGuestModeProvider.get(), singletonCImpl.provideFeatureGateProvider.get());

          case 4: // tech.estacionkus.camerastream.ui.screens.health.HealthViewModel 
          return (T) new HealthViewModel(singletonCImpl.provideStreamHealthProvider.get(), singletonCImpl.provideFeatureGateProvider.get());

          case 5: // tech.estacionkus.camerastream.ui.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.provideFeatureGateProvider.get(), singletonCImpl.provideStripeManagerProvider.get());

          case 6: // tech.estacionkus.camerastream.ui.screens.pro.ManualCameraViewModel 
          return (T) new ManualCameraViewModel();

          case 7: // tech.estacionkus.camerastream.ui.screens.media.MediaLibraryViewModel 
          return (T) new MediaLibraryViewModel(singletonCImpl.mediaRepositoryProvider.get());

          case 8: // tech.estacionkus.camerastream.ui.screens.studio.MyStudioViewModel 
          return (T) new MyStudioViewModel(singletonCImpl.provideSceneManagerProvider.get(), singletonCImpl.overlayRepositoryProvider.get(), singletonCImpl.provideFeatureGateProvider.get());

          case 9: // tech.estacionkus.camerastream.ui.screens.radio.RadioBroadcastViewModel 
          return (T) new RadioBroadcastViewModel(singletonCImpl.provideStripeManagerProvider.get());

          case 10: // tech.estacionkus.camerastream.ui.screens.scenes.ScenesViewModel 
          return (T) new ScenesViewModel(singletonCImpl.provideSceneManagerProvider.get(), singletonCImpl.provideFeatureGateProvider.get());

          case 11: // tech.estacionkus.camerastream.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideSettingsRepoProvider.get());

          case 12: // tech.estacionkus.camerastream.ui.screens.sports.SportsModeViewModel 
          return (T) new SportsModeViewModel(singletonCImpl.provideSportsStateProvider.get(), singletonCImpl.provideFeatureGateProvider.get());

          case 13: // tech.estacionkus.camerastream.ui.screens.pro.SrtServerViewModel 
          return (T) new SrtServerViewModel(singletonCImpl.provideSrtServerProvider.get(), singletonCImpl.provideCloudflaredProvider.get());

          case 14: // tech.estacionkus.camerastream.ui.screens.stream.StreamViewModel 
          return (T) new StreamViewModel(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule), singletonCImpl.provideRtmpProvider.get(), singletonCImpl.provideMultiStreamProvider.get(), singletonCImpl.provideRecordingManagerProvider.get(), singletonCImpl.provideChatManagerProvider.get(), singletonCImpl.provideSettingsRepoProvider.get(), singletonCImpl.provideLicenseRepositoryProvider.get(), singletonCImpl.provideFeatureGateProvider.get(), singletonCImpl.provideDisconnectProtectionProvider.get(), singletonCImpl.provideStreamHealthProvider.get(), singletonCImpl.provideSceneManagerProvider.get());

          case 15: // tech.estacionkus.camerastream.ui.screens.pro.UpgradeViewModel 
          return (T) new UpgradeViewModel(singletonCImpl.provideStripeManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CameraStreamApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CameraStreamApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends CameraStreamApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AuthRepository> provideAuthRepositoryProvider;

    private Provider<LicenseRepository> provideLicenseRepositoryProvider;

    private Provider<FeatureGate> provideFeatureGateProvider;

    private Provider<StripeManager> provideStripeManagerProvider;

    private Provider<MultiChatManager> provideMultiChatProvider;

    private Provider<GuestModeManager> provideGuestModeProvider;

    private Provider<StreamHealthMonitor> provideStreamHealthProvider;

    private Provider<MediaRepository> mediaRepositoryProvider;

    private Provider<SceneManager> provideSceneManagerProvider;

    private Provider<OverlayRepository> overlayRepositoryProvider;

    private Provider<SettingsRepository> provideSettingsRepoProvider;

    private Provider<SportsStateManager> provideSportsStateProvider;

    private Provider<SrtServerManager> provideSrtServerProvider;

    private Provider<CloudflaredManager> provideCloudflaredProvider;

    private Provider<RtmpStreamManager> provideRtmpProvider;

    private Provider<MultiStreamManager> provideMultiStreamProvider;

    private Provider<RecordingManager> provideRecordingManagerProvider;

    private Provider<ChatManager> provideChatManagerProvider;

    private Provider<DisconnectProtectionManager> provideDisconnectProtectionProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAuthRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 0));
      this.provideLicenseRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<LicenseRepository>(singletonCImpl, 1));
      this.provideFeatureGateProvider = DoubleCheck.provider(new SwitchingProvider<FeatureGate>(singletonCImpl, 3));
      this.provideStripeManagerProvider = DoubleCheck.provider(new SwitchingProvider<StripeManager>(singletonCImpl, 2));
      this.provideMultiChatProvider = DoubleCheck.provider(new SwitchingProvider<MultiChatManager>(singletonCImpl, 4));
      this.provideGuestModeProvider = DoubleCheck.provider(new SwitchingProvider<GuestModeManager>(singletonCImpl, 5));
      this.provideStreamHealthProvider = DoubleCheck.provider(new SwitchingProvider<StreamHealthMonitor>(singletonCImpl, 6));
      this.mediaRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MediaRepository>(singletonCImpl, 7));
      this.provideSceneManagerProvider = DoubleCheck.provider(new SwitchingProvider<SceneManager>(singletonCImpl, 8));
      this.overlayRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<OverlayRepository>(singletonCImpl, 9));
      this.provideSettingsRepoProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepository>(singletonCImpl, 10));
      this.provideSportsStateProvider = DoubleCheck.provider(new SwitchingProvider<SportsStateManager>(singletonCImpl, 11));
      this.provideSrtServerProvider = DoubleCheck.provider(new SwitchingProvider<SrtServerManager>(singletonCImpl, 12));
      this.provideCloudflaredProvider = DoubleCheck.provider(new SwitchingProvider<CloudflaredManager>(singletonCImpl, 13));
      this.provideRtmpProvider = DoubleCheck.provider(new SwitchingProvider<RtmpStreamManager>(singletonCImpl, 14));
      this.provideMultiStreamProvider = DoubleCheck.provider(new SwitchingProvider<MultiStreamManager>(singletonCImpl, 15));
      this.provideRecordingManagerProvider = DoubleCheck.provider(new SwitchingProvider<RecordingManager>(singletonCImpl, 16));
      this.provideChatManagerProvider = DoubleCheck.provider(new SwitchingProvider<ChatManager>(singletonCImpl, 17));
      this.provideDisconnectProtectionProvider = DoubleCheck.provider(new SwitchingProvider<DisconnectProtectionManager>(singletonCImpl, 18));
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @Override
    public void injectCameraStreamApp(CameraStreamApp arg0) {
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // tech.estacionkus.camerastream.data.auth.AuthRepository 
          return (T) AppModule_ProvideAuthRepositoryFactory.provideAuthRepository();

          case 1: // tech.estacionkus.camerastream.data.auth.LicenseRepository 
          return (T) AppModule_ProvideLicenseRepositoryFactory.provideLicenseRepository(singletonCImpl.provideAuthRepositoryProvider.get());

          case 2: // tech.estacionkus.camerastream.billing.StripeManager 
          return (T) AppModule_ProvideStripeManagerFactory.provideStripeManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideFeatureGateProvider.get());

          case 3: // tech.estacionkus.camerastream.domain.FeatureGate 
          return (T) AppModule_ProvideFeatureGateFactory.provideFeatureGate();

          case 4: // tech.estacionkus.camerastream.streaming.MultiChatManager 
          return (T) AppModule_ProvideMultiChatFactory.provideMultiChat();

          case 5: // tech.estacionkus.camerastream.streaming.GuestModeManager 
          return (T) AppModule_ProvideGuestModeFactory.provideGuestMode(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // tech.estacionkus.camerastream.streaming.StreamHealthMonitor 
          return (T) AppModule_ProvideStreamHealthFactory.provideStreamHealth();

          case 7: // tech.estacionkus.camerastream.data.media.MediaRepository 
          return (T) new MediaRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // tech.estacionkus.camerastream.domain.SceneManager 
          return (T) AppModule_ProvideSceneManagerFactory.provideSceneManager();

          case 9: // tech.estacionkus.camerastream.data.overlay.OverlayRepository 
          return (T) new OverlayRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // tech.estacionkus.camerastream.data.settings.SettingsRepository 
          return (T) AppModule_ProvideSettingsRepoFactory.provideSettingsRepo(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 11: // tech.estacionkus.camerastream.streaming.SportsStateManager 
          return (T) AppModule_ProvideSportsStateFactory.provideSportsState();

          case 12: // tech.estacionkus.camerastream.streaming.SrtServerManager 
          return (T) AppModule_ProvideSrtServerFactory.provideSrtServer();

          case 13: // tech.estacionkus.camerastream.streaming.CloudflaredManager 
          return (T) AppModule_ProvideCloudflaredFactory.provideCloudflared(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 14: // tech.estacionkus.camerastream.streaming.RtmpStreamManager 
          return (T) AppModule_ProvideRtmpFactory.provideRtmp(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 15: // tech.estacionkus.camerastream.streaming.MultiStreamManager 
          return (T) AppModule_ProvideMultiStreamFactory.provideMultiStream(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 16: // tech.estacionkus.camerastream.streaming.RecordingManager 
          return (T) AppModule_ProvideRecordingManagerFactory.provideRecordingManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 17: // tech.estacionkus.camerastream.streaming.ChatManager 
          return (T) AppModule_ProvideChatManagerFactory.provideChatManager();

          case 18: // tech.estacionkus.camerastream.streaming.DisconnectProtectionManager 
          return (T) AppModule_ProvideDisconnectProtectionFactory.provideDisconnectProtection(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
