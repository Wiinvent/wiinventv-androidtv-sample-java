package com.example.sampleandroidtv.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.ui.TV360InfoAdsButton;
import com.example.sampleandroidtv.ui.TV360ReportAdsButton;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.Util;

import tv.wiinvent.androidtv.OverlayBannerManager;
import tv.wiinvent.androidtv.interfaces.banner.BannerAdEventListener;
import tv.wiinvent.androidtv.models.ads.DisplayBannerAdsRequestData;
import tv.wiinvent.androidtv.models.type.BannerDisplayAdSize;
import tv.wiinvent.androidtv.models.type.BannerDisplayType;
import tv.wiinvent.androidtv.models.type.Environment;
import tv.wiinvent.androidtv.report.InfoButtonAds;
import tv.wiinvent.androidtv.report.ReportButtonAds;
import tv.wiinvent.androidtv.ui.banner.BannerAdView;

public class OverlayBannerFragment extends Fragment {
    private static final String TAG = "DisplayBannerFragment";

    private final String channelIdDefault = "998989";
    private final String streamIdDefault = "999999";

    private PlayerControlView playerControlView = null;
    private PlayerView playerView = null;
    private ExoPlayer player = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overlay_banner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerView = getActivity() != null ? getActivity().findViewById(R.id.player_view) : null;
        playerControlView = getActivity() != null ? getActivity().findViewById(R.id.playerControlView) : null;
        Button overlayBannerButton = getActivity().findViewById(R.id.overlay_banner);
        overlayBannerButton.setOnClickListener(v -> loadPlayer());

        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            initOverlayBannerManager();
            initializePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OverlayBannerManager.Companion.getInstance().release();
    }

    public void initOverlayBannerManager() {
        Activity activity = getActivity();
        if (activity == null) return;
        OverlayBannerManager.Companion.getInstance().init(activity, "14", Environment.SANDBOX, 10, true);
        OverlayBannerManager.Companion.getInstance().addBannerListener(new BannerAdEventListener() {
            @Override
            public void onDisplayAds(String positionId, BannerAdView adView, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                Log.d(TAG, "=========OverlayBannerManager onDisplayAds " + positionId);
                activity.runOnUiThread(() -> {
                    if (adView != null) adView.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onNoAds(String positionId, BannerAdView adView) {
                Log.d(TAG, "=========OverlayBannerManager khong co ads de show " + positionId);
            }

            @Override
            public void onAdsBannerDismiss(String positionId, BannerAdView adView, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                Log.d(TAG, "=========OverlayBannerManager onAdsBannerDismiss " + positionId);
                activity.runOnUiThread(() -> {
                    if (adView != null) {
                        adView.setVisibility(View.GONE);
                        OverlayBannerManager.Companion.getInstance().releaseBanner(adView, reportButton, infoButton);
                    }
                });
            }

            @Override
            public void onAdsBannerError(String positionId, BannerAdView adView, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                Log.d(TAG, "=========OverlayBannerManager onAdsWelcomeError " + positionId);
                activity.runOnUiThread(() -> {
                    if (adView != null) {
                        adView.setVisibility(View.GONE);
                        OverlayBannerManager.Companion.getInstance().releaseBanner(adView, reportButton, infoButton);
                    }
                });
            }

            @Override
            public void onAdsBannerClick(String positionId, String clickThroughLink) {
                Log.d(TAG, "=========OverlayBannerManager onAdsBannerClick " + positionId + " " + clickThroughLink);
            }

            @Override
            public void onShowReportButton(String positionId, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                if (reportButton != null) reportButton.show(getActivity());
                if (infoButton != null) infoButton.show(getActivity());
            }

            @Override
            public void onHideReportButton(String positionId, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                if (reportButton != null) reportButton.hide();
                if (infoButton != null) infoButton.hide();
            }
        });
    }

    public void showOverlayBanner() {
        showDisplayBanner(BannerDisplayAdSize.PAUSE_BANNER, BannerDisplayType.OVERLAY, R.id.banner_ad_overlay_view,
                R.id.overlay_banner_report_button, R.id.overlay_banner_info_button, "");
    }

    public void dismissOverlayBanner() {
        Activity activity = getActivity();
        if (activity == null) return;
        View v = activity.findViewById(R.id.banner_ad_overlay_view);
        BannerAdView bannerView = v instanceof BannerAdView ? (BannerAdView) v : null;
        TV360ReportAdsButton reportButton = activity.findViewById(R.id.overlay_banner_report_button);
        TV360InfoAdsButton infoButton = activity.findViewById(R.id.overlay_banner_info_button);
        OverlayBannerManager.Companion.getInstance().releaseBanner(bannerView, reportButton, infoButton);
    }

    public void showDisplayBanner(BannerDisplayAdSize adSize, BannerDisplayType displayType, int viewId, int reportButtonViewId, int infoButtonViewId, String positionId) {
        Activity activity = getActivity();
        if (activity == null) return;
        DisplayBannerAdsRequestData bannerAdsRequestData =
                new DisplayBannerAdsRequestData.Builder()
                        .adSize(adSize)
                        .bannerDisplayType(displayType)
                        .channelId(channelIdDefault.trim().isEmpty() ? "998989" : channelIdDefault)
                        .streamId(streamIdDefault.trim().isEmpty() ? "999999" : streamIdDefault)
                        .title("Day la title")
                        .category("category 1, category 2")
                        .transId("1112222222")
                        .userId("123123123")
                        .userImpressionLimit(5)
                        .color("#ffffff00")
                        .segments("a3,34,d3,d3")
                        .positionId(positionId)
                        .adPendingTime(20)
                        .build();

        View v = activity.findViewById(viewId);
        if (!(v instanceof BannerAdView)) return;
        BannerAdView bannerAdView = (BannerAdView) v;
        TV360ReportAdsButton reportButton = activity.findViewById(reportButtonViewId);
        TV360InfoAdsButton infoButton = activity.findViewById(infoButtonViewId);
        OverlayBannerManager.Companion.getInstance().requestAds(activity, bannerAdView, bannerAdsRequestData, 30L, reportButton, infoButton);
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(requireContext()).build();
        if (playerView != null) {
            playerView.setPlayer(player);
            playerView.setUseController(false);
        }
        if (playerControlView != null) playerControlView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY && !player.getPlayWhenReady()) {
                    Log.e("tamlog", "ExoPlayer is paused");
                    showOverlayBanner();
                } else if (playbackState == Player.STATE_READY && player.getPlayWhenReady()) {
                    dismissOverlayBanner();
                }
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                if (!playWhenReady && player.getPlaybackState() == Player.STATE_READY) {
                    Log.d("tamlog", "ExoPlayer is paused");
                    showOverlayBanner();
                } else if (player.getPlaybackState() == Player.STATE_READY && playWhenReady) {
                    dismissOverlayBanner();
                }
            }
        });
    }

    private void loadPlayer() {
        String contentUrl = "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8";
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(requireContext());
        MediaSource mediaSource = buildMediaSource(dataSourceFactory, contentUrl);
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(DataSource.Factory dataSourceFactory, String url) {
        Uri uri = Uri.parse(url);
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).setAllowChunklessPreparation(true).createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_SS:
                return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
            default:
                throw new IllegalStateException("Unsupported type :: " + type);
        }
    }
}
