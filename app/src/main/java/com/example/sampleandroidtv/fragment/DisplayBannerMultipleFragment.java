package com.example.sampleandroidtv.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.VerticalGridView;

import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.ui.DisplayBannerAdapter;

import java.util.ArrayList;

import tv.wiinvent.androidtv.DisplayBannerManager;
import tv.wiinvent.androidtv.interfaces.banner.BannerAdEventListener;
import tv.wiinvent.androidtv.models.type.BannerDisplayAdSize;
import tv.wiinvent.androidtv.models.type.Environment;
import tv.wiinvent.androidtv.report.InfoButtonAds;
import tv.wiinvent.androidtv.report.ReportButtonAds;
import tv.wiinvent.androidtv.ui.banner.BannerAdView;

public class DisplayBannerMultipleFragment extends Fragment {
    private static final String TAG = "DisplayMultipleFragment";

    private final String channelIdDefault = "998989";
    private final String streamIdDefault = "999999";
    private final ArrayList<Pair<String, BannerDisplayAdSize>> bannerPositionData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multiple_display_banner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);

        initBannerList();
        DisplayBannerAdapter displayBannerAdapter = new DisplayBannerAdapter(getActivity(), streamIdDefault, channelIdDefault);
        displayBannerAdapter.setBannerParams(bannerPositionData);
        VerticalGridView rvBanner = getActivity().findViewById(R.id.rvBanner);
        rvBanner.setAdapter(displayBannerAdapter);
    }

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            initDisplayBannerManager();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DisplayBannerManager.Companion.getInstance().release();
    }

    private void initBannerList() {
        bannerPositionData.add(new Pair<>("HOME_0", BannerDisplayAdSize.HOMEPAGE_BANNER));
        bannerPositionData.add(new Pair<>("subpage1", BannerDisplayAdSize.SUBPAGE_BANNER));
        bannerPositionData.add(new Pair<>("HOME_1", BannerDisplayAdSize.HOMEPAGE_BANNER));
        bannerPositionData.add(new Pair<>("subpage2", BannerDisplayAdSize.SUBPAGE_BANNER));
        bannerPositionData.add(new Pair<>("HOME_2", BannerDisplayAdSize.HOMEPAGE_BANNER));
        bannerPositionData.add(new Pair<>("subpage3", BannerDisplayAdSize.SUBPAGE_BANNER));
    }

    private void initDisplayBannerManager() {
        Activity activity = getActivity();
        if (activity == null) return;
        DisplayBannerManager.Companion.getInstance().init(activity, "14", Environment.SANDBOX, 10, true);
        DisplayBannerManager.Companion.getInstance().addBannerListener(new BannerAdEventListener() {
            @Override
            public void onDisplayAds(String positionId, BannerAdView adView, ReportButtonAds reportButton, InfoButtonAds infoButton) {
                Log.d(TAG, "=========DisplayBannerManager onDisplayAds  " + positionId);
                activity.runOnUiThread(() -> {
                    if (adView != null) adView.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onNoAds(String positionId, BannerAdView adView) {
                Log.d(TAG, "=========DisplayBannerManager khong co ads de show  " + positionId);
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
                Log.d(TAG, "=========DisplayBannerManager multiple onAdsWelcomeError  " + positionId);
                activity.runOnUiThread(() -> {
                    if (adView != null) {
                        adView.setVisibility(View.GONE);
                        DisplayBannerManager.Companion.getInstance().releaseBanner(adView);
                    }
                });
            }

            @Override
            public void onAdsBannerClick(String positionId, String clickThroughLink) {
                Log.d(TAG, "=========DisplayBannerManager onAdsBannerClick  " + positionId + " " + clickThroughLink);
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
}
