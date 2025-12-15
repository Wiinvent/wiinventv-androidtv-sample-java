package com.example.sampleandroidtv.pojo;

import java.util.ArrayList;
import java.util.List;

public final class MovieList {
  public static final String MOVIE_CATEGORY[] = {
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

  public static List<Movie> setupMovies() {
    list = new ArrayList<>();
    String title[] = {
            "Voting",
            "Banner"
    };

    String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. "
        + "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est "
        + "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit "
        + "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit "
        + "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id "
        + "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";
    String studio[] = {
            "Studio Zero",
            "Studio One"
    };
    String videoUrl[] = {
            "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4",
            "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4"
    };
    String bgImageUrl[] = {
            "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
            "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg"
    };
    String cardImageUrl[] = {
            "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/card.jpg",
            "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg"
    };

    for (int index = 0; index < title.length; ++index) {
      list.add(
          buildMovieInfo(
              title[index],
              description,
              studio[index],
              videoUrl[index],
              cardImageUrl[index],
              bgImageUrl[index]));
    }

    return list;
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