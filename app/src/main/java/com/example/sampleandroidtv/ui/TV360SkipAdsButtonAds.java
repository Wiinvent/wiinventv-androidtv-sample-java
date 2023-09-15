package com.example.sampleandroidtv.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sampleandroidtv.R;

import tv.wiinvent.androidtv.ui.instream.SkipAdsButtonAds;

public class TV360SkipAdsButtonAds extends SkipAdsButtonAds {
  public TV360SkipAdsButtonAds(@Nullable Context context) {
    super(context);
    init();
  }

  public TV360SkipAdsButtonAds(@NonNull Context context, @NonNull AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public TV360SkipAdsButtonAds(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override
  public void init() {
    inflate(getContext(), R.layout.layout_skip_button, this);
    this.setSkipButton(findViewById(R.id.skip_ads_button));
    if(getSkipButton() != null) {
      getSkipButton().setOnClickListener((View.OnClickListener) this);
    }
    setSkipLabel("Bỏ qua quảng cáo");
    setIconDrawable( R.drawable.next_black);
    setColorTextSelected(Color.parseColor("#28282A"));
  }
}
