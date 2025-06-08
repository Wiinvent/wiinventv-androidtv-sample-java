
### Hướng dẫn tích hợp quảng cáo Welcome TVC
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
1. Cấu hình layout với tv.wiinvent.androidtv.ui.welcomead.WelcomeAdView và layout bố cục của giao diện wisdk_welcome_tvc_detail
2. Khởi tạo AdsWelcomeManager với một số các tham được mô tả ở dưới
3. Thêm WelcomeAdsEventListener để lắng nghe một số các event để xử lý ẩn hiện WelcomeAdView
4. Khởi tạo WelcomeAdsRequestData với một số thông tin mô tả phía dưới
5. Request ad với WelcomeAdsRequestData và layout wisdk_welcome_tvc_detail

Layout activity_main.xml cho WelcomeAdView ở layer trên cùng:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                                                   xmlns:tools="http://schemas.android.com/tools"
                                                   android:layout_width="match_parent"
                                                   android:layout_height="match_parent"
                                                   tools:context=".activity.MainActivity">

    <FrameLayout
            android:id="@+id/main_browse_fragment"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            tools:context=".activity.MainActivity"
            tools:deviceIds="tv"
            tools:ignore="MergeRootFrame" />

    <tv.wiinvent.androidtv.ui.welcomead.WelcomeAdView
            android:id="@+id/welcome_ad_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:visibility="gone"
    />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Layout wisdk_welcome_tvc_detail:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                                                   xmlns:app="http://schemas.android.com/apk/res-auto"
                                                   android:layout_width="match_parent"
                                                   android:layout_height="match_parent"
                                                   android:background="@android:color/black">
    <com.google.android.exoplayer2.ui.PlayerView
            android:id="@+id/wisdk_exo_player_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
    />

    <tv.wiinvent.androidtv.ui.instream.player.AdPlayerView
            android:id="@+id/wisdk_ad_player_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:visibility="gone"/>

    <Button
            android:id="@+id/wisdk_skip_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            android:layout_marginEnd="20dp"
            android:layout_marginBottom="60dp"
            android:background="@drawable/skip_background_button"
            android:visibility="gone"
            android:paddingLeft="10dp"
            android:paddingRight="10dp"
            android:textColor="@color/skip_color_button"
            android:textSize="16sp"
            android:textAllCaps="false"
            android:text=""/>

</androidx.constraintlayout.widget.ConstraintLayout>
```


Code mẫu triển khai trong file MainActivity:
```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_browse_fragment, new MainFragment())
                .commitNow();
    }

    //1. init welcome view
    welcomeAdView = findViewById(R.id.welcome_ad_view);

    //2. init AdsWelcomeManager
    AdsWelcomeManager.Companion.getInstance()
            .init(this,  SAMPLE_ACCOUNT_ID, DeviceType.TV, Environment.SANDBOX,
                    5, 5,
                    5,  2500, "", 6, true);

    //3. add WelcomeAdsEventListener
    AdsWelcomeManager.Companion.getInstance().addWelcomeListener(new WelcomeAdsEventListener() {
        @Override
        public void onDisplayAds() {
            Log.d(TAG, "=========onDisplayAds");

            runOnUiThread(() -> {
                if(welcomeAdView != null)
                    welcomeAdView.setVisibility(View.VISIBLE);
            });
        }

        @Override
        public void onNoAds() {
            Log.d(TAG, "=========onNoAds");
        }

        @Override
        public void onAdsWelcomeDismiss() {
            Log.d(TAG, "=========onAdsWelcomeDismiss");

            runOnUiThread(() -> {
                if(welcomeAdView != null)
                    welcomeAdView.setVisibility(View.GONE);
            });
        }

        @Override
        public void onAdsWelcomeError() {
            Log.d(TAG, "=========onAdsWelcomeError");
            runOnUiThread(() -> {
                if(welcomeAdView != null)
                    welcomeAdView.setVisibility(View.GONE);
            });
        }
    });

    //4. Khời tạo WelcomeAdsRequestData
    WelcomeAdsRequestData adsRequestData = new WelcomeAdsRequestData.Builder()
            .transId("22222") // mã giao dịch tạo từ server đối tác - client liên hệ server
            .age(30)
            .gender(Gender.FEMALE)
            .uid20("") // unified id 2.0, nếu không có thì set ""
            .segments("123,12,23") //segment id của user phân tách nhau bời, dữ liệu này lấy từ backend đối tác
            .build();

    //5. Request ad
    AdsWelcomeManager.Companion.getInstance().requestAds(this,
            R.id.welcome_ad_view, // truyền layout welcome_ad_view
            R.layout.wisdk_welcome_tvc_detail,
            R.id.wisdk_exo_player_view,
            R.id.wisdk_ad_player_view,
            R.id.wisdk_skip_button,
            "Bỏ qua quảng cáo",
            R.drawable.skip_icon_button,
            adsRequestData);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    AdsWelcomeManager.Companion.getInstance().release();
}
```

#### 3. Mô tả các hằng số và tham số
1. Tham số init của AdsWelcomeManager

| Key                   | Description                            |     Type |
|:----------------------|:---------------------------------------|---------:|
| tenantId              | id đối tác được cung cấp bởi wiinvent  |  integer |
| deviceType            | Đối với TV luôn là DeviceType.TV       | constant |
| environment           | Môi trường SANDBOX hoặc PRODUCTION     | constant |
| vastLoadTimeout       | Timeout tải vast                       |  integer |
| mediaLoadTimeout      | Timeout tải media                      |  integer |
| bufferingVideoTimeout | Timeout buffer media                   |  integer |
| bitrate               | Max bitrate cho quảng cáo tvc          |  integer |    
| domainUrl             | Hiện tạm không dùng vui lòng truyền "" |   string |                                  
| partnerSkipOffset     | Thời gian bỏ qua                       |  integer |
| debug                 | bật tắt debug                          |     bool |                                  

2. Tham số của WelcomeAdsRequestData

| Key          | Description                                                                       |       Type |
|:-------------|:----------------------------------------------------------------------------------|-----------:|
| tranId       | Mã giao dịch tạo từ server đối tác - client liên hệ server để biết thêm thông tin |     string |
| segments     | segment id của user phân tách nhau bời, dữ liệu này lấy từ backend đối tác        |     string |
| age          | Tuổi (Nếu có)                                                                     |     number |
| gender       | Giới tính (nếu có)                                                                |   constant |
| uid20        | Unified id 2.0 (nếu có)                                                           |     string |

3. Hằng số

| Key         | Description                                                                                     |
|:------------|:------------------------------------------------------------------------------------------------|
| env         | Environment.SANDBOX <br/> WI.Environment.PRODUCTION                                             |
| contentType | ContentType.TV <br/>WI.ContentType.FILM <br/>WI.ContentType.VIDEO <br/>WI.ContentType.SHORT_VOD |
| gender      | Gender.MALE <br/>WI.Gender.FEMALE <br/>WI.Gender.OTHER <br/>WI.Gender.NONE                      |

