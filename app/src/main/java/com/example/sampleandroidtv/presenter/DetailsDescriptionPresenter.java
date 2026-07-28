package com.example.sampleandroidtv.Presenter;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.sampleandroidtv.model.Movie;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

  @Override
  protected void onBindDescription(ViewHolder viewHolder, Object item) {
    Movie movie = (Movie) item;

    viewHolder.getTitle().setText(movie.getTitle());
    viewHolder.getSubtitle().setText(movie.getStudio());
    viewHolder.getBody().setText(movie.getDescription());
  }
}
