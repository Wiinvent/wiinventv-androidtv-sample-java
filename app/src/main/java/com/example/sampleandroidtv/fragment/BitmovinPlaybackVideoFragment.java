package com.example.sampleandroidtv.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bitmovin.analytics.api.AnalyticsConfig;
import com.bitmovin.player.PlayerView;
import com.bitmovin.player.api.Player;
import com.bitmovin.player.api.PlayerConfig;
import com.bitmovin.player.api.analytics.PlayerFactory;
import com.bitmovin.player.api.source.SourceConfig;
import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.activity.DetailsActivity;
import com.example.sampleandroidtv.pojo.Movie;
import com.example.sampleandroidtv.ui.TV360SkipAdsButtonAds;
import com.example.sampleandroidtv.util.VideoCache;
import com.google.ads.interactivemedia.v3.api.FriendlyObstruction;
import com.google.ads.interactivemedia.v3.api.FriendlyObstructionPurpose;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import tv.wiinvent.androidtv.InStreamManager;
import tv.wiinvent.androidtv.OverlayManager;
import tv.wiinvent.androidtv.logging.LevelLog;
import tv.wiinvent.androidtv.models.ads.AdInStreamEvent;
import tv.wiinvent.androidtv.models.ads.AdsRequestData;
import tv.wiinvent.androidtv.models.type.ContentType;
import tv.wiinvent.androidtv.models.type.DeviceType;
import tv.wiinvent.androidtv.models.type.Environment;
import tv.wiinvent.androidtv.ui.instream.player.AdPlayerView;

/**
 * Handles video playback with media controls.
 */
public class BitmovinPlaybackVideoFragment extends Fragment {
  private static final String DRM_LICENSE_URL = "https://license.uat.widevine.com/cenc/getcontentkey/widevine_test";

  private static final String TAG = "PlaybackVideoFragment";
  private PlayerView playerView;
  private AdPlayerView adPlayerView;
  private Player player;
  public static final String SAMPLE_ACCOUNT_ID = "14";
  public static final String SAMPLE_CHANNEL_ID = "11683";
  public static final String SAMPLE_STREAM_ID = "5656262327";
  public static final String SAMPLE_TOKEN = "3001";

  private OverlayManager overlayManager;

//  private OverlayView overlayView;
  private TV360SkipAdsButtonAds skipButton;

  private int currentVolume = 0;

  private String analyticsLicenseKey = "2b6d64c1-afd6-4d1c-9b8c-5c514271172d";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Movie movie =
        (Movie) requireActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.bitmovin_playback_video_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    playerView = requireActivity().findViewById(R.id.bitmovinPlayerView);
    adPlayerView = requireActivity().findViewById(R.id.ad_player_view);
    skipButton = requireActivity().findViewById(R.id.skip_button);
//    overlayView = requireActivity().findViewById(R.id.wisdk_overlay_view);
    init(savedInstanceState);
  }

  @Override
  public void onPause() {
    super.onPause();
    if(player != null) {
      player.onPause();
    }

    if(skipButton != null) {
      skipButton.pause();
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if(skipButton != null) {
      skipButton.resume();
    }

    if(player != null) {
      player.onResume();
    }
  }

  protected void init(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      initializePlayer();
//      initializeOverlays();
    }
  }

//  private void initializeOverlays() {
////        OverlayData overlayData = null;
//    OverlayData overlayData = new OverlayData.Builder()
//        .accountId(SAMPLE_ACCOUNT_ID)
//        .channelId(SAMPLE_CHANNEL_ID)
//        .streamId(SAMPLE_STREAM_ID)
//        .thirdPartyToken(SAMPLE_TOKEN)
//        .env(OverlayData.Environment.SANDBOX)
//        .deviceType(OverlayData.DeviceType.TV)
//        .contentType(OverlayData.ContentType.LIVESTREAM)
//        .build();
//
//    overlayManager = new OverlayManager(requireActivity(), R.id.wisdk_overlay_view, overlayData);
//
//    overlayManager.addPlayerListener(() -> exoPlayer != null ? exoPlayer.getCurrentPosition() : 0L);
//
//    // Add player event listeners to determine overlay visibility.
//    exoPlayer.addListener(new Player.Listener() {
//      @Override
//      public void onPlaybackStateChanged(int playbackState) {
//        Log.d(TAG, "====onPlayerStateChanged playWhenReady: $playWhenReady - $playbackState");
//
//        if (overlayManager != null)
//          overlayManager.setVisible(playbackState == Player.STATE_READY);
//      }
//    });
//  }

  private void initializePlayer() {
    // 2. Khởi tạo InStreamManager
    InStreamManager.Companion.getInstance().init(requireContext(), SAMPLE_ACCOUNT_ID, DeviceType.TV, Environment.SANDBOX, 5, 5, 5, 2500, LevelLog.BODY,true, 8);

    player = PlayerFactory.create(requireContext(), new PlayerConfig(), new AnalyticsConfig(analyticsLicenseKey));
    playerView.setPlayer(player);

    String contentUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8";

    ContentType contentType = ContentType.FILM;
    currentVolume = player.getVolume();

    //3. Thêm WiAdsLoaderListener
    InStreamManager.Companion.getInstance().setLoaderListener(new InStreamManager.WiAdsLoaderListener() {
      @Override
      public void onEvent(@NonNull AdInStreamEvent event) {
        Log.d(TAG, "==========event " + event.getEventType() + " - " + event.getCampaignId() + ")");
        if(event.getEventType() == AdInStreamEvent.EventType.ERROR) {
          Log.d(TAG, "===========Xu ly error");
        }
      }

      @Override
      public void showSkipButton(@NonNull String campaignId, int duration) {
        if(skipButton != null)
          skipButton.startCountdown(duration, () -> {
            if(skipButton != null) {
              skipButton.requestFocusToSkip();

              Log.d(TAG, "=========request focus");
            }
          }); //neu khong muon tu dong focus thi set = true
      }

      @Override
      public void hideSkipButton(@NonNull String campaignId) {
        if(skipButton != null)
          skipButton.hide();
      }

      @Override
      public void showContentPlayer() {
        requireActivity().runOnUiThread(() -> {
          if (contentType == ContentType.TV) {
            if (player != null) {
              player.setVolume(currentVolume);
            }
          } else {
            if (player != null) {
              player.play();
            }
          }

          if (playerView != null) {
            playerView.setVisibility(View.VISIBLE);
          }
        });
      }

      @Override
      public void hideContentPlayer() {
        requireActivity().runOnUiThread(() -> {
          if (contentType == ContentType.TV) {
            if (player != null) {
              currentVolume = player.getVolume();
              player.setVolume(0);
            }
          } else {
            if (player != null) {
              player.pause();
            }
          }

          if (playerView != null) {
            playerView.setVisibility(View.GONE);
          }
        });
      }

      @Override
      public void onEventSkip(@NonNull String s) {
        //write log
      }

      @Override
      public void onFailure() {
        //write log
      }

      @Override
      public void onTimeout() {
        //write log
      }
    });

    //4. Khởi tạo AdsRequestData
    AdsRequestData adsRequestData = new AdsRequestData.Builder()
        .channelId("998989,222222") // danh sách id của category của nội dung & cách nhau bằng dấu ,
        .streamId("179") // id nội dung
        .transId("222222") // Transaction cua TV360
        .contentType(ContentType.FILM) // content type TV | FILM | VIDEO
        .title("Tieu de cua noi dung") // tiêu đề nội dung
        .category("category 1, category 2") // danh sach tiêu đề category của nội dung & cách nhau bằng dấu ,
        .keyword("keyword 1, keyword 2") // từ khoá nếu có | để "" nếu ko có
        .uid20("") // unified id 2.0, nếu không có thì set ""
        .segments("123,1,23") //segment id của user phân tách nhau bời, dữ liệu này lấy từ backend đối tác
        .build();

    //5. khai bao friendly obstruction --- quan trong => can phai cai khao het cac lop phu len tren player
    List<FriendlyObstruction> friendlyObstructionList = Lists.newArrayList();
    FriendlyObstruction skipButtonObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
        skipButton,
        FriendlyObstructionPurpose.CLOSE_AD,
        "This is close ad"
    );
    friendlyObstructionList.add(skipButtonObstruction);

//    FriendlyObstruction overlaysObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
//        overlayView,
//        FriendlyObstructionPurpose.OTHER,
//        "This is transparent overlays"
//    );
//    friendlyObstructionList.add(overlaysObstruction);


    InStreamManager.Companion.getInstance()
            .requestAdsForBitmovin(adsRequestData,
                adPlayerView,
                player,
                friendlyObstructionList);

    player.load(SourceConfig.fromUrl(contentUrl));
    player.play();
  }
}