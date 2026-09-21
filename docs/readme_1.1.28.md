# Wiinvent SDK Android TV v1.1.28 — Hướng dẫn tích hợp quảng cáo Pause

Tài liệu này mô tả phần tích hợp **quảng cáo Pause** trong SDK Wiinvent Android TV **1.1.28** để đối tác
hiển thị banner khi người dùng tạm dừng nội dung video.

Nếu đang tích hợp mới hoàn toàn, đọc kèm:

- [readme_1.1.24.md](readme_1.1.24.md): luồng tích hợp cơ bản, report button.
- [readme_1.1.25.md](readme_1.1.25.md): `userId`, info button, focus report/skip.
- [readme_1.1.26.md](readme_1.1.26.md): release ExoPlayer và fix focus.

Dependency:

```gradle
implementation 'tv.wiinvent:wiinvent-sdk-android-tv:1.1.28'
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
        // Không có quảng cáo pause để hiển thị.
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

Đặt `BannerAdView` overlay cùng vùng player. Nếu dùng report/info button, đặt các view này trong cùng
container để SDK có thể đưa button lên trên banner.

Ví dụ:

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/banner_overlay_wrapper"
    android:layout_width="0dp"
    android:layout_height="0dp"
    app:layout_constraintTop_toTopOf="@id/video_frame"
    app:layout_constraintBottom_toBottomOf="@id/video_frame"
    app:layout_constraintStart_toStartOf="@id/video_frame"
    app:layout_constraintEnd_toEndOf="@id/video_frame">

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
            android:visibility="gone"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

        <com.partner.app.ui.PartnerInfoAdsButton
            android:id="@+id/pause_info_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:visibility="gone"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent" />

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

## 8. Checklist tích hợp

- [ ] Cập nhật dependency SDK lên `1.1.28`.
- [ ] Khởi tạo `OverlayBannerManager` trong màn hình player.
- [ ] Thêm `BannerAdView`, `ReportButtonAds`, `InfoButtonAds` vào vùng overlay của player.
- [ ] Request pause ad với `BannerDisplayAdSize.PAUSE_BANNER` và `BannerDisplayType.OVERLAY`.
- [ ] Gọi `showPauseAd()` khi ExoPlayer `STATE_READY && !playWhenReady`.
- [ ] Gọi `dismissPauseAd()` khi ExoPlayer resume hoặc rời màn hình.
- [ ] Gọi `OverlayBannerManager.getInstance().release()` khi huỷ màn hình player.
- [ ] Kiểm tra focus bằng remote TV nếu có report button.
