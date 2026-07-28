package com.example.sampleandroidtv.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.activity.OnItemSelectedListener;

import tv.wiinvent.androidtv.models.type.BannerDisplayAdSize;

public class DisplayBannerParamsFragment extends Fragment {
    private static final String TAG = "DisplayBannerParamsFragment";

    private OnItemSelectedListener listener;
    private Spinner edtBannerSizeValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display_banner_params, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnNext = getActivity().findViewById(R.id.btnNext);
        edtBannerSizeValue = getActivity().findViewById(R.id.edtBannerSizeValue);
        ArrayAdapter<BannerDisplayAdSize> adapterAnimationName =
                new ArrayAdapter<>(requireContext(), R.layout.spinner_item, BannerDisplayAdSize.values());
        adapterAnimationName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edtBannerSizeValue.setAdapter(adapterAnimationName);

        btnNext.setOnClickListener(v -> onNext());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) context;
        } else {
            throw new ClassCastException(context + " must implement MainActivity.OnItemSelectedListener");
        }
    }

    private void onNext() {
        EditText streamIdEdt = getActivity().findViewById(R.id.edtStreamIdValue);
        EditText channelIdEdt = getActivity().findViewById(R.id.edtChannelIdValue);
        EditText positionIdEdt = getActivity().findViewById(R.id.edtPositionIdValue);
        Object selected = edtBannerSizeValue.getSelectedItem();
        BannerDisplayAdSize bannerSize = selected instanceof BannerDisplayAdSize
                ? (BannerDisplayAdSize) selected : BannerDisplayAdSize.HOMEPAGE_BANNER;

        if (listener != null) {
            listener.onDisplayBanner(streamIdEdt.getText().toString(), channelIdEdt.getText().toString(),
                    positionIdEdt.getText().toString(), bannerSize);
        }
    }
}
