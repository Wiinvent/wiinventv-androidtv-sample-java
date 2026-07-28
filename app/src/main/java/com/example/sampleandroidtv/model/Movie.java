package com.example.sampleandroidtv.model;

import java.io.Serializable;

/**
 * Movie class represents video entity with title, description, image thumbs and video url.
 */
public class Movie implements Serializable {
  static final long serialVersionUID = 727566175075960653L;

  private long id;
  private String title;
  private String description;
  private String backgroundImageUrl;
  private String cardImageUrl;
  private String videoUrl;
  private String studio;

  public long getId() { return id; }
  public void setId(long id) { this.id = id; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getBackgroundImageUrl() { return backgroundImageUrl; }
  public void setBackgroundImageUrl(String backgroundImageUrl) { this.backgroundImageUrl = backgroundImageUrl; }

  public String getCardImageUrl() { return cardImageUrl; }
  public void setCardImageUrl(String cardImageUrl) { this.cardImageUrl = cardImageUrl; }

  public String getVideoUrl() { return videoUrl; }
  public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

  public String getStudio() { return studio; }
  public void setStudio(String studio) { this.studio = studio; }

  @Override
  public String toString() {
    return "Movie{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", videoUrl='" + videoUrl + '\'' +
        ", backgroundImageUrl='" + backgroundImageUrl + '\'' +
        ", cardImageUrl='" + cardImageUrl + '\'' +
        '}';
  }
}
