package com.example.sampleandroidtv.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sampleandroidtv.R;

import tv.wiinvent.androidtv.report.ReportButtonAds;

// Nút báo cáo quảng cáo tuỳ chỉnh - dùng chung cho cả 4 loại quảng cáo (1.1.24).
// Kế thừa ReportButtonAds, override init() để inflate layout của đối tác và gán reportButton.
public class TV360ReportAdsButton extends ReportButtonAds {
  public TV360ReportAdsButton(@NonNull Context context) {
    super(context);
    init();
  }

  public TV360ReportAdsButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public TV360ReportAdsButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override
  public void init() {
    inflate(getContext(), R.layout.layout_report_ads_button, this);
    setBackgroundResource(R.drawable.report_button_bg);
    setReportButton(findViewById(R.id.report_ads_button));
  }
}
