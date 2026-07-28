package com.example.sampleandroidtv.model;

import java.util.ArrayList;
import java.util.List;

public final class MovieList {
  private MovieList() {}

  public static final String[] MOVIE_CATEGORY = {
      "K+1",
      "VTV1"
  };

  private static List<Movie> list;
  private static long count = 0;

  public static List<Movie> getList() {
    if (list == null) {
      list = setupMovies();
    }
    return list;
  }

  private static List<Movie> setupMovies() {
    String[] title = {
        "Voting",
        "Banner"
    };

    String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. " +
        "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est " +
        "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit " +
        "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit " +
        "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id " +
        "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";
    String[] studio = {
        "Studio Zero",
        "Studio One"
    };
    String[] videoUrl = {
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4",
        "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4"
    };
    String[] bgImageUrl = {
        "https://png.pngtree.com/thumb_back/fh260/background/20250512/pngtree-blue-gradient-soft-background-vector-image_17280771.jpg",
        "https://png.pngtree.com/thumb_back/fh260/background/20250512/pngtree-blue-gradient-soft-background-vector-image_17280771.jpg"
    };
    String[] cardImageUrl = {
        "https://img.freepik.com/free-vector/abstract-bright-geometric-line-modern-wallpaper-design_1017-60099.jpg?semt=ais_hybrid&w=740&q=80",
        "https://img.freepik.com/free-vector/abstract-bright-geometric-line-modern-wallpaper-design_1017-60099.jpg?semt=ais_hybrid&w=740&q=80"
    };

    List<Movie> result = new ArrayList<>();
    for (int i = 0; i < title.length; i++) {
      result.add(buildMovieInfo(
          title[i],
          description,
          studio[i],
          videoUrl[i],
          cardImageUrl[i],
          bgImageUrl[i]));
    }
    return result;
  }

  private static Movie buildMovieInfo(
      String title,
      String description,
      String studio,
      String videoUrl,
      String cardImageUrl,
      String backgroundImageUrl) {
    Movie movie = new Movie();
    movie.setId(count++);
    movie.setTitle(title);
    movie.setDescription(description);
    movie.setStudio(studio);
    movie.setCardImageUrl(cardImageUrl);
    movie.setBackgroundImageUrl(backgroundImageUrl);
    movie.setVideoUrl(videoUrl);
    return movie;
  }
}
