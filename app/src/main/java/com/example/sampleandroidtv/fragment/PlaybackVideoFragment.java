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
import androidx.leanback.app.VideoSupportFragment;
import androidx.leanback.app.VideoSupportFragmentGlueHost;
import androidx.leanback.media.MediaPlayerAdapter;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.PlaybackControlsRow;

import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.pojo.Movie;
import com.example.sampleandroidtv.activity.DetailsActivity;
import com.example.sampleandroidtv.ui.TV360SkipAdsButtonAds;
import com.google.ads.interactivemedia.v3.api.FriendlyObstruction;
import com.google.ads.interactivemedia.v3.api.FriendlyObstructionPurpose;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tv.wiinvent.androidtv.InStreamManager;
import tv.wiinvent.androidtv.OverlayManager;
import tv.wiinvent.androidtv.interfaces.PlayerChangeListener;
import tv.wiinvent.androidtv.models.OverlayData;
import tv.wiinvent.androidtv.models.ads.AdInStreamEvent;
import tv.wiinvent.androidtv.models.ads.AdsRequestData;
import tv.wiinvent.androidtv.ui.OverlayView;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends Fragment {
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

    DataSource.Factory factory = new DefaultDataSource.Factory(requireActivity());
    ImaAdsLoader adsLoader = InStreamManager.Companion.getInstance().getLoader(friendlyObstructionList);

    MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(factory)
        .setAdsLoaderProvider(unusedAdTagUri -> adsLoader)
        .setAdViewProvider(playerView);

    exoPlayer = new ExoPlayer.Builder(requireContext()).setMediaSourceFactory(mediaSourceFactory).build();
    playerView.setPlayer(exoPlayer);
    Objects.requireNonNull(adsLoader).setPlayer(exoPlayer);

    String contentUrl = "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8";
    AdsRequestData adsRequestData = new AdsRequestData.Builder()
        .channelId(SAMPLE_CHANNEL_ID)
        .streamId(SAMPLE_STREAM_ID)
        .build();

    MediaItem mediaItem =
        new MediaItem.Builder()
            .setUri(Uri.parse(contentUrl))
            .setAdsConfiguration(InStreamManager.Companion.getInstance().requestAds(adsRequestData))
            .build();

    exoPlayer.setMediaItem(mediaItem);
    exoPlayer.prepare();

    exoPlayer.setPlayWhenReady(true);

    InStreamManager.Companion.getInstance().setLoaderListener(new InStreamManager.WiAdsLoaderListener() {
      @Override
      public void onEvent(@NonNull AdInStreamEvent adInStreamEvent) {
        Log.d(TAG, "==========event ${event.eventType} - ${event.campaignId} - ${player?.duration})");

      }

      @Override
      public void showSkipButton(@NonNull String campaignId, int duration) {
        if(skipButton != null)
          skipButton.startCountdown(duration);
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
  }
}