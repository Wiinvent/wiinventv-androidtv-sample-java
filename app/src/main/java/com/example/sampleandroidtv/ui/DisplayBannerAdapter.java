package com.example.sampleandroidtv.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import tv.wiinvent.androidtv.DisplayBannerManager;
import tv.wiinvent.androidtv.models.ads.DisplayBannerAdsRequestData;
import tv.wiinvent.androidtv.models.type.BannerDisplayAdSize;
import tv.wiinvent.androidtv.models.type.BannerDisplayType;
import tv.wiinvent.androidtv.ui.banner.BannerAdView;

public class DisplayBannerAdapter extends RecyclerView.Adapter<DisplayBannerAdapter.DisplayBannerViewHolder> {

    private final Activity activity;
    private final String streamIdDefault;
    private final String channelIdDefault;

    private List<Pair<String, BannerDisplayAdSize>> bannerParams = new ArrayList<>();

    public DisplayBannerAdapter(Activity activity, String streamIdDefault, String channelIdDefault) {
        this.activity = activity;
        this.streamIdDefault = streamIdDefault;
        this.channelIdDefault = channelIdDefault;
    }

    public void setBannerParams(List<Pair<String, BannerDisplayAdSize>> bannerParams) {
        this.bannerParams = bannerParams;
    }

    @Override
    public DisplayBannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DisplayBannerViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.display_banner_item, parent, false));
    }

    @Override
    public int getItemCount() {
        return bannerParams.size();
    }

    @Override
    public void onBindViewHolder(DisplayBannerViewHolder holder, int position) {
        holder.bind(bannerParams.get(position));
    }

    class DisplayBannerViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout ctlBanner;
        private final TextView tvTitle;
        private final BannerAdView bannerAdView;
        private final TV360ReportAdsButton reportButton;
        private final TV360InfoAdsButton infoAdsBtn;

        DisplayBannerViewHolder(View itemView) {
            super(itemView);

            ctlBanner = itemView.findViewById(R.id.ctlBanner);
            tvTitle = itemView.findViewById(R.id.tvTitle);

            bannerAdView = new BannerAdView(itemView.getContext());
            bannerAdView.setId(View.generateViewId());
            // Use foreground instead of background so the border is drawn ON TOP of the ad content
            bannerAdView.setForeground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.bg_banner_display_ad_view));

            LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
            layoutParams.topToBottom = tvTitle.getId();
            layoutParams.startToStart = LayoutParams.PARENT_ID;
            layoutParams.endToEnd = LayoutParams.PARENT_ID;
            layoutParams.topMargin = 20;

            // Allow the banner to follow the focus state of the parent item (ctlBanner)
            bannerAdView.setDuplicateParentStateEnabled(true);
            bannerAdView.setFocusable(false); // Let ctlBanner handle the actual focus

            bannerAdView.setLayoutParams(layoutParams);
            ctlBanner.addView(bannerAdView);

            reportButton = new TV360ReportAdsButton(itemView.getContext());
            reportButton.setId(View.generateViewId());
            // Configure D-pad navigation: Right from ctlBanner goes to reportButton
            ctlBanner.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            ctlBanner.setNextFocusRightId(reportButton.getId());
            reportButton.setNextFocusLeftId(ctlBanner.getId());

            LayoutParams layoutParamsReportButton = new LayoutParams(0, 0);
            layoutParamsReportButton.topToTop = bannerAdView.getId();
            layoutParamsReportButton.bottomToBottom = bannerAdView.getId();
            layoutParamsReportButton.endToEnd = bannerAdView.getId();
            layoutParamsReportButton.matchConstraintPercentHeight = 0.2f;
            layoutParamsReportButton.verticalBias = 0f;
            layoutParamsReportButton.dimensionRatio = "h,1:1";
            layoutParamsReportButton.topMargin = 20;
            layoutParamsReportButton.setMarginEnd(20);
            reportButton.setLayoutParams(layoutParamsReportButton);
            reportButton.setVisibility(View.GONE);
            ctlBanner.addView(reportButton);

            infoAdsBtn = new TV360InfoAdsButton(itemView.getContext());
            infoAdsBtn.setId(View.generateViewId());
            LayoutParams layoutParamsInfoAdsButton = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParamsInfoAdsButton.topToTop = bannerAdView.getId();
            layoutParamsInfoAdsButton.startToStart = bannerAdView.getId();
            infoAdsBtn.setTextSize(18f);
            infoAdsBtn.setLayoutParams(layoutParamsInfoAdsButton);
            infoAdsBtn.setVisibility(View.GONE);
            ctlBanner.addView(infoAdsBtn);
        }

        void bind(Pair<String, BannerDisplayAdSize> params) {
            tvTitle.setText("Banner " + params.first);
            showDisplayBanner(params.second, BannerDisplayType.DISPLAY, bannerAdView.getId(), params.first);
        }

        void showDisplayBanner(BannerDisplayAdSize adSize, BannerDisplayType displayType, int viewId, String positionId) {
            if (activity == null) return;
            DisplayBannerAdsRequestData bannerAdsRequestData =
                    new DisplayBannerAdsRequestData.Builder()
                            .adSize(adSize)
                            .bannerDisplayType(displayType)
                            .channelId(channelIdDefault.trim().isEmpty() ? Constants.CHANNEL_ID_DEFAULT : channelIdDefault)
                            .streamId(streamIdDefault.trim().isEmpty() ? Constants.STREAM_ID_DEFAULT : streamIdDefault)
                            .title(Constants.TITLE_DEFAULT)
                            .category(Constants.CATEGORY_ID_DEFAULT)
                            .transId(Constants.TRANS_ID_DEFAULT)
                            .userId(Constants.USER_ID_DEFAULT)
                            .userImpressionLimit(Constants.USER_IMPRESSION_LIMIT_DEFAULT)
                            .color(Constants.COLOR_BANNER_DEFAULT)
                            .segments(Constants.SEGMENT_DEFAULT)
                            .positionId(positionId)
                            .adPendingTime(20)
                            .build();

            DisplayBannerManager.Companion.getInstance().requestAds(
                    activity, bannerAdView, reportButton, infoAdsBtn, bannerAdsRequestData);
        }
    }
}
