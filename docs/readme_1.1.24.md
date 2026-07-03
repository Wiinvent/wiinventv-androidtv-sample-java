# Wiinvent SDK Android TV v1.1.24 — Hướng dẫn tích hợp

## Tổng quan

Tài liệu này hướng dẫn tích hợp SDK quảng cáo Wiinvent cho Android TV / TV Box, phiên bản **1.1.24** (nâng cấp từ **1.1.23** đã phát hành cho đối tác).

Bản 1.1.24 tập trung vào hai nhóm thay đổi chính:

1. **Bổ sung nút Báo cáo quảng cáo (Report Ads Button)** dùng chung cho cả 4 loại quảng cáo: Welcome Ads, InStream Ads, Display Banner và Overlay Banner.
2. **Bổ sung tham số `userImpressionLimit` (`uil`)** — giới hạn số lần hiển thị quảng cáo trên mỗi người dùng — cho tất cả các API quảng cáo.

---

## Các thay đổi chính trong 1.1.24

| Nhóm | Nội dung |
|---|---|
| Report Ads | Thêm lớp trừu tượng `ReportButtonAds`, hộp thoại `ReportDialogFragment` và endpoint gửi báo cáo `POST /v1/adserving/report`. Đối tác tự tạo nút báo cáo (kế thừa `ReportButtonAds`) và truyền vào từng loại quảng cáo. |
| Định danh người dùng | Bổ sung `userImpressionLimit` (`uil`) cho `WelcomeAdsRequestData`, `DisplayBannerAdsRequestData`, `AdsRequestData` (InStream). |
| InStream (VMAP) | URL VMAP nay gửi kèm cả `uid` và `uil`; lưu `uid`/`uil` vào cache để các log phụ (error, skip) gửi đúng giá trị. |
| Callback Banner | `BannerAdEventListener` bổ sung các callback `onShowReportButton` / `onHideReportButton` và tham số `reportButton` trong các callback hiển thị. |

---

## Phụ thuộc (Dependency)

Thêm Maven repository của Wiinvent (nếu chưa có) ở `build.gradle` cấp project:

```gradle
allprojects {
  repositories {
    google()
    mavenCentral()
    maven { url "https://maven.wiinvent.tv/repository/maven-releases/" }
  }
}
```

Khai báo dependency ở module ứng dụng:

```gradle
dependencies {
  implementation 'tv.wiinvent:wiinvent-sdk-android-tv:1.1.24'
}
```

---

## 1. Tạo nút Báo cáo quảng cáo tuỳ chỉnh

Đối tác tạo một lớp kế thừa `ReportButtonAds` (một `ConstraintLayout`), override `init()` để inflate layout của mình và gán `reportButton` là view sẽ nhận focus / click.

```kotlin
class TV360ReportAdsButton : ReportButtonAds {

    constructor(context: Context) : super(context) { init() }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
        : super(context, attrs, defStyleAttr) { init() }

    override fun init() {
        inflate(context, R.layout.layout_report_ads_button, this)
        setBackgroundResource(R.drawable.report_button_bg)
        reportButton = findViewById(R.id.report_ads_button)
    }
}
```

> ℹ️ Lớp `ReportButtonAds`, hộp thoại `ReportDialogFragment` và toàn bộ resource của hộp thoại (layout, drawable, style của dialog báo cáo) đã được đóng gói sẵn trong SDK. Đối tác **chỉ cần bổ sung các resource cho nút báo cáo của mình** như bên dưới.

### 1.1. Các resource cần bổ sung cho nút báo cáo

Nút báo cáo tuỳ chỉnh (`TV360ReportAdsButton`) ở trên tham chiếu tới các file resource sau — đối tác cần tạo trong ứng dụng:

**`res/layout/layout_report_ads_button.xml`** — giao diện nút (ở đây là một `TextView` hình tròn chữ "i"):

```xml
<?xml version="1.0" encoding="utf-8"?>
<merge xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:focusable="true"
    android:focusableInTouchMode="true"
    android:background="@drawable/report_button_bg"
    tools:parentTag="androidx.constraintlayout.widget.ConstraintLayout">

    <TextView
        android:id="@+id/report_ads_button"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        android:text="i"
        android:textColor="#555"
        android:textSize="12sp"
        android:textAllCaps="false"
        android:gravity="center" />
</merge>
```

**`res/drawable/report_button_bg.xml`** — selector đổi màu theo trạng thái focus/pressed (quan trọng với remote TV):

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/report_button_selected" android:state_pressed="true" />
    <item android:drawable="@drawable/report_button_selected" android:state_focused="true" />
    <item android:drawable="@drawable/report_button_selected" android:state_active="true" />
    <item android:drawable="@drawable/repot_button_normal" />
</selector>
```

**`res/drawable/report_button_selected.xml`** — nền khi được focus (trắng đặc):

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="#FFFFFF" />
</shape>
```

**`res/drawable/repot_button_normal.xml`** — nền trạng thái thường (trắng mờ):

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="#B3FFFFFF" />
</shape>
```

---

## 2. Welcome Ads (kèm nút báo cáo)

Khởi tạo manager trong `onCreate`:

```kotlin
AdsWelcomeManager.getInstance().init(
    context = baseContext,
    tenantId = "14",
    deviceType = DeviceType.TV,
    environment = Environment.SANDBOX,
    // vastLoadTimeout, mediaLoadTimeout, bufferingVideoTimeout, bitrate, domainUrl, partnerSkipOffset
    debug = true
)
```

Đăng ký listener và gọi `requestAds`. Tham số `reportButtonId` trỏ tới id của nút báo cáo nằm trong layout TVC (`tvcLayoutId`):

```kotlin
val adsRequestData = WelcomeAdsRequestData.Builder()
    .transId("22222")
    .uid("123123123")            // định danh người dùng
    .userImpressionLimit(5)      // giới hạn số lần hiển thị (0 = không giới hạn)
    .segments("123,123,123")
    .build()

AdsWelcomeManager.getInstance().requestAds(
    activity = this,
    viewId = R.id.welcome_ad_view,
    tvcLayoutId = R.layout.wisdk_welcome_tvc_detail,
    tvcPlayerViewId = R.id.wisdk_exo_player_view,
    tvcSkipButtonId = R.id.wisdk_skip_button,
    skipLabel = "Bỏ qua quảng cáo",
    iconDrawable = R.drawable.skip_icon_button,
    adsRequestData = adsRequestData,
    reportButtonId = R.id.wisdk_report_button   // id của TV360ReportAdsButton trong layout TVC
)
```

Nhớ giải phóng tài nguyên khi kết thúc:

```kotlin
override fun onDestroy() {
    super.onDestroy()
    AdsWelcomeManager.getInstance().release()
}
```

---

## 3. InStream Ads (VAST/VMAP, kèm nút báo cáo)

Khởi tạo và đăng ký `WiAdsLoaderListener`:

```kotlin
InStreamManager.getInstance().init(
    requireContext(), "14", DeviceType.TV, Environment.SANDBOX,
    5, 10, 5, 2500, LevelLog.BODY, 8
)
```

Tạo `AdsRequestData` (đã dùng `uid` + `userImpressionLimit`) và gọi `requestAds` — truyền `reportButtonAds` là nút báo cáo tuỳ chỉnh:

```kotlin
val adsRequestData = AdsRequestData.Builder()
    .channelId("998989,222222")
    .streamId("7600")
    .transId("222222")
    .contentType(ContentType.SHORT_VOD)
    .title("Tiêu đề nội dung")
    .category("category 1, category 2")
    .keyword("keyword 1, keyword 2")
    .uid("123123123")            // định danh người dùng
    .userImpressionLimit(5)      // giới hạn số lần hiển thị (0 = không giới hạn)
    .segments("123,1,23")
    .build()

val adsMediaSource = InStreamManager.getInstance().requestAds(
    requestData = adsRequestData,
    mediaSource = mediaSource,
    adViewProvider = playerView!!,
    player = exoPlayer!!,
    mediaSourceFactory = defaultMediaSourceFactory,
    reportButtonAds = reportButton   // TV360ReportAdsButton
)

exoPlayer!!.addMediaSource(adsMediaSource)
exoPlayer!!.prepare()
exoPlayer!!.playWhenReady = true
```

**Quan trọng — Friendly Obstruction:** vì nút báo cáo phủ lên trên player, cần khai báo friendly obstruction để không ảnh hưởng đo lường hiển thị:

```kotlin
reportButton?.let {
    val reportButtonObstruction = InStreamManager.getInstance().createFriendlyObstruction(
        it, FriendlyObstructionPurpose.OTHER, "report button"
    )
    friendlyObstructionList.add(reportButtonObstruction)
}
```

---

## 4. Display Banner (kèm nút báo cáo)

Khởi tạo manager:

```kotlin
DisplayBannerManager.getInstance().init(
    context = context,
    tenantId = "14",
    environment = Environment.SANDBOX,
    loadTimeout = 10,
    debug = true
)
```

Đăng ký `BannerAdEventListener` — các callback nay có tham số `reportButton`; gọi `show()` / `hide()` để hiển thị nút báo cáo:

```kotlin
DisplayBannerManager.getInstance().addBannerListener(object : BannerAdEventListener {
    override fun onDisplayAds(positionId: String, adView: BannerAdView?, reportButton: ReportButtonAds?) { }
    override fun onNoAds(positionId: String, adView: BannerAdView?) { }
    override fun onAdsBannerDismiss(positionId: String, adView: BannerAdView?, reportButton: ReportButtonAds?) { }
    override fun onAdsBannerError(positionId: String, adView: BannerAdView?, reportButton: ReportButtonAds?) { }
    override fun onAdsBannerClick(positionId: String, clickThroughLink: String) { }
    override fun onShowReportButton(positionId: String, reportButton: ReportButtonAds?) {
        reportButton?.show(activity = activity)
    }
    override fun onHideReportButton(positionId: String, reportButton: ReportButtonAds?) {
        reportButton?.hide()
    }
})
```

Tạo request và gọi `requestAds` (truyền `reportButton`):

```kotlin
val bannerAdsRequestData = DisplayBannerAdsRequestData.Builder(
        adSize = adSize, bannerDisplayType = displayType
    )
    .channelId("998989")
    .streamId("999999")
    .title("Tiêu đề")
    .transId("1112222222")
    .uid("123123123")           // định danh người dùng
    .userImpressionLimit(5)     // giới hạn số lần hiển thị (0 = không giới hạn)
    .color("#ffffff00")
    .segments("a3,34,d3,d3")
    .positionId(positionId)
    .build()

DisplayBannerManager.getInstance().requestAds(
    activity = activity,
    bannerAdView = bannerAdView,
    reportButton = reportButton,
    adsRequestData = bannerAdsRequestData
)
```

---

## 5. Overlay Banner (kèm nút báo cáo)

Tương tự Display Banner nhưng dùng `OverlayBannerManager`. `requestAds` có thêm tham số `cacheTimeSec` (thời gian cache dữ liệu quảng cáo, giây):

```kotlin
OverlayBannerManager.getInstance().requestAds(
    activity = activity,
    bannerAdView = bannerAdView,
    adsRequestData = bannerAdsRequestData,   // có .uid(...) và .userImpressionLimit(...)
    cacheTimeSec = 0,
    reportButton = reportButton
)
```

Việc đăng ký listener và hiển thị nút báo cáo (`onShowReportButton` → `reportButton?.show(...)`, `onHideReportButton` → `reportButton?.hide()`) giống mục Display Banner.

---

## 6. Đặt nút báo cáo vào layout quảng cáo

Với mỗi loại quảng cáo, đối tác đặt view nút báo cáo tuỳ chỉnh vào layout tương ứng. Nút để `android:visibility="gone"` — SDK sẽ tự bật khi có quảng cáo (qua `reportButton?.show(...)`).

**Welcome Ads** — đặt trong layout TVC (`tvcLayoutId`, ví dụ `wisdk_welcome_tvc_detail.xml`). Nên gắn `nextFocusUp` từ nút Skip sang nút báo cáo để điều hướng bằng remote:

```xml
<com.example.sampleandroidtv.ui.TV360ReportAdsButton
    android:id="@+id/wisdk_report_button"
    android:layout_width="30dp"
    android:layout_height="30dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    android:layout_marginEnd="10dp"
    android:layout_marginTop="50dp"
    android:visibility="gone"
    android:gravity="center" />
```

**InStream Ads** — đặt trong layout màn hình player (ví dụ `playback_video_fragment.xml`), id ví dụ `instream_report_button`:

```xml
<com.example.sampleandroidtv.ui.TV360ReportAdsButton
    android:id="@+id/instream_report_button"
    android:layout_width="30dp"
    android:layout_height="30dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    android:layout_marginStart="10dp"
    android:layout_marginTop="10dp"
    android:visibility="gone"
    android:gravity="center" />
```

**Display Banner / Overlay Banner** — đặt cạnh view banner, căn theo `BannerAdView` (id ví dụ `banner_ad_display_view` / `banner_ad_overlay_view`):

```xml
<com.example.sampleandroidtv.ui.TV360ReportAdsButton
    android:id="@+id/display_banner_report_button"
    android:layout_width="30dp"
    android:layout_height="30dp"
    app:layout_constraintTop_toTopOf="@id/banner_ad_display_view"
    app:layout_constraintRight_toRightOf="@id/banner_ad_display_view"
    android:layout_marginEnd="10dp"
    android:layout_marginTop="10dp"
    android:visibility="gone"
    android:gravity="center" />
```

---

## 7. Màn hình báo cáo (khi bấm vào nút report)

Khi người dùng focus vào nút báo cáo và bấm OK trên remote, SDK **tự động mở** hộp thoại báo cáo `ReportDialogFragment`. **Toàn bộ giao diện màn hình này đã được đóng gói sẵn trong SDK** — đối tác không cần tự tạo layout cho màn hình báo cáo.

### Bố cục màn hình

```
┌─────────────────────────────────────────────┐
│ (nền mờ tối phủ toàn màn hình #CC000000)      │
│                          ┌──────────────────┐ │
│                          │ Bạn gặp vấn đề gì │ │  ← tiêu đề
│                          │ ──────────────── │ │
│                          │ ☐ Quảng cáo sản  │ │
│                          │   phẩm/dịch vụ   │ │  ← danh sách lý do
│                          │   hạn chế        │ │    (checkbox, chọn nhiều)
│                          │ ☐ Nội dung gây   │ │
│                          │   hại, bạo lực…  │ │
│                          │ ☐ Gây hiểu lầm,  │ │
│                          │   sai sự thật    │ │
│                          │ ──────────────── │ │
│                          │  [ Hủy ] [ Gửi ] │ │  ← footer 2 nút
│                          └──────────────────┘ │
└─────────────────────────────────────────────┘
```

- **Panel** rộng `300dp`, nền tối (`#2C2C2E`), neo sát mép **phải** màn hình; phần còn lại là lớp nền mờ đen.
- **Tiêu đề:** "Bạn gặp vấn đề gì".
- **Danh sách lý do:** dạng `VerticalGridView` với các item checkbox, **cho phép chọn nhiều lý do**. Ba lý do mặc định (enum `ReportItem` trong SDK):
  - Quảng cáo sản phẩm hoặc dịch vụ hạn chế
  - Quảng cáo chứa nội dung gây hại, bạo lực hoặc nguy hiểm
  - Quảng cáo gây hiểu lầm, sai sự thật
- **Footer:** nút **Hủy** và nút **Gửi**. Nút **Gửi bị vô hiệu hoá** cho tới khi người dùng chọn ít nhất một lý do.

### Nhận sự kiện qua ReportListener (tuỳ chọn)

Nếu muốn hiển thị thông báo sau khi gửi (ví dụ Toast cảm ơn), gắn `ReportListener` vào nút báo cáo:

```kotlin
reportButton.addListener(object : ReportListener {
    override fun onShowReportFragment() { /* mở màn hình báo cáo */ }
    override fun onDismissReportFragment() { /* đóng màn hình báo cáo */ }
    override fun onSentReport(message: String) {
        // message = nội dung cảm ơn mặc định (ReportUtils.REPORT_SUCCESS_MESSAGE)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
})
```

---

## Tham số userImpressionLimit (uil)

Bổ sung builder `.userImpressionLimit(...)` cho **tất cả** các model request: `WelcomeAdsRequestData`, `DisplayBannerAdsRequestData`, `AdsRequestData` (InStream).

- `userImpressionLimit`: giới hạn số lần hiển thị quảng cáo trên mỗi người dùng. `0` = không giới hạn.
- Giá trị `uid` và `uil` được gửi lên tất cả API quảng cáo dưới dạng query param `uid` và `uil`.

---

## Bảng tham số cấu hình

| Tham số | Kiểu | Mô tả |
|---|---|---|
| `tenantId` | String | Mã đối tác do Wiinvent cấp. |
| `deviceType` | DeviceType | Loại thiết bị: `TV`. |
| `environment` | Environment | Môi trường: `SANDBOX`, `PRODUCTION`, `VIETTEL_PRODUCTION`, `WIINVENT_PRODUCTION`. |
| `uid` | String | Định danh người dùng (unified id). Để `""` nếu không có. |
| `userImpressionLimit` | Int | Giới hạn số lần hiển thị / người dùng. `0` = không giới hạn. |
| `transId` | String | Mã giao dịch do đối tác sinh ra. |
| `channelId` | String | Danh sách id category của nội dung, cách nhau bằng dấu `,`. |
| `streamId` | String | Id nội dung. |
| `contentType` | ContentType | `FILM`, `VIDEO`, `TV`, `SHORT_VOD`. |
| `title` / `category` / `keyword` | String | Tiêu đề / danh sách category / từ khoá của nội dung. |
| `age` / `gender` | Int / Gender | Tuổi và giới tính người dùng (`MALE`, `FEMALE`, `OTHER`, `NONE`). |
| `segments` | String | Danh sách segment id của người dùng (từ backend đối tác), cách nhau bằng `,`. |
| `positionId` | String | Vị trí hiển thị banner (ví dụ `homepage1`). |
| `adSize` / `bannerDisplayType` | BannerAdSize / BannerDisplayType | Kích thước và loại hiển thị của banner. |
| `cacheTimeSec` | Long | (Overlay Banner) Thời gian cache dữ liệu quảng cáo, tính bằng giây. |

---

## Ghi chú phát hành

- Trước khi build release, version đã được cập nhật thành `1.1.24` trong `app-tv-2-16/build.gradle` (hàm `getVersionName`).
- Quy trình publish: `./publishmaven` (chạy `./gradlew publish` lên `maven.wiinvent.tv`).
