package com.example.sampleandroidtv.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.example.sampleandroidtv.R;

import tv.wiinvent.androidtv.ui.instream.SkipAdsButtonAds;

public class TV360SkipAdsButtonAds extends SkipAdsButtonAds {

  public TV360SkipAdsButtonAds(Context context) {
    super(context);
    init();
  }

  public TV360SkipAdsButtonAds(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public TV360SkipAdsButtonAds(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override
  public void init() {
    inflate(getContext(), R.layout.layout_skip_button, this);
    setSkipButton(findViewById(R.id.skip_ads_button));
    setSkipLabel("Bỏ qua quảng cáo");
    setCountdownLabel("Bỏ qua sau");
    setUnitLabel("giây");
    setIconDrawable(R.drawable.skip_icon_button);
  }
}
