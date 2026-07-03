package com.example.sampleandroidtv.ui;


import android.app.Activity;
import android.os.Build;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sampleandroidtv.R;

import java.util.ArrayList;
import java.util.List;

import tv.wiinvent.androidtv.DisplayBannerManager;
import tv.wiinvent.androidtv.models.ads.DisplayBannerAdsRequestData;
import tv.wiinvent.androidtv.models.type.BannerDisplayAdSize;
import tv.wiinvent.androidtv.models.type.BannerDisplayType;
import tv.wiinvent.androidtv.ui.banner.BannerAdView;

public class DisplayBannerAdapter extends RecyclerView.Adapter<DisplayBannerAdapter.DisplayBannerViewHolder> {
    ArrayList<Pair<String, BannerDisplayAdSize>> bannerParams = new ArrayList();
    private String channelIdDefault = "998989";
    private String streamIdDefault = "999999";

    private Activity activity;

    public DisplayBannerAdapter(Activity activity, ArrayList<Pair<String, BannerDisplayAdSize>>  bannerParams) {
        super();
        this.activity = activity;
        this.bannerParams = bannerParams;
    }

    public void setBannerParams(ArrayList<Pair<String, BannerDisplayAdSize>> bannerParams) {
        this.bannerParams = bannerParams;
    }

    @Override
    public DisplayBannerViewHolder onCreateViewHolder(ViewGroup parent , int viewType)  {

        return new DisplayBannerViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.display_banner_item, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull DisplayBannerViewHolder holder, int position) {
        Pair<String, BannerDisplayAdSize> item = bannerParams.get(position);
        holder.bind(item);

    }

    @Override
    public int getItemCount() {
        return bannerParams.size();
    }

    private Pair<String, BannerDisplayAdSize> item(int position) {
        if (position < 0 || position >= bannerParams.size()) {
            return null;
        } else {
            return bannerParams.get(position);
        }
    }

    @Override
    public void onViewRecycled(@NonNull DisplayBannerViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycled();
    }

    class DisplayBannerViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout ctlBanner;
        private TextView tvTitle;
        private BannerAdView bannerAdView;
        private TV360ReportAdsButton reportButton;

        public DisplayBannerViewHolder(View itemView) {
            super(itemView);

            ctlBanner = itemView.findViewById(R.id.ctlBanner);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            bannerAdView = new BannerAdView(itemView.getContext());
            bannerAdView.setId(generateViewId());
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(0, 100);
            layoutParams.topToBottom = tvTitle.getId();
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.topMargin = 20;
            layoutParams.rightMargin = 2;
            layoutParams.leftMargin = 2;
            layoutParams.bottomMargin = 2;
            bannerAdView.setLayoutParams(layoutParams);
            ctlBanner.addView(bannerAdView);

            // nút báo cáo (1.1.24) - tạo per-row, neo góc trên-phải của banner; SDK tự show/hide
            reportButton = new TV360ReportAdsButton(itemView.getContext());
            reportButton.setId(generateViewId());
            ConstraintLayout.LayoutParams reportParams = new ConstraintLayout.LayoutParams(60, 60);
            reportParams.topToTop = bannerAdView.getId();
            reportParams.endToEnd = bannerAdView.getId();
            reportParams.rightMargin = 10;
            reportParams.topMargin = 10;
            reportButton.setLayoutParams(reportParams);
            reportButton.setVisibility(View.GONE);
            ctlBanner.addView(reportButton);
        }

        void recycled() {
            if(bannerAdView != null) {
                DisplayBannerManager.Companion.getInstance().releaseBanner(bannerAdView);
            }
        }

        void bind(Pair<String, BannerDisplayAdSize> params ) {
            tvTitle.setText(String.format("Banner %s", params.first));
            showDisplayBanner(
                    params.second,
                    BannerDisplayType.DISPLAY,
                    bannerAdView.getId(),
                    params.first
            );
        }

        void showDisplayBanner(
                BannerDisplayAdSize adSize,
                BannerDisplayType displayType,
                int viewId,
                String positionId
        ) {
            DisplayBannerAdsRequestData bannerAdsRequestData =
                    new DisplayBannerAdsRequestData.Builder()
                            .channelId(channelIdDefault.isEmpty() ? "998989" : channelIdDefault)
                            .streamId(streamIdDefault.isEmpty() ? "999999" : streamIdDefault)
                            .adSize(adSize)
                            .bannerDisplayType(displayType)
                            .title("Day la title")
                            .category("category 1, category 2")
                            .transId("1112222222")
                            // .age(30)
                            // .gender(Gender.FEMALE)
                            .uid("123123123")
                            .userImpressionLimit(5) // giới hạn số lần hiển thị / người dùng (0 = không giới hạn)
                            .color("#ffffff00")
                            .segments("a3,34,d3,d3")
                            .positionId(positionId)
                            .build();


            DisplayBannerManager.Companion.getInstance().requestAds(
                    activity,
                    bannerAdView,
                    reportButton, // nút báo cáo quảng cáo (1.1.24)
                    bannerAdsRequestData
            );



        }

        private int generateViewId() {
            return View.generateViewId();
        }


    }
}
