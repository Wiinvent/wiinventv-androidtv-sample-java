package com.example.sampleandroidtv.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.sampleandroidtv.fragment.BitmovinPlaybackVideoFragment;
import com.example.sampleandroidtv.fragment.PlaybackVideoFragment;
import com.example.sampleandroidtv.pojo.Movie;

/**
 * Loads {@link PlaybackVideoFragment}.
 */
public class PlaybackActivity extends FragmentActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {

      Fragment playbackFragment;
      Movie movie = (Movie) getIntent().getSerializableExtra(DetailsActivity.MOVIE);
      if(movie != null && movie.getTitle().equals("Bitmovin")) {
        playbackFragment = new BitmovinPlaybackVideoFragment();
      } else {
        playbackFragment = new PlaybackVideoFragment();
      }

      getSupportFragmentManager()
          .beginTransaction()
          .replace(android.R.id.content, playbackFragment)
          .commit();
    }
  }
}