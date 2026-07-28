package com.example.sampleandroidtv.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.activity.DetailsActivity;
import com.example.sampleandroidtv.model.Movie;
import com.example.sampleandroidtv.ui.TV360ReportAdsButton;
import com.example.sampleandroidtv.ui.TV360SkipAdsButtonAds;
import com.google.ads.interactivemedia.v3.api.FriendlyObstruction;
import com.google.ads.interactivemedia.v3.api.FriendlyObstructionPurpose;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import tv.wiinvent.androidtv.InStreamManager;
import tv.wiinvent.androidtv.logging.LevelLog;
import tv.wiinvent.androidtv.models.ads.AdInStreamEvent;
import tv.wiinvent.androidtv.models.ads.AdsRequestData;
import tv.wiinvent.androidtv.models.type.ContentType;
import tv.wiinvent.androidtv.models.type.DeviceType;
import tv.wiinvent.androidtv.models.type.Environment;
import tv.wiinvent.androidtv.ui.FriendlyPlayerView;
import tv.wiinvent.androidtv.ui.OverlayView;
import tv.wiinvent.androidtv.ui.instream.SkipAdsButtonAds;

/** Handles video playback with media controls. */
public class PlaybackVideoFragment extends Fragment {
  private static final String TAG = "PlaybackVideoFragment";

  private FriendlyPlayerView playerView = null;
  private ExoPlayer exoPlayer = null;

  private OverlayView overlayView = null;
  private TV360SkipAdsButtonAds skipButton = null;
  private TV360ReportAdsButton reportButton = null;
  private boolean isInStreamAdPlaying = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Movie movie = (Movie) requireActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.playback_video_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    playerView = requireActivity().findViewById(R.id.simple_exo_player_view);
    skipButton = requireActivity().findViewById(R.id.skip_button);
    reportButton = requireActivity().findViewById(R.id.instream_report_button);
    overlayView = requireActivity().findViewById(R.id.wisdk_overlay_view);
    init(savedInstanceState);
  }

  /**
   * Bắc cầu focus giữa nút skip native của IMA và nút report (view của app) khi
   * điều khiển bằng remote TV. Activity gọi hàm này trong dispatchKeyEvent.
   * Trả về true nếu đã xử lý sự kiện (nuốt key).
   */
  public boolean onDpadKey(int keyCode) {
    if (!isInStreamAdPlaying || reportButton == null) return false;
    switch (keyCode) {
      // Từ nút skip IMA -> chuyển focus sang nút report
      case KeyEvent.KEYCODE_DPAD_UP:
      case KeyEvent.KEYCODE_DPAD_LEFT:
        if (!reportButton.hasFocus()) {
          reportButton.requestFocusToReportButton();
          return true;
        }
        return false;
      // Từ nút report -> trả focus về nút skip của IMA
      case KeyEvent.KEYCODE_DPAD_DOWN:
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        if (reportButton.hasFocus()) {
          InStreamManager.Companion.getInstance().focusSkipButton();
          return true;
        }
        return false;
      default:
        return false;
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (exoPlayer != null) exoPlayer.setPlayWhenReady(false);
    if (skipButton != null) skipButton.pause();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (skipButton != null) skipButton.resume();
    if (exoPlayer != null) exoPlayer.setPlayWhenReady(true);
  }

  protected void init(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      initializePlayer();
    }
  }

  private void initializePlayer() {
    //1. Khởi tạo InStreamManager
    InStreamManager.Companion.getInstance().init(requireContext(), "14", DeviceType.TV, Environment.SANDBOX, 5, 10, 5, 2500, LevelLog.BODY, 8);

    String userAgent = Util.getUserAgent(requireContext(), "Exo");

    exoPlayer = new ExoPlayer.Builder(requireContext()).build();
    playerView.setPlayer(exoPlayer);

    //2. Thêm WiAdsLoaderListener
    InStreamManager.Companion.getInstance().setLoaderListener(new InStreamManager.WiAdsLoaderListener() {
      @Override
      public void onEvent(@NonNull AdInStreamEvent event) {
        Log.d(TAG, "==========event " + event.getEventType() + " - " + event.getCampaignId() + ")");
        if (event.getEventType() == AdInStreamEvent.EventType.ERROR) {
          Log.d(TAG, "===========Xu ly error");
        }
      }

      @Override
      public void showSkipButton(@NonNull String campaignId, int duration) {
        if (skipButton != null) {
          skipButton.startCountdown(duration, new SkipAdsButtonAds.WiSkipButtonListener() {
            @Override
            public void onRequestFocus() {
              Log.d(TAG, "=====on request forcus");
              if (skipButton != null) skipButton.requestFocusToSkip();
            }
          });
        }
      }

      @Override
      public void hideSkipButton(@NonNull String campaignId) {
        if (skipButton != null) skipButton.hide();
      }

      @Override
      public void pauseSkipButton() {
        if (skipButton != null) skipButton.pause();
      }

      @Override
      public void resumeSkipButton() {
        if (skipButton != null) skipButton.resume();
      }

      @Override
      public void showReportButton(@NonNull String campaignId) {
        isInStreamAdPlaying = true;
        if (reportButton != null) reportButton.show(getActivity());
      }

      @Override
      public void hideReportButton(@NonNull String campaignId) {
        isInStreamAdPlaying = false;
        if (reportButton != null) reportButton.hide();
      }

      @Override
      public void showInfoButton(@NonNull String campaignId) {
      }

      @Override
      public void hideInfoButton(@NonNull String campaignId) {
      }

      @Override
      public void onError() {
        Log.d(TAG, "==========onError");
        InStreamManager.Companion.getInstance().release();
      }
    });

    //3. Khởi tạo AdsRequestData
    AdsRequestData adsRequestData = new AdsRequestData.Builder()
        .channelId("998989,222222") // danh sách id của category của nội dung & cách nhau bằng dấu ,
        .streamId("7600") // id nội dung
        .transId("222222") // Transaction cua TV360
        .contentType(ContentType.FILM) // content type TV | FILM | VIDEO
        .title("Tieu de cua noi dung") // tiêu đề nội dung
        .category("category 1, category 2") // danh sach tiêu đề category của nội dung & cách nhau bằng dấu ,
        .keyword("keyword 1, keyword 2") // từ khoá nếu có | để "" nếu ko có
        .userId("123123123") // unified id, nếu không có thì set ""
        .userImpressionLimit(5) // giới hạn số lần hiển thị cho user, 0 nếu không giới hạn
        .segments("123,1,23") //segment id của user phân tách nhau bời, dữ liệu này lấy từ backend đối tác
        .adPendingTime(20)
        .build();

    String contentUrl = "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8";

    //4. khai bao friendly obstruction --- quan trong => can phai cai khao het cac lop phu len tren player
    List<FriendlyObstruction> friendlyObstructionList = Lists.newArrayList();
    FriendlyObstruction skipButtonObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
        skipButton, FriendlyObstructionPurpose.CLOSE_AD, "This is close ad");
    friendlyObstructionList.add(skipButtonObstruction);

    if (reportButton != null) {
      FriendlyObstruction reportButtonObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
          reportButton, FriendlyObstructionPurpose.CLOSE_AD, "This is close ad");
      friendlyObstructionList.add(reportButtonObstruction);
    }

    if (playerView != null) {
      playerView.addFriendlyObstructionList(friendlyObstructionList);
    }

    DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
    httpDataSourceFactory.setUserAgent(userAgent);
    httpDataSourceFactory.setTransferListener(new DefaultBandwidthMeter.Builder(requireContext())
        .setResetOnNetworkTypeChange(false).build());

    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(requireContext());
    MediaSource mediaSource = buildMediaSource(dataSourceFactory, contentUrl);

    DefaultMediaSourceFactory defaultMediaSourceFactory = new DefaultMediaSourceFactory(requireContext());

    AdsMediaSource adsMediaSource = InStreamManager.Companion.getInstance().requestAds(
        adsRequestData,
        mediaSource,
        playerView,
        exoPlayer,
        defaultMediaSourceFactory,
        reportButton, // nút báo cáo quảng cáo
        null); // infoButton: không dùng thẻ đánh dấu quảng cáo

    exoPlayer.addMediaSource(adsMediaSource);
    exoPlayer.prepare();
    exoPlayer.setPlayWhenReady(true);
  }

  private DrmSessionManager getDrmSessionManager(DefaultHttpDataSource.Factory dataSourceFactory) {
    String licenseUrl = "https://your-license-server.com";
    HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, dataSourceFactory);
    return new DefaultDrmSessionManager.Builder()
        .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
        .build(drmCallback);
  }

  private MediaSource buildMediaSource(DataSource.Factory dataSourceFactory, String url) {
    Uri uri = Uri.parse(url);
    switch (Util.inferContentType(uri)) {
      case C.TYPE_DASH:
        return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory)
            .setManifestParser(new DashManifestParser() {
              @NonNull
              @Override
              public DashManifest parse(@NonNull Uri uri, @NonNull InputStream inputStream) throws IOException {
                return super.parse(uri, inputStream);
              }
            }).createMediaSource(MediaItem.fromUri(uri));
      case C.TYPE_HLS:
        return new HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(MediaItem.fromUri(uri));
      case C.TYPE_SS:
        return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
      case C.TYPE_OTHER:
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
      default:
        throw new IllegalStateException("Unexpected value: " + Util.inferContentType(uri));
    }
  }

  protected static CacheDataSource.Factory buildReadOnlyCacheDataSource(DataSource.Factory upstreamFactory, Cache cache) {
    return new CacheDataSource.Factory().setCache(cache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setCacheReadDataSourceFactory(new FileDataSource.Factory())
        .setCacheWriteDataSinkFactory(null)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        .setEventListener(null);
  }
}
