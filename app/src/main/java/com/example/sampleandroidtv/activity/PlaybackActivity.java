package com.example.sampleandroidtv.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.sampleandroidtv.fragment.PlaybackVideoFragment;

public class PlaybackActivity extends FragmentActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(android.R.id.content, new PlaybackVideoFragment())
          .commit();
    }
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
      if (fragment instanceof PlaybackVideoFragment
          && ((PlaybackVideoFragment) fragment).onDpadKey(event.getKeyCode())) {
        return true;
      }
    }
    return super.dispatchKeyEvent(event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    Toast.makeText(this, "keycode:  " + keyCode, Toast.LENGTH_LONG).show();
    return super.onKeyUp(keyCode, event);
  }
}
