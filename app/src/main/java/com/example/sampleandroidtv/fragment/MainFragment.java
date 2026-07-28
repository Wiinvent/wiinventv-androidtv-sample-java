package com.example.sampleandroidtv.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.sampleandroidtv.Presenter.CardPresenter;
import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.activity.BrowseErrorActivity;
import com.example.sampleandroidtv.activity.DetailsActivity;
import com.example.sampleandroidtv.activity.DisplayBannerActivity;
import com.example.sampleandroidtv.activity.OnMainListener;
import com.example.sampleandroidtv.activity.OverlayBannerActivity;
import com.example.sampleandroidtv.activity.PlaybackActivity;
import com.example.sampleandroidtv.model.Movie;
import com.example.sampleandroidtv.model.MovieList;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Loads a grid of cards with movies to browse.
 */
public class MainFragment extends BrowseSupportFragment {

  private static final String TAG = "MainFragment";

  private static final int BACKGROUND_UPDATE_DELAY = 300;
  private static final int GRID_ITEM_WIDTH = 200;
  private static final int GRID_ITEM_HEIGHT = 200;
  private static final int NUM_ROWS = 1;
  private static final int NUM_COLS = 2;

  private final Handler mHandler = new Handler();
  private BackgroundManager mBackgroundManager;
  private Drawable mDefaultBackground;
  private DisplayMetrics mMetrics;
  private Timer mBackgroundTimer;
  private String mBackgroundUri;

  private OnMainListener onMainListener;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Log.i(TAG, "onCreate");
    super.onActivityCreated(savedInstanceState);

    prepareBackgroundManager();
    setupUIElements();
    loadRows();
    setupEventListeners();
    if (getActivity() instanceof OnMainListener) {
      onMainListener = (OnMainListener) getActivity();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy: " + mBackgroundTimer);
    if (mBackgroundTimer != null) mBackgroundTimer.cancel();
  }

  private void prepareBackgroundManager() {
    mBackgroundManager = BackgroundManager.getInstance(getActivity());
    if (getActivity() != null) mBackgroundManager.attach(getActivity().getWindow());
    if (getContext() != null) mDefaultBackground = ContextCompat.getDrawable(getContext(), R.drawable.default_background);
    mMetrics = new DisplayMetrics();
    if (getActivity() != null) getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
  }

  private void setupUIElements() {
    setTitle(getString(R.string.browse_title));
    setHeadersState(HEADERS_ENABLED);
    setHeadersTransitionOnBackEnabled(true);
    setBrandColor(ContextCompat.getColor(requireContext(), R.color.fastlane_background));
    setSearchAffordanceColor(ContextCompat.getColor(requireContext(), R.color.search_opaque));
  }

  private void loadRows() {
    List<Movie> list = MovieList.getList();

    ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
    CardPresenter cardPresenter = new CardPresenter();

    for (int i = 0; i < NUM_ROWS; i++) {
      if (i != 0) {
        Collections.shuffle(list);
      }
      ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
      for (int j = 0; j < NUM_COLS; j++) {
        listRowAdapter.add(list.get(j % 2));
      }
      HeaderItem header = new HeaderItem(i, MovieList.MOVIE_CATEGORY[i]);
      rowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    HeaderItem gridHeader = new HeaderItem(NUM_ROWS, "PREFERENCES");

    GridItemPresenter mGridPresenter = new GridItemPresenter();
    ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
    gridRowAdapter.add(getResources().getString(R.string.watch_trailer_1));
    gridRowAdapter.add(getResources().getString(R.string.test_welcome_banner));
    gridRowAdapter.add(getResources().getString(R.string.test_display_banner));
    gridRowAdapter.add(getResources().getString(R.string.test_overlay_banner));
    gridRowAdapter.add(getString(R.string.error_fragment));
    gridRowAdapter.add(getResources().getString(R.string.personal_settings));
    rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

    setAdapter(rowsAdapter);
  }

  private void setupEventListeners() {
    setOnSearchClickedListener(view ->
        Toast.makeText(getContext(), "Implement your own in-app search", Toast.LENGTH_LONG).show());

    setOnItemViewClickedListener(new ItemViewClickedListener());
    setOnItemViewSelectedListener(new ItemViewSelectedListener());
  }

  private final class ItemViewClickedListener implements OnItemViewClickedListener {
    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Row row) {
      if (item instanceof Movie) {
        Log.d(TAG, "Item: " + item);
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.MOVIE, (Movie) item);

        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
            getActivity(),
            ((ImageCardView) itemViewHolder.view).getMainImageView(),
            DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
        getActivity().startActivity(intent, bundle);
      } else if (item instanceof String) {
        String s = (String) item;
        if (s.contains(getString(R.string.error_fragment))) {
          startActivity(new Intent(getContext(), BrowseErrorActivity.class));
        } else if (s.contains(getString(R.string.test_display_banner))) {
          startActivity(new Intent(getContext(), DisplayBannerActivity.class));
        } else if (s.contains(getString(R.string.test_overlay_banner))) {
          startActivity(new Intent(getContext(), OverlayBannerActivity.class));
        } else if (s.contains(getString(R.string.watch_trailer_1))) {
          Intent intent = new Intent(getContext(), PlaybackActivity.class);
          intent.putExtra(DetailsActivity.MOVIE, MovieList.getList().get(0));
          startActivity(intent);
        } else if (s.contains(getString(R.string.test_welcome_banner))) {
          Log.e("tamlog", "test_welcome_banner " + onMainListener);
          if (onMainListener != null) onMainListener.onWelcome();
        } else {
          Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                               RowPresenter.ViewHolder rowViewHolder, Row row) {
      if (item instanceof Movie) {
        mBackgroundUri = ((Movie) item).getBackgroundImageUrl();
        startBackgroundTimer();
      }
    }
  }

  private void updateBackground(String uri) {
    Glide.with(requireActivity())
        .asBitmap()
        .load(uri)
        .centerCrop()
        .error(R.drawable.default_background)
        .into(new CustomTarget<Bitmap>() {
          @Override
          public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
            mBackgroundManager.setBitmap(bitmap);
          }

          @Override
          public void onLoadCleared(@Nullable Drawable placeholder) {
          }
        });
    if (mBackgroundTimer != null) mBackgroundTimer.cancel();
  }

  private void startBackgroundTimer() {
    if (mBackgroundTimer != null) mBackgroundTimer.cancel();
    mBackgroundTimer = new Timer();
    mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
  }

  private final class UpdateBackgroundTask extends TimerTask {
    @Override
    public void run() {
      mHandler.post(() -> updateBackground(mBackgroundUri));
    }
  }

  private final class GridItemPresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
      TextView view = new TextView(parent.getContext());
      view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
      view.setFocusable(true);
      view.setFocusableInTouchMode(true);
      view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.default_background));
      view.setTextColor(Color.WHITE);
      view.setGravity(Gravity.CENTER);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
      ((TextView) viewHolder.view).setText((String) item);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
  }
}
