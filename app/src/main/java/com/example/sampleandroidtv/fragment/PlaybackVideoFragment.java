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
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tv.wiinvent.androidtv.InStreamManager;
import tv.wiinvent.androidtv.OverlayManager;
import tv.wiinvent.androidtv.models.ads.AdInStreamEvent;
import tv.wiinvent.androidtv.models.ads.AdsRequestData;

/**
 * Handles video playback with media controls.
 */
public class PlaybackVideoFragment extends Fragment {
  private static final String TAG = "PlaybackVideoFragment";
  private StyledPlayerView playerView;
  private ExoPlayer exoPlayer;
  private OverlayManager overlayManager;
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
    init(savedInstanceState);
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  protected void init(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      initializePlayer();
//      initializeOverlays();
    }
  }

  private void initializePlayer() {
    //khai bao friendly obstruction
    List<FriendlyObstruction> friendlyObstructionList = new ArrayList<>();
    FriendlyObstruction skipButtonObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
      skipButton,
        FriendlyObstructionPurpose.CLOSE_AD,
        "This is close ad"
    );

    friendlyObstructionList.add(skipButtonObstruction);

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
        .channelId("998989")
        .streamId("999999")
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
      public void showSkipButton(int duration) {
        if(skipButton != null)
          skipButton.startCountdown(duration);
      }

      @Override
      public void hideSkipButton() {
        if(skipButton != null)
          skipButton.hide();
      }

      @Override
      public void onTimeout() {

      }
    });
  }
}