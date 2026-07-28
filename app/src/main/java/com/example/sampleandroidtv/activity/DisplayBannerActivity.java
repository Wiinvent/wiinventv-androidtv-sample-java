package com.example.sampleandroidtv.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.sampleandroidtv.fragment.DisplayBannerFragment;
import com.example.sampleandroidtv.fragment.DisplayBannerMultipleFragment;

import tv.wiinvent.androidtv.models.type.BannerDisplayAdSize;

public class DisplayBannerActivity extends FragmentActivity implements OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new DisplayBannerMultipleFragment())
                    .commit();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Toast.makeText(this, "keycode:  " + keyCode, Toast.LENGTH_LONG).show();
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onDisplayBannerParams() {
    }

    @Override
    public void onDisplayBanner(String streamId, String channelId, String positionId, BannerDisplayAdSize adSize) {
        DisplayBannerFragment newFragment = new DisplayBannerFragment(streamId, channelId, positionId, adSize);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
