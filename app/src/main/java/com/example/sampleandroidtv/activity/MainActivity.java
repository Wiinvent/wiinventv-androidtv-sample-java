package com.example.sampleandroidtv.activity;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.sampleandroidtv.fragment.MainFragment;
import com.example.sampleandroidtv.R;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_browse_fragment, new MainFragment())
          .commitNow();
    }
  }
}