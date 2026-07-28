package com.example.sampleandroidtv.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.app.DetailsSupportFragmentBackgroundController;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.sampleandroidtv.Presenter.CardPresenter;
import com.example.sampleandroidtv.Presenter.DetailsDescriptionPresenter;
import com.example.sampleandroidtv.R;
import com.example.sampleandroidtv.activity.DetailsActivity;
import com.example.sampleandroidtv.activity.MainActivity;
import com.example.sampleandroidtv.activity.PlaybackActivity;
import com.example.sampleandroidtv.model.Movie;
import com.example.sampleandroidtv.model.MovieList;

import java.util.Collections;
import java.util.List;

/**
 * A wrapper fragment for leanback details screens.
 */
public class VideoDetailsFragment extends DetailsSupportFragment {

  private static final String TAG = "VideoDetailsFragment";

  private static final long ACTION_WATCH_TRAILER = 1L;
  private static final long ACTION_WATCH_BANNER = 4L;
  private static final long ACTION_RENT = 2L;
  private static final long ACTION_BUY = 3L;

  private static final int DETAIL_THUMB_WIDTH = 274;
  private static final int DETAIL_THUMB_HEIGHT = 274;

  private static final int NUM_COLS = 10;

  private Movie mSelectedMovie;
  private DetailsSupportFragmentBackgroundController mDetailsBackground;
  private ClassPresenterSelector mPresenterSelector;
  private ArrayObjectAdapter mAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate DetailsFragment");
    super.onCreate(savedInstanceState);

    mDetailsBackground = new DetailsSupportFragmentBackgroundController(this);

    mSelectedMovie = (Movie) requireActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);
    if (mSelectedMovie != null) {
      mPresenterSelector = new ClassPresenterSelector();
      mAdapter = new ArrayObjectAdapter(mPresenterSelector);
      setupDetailsOverviewRow();
      setupDetailsOverviewRowPresenter();
      setupRelatedMovieListRow();
      setAdapter(mAdapter);
      initializeBackground(mSelectedMovie);
      setOnItemViewClickedListener(new ItemViewClickedListener());
    } else {
      startActivity(new Intent(getContext(), MainActivity.class));
    }
  }

  private void initializeBackground(Movie movie) {
    mDetailsBackground.enableParallax();
    Glide.with(requireActivity())
        .asBitmap()
        .load(movie != null ? movie.getBackgroundImageUrl() : null)
        .centerCrop()
        .error(R.drawable.default_background)
        .into(new CustomTarget<Bitmap>() {
          @Override
          public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
            mDetailsBackground.setCoverBitmap(bitmap);
            mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
          }

          @Override
          public void onLoadCleared(@Nullable Drawable placeholder) {
          }
        });
  }

  private void setupDetailsOverviewRow() {
    Log.d(TAG, "doInBackground: " + mSelectedMovie);
    final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMovie);
    row.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.default_background));
    int width = convertDpToPixel(requireContext(), DETAIL_THUMB_WIDTH);
    int height = convertDpToPixel(requireContext(), DETAIL_THUMB_HEIGHT);
    Glide.with(requireActivity())
        .asBitmap()
        .load(mSelectedMovie != null ? mSelectedMovie.getCardImageUrl() : null)
        .centerCrop()
        .error(R.drawable.default_background)
        .into(new CustomTarget<Bitmap>(width, height) {
          @Override
          public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
            Log.d(TAG, "details overview card image url ready: " + bitmap);
            row.setImageBitmap(requireContext(), bitmap);
            mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
          }

          @Override
          public void onLoadCleared(@Nullable Drawable placeholder) {
          }
        });

    ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();
    actionAdapter.add(new Action(ACTION_WATCH_TRAILER,
        getResources().getString(R.string.watch_trailer_1),
        getResources().getString(R.string.watch_trailer_2)));
    actionAdapter.add(new Action(ACTION_WATCH_BANNER,
        getResources().getString(R.string.test_display_banner),
        getResources().getString(R.string.watch_banner_2)));
    actionAdapter.add(new Action(ACTION_RENT,
        getResources().getString(R.string.rent_1),
        getResources().getString(R.string.rent_2)));
    actionAdapter.add(new Action(ACTION_BUY,
        getResources().getString(R.string.buy_1),
        getResources().getString(R.string.buy_2)));
    row.setActionsAdapter(actionAdapter);

    mAdapter.add(row);
  }

  private void setupDetailsOverviewRowPresenter() {
    FullWidthDetailsOverviewRowPresenter detailsPresenter =
        new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
    detailsPresenter.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_background));

    FullWidthDetailsOverviewSharedElementHelper sharedElementHelper =
        new FullWidthDetailsOverviewSharedElementHelper();
    sharedElementHelper.setSharedElementEnterTransition(getActivity(), DetailsActivity.SHARED_ELEMENT_NAME);
    detailsPresenter.setListener(sharedElementHelper);
    detailsPresenter.setParticipatingEntranceTransition(true);

    detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
      @Override
      public void onActionClicked(Action action) {
        if (action.getId() == ACTION_WATCH_TRAILER) {
          Intent intent = new Intent(getContext(), PlaybackActivity.class);
          intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie);
          startActivity(intent);
        } else {
          Toast.makeText(getContext(), action.toString(), Toast.LENGTH_SHORT).show();
        }
      }
    });
    mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
  }

  private void setupRelatedMovieListRow() {
    String[] subcategories = {getString(R.string.related_movies)};
    List<Movie> list = MovieList.getList();

    Collections.shuffle(list);
    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
    for (int j = 0; j < NUM_COLS; j++) {
      listRowAdapter.add(list.get(j % 2));
    }

    HeaderItem header = new HeaderItem(0, subcategories[0]);
    mAdapter.add(new ListRow(header, listRowAdapter));
    mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
  }

  private int convertDpToPixel(Context context, int dp) {
    float density = context.getApplicationContext().getResources().getDisplayMetrics().density;
    return Math.round((float) dp * density);
  }

  private final class ItemViewClickedListener implements OnItemViewClickedListener {
    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                              RowPresenter.ViewHolder rowViewHolder, Row row) {
      if (item instanceof Movie) {
        Log.d(TAG, "Item: " + item);
        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra(getResources().getString(R.string.movie), mSelectedMovie);

        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
            getActivity(),
            ((ImageCardView) itemViewHolder.view).getMainImageView(),
            DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
        getActivity().startActivity(intent, bundle);
      }
    }
  }
}
