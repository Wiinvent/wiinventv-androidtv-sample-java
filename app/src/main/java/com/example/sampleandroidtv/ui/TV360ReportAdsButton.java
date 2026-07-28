package com.example.sampleandroidtv.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.example.sampleandroidtv.R;

import tv.wiinvent.androidtv.report.ReportButtonAds;

public class TV360ReportAdsButton extends ReportButtonAds {

    public TV360ReportAdsButton(Context context) {
        super(context);
    }

    public TV360ReportAdsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TV360ReportAdsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        inflate(getContext(), R.layout.layout_report_ads_button, this);
        setReportButton(findViewById(R.id.report_ads_button));
    }
}
