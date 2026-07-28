package com.example.sampleandroidtv.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.sampleandroidtv.R;

import tv.wiinvent.androidtv.AdsWelcomeManager;
import tv.wiinvent.androidtv.InStreamManager;
import tv.wiinvent.androidtv.interfaces.welcome.WelcomeAdsEventListener;
import tv.wiinvent.androidtv.logging.LevelLog;
import tv.wiinvent.androidtv.models.ads.WelcomeAdsRequestData;
import tv.wiinvent.androidtv.models.type.DeviceType;
import tv.wiinvent.androidtv.models.type.Environment;
import tv.wiinvent.androidtv.ui.welcomead.WelcomeAdView;

/**
 * Loads MainFragment.
 */
public class MainActivity extends FragmentActivity implements OnMainListener {
  private final String TAG = getClass().getCanonicalName();

  private WelcomeAdView welcomeAdView = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //Ham nay init onCreate
    InStreamManager.Companion.getInstance().init(getBaseContext(), "14", DeviceType.TV, Environment.SANDBOX, 15, 15, 15, 2048, LevelLog.BODY, 6);

    //init welcome
    welcomeAdView = findViewById(R.id.welcome_ad_view);

    AdsWelcomeManager.Companion.getInstance().init(getBaseContext(), "14", DeviceType.TV, Environment.SANDBOX, 15, 15, 15, 2048, "", 8, true);

    AdsWelcomeManager.Companion.getInstance().addWelcomeListener(new WelcomeAdsEventListener() {
      @Override
      public void onDisplayAds() {
        Log.d(TAG, "=========onDisplayAds");
        runOnUiThread(() -> {
          if (welcomeAdView != null) welcomeAdView.setVisibility(View.VISIBLE);
        });
        Toast.makeText(getBaseContext(), "OnDisplayAds", Toast.LENGTH_LONG).show();
      }

      @Override
      public void onNoAds() {
        Log.d(TAG, "=========khong co ads de show 1");
        Toast.makeText(getBaseContext(), "onNoAds", Toast.LENGTH_LONG).show();
      }

      @Override
      public void onAdsWelcomeDismiss() {
        Log.d(TAG, "=========onAdsWelcomeDismiss");
        runOnUiThread(() -> {
          AdsWelcomeManager.Companion.getInstance().release();
          if (welcomeAdView != null) welcomeAdView.setVisibility(View.GONE);
        });
        Toast.makeText(getBaseContext(), "onAdsWelcomeDismiss", Toast.LENGTH_LONG).show();
      }

      @Override
      public void onAdsWelcomeError() {
        Log.d(TAG, "=========onAdsWelcomeError");
        runOnUiThread(() -> {
          AdsWelcomeManager.Companion.getInstance().release();
          if (welcomeAdView != null) welcomeAdView.setVisibility(View.GONE);
        });
        Toast.makeText(getBaseContext(), "onAdsWelcomeError", Toast.LENGTH_LONG).show();
      }
    });

    Log.d(TAG, "========requestAds " + this.hashCode());

    WelcomeAdsRequestData adsRequestData = new WelcomeAdsRequestData.Builder()
        .transId("22222")
        .userId("123123123")
        .userImpressionLimit(5)
        .segments("123,123,123")
        .adPendingTime(20)
        .build();

    AdsWelcomeManager.Companion.getInstance().requestAds(
        this,
        R.id.welcome_ad_view,
        R.layout.wisdk_welcome_tvc_detail,
        R.id.wisdk_exo_player_view,
        R.id.wisdk_skip_button,
        "Bỏ qua quảng cáo",
        R.drawable.skip_icon_button,
        adsRequestData,
        R.id.wisdk_report_button,
        -1); // infoButtonId: -1 = không hiển thị thẻ đánh dấu quảng cáo
  }

  // Bắc cầu focus D-pad giữa nút skip IMA và nút report cho quảng cáo Welcome (VAST).
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN
        && AdsWelcomeManager.Companion.getInstance().dispatchKeyEvent(event.getKeyCode())) {
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "=========onAdsWelcome-Destroy");
    AdsWelcomeManager.Companion.getInstance().release();
  }

  @Override
  public void onWelcome() {
    WelcomeAdsRequestData adsRequestData = new WelcomeAdsRequestData.Builder()
        .transId("22222").userId("123123123").userImpressionLimit(5).segments("123,123,123")
        .adPendingTime(20).build();

    AdsWelcomeManager.Companion.getInstance().requestAds(
        this,
        R.id.welcome_ad_view,
        R.layout.wisdk_welcome_tvc_detail,
        R.id.wisdk_exo_player_view,
        R.id.wisdk_skip_button,
        "Bỏ qua quảng cáo",
        R.drawable.skip_icon_button,
        adsRequestData,
        R.id.wisdk_report_button,
        -1);
  }
}
