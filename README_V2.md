
### Hướng dẫn tích hợp quảng cáo Instream phương án TVC quảng cáo và nội dung chạy riêng player
#### 1. Thêm Repository
Bổ sung config sau đây vào file `build.gradle` ở thư mục gốc của project.

```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://maven.wiinvent.tv/repository/maven-releases/" }
    }
}  
```

Tiếp theo bổ sung dependency trong file `build.gradle` trong app module

```gralde
implementation 'tv.wiinvent:wiinvent-sdk-android:1.2.0'
```

#### 2. Sử dụng

Khởi tạo SDK với một vài bước như sau:
1. Cấu hình layout với: PlayerView cho player hiển thị nội dung, FriendlyPlayerView cho player hiển tvc quảng cáo, TV360SkipAdsButtonAds nút bỏ qua
2. Khởi tạo InStreamManager với một số các tham được mô tả ở dưới
3. Thêm WiAdsLoaderListener để lắng nghe một số các event, lỗi và ẩn hiện nút bỏ qua, xử lý logic show/hide (pause/resume) của player nội dung và player của tvc quảng cáo
4. Khởi tạo AdsRequestData với channelId, streamId cùng các tham số khác (mô tả ở phía dưới)
5. Khai báo thêm FriendlyObstruction đối với các view đè lên trên player.

Layout playback_video_fragment.xml:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                                                   xmlns:tools="http://schemas.android.com/tools"
                                                   android:layout_width="match_parent"
                                                   android:layout_height="match_parent"
                                                   xmlns:app="http://schemas.android.com/apk/res-auto">

    <com.google.android.exoplayer2.ui.PlayerView
            android:id="@+id/simple_exo_player_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

    <tv.wiinvent.androidtv.ui.instream.player.AdPlayerView
            android:id="@+id/ad_player_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:visibility="gone"
    />

    <com.example.sampleandroidtv.ui.TV360SkipAdsButtonAds
            android:id="@+id/skip_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            android:layout_marginEnd="20dp"
            android:layout_marginBottom="60dp"
            android:visibility="gone"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

Code mẫu triển khai trong file PlaybackVideoFragment:
```java
private PlayerView playerView;
private AdPlayerView adPlayerView;
private ExoPlayer exoPlayer;

@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
  super.onViewCreated(view, savedInstanceState);
  playerView = requireActivity().findViewById(R.id.simple_exo_player_view);
  adPlayerView = requireActivity().findViewById(R.id.ad_player_view);
  skipButton = requireActivity().findViewById(R.id.skip_button);
  init(savedInstanceState);
}

private void initializePlayer() {
  // 2. Khởi tạo InStreamManager
  InStreamManager.Companion.getInstance().init(requireContext(), SAMPLE_ACCOUNT_ID, DeviceType.TV, Environment.SANDBOX, 5, 1, 5, 2500, LevelLog.BODY,true, 8);


  String userAgent = Util.getUserAgent(requireContext(), "Exo");

  exoPlayer = new ExoPlayer.Builder(requireContext()).build();
  playerView.setPlayer(exoPlayer);

  String contentUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8";

  ContentType contentType = ContentType.FILM;
  currentVolume = exoPlayer.getVolume();

  //3. Thêm WiAdsLoaderListener
  InStreamManager.Companion.getInstance().setLoaderListener(new InStreamManager.WiAdsLoaderListener() {
    @Override
    public void onEvent(@NonNull AdInStreamEvent event) {
      Log.d(TAG, "==========event " + event.getEventType() + " - " + event.getCampaignId() + ")");
      if(event.getEventType() == AdInStreamEvent.EventType.ERROR) {
        Log.d(TAG, "===========Xu ly error");
      }
    }

    @Override
    public void showSkipButton(@NonNull String campaignId, int duration) {
      if(skipButton != null)
        skipButton.startCountdown(duration, () -> {
          if(skipButton != null) {
            skipButton.requestFocusToSkip();

            Log.d(TAG, "=========request focus");
          }
        }); //neu khong muon tu dong focus thi set = true
    }

    @Override
    public void hideSkipButton(@NonNull String campaignId) {
      if(skipButton != null)
        skipButton.hide();
    }

    @Override
    public void showContentPlayer() {
      requireActivity().runOnUiThread(() -> {
        if (contentType == ContentType.TV) {
          if (exoPlayer != null) {
            exoPlayer.setVolume(currentVolume);
          }
        } else {
          if (exoPlayer != null) {
            exoPlayer.play();
          }
        }

        if (playerView != null) {
          playerView.setVisibility(View.VISIBLE);
        }
      });
    }

    @Override
    public void hideContentPlayer() {
      requireActivity().runOnUiThread(() -> {
        if (contentType == ContentType.TV) {
          if (exoPlayer != null) {
            currentVolume = exoPlayer.getVolume();
            exoPlayer.setVolume(0);
          }
        } else {
          if (exoPlayer != null) {
            exoPlayer.pause();
          }
        }

        if (playerView != null) {
          playerView.setVisibility(View.GONE);
        }
      });
    }

    @Override
    public void onEventSkip(@NonNull String s) {
      //write log
    }

    @Override
    public void onFailure() {
      //write log
    }

    @Override
    public void onTimeout() {
      //write log
    }
  });

  //4. Khởi tạo AdsRequestData
  AdsRequestData adsRequestData = new AdsRequestData.Builder()
      .channelId("998989,222222") // danh sách id của category của nội dung & cách nhau bằng dấu ,
      .streamId(SAMPLE_STREAM_ID) // id nội dung
      .transId("222222") // Transaction cua TV360
      .contentType(ContentType.FILM) // content type TV | FILM | VIDEO
      .title("Tieu de cua noi dung") // tiêu đề nội dung
      .category("category 1, category 2") // danh sach tiêu đề category của nội dung & cách nhau bằng dấu ,
      .keyword("keyword 1, keyword 2") // từ khoá nếu có | để "" nếu ko có
      .uid20("") // unified id 2.0, nếu không có thì set ""
      .segments("123,1,23") //segment id của user phân tách nhau bời, dữ liệu này lấy từ backend đối tác
      .build();

  //5. khai bao friendly obstruction --- quan trong => can phai cai khao het cac lop phu len tren player
  List<FriendlyObstruction> friendlyObstructionList = Lists.newArrayList();
  FriendlyObstruction skipButtonObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
      skipButton,
      FriendlyObstructionPurpose.CLOSE_AD,
      "This is close ad"
  );
  friendlyObstructionList.add(skipButtonObstruction);

//    FriendlyObstruction overlaysObstruction = InStreamManager.Companion.getInstance().createFriendlyObstruction(
//        overlayView,
//        FriendlyObstructionPurpose.OTHER,
//        "This is transparent overlays"
//    );
//    friendlyObstructionList.add(overlaysObstruction);

  DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
  httpDataSourceFactory.setUserAgent(userAgent);
  httpDataSourceFactory.setTransferListener(new DefaultBandwidthMeter.Builder(requireContext())
      .setResetOnNetworkTypeChange(false).build());

  MediaSource mediaSource = buildMediaSource(buildDataSourceFactory(httpDataSourceFactory), contentUrl, getDrmSessionManager(httpDataSourceFactory));

  InStreamManager.Companion.getInstance()
      .requestAds(adsRequestData,
          adPlayerView,
          exoPlayer,
          friendlyObstructionList);

  exoPlayer.addMediaSource(mediaSource);
  exoPlayer.prepare();

  exoPlayer.setPlayWhenReady(true);
}
```

#### 3. Mô tả các hằng số và tham số
1. Tham số init của InStreamManager

| Key                   | Description                                               |     Type |
|:----------------------|:----------------------------------------------------------|---------:|
| tenantId              | id đối tác được cung cấp bởi wiinvent                     |  integer |
| deviceType            | Đối với TV luôn là DeviceType.TV                          | constant |
| environment           | Môi trường SANDBOX hoặc PRODUCTION                        | constant |
| vastLoadTimeout       | Timeout tải vast                                          |  integer |
| mediaLoadTimeout      | Timeout tải media                                         |  integer |
| bufferingVideoTimeout | Timeout buffer media                                      |  integer |
| bitrate               | Max bitrate cho quảng cáo tvc                             |  integer |
| logLevel              | level của log , môi trường PRODUCTION cần set về mức NONE | constant |                                  
| alwaysCustomSkip      | Biến hiển thị nút skip mặc định hay custom                |  boolean |                                  
| partnerSkipOffset     | Thời gian bỏ qua                                          |  integer |

2. Tham số của AdsRequestData

| Key          | Description                                                                        |       Type |
|:-------------|:-----------------------------------------------------------------------------------|-----------:|
| channelId    | Danh sách id của category của nội dung & cách nhau bằng dấu ","                    |     string |
| streamId     | Id nội dung                                                                        |     string |
| tranId       | Mã giao dịch tạo từ server đối tác - client liên hệ server để biết thêm thông tin  |     string |
| contentType  | Loại nội dung là film, video hoặc tv                                               |   constant |
| title        | Tiêu đề của nội dung                                                               |     string |
| category     | Danh sach tiêu đề category của nội dung & cách nhau bằng dấu ","                   |     string |
| keyword      | Từ khoá tìm kiếm của nội dung (nếu có)                                             |     string |
| segments     | /segment id của user phân tách nhau bời, dữ liệu này lấy từ backend đối tác        |     string |
| age          | Tuổi (Nếu có)                                                                      |     number |
| gender       | Giới tính (nếu có)                                                                 |   constant |
| uid20        | Unified id 2.0 (nếu có)                                                            |     string |

3. Hằng số

| Key         | Description                                                                                     |
|:------------|:------------------------------------------------------------------------------------------------|
| env         | Environment.SANDBOX <br/> WI.Environment.PRODUCTION                                             |
| contentType | ContentType.TV <br/>WI.ContentType.FILM <br/>WI.ContentType.VIDEO <br/>WI.ContentType.SHORT_VOD |
| gender      | Gender.MALE <br/>WI.Gender.FEMALE <br/>WI.Gender.OTHER <br/>WI.Gender.NONE                      |
| logLevel    | LevelLog.NONE <br/> LevelLog.BODY                                                               |
| adSize      | BannerAdSize.BANNER <br/> BannerAdSize.LARGE_BANNER <br/> BannerAdSize.RECTANGLE                |

4. Hằng số callback

| Type      | Value      | Description                                   |
|:----------|:-----------|:----------------------------------------------|
| EventType | REQUEST    | Fired when the ad requests                    |
| EventType | LOADED     | Fired when the ad loaded                      |
| EventType | START      | Fired when the ad starts playing              |
| EventType | IMPRESSION | Fired when the impression URL has been pinged |
| EventType | CLICK      | Fired when the ad is clicked                  |
| EventType | COMPLETE   | Fired when the ad completes playing           |
| EventType | SKIPPED    | Fired when the ad is skipped by the user      |
| EventType | ERROR      | Fired when the ad has an error                |
| EventType | DESTROY    | Fires when the ad destroyed                   |
