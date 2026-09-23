# Wiinvent SDK Android TV v1.1.28-2.16 — Hướng dẫn tích hợp quảng cáo Pause

Tài liệu này mô tả phần tích hợp **quảng cáo Pause** trong SDK Wiinvent Android TV **1.1.28-2.16** để đối tác
hiển thị banner khi người dùng tạm dừng nội dung video.

Nếu đang tích hợp mới hoàn toàn, đọc kèm:

- [readme_1.1.24.md](readme_1.1.24.md): luồng tích hợp cơ bản, report button.
- [readme_1.1.25.md](readme_1.1.25.md): `userId`, info button, focus report/skip.
- [readme_1.1.26.md](readme_1.1.26.md): release ExoPlayer và fix focus.

Dependency:

```gradle
implementation 'tv.wiinvent:wiinvent-sdk-android-tv:1.1.28-2.16'
implementation 'com.google.android.exoplayer:exoplayer:2.19.1'
implementation 'com.google.android.exoplayer:extension-ima:2.19.1'
```

---

## Tóm tắt thay đổi

| # | Thay đổi | Loại | Ảnh hưởng tích hợp |
|---|---|---|---|
| 1 | Bổ sung quảng cáo **Pause Banner** cho màn hình player | Tính năng mới | Gọi banner khi player pause, đóng banner khi player resume |
| 2 | Hỗ trợ size `BannerDisplayAdSize.PAUSE_BANNER` | Tính năng mới | Dùng trong `DisplayBannerAdsRequestData.Builder(...)` |
| 3 | Hỗ trợ kiểu hiển thị `BannerDisplayType.OVERLAY` cho pause | Tính năng mới | Banner overlay lên player, không thay đổi layout nội dung |
| 4 | `OverlayBannerManager.releaseBanner(...)` nhận thêm `reportButton` và `infoButton` | Bổ sung | Khi ẩn pause ad nên truyền đủ 3 view để SDK clear banner và ẩn button |

---

## 1. Cơ chế hoạt động

Quảng cáo Pause là một dạng **Overlay Banner** hiển thị trong vùng player khi nội dung đang ở trạng thái
tạm dừng. App đối tác chủ động quyết định thời điểm gọi quảng cáo dựa trên trạng thái player:

- Khi player `STATE_READY` và `playWhenReady == false`: gọi `showPauseAd()`.
- Khi player `STATE_READY` và `playWhenReady == true`: gọi `dismissPauseAd()`.
- Khi rời màn hình player: gọi `OverlayBannerManager.getInstance().release()`.

SDK không tự pause/resume video. App chỉ dùng trạng thái player để request hoặc đóng banner.

---

## 2. Khởi tạo `OverlayBannerManager`

Khởi tạo một lần trong màn hình player, trước khi request pause ad:

```kotlin
OverlayBannerManager.getInstance().init(
    context = requireContext(),
    tenantId = "14",
    environment = Environment.PRODUCTION, // hoặc Environment.SANDBOX khi test
    loadTimeout = 10,
    debug = false
)

OverlayBannerManager.getInstance().addBannerListener(object : BannerAdEventListener {
    override fun onDisplayAds(
        positionId: String,
        adView: BannerAdView?,
        reportButton: ReportButtonAds?,
        infoButton: InfoButtonAds?
    ) {
        activity?.runOnUiThread {
            adView?.visibility = View.VISIBLE
        }
    }

    override fun onNoAds(positionId: String, adView: BannerAdView?) {
        // Không có quảng cáo pause: vẫn phải ẩn banner và reset cờ đã request,
        // nếu không lớp phủ rỗng sẽ nằm lại trên player.
        activity?.runOnUiThread {
            adView?.visibility = View.GONE
            OverlayBannerManager.getInstance().releaseBanner(adView, reportButton, infoButton)
        }
    }

    override fun onAdsBannerDismiss(
        positionId: String,
        adView: BannerAdView?,
        reportButton: ReportButtonAds?,
        infoButton: InfoButtonAds?
    ) {
        activity?.runOnUiThread {
            adView?.visibility = View.GONE
            OverlayBannerManager.getInstance().releaseBanner(adView, reportButton, infoButton)
        }
    }

    override fun onAdsBannerError(
        positionId: String,
        adView: BannerAdView?,
        reportButton: ReportButtonAds?,
        infoButton: InfoButtonAds?
    ) {
        activity?.runOnUiThread {
            adView?.visibility = View.GONE
            OverlayBannerManager.getInstance().releaseBanner(adView, reportButton, infoButton)
        }
    }

    override fun onAdsBannerClick(positionId: String, clickThroughLink: String) {
        // App có thể log hoặc điều hướng thêm nếu cần.
    }

    override fun onShowReportButton(
        positionId: String,
        reportButton: ReportButtonAds?,
        infoButton: InfoButtonAds?
    ) {
        reportButton?.show(activity = activity)
        infoButton?.show(activity = activity)
    }

    override fun onHideReportButton(
        positionId: String,
        reportButton: ReportButtonAds?,
        infoButton: InfoButtonAds?
    ) {
        reportButton?.hide()
        infoButton?.hide()
    }
})
```

---

## 3. Layout cho Pause Ad

Điểm dễ sai nhất của pause ad. Khi có quảng cáo, SDK **ghi đè `layoutParams` của `BannerAdView`**
thành match 4 cạnh của **parent**, rồi tự đặt creative bên trong theo loại banner backend trả về:

| `bannerAdSize` | Vị trí / kích thước creative (so với `BannerAdView`) |
|---|---|
| `PAUSE_LARGE_BANNER` | rộng 80%, bám đáy |
| `CENTER_BANNER` | rộng 40%, canh giữa |
| `TRANSPARENT_BANNER` | cao 70%, bám phải |

Chiều cao (hoặc rộng) còn lại suy ra theo `dimensionRatio` của creative. Hệ quả cho app đối tác:

- Mọi constraint đặt trên chính `BannerAdView` trong XML đều **bị bỏ** khi ads về. Không dùng
  `BannerAdView` để định vị vùng quảng cáo được.
- Phải bọc `BannerAdView` trong một `ConstraintLayout` **wrapper** và constraint wrapper vào đúng
  vùng player. SDK tự lo tỉ lệ và vị trí, wrapper chỉ cần phủ đúng vùng player.
- **Wrapper bắt buộc có kích thước xác định**: `layout_width="0dp"` + `layout_height="0dp"` +
  constraint đủ 4 cạnh. Dùng `wrap_content` sẽ tạo vòng lặp với con `0dp` mà SDK set, ConstraintLayout
  resolve ra chiều cao ~0 và **banner bị bẹp còn vài pixel**.

Hai điểm bắt buộc còn lại:

1. `ReportButtonAds` / `InfoButtonAds` phải là **con trực tiếp của `BannerAdView`** — SDK constraint
   hai nút này vào creative (`topToTop` / `endToEnd` của ảnh quảng cáo) khi runtime. Đặt ra ngoài
   `BannerAdView` thì constraint không resolve được và nút sẽ nằm sai vị trí.
2. Trong XML **không tự constraint** hai nút vào `BannerAdView` (kể cả bằng chính id của nó — đó là
   parent, ConstraintLayout không resolve id parent từ con). Chỉ khai báo kích thước và margin.

Để `BannerAdView` ở `visibility="gone"`, chỉ bật `VISIBLE` trong `onDisplayAds`. Nếu để `visible`
sẵn, một lớp phủ rỗng nằm trên player suốt thời gian phát.

Về `focusable` của wrapper, tuỳ màn hình:

- Màn hình chỉ có overlay/pause ad (có `PlayerControlView` để điều hướng): set `focusable`,
  `focusableInTouchMode`, `descendantFocusability="beforeDescendants"` cho wrapper để bắc cầu focus
  giữa control player và nút report — xem `fragment_overlay_banner.xml` trong sample.
- Màn hình player full-screen có **kèm quảng cáo InStream**: **không** set `focusable` cho wrapper.
  Một wrapper focusable phủ kín player sẽ ăn focus D-pad của nút skip/report InStream.

Ví dụ (player full-screen):

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/pause_banner_overlay_wrapper"
    android:layout_width="0dp"
    android:layout_height="0dp"
    app:layout_constraintTop_toTopOf="@id/simple_exo_player_view"
    app:layout_constraintBottom_toBottomOf="@id/simple_exo_player_view"
    app:layout_constraintStart_toStartOf="@id/simple_exo_player_view"
    app:layout_constraintEnd_toEndOf="@id/simple_exo_player_view">

    <tv.wiinvent.androidtv.ui.banner.BannerAdView
        android:id="@+id/pause_banner_view"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <com.partner.app.ui.PartnerReportAdsButton
            android:id="@+id/pause_report_button"
            android:layout_width="25dp"
            android:layout_height="25dp"
            android:layout_marginTop="3dp"
            android:layout_marginEnd="3dp"
            android:focusable="true"
            android:visibility="gone" />

        <com.partner.app.ui.PartnerInfoAdsButton
            android:id="@+id/pause_info_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:visibility="gone" />

    </tv.wiinvent.androidtv.ui.banner.BannerAdView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

> `PartnerReportAdsButton` kế thừa `ReportButtonAds`, `PartnerInfoAdsButton` kế thừa `InfoButtonAds`.
> Cách tạo hai button này giống tài liệu 1.1.24 và 1.1.25.

---

## 4. Request quảng cáo Pause

Dùng `DisplayBannerAdsRequestData` với:

- `adSize = BannerDisplayAdSize.PAUSE_BANNER`
- `bannerDisplayType = BannerDisplayType.OVERLAY`
- `channelId`, `streamId`, `title`, `category`, `contentType`: thông tin nội dung đang phát.
- `userId`: định danh người dùng phía đối tác.
- `positionId`: mã vị trí quảng cáo nếu backend cấu hình theo vị trí; có thể để rỗng nếu không dùng.
- `color("#ffffff00")`: nền trong suốt để banner overlay không che nền ngoài creative.

```kotlin
private fun showPauseAd() {
    val activity = activity ?: return

    val bannerAdView = activity.findViewById<BannerAdView>(R.id.pause_banner_view)
    val reportButton = activity.findViewById<PartnerReportAdsButton>(R.id.pause_report_button)
    val infoButton = activity.findViewById<PartnerInfoAdsButton>(R.id.pause_info_button)

    val requestData = DisplayBannerAdsRequestData.Builder(
        adSize = BannerDisplayAdSize.PAUSE_BANNER,
        bannerDisplayType = BannerDisplayType.OVERLAY
    )
        .channelId(currentChannelId)
        .streamId(currentStreamId)
        .contentType(ContentType.FILM)
        .title(currentTitle)
        .category(currentCategory)
        .transId(currentTransactionId)
        .userId(currentUserId)
        .userImpressionLimit(5)
        .segments(currentSegments)
        .positionId("pause")
        .color("#ffffff00")
        .adPendingTime(20)
        .build()

    OverlayBannerManager.getInstance().requestAds(
        activity = activity,
        bannerAdView = bannerAdView,
        adsRequestData = requestData,
        cacheTimeSec = 2,
        reportButton = reportButton,
        infoButton = infoButton
    )
}
```

`cacheTimeSec` là thời gian SDK được dùng lại dữ liệu quảng cáo overlay đã request gần nhất cho cùng
`channelId-streamId`. Nên đặt giá trị nhỏ cho pause ad để tránh gọi API quá dày khi người dùng pause/resume
liên tục.

---

## 5. Đóng quảng cáo Pause

Khi nội dung resume, hoặc khi fragment/activity bị huỷ, đóng banner bằng `releaseBanner`:

```kotlin
private fun dismissPauseAd() {
    val activity = activity ?: return

    val bannerAdView = activity.findViewById<BannerAdView>(R.id.pause_banner_view)
    val reportButton = activity.findViewById<PartnerReportAdsButton>(R.id.pause_report_button)
    val infoButton = activity.findViewById<PartnerInfoAdsButton>(R.id.pause_info_button)

    OverlayBannerManager.getInstance().releaseBanner(
        adView = bannerAdView,
        reportButton = reportButton,
        infoButton = infoButton
    )
}
```

Khi rời màn hình player:

```kotlin
override fun onDestroyView() {
    dismissPauseAd()
    OverlayBannerManager.getInstance().release()
    super.onDestroyView()
}
```

---

## 6. Bắt trạng thái pause/resume của ExoPlayer

Gắn listener vào player và chỉ hiển thị pause ad khi nội dung đã sẵn sàng (`STATE_READY`). Cách này tránh
gọi quảng cáo trong lúc player đang loading/buffering.

```kotlin
player.addListener(object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        handlePauseAdState()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        handlePauseAdState()
    }
})

private fun handlePauseAdState() {
    val player = player ?: return
    if (player.playbackState != Player.STATE_READY) return

    if (player.playWhenReady) {
        dismissPauseAd()
    } else {
        showPauseAd()
    }
}
```

Nếu app có trạng thái khác như mở dialog, seek, chuyển tập, hoặc rời màn hình, nên gọi `dismissPauseAd()`
để tránh banner còn hiển thị khi không còn ở trạng thái pause thật sự.

---

## 7. Điều hướng focus bằng remote TV

Nếu layout player có `PlayerControlView`, nên xử lý D-pad để người dùng di chuyển giữa control của player
và nút report của pause ad.

Ví dụ:

```kotlin
override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (event.action == KeyEvent.ACTION_DOWN && handlePauseAdKey(event.keyCode)) {
        return true
    }
    return super.dispatchKeyEvent(event)
}

private fun handlePauseAdKey(keyCode: Int): Boolean {
    val reportButton = findViewById<PartnerReportAdsButton>(R.id.pause_report_button)
    val playerControlView = findViewById<PlayerControlView>(R.id.player_control_view)
    val focused = currentFocus ?: return false

    if (keyCode == KeyEvent.KEYCODE_DPAD_UP &&
        isDescendantOf(focused, playerControlView) &&
        reportButton.visibility == View.VISIBLE
    ) {
        reportButton.requestFocusToReportButton()
        return true
    }

    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && reportButton.hasFocus()) {
        playerControlView.findViewById<View>(R.id.exo_play)?.requestFocus()
            ?: playerControlView.requestFocus()
        return true
    }

    return false
}
```

App có thể thay `KEYCODE_DPAD_UP/DOWN` bằng hướng điều hướng phù hợp với vị trí report button thực tế.

---

## 8. Lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách sửa |
|---|---|---|
| Banner bị **bẹp** còn vài pixel chiều cao | Wrapper để `wrap_content`, trong khi SDK set `BannerAdView` thành `0dp` (MATCH_CONSTRAINT) bám 4 cạnh parent → vòng lặp, resolve ra ~0 | Wrapper phải `layout_width="0dp"` + `layout_height="0dp"` + constraint đủ 4 cạnh |
| Banner **hẹp / lệch tâm**, không đúng tỉ lệ | Wrapper tự giới hạn bằng `layout_constraintWidth_percent` hoặc margin | Bỏ hết; SDK đã tự set % theo loại banner, wrapper chỉ cần phủ đúng vùng player |
| Banner **phủ gần kín** player | Wrapper phủ vùng lớn hơn player (hoặc constraint vào `parent` thay vì player) | Constraint wrapper vào đúng view player |
| Nút report/info **lệch vị trí** hoặc sai kích thước | Hai nút đặt ngoài `BannerAdView`; SDK constraint chúng vào id của **ảnh creative**, chỉ resolve được giữa sibling | Đưa hai nút vào làm **con trực tiếp** của `BannerAdView`, XML chỉ khai báo size + margin |
| Lớp phủ trong suốt che player khi **không có** quảng cáo | `BannerAdView` để `visibility="visible"`, hoặc `onNoAds` không ẩn banner | Để `gone` trong XML, bật VISIBLE ở `onDisplayAds`, gọi `releaseBanner` trong `onNoAds` |
| Remote không bấm được nút skip InStream | Wrapper `focusable` phủ kín player full-screen | Bỏ `focusable` trên wrapper ở màn hình có InStream (xem mục 3) |

---

## 9. Checklist tích hợp

- [ ] Cập nhật dependency SDK lên `1.1.28-2.16`.
- [ ] Khởi tạo `OverlayBannerManager` trong màn hình player.
- [ ] Bọc `BannerAdView` trong wrapper `ConstraintLayout` có kích thước xác định (`0dp` + đủ 4 cạnh),
      constraint vào đúng vùng player; không dùng `wrap_content` / `width_percent` cho wrapper.
- [ ] Đặt `ReportButtonAds`, `InfoButtonAds` làm **con trực tiếp** của `BannerAdView`, XML không tự
      constraint hai nút này.
- [ ] `BannerAdView` để `visibility="gone"`, chỉ bật VISIBLE trong `onDisplayAds`.
- [ ] Request pause ad với `BannerDisplayAdSize.PAUSE_BANNER` và `BannerDisplayType.OVERLAY`.
- [ ] Gọi `showPauseAd()` khi ExoPlayer `STATE_READY && !playWhenReady`.
- [ ] Gọi `dismissPauseAd()` khi ExoPlayer resume hoặc rời màn hình.
- [ ] Gọi `OverlayBannerManager.getInstance().release()` khi huỷ màn hình player.
- [ ] Kiểm tra focus bằng remote TV nếu có report button.
