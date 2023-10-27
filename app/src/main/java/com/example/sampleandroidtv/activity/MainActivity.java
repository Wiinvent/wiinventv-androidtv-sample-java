package com.example.sampleandroidtv.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.example.sampleandroidtv.fragment.MainFragment;
import com.example.sampleandroidtv.R;

import tv.wiinvent.androidtv.AdsWelcomeManager;
import tv.wiinvent.androidtv.InStreamManager;
import tv.wiinvent.androidtv.interfaces.welcome.WelcomeAdsEventListener;
import tv.wiinvent.androidtv.logging.LevelLog;
import tv.wiinvent.androidtv.models.type.DeviceType;
import tv.wiinvent.androidtv.models.type.Environment;
import tv.wiinvent.androidtv.ui.welcomead.WelcomeAdView;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {

  private static final String TAG = MainActivity.class.getCanonicalName();

  public static final String SAMPLE_ACCOUNT_ID = "14";

  private WelcomeAdView welcomeAdView = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_browse_fragment, new MainFragment())
          .commitNow();
    }

    //Ham nay init onCreate
    InStreamManager.Companion.getInstance().init(this, SAMPLE_ACCOUNT_ID, DeviceType.TV, Environment.SANDBOX, 3, 5, 15, LevelLog.BODY,true, 8);

    //init welcome
    welcomeAdView = findViewById(R.id.welcome_ad_view);

    //Bo
    AdsWelcomeManager.Companion.getInstance().init(this,  "14", DeviceType.TV, Environment.SANDBOX, 10, "", 8, true); //TODO: them partnerSkipOffset
    AdsWelcomeManager.Companion.getInstance().addWelcomeListener(new WelcomeAdsEventListener() {
      @Override
      public void onDisplayAds() {
        Log.d(TAG, "=========onDisplayAds");

        runOnUiThread(() -> {
          if(welcomeAdView != null)
            welcomeAdView.setVisibility(View.VISIBLE);
        });
      }

      @Override
      public void onNoAds() {
        Log.d(TAG, "=========onNoAds");
      }

      @Override
      public void onAdsWelcomeDismiss() {
        Log.d(TAG, "=========onAdsWelcomeDismiss");

        runOnUiThread(() -> {
          if(welcomeAdView != null)
            welcomeAdView.setVisibility(View.GONE);
        });
      }

      @Override
      public void onAdsWelcomeError() {
        Log.d(TAG, "=========onAdsWelcomeError");
        runOnUiThread(() -> {
          if(welcomeAdView != null)
            welcomeAdView.setVisibility(View.GONE);
        });
      }
    });

    AdsWelcomeManager.Companion.getInstance().requestAds(this,
    R.id.welcome_ad_view,
        R.layout.wisdk_welcome_tvc_detail,
        R.id.wisdk_exo_player_view,
        R.id.wisdk_skip_button,
        "Bỏ qua quảng cáo",
        R.drawable.skip_icon_button);
  }
}