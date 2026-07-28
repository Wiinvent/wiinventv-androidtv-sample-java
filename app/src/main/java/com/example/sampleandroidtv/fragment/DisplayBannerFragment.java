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
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.Util;

import tv.wiinvent.androidtv.DisplayBannerManager;
import tv.wiinvent.androidtv.interfaces.banner.BannerAdEventListener;
import tv.wiinvent.androidtv.models.ads.DisplayBannerAdsRequestData;
import tv.wiinvent.androidtv.models.type.BannerDisplayAdSize;
import tv.wiinvent.androidtv.models.type.BannerDisplayType;
import tv.wiinvent.androidtv.models.type.Environment;
import tv.wiinvent.androidtv.report.InfoButtonAds;
import tv.wiinvent.androidtv.report.ReportButtonAds;
import tv.wiinvent.androidtv.ui.banner.BannerAdView;

public class DisplayBannerFragment extends Fragment {
    private static final String TAG = "DisplayBannerFragment";

    private final String streamIdDefault;
    private final String channelIdDefault;
    private final String positionIdDefault;
    private final BannerDisplayAdSize adSize;

    private PlayerView playerView = null;
    private ExoPlayer player = null;

    public DisplayBannerFragment(String streamIdDefault, String channelIdDefault, String positionIdDefault, BannerDisplayAdSize adSize) {
        this.streamIdDefault = streamIdDefault;
        this.channelIdDefault = channelIdDefault;
        this.positionIdDefault = positionIdDefault;
        this.adSize = adSize;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display_banner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerView = getActivity() != null ? getActivity().findViewById(R.id.player_view) : null;
        Button displayBannerButton = getActivity().findViewById(R.id.display_banner);
        displayBannerButton.setOnClickListener(v -> showDisplayBanner());
        Button overlayBannerButton = getActivity().findViewById(R.id.overlay_banner);
        overlayBannerButton.setOnClickListener(v -> loadPlayer());

        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            initDisplayBannerManager();
            initializePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DisplayBannerManager.Companion.getInstance().release();
    }

    public void initDisplayBannerManager() {
        Activity activity = getActivity();
        if (activity == null) return;
        DisplayBannerManager.Companion.getInstance().init(activity, "14", Environment.SANDBOX, 10, true);
        DisplayBannerManager.Companion.getInstance().addBannerListener(new BannerAdEventListener() {
            @Override
            public void onDisplayAds(String positionId, BannerAdView adView, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                Log.d(TAG, "=========DisplayBannerManager onDisplayAds " + positionId);
                activity.runOnUiThread(() -> {
                    if (adView != null) adView.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onNoAds(String positionId, BannerAdView adView) {
                Log.d(TAG, "=========DisplayBannerManager khong co ads de show :  " + positionId);
            }

            @Override
            public void onAdsBannerDismiss(String positionId, BannerAdView adView, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                Log.d(TAG, "=========DisplayBannerManager onAdsBannerDismiss  " + positionId);
                activity.runOnUiThread(() -> {
                    if (adView != null) {
                        adView.setVisibility(View.GONE);
                        DisplayBannerManager.Companion.getInstance().releaseBanner(adView);
                    }
                });
            }

            @Override
            public void onAdsBannerError(String positionId, BannerAdView adView, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                Log.d(TAG, "=========DisplayBannerManager onAdsWelcomeError  " + positionId);
                activity.runOnUiThread(() -> {
                    if (adView != null) {
                        adView.setVisibility(View.GONE);
                        DisplayBannerManager.Companion.getInstance().releaseBanner(adView);
                    }
                });
            }

            @Override
            public void onAdsBannerClick(String positionId, String clickThroughLink) {
                Log.d(TAG, "=========DisplayBannerManager onAdsBannerClick " + positionId + " " + clickThroughLink);
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

    public void showDisplayBanner() {
        showDisplayBanner(adSize, BannerDisplayType.DISPLAY, R.id.banner_ad_display_view,
                R.id.display_banner_report_button, R.id.display_banner_info_button,
                positionIdDefault.trim().isEmpty() ? "homepage1" : positionIdDefault);
    }

    public void showOverlayBanner() {
        showDisplayBanner(BannerDisplayAdSize.PAUSE_BANNER, BannerDisplayType.OVERLAY, R.id.banner_ad_overlay_view,
                R.id.overlay_banner_report_button, R.id.overlay_banner_info_button, "");
    }

    public void dismissOverlayBanner() {
        BannerAdView bannerView = getActivity().findViewById(R.id.banner_ad_overlay_view);
        DisplayBannerManager.Companion.getInstance().releaseBanner(bannerView);
    }

    public void showDisplayBanner(BannerDisplayAdSize adSize, BannerDisplayType displayType, int viewId, int reportViewId, int infoViewId, String positionId) {
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
        TV360ReportAdsButton reportButton = activity.findViewById(reportViewId);
        TV360InfoAdsButton infoButton = activity.findViewById(infoViewId);
        DisplayBannerManager.Companion.getInstance().requestAds(activity, bannerAdView, reportButton, infoButton, bannerAdsRequestData);
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(requireContext()).build();
        if (playerView != null) {
            playerView.setPlayer(player);
            playerView.setUseController(true);
        }
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
