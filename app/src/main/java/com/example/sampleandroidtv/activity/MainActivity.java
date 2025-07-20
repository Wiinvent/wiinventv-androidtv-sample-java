package com.example.sampleandroidtv.activity;

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
import tv.wiinvent.androidtv.models.ads.WelcomeAdsRequestData;
import tv.wiinvent.androidtv.models.type.DeviceType;
import tv.wiinvent.androidtv.models.type.Environment;
import tv.wiinvent.androidtv.ui.welcomead.WelcomeAdView;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {

  private static final String TAG = MainActivity.class.getCanonicalName();

  public static final String SAMPLE_TENANT_ID = "14";

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
//    InStreamManager.Companion.getInstance().init(this, SAMPLE_TENANT_ID, DeviceType.TV, Environment.SANDBOX, 5, 1, 5, 2500, LevelLog.BODY,true, 8);

    initWelcome();
  }

  private void initWelcome() {
    //init welcome
    welcomeAdView = findViewById(R.id.welcome_ad_view);

    //Welcome
    AdsWelcomeManager.Companion.getInstance()
            .init(this,
                    SAMPLE_TENANT_ID, // tenant id giống với instream (lấy config từ backend)
                    DeviceType.TV,  // cố định là TV
                    Environment.SANDBOX,  // môi truòng prod | sandbox
                    5, // load vast timeout (lấy config từ backend)
                    5, // load media timeout (lấy config từ backend)
                    5,  // buffer media timeout (lấy config từ backend)
                    2500, // max bitrate (lấy config từ backend)
                    "",
                    6, // thời gian bo qua quang cao welcome
                    true); // debug

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

    WelcomeAdsRequestData adsRequestData = new WelcomeAdsRequestData.Builder()
            .transId("22222") // mã giao dịch tạo từ server đối tác - client liên hệ server
            .uid20("") // unified id 2.0, nếu không có thì set ""
            .segments("123,12,23") //segment id của user phân tách nhau bời, dữ liệu này lấy từ backend đối tác
            .build();

    AdsWelcomeManager.Companion.getInstance().requestAds(this,
            R.id.welcome_ad_view,
            R.layout.wisdk_welcome_tvc_detail,
            R.id.wisdk_exo_player_view,
            R.id.wisdk_skip_button,
            "Bỏ qua quảng cáo",
            R.drawable.skip_icon_button,
            adsRequestData);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    AdsWelcomeManager.Companion.getInstance().release();
  }
}