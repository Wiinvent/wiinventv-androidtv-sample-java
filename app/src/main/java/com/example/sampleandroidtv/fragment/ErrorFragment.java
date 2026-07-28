package com.example.sampleandroidtv.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.ErrorSupportFragment;

import com.example.sampleandroidtv.R;

/**
 * This class demonstrates how to extend {@link androidx.leanback.app.ErrorSupportFragment}.
 */
public class ErrorFragment extends ErrorSupportFragment {

  private static final boolean TRANSLUCENT = true;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(getResources().getString(R.string.app_name));
  }

  public void setErrorContent() {
    setImageDrawable(ContextCompat.getDrawable(requireContext(), androidx.leanback.R.drawable.lb_ic_sad_cloud));
    setMessage(getResources().getString(R.string.error_fragment_message));
    setDefaultBackground(TRANSLUCENT);

    setButtonText(getResources().getString(R.string.dismiss_error));
    setButtonClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (getFragmentManager() != null) {
          getFragmentManager().beginTransaction().remove(ErrorFragment.this).commit();
        }
      }
    });
  }
}
