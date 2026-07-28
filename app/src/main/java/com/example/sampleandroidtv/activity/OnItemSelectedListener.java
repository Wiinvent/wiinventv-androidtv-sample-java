package com.example.sampleandroidtv.activity;

import tv.wiinvent.androidtv.models.type.BannerDisplayAdSize;

public interface OnItemSelectedListener {
    void onDisplayBannerParams();
    void onDisplayBanner(String streamId, String channelId, String positionId, BannerDisplayAdSize adSize);
}
