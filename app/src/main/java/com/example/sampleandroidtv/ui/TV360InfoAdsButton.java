package com.example.sampleandroidtv.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.sampleandroidtv.R;

import tv.wiinvent.androidtv.report.InfoButtonAds;

public class TV360InfoAdsButton extends InfoButtonAds {

    private View infoButton = null;
    private float customTextSize = 0f;

    public TV360InfoAdsButton(Context context) {
        super(context);
    }

    public TV360InfoAdsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TV360InfoAdsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTextSize(float textSize) {
        if (infoButton instanceof TextView) {
            ((TextView) infoButton).setTextSize(textSize);
        }
    }

    private void initializeAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        Log.d("tamlog", "initializeAttr");
        if (attrs != null) {
            // Lấy mảng thuộc tính đã khai báo trong attrs.xml
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TV360InfoAdsButton, defStyleAttr, 0);
            try {
                // Đọc giá trị textSize (đơn vị Pixels). Mặc định 14sp quy đổi sang px.
                float defaultSize = 14 * context.getResources().getDisplayMetrics().scaledDensity;
                customTextSize = typedArray.getDimension(R.styleable.TV360InfoAdsButton_android_textSize, defaultSize);
            } finally {
                // Bắt buộc gọi recycle() để giải phóng bộ nhớ
                typedArray.recycle();
            }
        }
    }

    @Override
    public void init(Context context, AttributeSet attrs, int defStyleAttr) {
        initializeAttr(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_info_ads_button, this);
        infoButton = findViewById(R.id.info_ads_button);
        Log.d("tamlog", "customTextSize " + customTextSize);
        if (infoButton instanceof TextView) {
            ((TextView) infoButton).setTextSize(customTextSize);
        }
    }
}
