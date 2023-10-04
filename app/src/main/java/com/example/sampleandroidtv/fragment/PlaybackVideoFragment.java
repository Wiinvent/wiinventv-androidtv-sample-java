package com.example.sampleandroidtv.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.activity.DetailsActivity;
import com.example.sampleandroidtv.pojo.Movie;
import com.example.sampleandroidtv.ui.TV360SkipAdsButtonAds;
import com.example.sampleandroidtv.util.VideoCache;
import com.google.ads.interactivemedia.v3.api.FriendlyObstruction;
import com.google.ads.interactivemedia.v3.api.FriendlyObstructionPurpose;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
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
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
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
import tv.wiinvent.androidtv.OverlayManager;
import tv.wiinvent.androidtv.models.OverlayData;
import tv.wiinvent.androidtv.models.ads.AdInStreamEvent;
import tv.wiinvent.androidtv.models.ads.AdsRequestData;
import tv.wiinvent.androidtv.ui.OverlayView;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends Fragment {
  private static final String DRM_LICENSE_URL = "https://license.uat.widevine.com/cenc/getcontentkey/widevine_test";

  private static final String TAG = "PlaybackVideoFragment";
  private StyledPlayerView playerView;
  private ExoPlayer exoPlayer;
  public static final String SAMPLE_ACCOUNT_ID = "14";
  public static final String SAMPLE_CHANNEL_ID = "11683";
  public static final String SAMPLE_STREAM_ID = "999999";
  public static final String SAMPLE_TOKEN = "3001";

  private OverlayManager overlayManager;

  private OverlayView overlayView;
  private TV360SkipAdsButtonAds skipButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Movie movie =
        (Movie) requireActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);
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
    overlayView = requireActivity().findViewById(R.id.wisdk_overlay_view);
    init(savedInstanceState);
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  protected void init(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      initializePlayer();
      initializeOverlays();
    }
  }

  private void initializeOverlays() {
//        OverlayData overlayData = null;
    OverlayData overlayData = new OverlayData.Builder()
        .accountId(SAMPLE_ACCOUNT_ID)
        .channelId(SAMPLE_CHANNEL_ID)
        .streamId(SAMPLE_STREAM_ID)
        .thirdPartyToken(SAMPLE_TOKEN)
        .env(OverlayData.Environment.SANDBOX)
        .deviceType(OverlayData.DeviceType.TV)
        .contentType(OverlayData.ContentType.LIVESTREAM)
        .build();

    overlayManager = new OverlayManager(requireActivity(), R.id.wisdk_overlay_view, overlayData);

    overlayManager.addPlayerListener(() -> exoPlayer != null ? exoPlayer.getCurrentPosition() : 0L);

    // Add player event listeners to determine overlay visibility.
    exoPlayer.addListener(new Player.EventListener() {
      @Override
      public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d(TAG, "====onPlayerStateChanged playWhenReady: $playWhenReady - $playbackState");

        if (overlayManager != null)
          overlayManager.setVisible(playWhenReady && playbackState == Player.STATE_READY);
      }
    });
  }

  private void initializePlayer() {
    String userAgent = Util.getUserAgent(requireContext(), "Exo");

    exoPlayer = new ExoPlayer.Builder(requireContext()).build();
    playerView.setPlayer(exoPlayer);

    String contentUrl = "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8";

    InStreamManager.Companion.getInstance().setLoaderListener(new InStreamManager.WiAdsLoaderListener() {
      @Override
      public void onEvent(@NonNull AdInStreamEvent adInStreamEvent) {
        Log.d(TAG, "==========event ${event.eventType} - ${event.campaignId} - ${player?.duration})");

      }

      @Override
      public void showSkipButton(@NonNull String campaignId, int duration) {
        if(skipButton != null)
          skipButton.startCountdown(duration, true); //neu khong muon tu dong focus thi set = true
      }

      @Override
      public void hideSkipButton(@NonNull String campaignId) {
        if(skipButton != null)
          skipButton.hide();
      }

      @Override
      public void onTimeout() {
        InStreamManager.Companion.getInstance().release();
      }
    });

    AdsRequestData adsRequestData = new AdsRequestData.Builder()
        .channelId(SAMPLE_CHANNEL_ID)
        .streamId(SAMPLE_STREAM_ID)
        .build();

    //khai bao friendly obstruction --- quan trong => can phai cai khao het cac lop phu len tren player
    List<FriendlyObstruction> friendlyObstructionList = Lists.newArrayList();
    FriendlyObstruction skipButtonObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
        skipButton,
        FriendlyObstructionPurpose.CLOSE_AD,
        "This is close ad"
    );
    friendlyObstructionList.add(skipButtonObstruction);

    FriendlyObstruction overlaysObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
        overlayView,
        FriendlyObstructionPurpose.OTHER,
        "This is transparent overlays"
    );
    friendlyObstructionList.add(overlaysObstruction);

    DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
    httpDataSourceFactory.setUserAgent(userAgent);
    httpDataSourceFactory.setTransferListener(new DefaultBandwidthMeter.Builder(requireContext())
        .setResetOnNetworkTypeChange(false).build());

    MediaSource mediaSource = buildMediaSource(buildDataSourceFactory(httpDataSourceFactory), contentUrl, getDrmSessionManager(httpDataSourceFactory));

    DefaultMediaSourceFactory defaultMediaSourceFactory = new DefaultMediaSourceFactory(requireContext());

    AdsMediaSource adsMediaSource = InStreamManager.Companion.getInstance()
            .requestAds(adsRequestData,
                mediaSource,
                playerView,
                exoPlayer,
                defaultMediaSourceFactory,
                friendlyObstructionList);

    exoPlayer.addMediaSource(adsMediaSource);
    exoPlayer.prepare();

    exoPlayer.setPlayWhenReady(true);
  }

  private DrmSessionManager getDrmSessionManager(DefaultHttpDataSource.Factory dataSourceFactory) {
    try {
      HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(DRM_LICENSE_URL, dataSourceFactory);
      return new DefaultDrmSessionManager(C.WIDEVINE_UUID, FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID), drmCallback, null, true, 10);
    } catch (Exception e) {
      return null;
    }
  }

  public DataSource.Factory buildDataSourceFactory(DefaultHttpDataSource.Factory httpDataSourceFactory) {
    return buildReadOnlyCacheDataSource(httpDataSourceFactory, VideoCache.getInstance(requireContext()).getCache());
  }
  protected static CacheDataSource.Factory buildReadOnlyCacheDataSource(
      DataSource.Factory upstreamFactory, Cache cache) {
    return new CacheDataSource.Factory().setCache(cache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setCacheReadDataSourceFactory(new FileDataSource.Factory())
        .setCacheWriteDataSinkFactory(null)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        .setEventListener(null);
  }



  private MediaSource buildMediaSource(DataSource.Factory dataSourceFactory, String url, DrmSessionManager drmSessionManager) {
    Uri uri = Uri.parse(url);
    switch (Util.inferContentType(uri)) {
      case C.TYPE_DASH:
        return new DashMediaSource
            .Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory)
            .setDrmSessionManagerProvider(mediaItem -> drmSessionManager)
            .setManifestParser(new DashManifestParser() {
              @NonNull
              @Override
              public DashManifest parse(@NonNull Uri uri, @NonNull InputStream inputStream) throws IOException {
                return super.parse(uri, inputStream);
              }
            }).createMediaSource(MediaItem.fromUri(uri));
      case C.TYPE_HLS:
        return new HlsMediaSource
            .Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(MediaItem.fromUri(uri));
      case C.TYPE_SS:
        return new SsMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri));
      case C.TYPE_OTHER:
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri));
      default:
        throw new IllegalStateException("Unexpected value: " + Util.inferContentType(uri));
    }
  }

}