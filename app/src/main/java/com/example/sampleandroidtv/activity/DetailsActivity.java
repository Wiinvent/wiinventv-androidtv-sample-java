package com.example.sampleandroidtv.activity;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.sampleandroidtv.R;

/**
 * Details activity class that loads VideoDetailsFragment.
 */
public class DetailsActivity extends FragmentActivity {

  public static final String SHARED_ELEMENT_NAME = "hero";
  public static final String MOVIE = "Movie";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_details);
  }
}
