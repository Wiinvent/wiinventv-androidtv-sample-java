# Wiinvent SDK Android TV v1.1.25 — Thay đổi so với 1.1.24 (Hướng dẫn nâng cấp)

Tài liệu này tổng hợp **các thay đổi từ bản 1.1.24** để đối tác cập nhật tích hợp. Nếu đang tích hợp
mới hoàn toàn, đọc kèm [readme_1.1.24.md](readme_1.1.24.md) (report button, luồng cơ bản từng loại quảng cáo).

Dependency:

```gradle
implementation 'tv.wiinvent:wiinvent-sdk-android-tv:1.1.25'
implementation 'com.google.android.exoplayer:exoplayer:2.19.1'
implementation 'com.google.android.exoplayer:extension-ima:2.19.1'
```

---

## Tóm tắt thay đổi

| # | Thay đổi | Loại | Ảnh hưởng tích hợp |
|---|---|---|---|
| 1 | Đổi builder `uid` / `uid20` → **`userId`** | ⚠️ Breaking | Phải đổi `.uid(...)`/`.uid20(...)` → `.userId(...)` |
| 2 | `BannerAdEventListener` thêm tham số **`infoButton`** trong các callback | ⚠️ Breaking | Cập nhật lại các hàm override |
| 3 | Nút report **không tự động focus** khi hiện | ⚠️ Đổi hành vi | Tự gọi focus nếu vẫn muốn focus |
| 4 | Thêm **Info Button** (view đánh dấu quảng cáo) | Tính năng mới | Tuỳ chọn (`infoButtonId`, mặc định tắt) |
| 5 | Thêm tham số **`adPendingTime`** cho request | Tính năng mới | Tuỳ chọn |
| 6 | API xử lý **focus report/skip trên remote TV** | Tính năng mới | Nên wire cho quảng cáo VAST (Welcome/InStream) |
---

## 1. ⚠️ Đổi `uid` / `uid20` → `userId`

Tất cả model request đổi builder sang `.userId(...)`. Áp dụng cho `WelcomeAdsRequestData`,
`DisplayBannerAdsRequestData`, `AdsRequestData` (InStream).

```kotlin
// 1.1.24
.uid("123123123")       // banner / welcome
.uid20("123123123")     // instream (AdsRequestData)

// 1.1.25
.userId("123123123")    // dùng chung cho mọi loại
```

Giá trị vẫn được gửi lên server dưới query param `uid` như cũ — **không đổi phía backend**.

---

## 2. ⚠️ `BannerAdEventListener` bổ sung infoButton

Các callback nay có thêm tham số `infoButton: InfoButtonAds?`. Cập nhật lại phần override (Display &
Overlay Banner):

```kotlin
DisplayBannerManager.getInstance().addBannerListener(object : BannerAdEventListener {
    override fun onDisplayAds(positionId: String, adView: BannerAdView?, reportButton: ReportButtonAds?, infoButton: InfoButtonAds?) {
        adView?.visibility = View.VISIBLE
    }
    override fun onNoAds(positionId: String, adView: BannerAdView?) { }
    override fun onAdsBannerDismiss(positionId: String, adView: BannerAdView?, reportButton: ReportButtonAds?, infoButton: InfoButtonAds?) { }
    override fun onAdsBannerError(positionId: String, adView: BannerAdView?, reportButton: ReportButtonAds?, infoButton: InfoButtonAds?) { }
    override fun onAdsBannerClick(positionId: String, clickThroughLink: String) { }

    override fun onShowReportButton(positionId: String, reportButton: ReportButtonAds?, infoButton: InfoButtonAds?) {
        reportButton?.show(activity = activity)
        infoButton?.show(activity = activity)   // bỏ dòng này nếu không dùng info button
    }
    override fun onHideReportButton(positionId: String, reportButton: ReportButtonAds?, infoButton: InfoButtonAds?) {
        reportButton?.hide()
        infoButton?.hide()
    }
})
```

---

## 3. ⚠️ Nút report không tự động focus

Trước đây `ReportButtonAds.show()` tự gọi `requestFocus()`, khiến nút report **lấy focus** ngay khi
hiện. Từ 1.1.25, nút report hiện lên nhưng **không tự lấy focus** — focus giữ ở nút skip.

Nếu vẫn muốn ép focus vào nút report (chủ động), gọi:

```kotlin
reportButton.requestFocusToReportButton()
// hoặc, với InStream:
InStreamManager.getInstance().focusReportButton()
```

---

## 4. Info Button — hiển thị thẻ quảng cáo

Info button là một view để đánh dấu "đây là quảng cáo". Đây là tính năng
tuỳ chọn: nếu **không truyền** `infoButtonId` (mặc định `-1`) hoặc không gọi `infoButton?.show()` thì
view này sẽ không xuất hiện.

Tạo lớp kế thừa `InfoButtonAds` (giống cách tạo `ReportButtonAds`), override `init(...)` để inflate
layout của mình:

```kotlin
class TV360InfoAdsButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : InfoButtonAds(context, attrs, defStyleAttr) {
    var infoButton: View? = null
    override fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        inflate(context, R.layout.layout_info_ads_button, this)
        infoButton = findViewById(R.id.info_ads_button)
    }
}
```

Cách dùng theo từng loại:
- **Welcome / InStream**: truyền thêm `infoButtonId` (Welcome) hoặc `infoButtonAds` (InStream) khi gọi `requestAds`.
- **Banner**: hiển thị qua `infoButton?.show(...)` trong callback `onShowReportButton`.

> Nếu **không muốn có view đánh dấu quảng cáo** (ví dụ ở Welcome): chỉ cần không truyền `infoButtonId`
> (để mặc định `-1`) và không đặt view info button trong layout.

---

## 5. Tham số `adPendingTime` (tuỳ chọn)

Tất cả builder request (`WelcomeAdsRequestData`, `DisplayBannerAdsRequestData`, `AdsRequestData`) có
thêm `.adPendingTime(Int?)`.

```kotlin
AdsRequestData.Builder()
    // ...
    .userId("123123123")
    .userImpressionLimit(5)
    .adPendingTime(20)     // MỚi
    .build()
```

---

## 6. Xử lý focus nút report trên remote TV

Với quảng cáo VAST (Welcome & InStream), nút **skip** do Google IMA vẽ nằm trong cây view riêng của IMA
và tự giữ focus, nên D-pad **không tự chuyển sang nút report** của app. SDK 1.1.25 cung cấp API bắc cầu
focus; đối tác wire vào `dispatchKeyEvent` của activity host quảng cáo.

### 6.1. Welcome Ads

SDK tự quản lý UI welcome, nên chỉ cần forward phím:

```kotlin
// Trong activity hiển thị welcome ad
override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (event.action == KeyEvent.ACTION_DOWN &&
        AdsWelcomeManager.getInstance().dispatchKeyEvent(event.keyCode)) {
        return true
    }
    return super.dispatchKeyEvent(event)
}
```

SDK sẽ tự xử lý: từ nút skip IMA nhấn **UP/LEFT** → focus sang report; từ report nhấn **DOWN/RIGHT** →
trả focus về skip IMA. Chỉ áp dụng khi đang dùng nút skip native của IMA.

### 6.2. InStream Ads

Khác với Welcome, app **tự quản lý** nút report/skip của InStream nên có thể viết logic bắc cầu, dùng phương thức mới
của SDK: `InStreamManager.getInstance().focusSkipButton()` (đưa focus về nút skip IMA) và
`focusReportButton()` (đưa focus sang nút report).

Dưới đây là cách sample xử lý — đối tác làm tương tự.

**Bước 1 — Fragment chứa player: cờ trạng thái ad + hàm xử lý phím**

```kotlin
class PlaybackVideoFragment : Fragment() {

  private var reportButton: TV360ReportAdsButton? = null
  private var isInStreamAdPlaying = false      // cờ: đang phát ad InStream hay không

  // ... findViewById(reportButton) trong onViewCreated ...

  /**
   * Bắc cầu focus giữa nút skip native của IMA và nút report khi điều khiển bằng remote TV.
   * Activity gọi hàm này trong dispatchKeyEvent. Trả về true nếu đã xử lý (nuốt phím).
   */
  fun onDpadKey(keyCode: Int): Boolean {
    if (!isInStreamAdPlaying) return false
    val report = reportButton ?: return false
    return when (keyCode) {
      // Từ nút skip IMA -> chuyển focus sang nút report
      KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_LEFT -> {
        if (!report.hasFocus()) {
          report.requestFocusToReportButton()
          true
        } else false
      }
      // Từ nút report -> trả focus về nút skip của IMA
      KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT -> {
        if (report.hasFocus()) {
          InStreamManager.getInstance().focusSkipButton()
          true
        } else false
      }
      else -> false
    }
  }
}
```

**Bước 2 — Bật/tắt cờ `isInStreamAdPlaying` trong `WiAdsLoaderListener`**

```kotlin
InStreamManager.getInstance().setLoaderListener(object : InStreamManager.WiAdsLoaderListener {
    // ... các callback khác ...

    override fun showReportButton(campaignId: String) {
        isInStreamAdPlaying = true
        reportButton?.show(activity)
    }

    override fun hideReportButton(campaignId: String) {
        isInStreamAdPlaying = false
        reportButton?.hide()
    }
})
```

**Bước 3 — Activity host player: forward phím xuống Fragment**

```kotlin
class PlaybackActivity : FragmentActivity() {
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val fragment = supportFragmentManager.findFragmentById(android.R.id.content)
                as? PlaybackVideoFragment
            if (fragment?.onDpadKey(event.keyCode) == true) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
```

**Cơ chế:** `dispatchKeyEvent` ở Activity chạy **trước** khi IMA nhận phím, nên dù nút skip của IMA đang
giữ focus, việc nhấn UP/LEFT vẫn được app chặn lại và ép focus sang nút report; chiều ngược lại dùng
`focusSkipButton()` của IMA.

> Với nút skip **custom** của app (ad không skippable / ad ngắn), focus điều hướng bình thường bằng
> `nextFocusXxx` trong layout — không cần bắc cầu.

### 6.3. Display Banner & Overlay Banner

Với banner, `BannerAdView` do SDK cung cấp **không nhận focus** (`isFocusable = false`), nên D-pad không tự
dừng ở banner và cũng không tới được nút report. Cách xử lý: dùng **một container bao ngoài làm điểm
focus** cho banner, rồi wire `nextFocus` giữa container và nút report.

**Cách A — Banner đặt bằng XML (như Overlay Banner):**

```xml
<!-- Container bao banner: đây là điểm dừng focus đại diện cho banner -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/banner_overlay_wrapper"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:focusable="true"
    android:focusableInTouchMode="true"
    android:descendantFocusability="beforeDescendants"
    android:nextFocusRight="@id/overlay_banner_report_button">

    <tv.wiinvent.androidtv.ui.banner.BannerAdView
        android:id="@+id/banner_ad_overlay_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:duplicateParentState="true" />   <!-- sáng theo focus của container -->

    <com.example.sampleandroidtv.ui.TV360ReportAdsButton
        android:id="@+id/overlay_banner_report_button"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:focusable="true"
        android:nextFocusLeft="@id/banner_overlay_wrapper"
        android:visibility="gone"
        ... />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Cách B — Banner tạo bằng code (như Display Banner nhiều item trong RecyclerView):**

```kotlin
// ctlBanner là container (ConstraintLayout) bao BannerAdView + reportButton
bannerAdView.isFocusable = false                 // để container xử lý focus
bannerAdView.isDuplicateParentStateEnabled = true

ctlBanner.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
ctlBanner.nextFocusRightId = reportButton.id     // banner -> report (D-pad phải)
reportButton.nextFocusLeftId = ctlBanner.id      // report -> banner (D-pad trái)
```

**Nguyên tắc chung:**
- `BannerAdView` để `isFocusable = false`; dùng container bao ngoài làm điểm focus.
- `descendantFocusability = beforeDescendants` để container nhận focus trước, nút report chỉ tới qua `nextFocus`.
- Wire `nextFocusRight` (container → report) và `nextFocusLeft` (report → container). Có thể đổi cặp phím tuỳ bố cục.
- `duplicateParentState = true` trên `BannerAdView` để banner hiển thị trạng thái focus (highlight) theo container.

---

## Bảng tham số bổ sung so với 1.1.24

| Tham số | Kiểu | Mô tả |
|---|---|---|
| `userId` | String | Định danh người dùng (thay cho `uid`/`uid20`). Gửi lên query param `uid`. |
| `adPendingTime` | Int? | Thời gian chờ quảng cáo; gửi lên query param `apt`. Bỏ qua nếu không dùng. |
| `infoButtonId` (Welcome) | Int | Id của info button trong layout TVC. `-1` = không hiển thị (mặc định). |
| `infoButtonAds` (InStream) | InfoButtonAds? | View info button; `null` = không hiển thị. |

---

## Checklist nâng cấp nhanh từ 1.1.24

- [ ] Đổi tất cả `.uid(...)` / `.uid20(...)` → `.userId(...)`.
- [ ] Cập nhật chữ ký các callback của `BannerAdEventListener` (thêm `infoButton`).
- [ ] Kiểm tra lại focus: nút report không còn tự focus — wire `dispatchKeyEvent` cho Welcome/InStream nếu dùng VAST (mục 6.1, 6.2).
- [ ] Wire focus D-pad cho Display/Overlay Banner: container bao banner focusable + `nextFocus` tới nút report (mục 6.3).
- [ ] (Tuỳ chọn) Thêm Info Button nếu muốn view đánh dấu quảng cáo; hoặc bỏ qua để không hiển thị.
- [ ] (Tuỳ chọn) Truyền `adPendingTime`.
