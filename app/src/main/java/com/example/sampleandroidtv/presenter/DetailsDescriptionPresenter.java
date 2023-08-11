package com.example.sampleandroidtv.presenter;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.sampleandroidtv.pojo.Movie;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

  @Override
  protected void onBindDescription(ViewHolder viewHolder, Object item) {
    Movie movie = (Movie) item;

    if (movie != null) {
      viewHolder.getTitle().setText(movie.getTitle());
      viewHolder.getSubtitle().setText(movie.getStudio());
      viewHolder.getBody().setText(movie.getDescription());
    }
  }
}