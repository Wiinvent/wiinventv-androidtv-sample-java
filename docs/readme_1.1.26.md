# Wiinvent SDK Android TV v1.1.26 — Thay đổi so với 1.1.25

Bản 1.1.26 **không đổi API công khai** so với 1.1.25 — chỉ là **sửa lỗi** và **bổ sung hướng dẫn tích hợp**.
Nếu đang tích hợp từ 1.1.24, đọc trước [readme_1.1.25.md](readme_1.1.25.md) (userId, report button, info button,
focus report trên TV…).

Dependency:

```gradle
implementation 'tv.wiinvent:wiinvent-sdk-android-tv:1.1.26'
implementation 'com.google.android.exoplayer:exoplayer:2.19.1'
implementation 'com.google.android.exoplayer:extension-ima:2.19.1'
```

---

## Tóm tắt thay đổi

| # | Thay đổi | Loại | Ảnh hưởng tích hợp |
|---|---|---|---|
| 1 | Sửa lỗi **đứng hình quảng cáo InStream** trên một số TV box | 🐞 Fix | Đối tác **phải release ExoPlayer** đúng vòng đời (xem mục 1) |
| 2 | Sửa **điều hướng focus nút report khi nút skip đang đếm ngược** | 🐞 Fix | InStream cần theo dõi trạng thái nút skip (xem mục 2) |

---

## 1. ⚠️ Bắt buộc release ExoPlayer đúng vòng đời (fix đứng hình InStream)

**Nguyên nhân gốc:** khi màn hình player bị huỷ mà **không release ExoPlayer**, player cũ vẫn **giữ hardware
video decoder** của thiết bị (tài nguyên có giới hạn). Lần phát sau không xin được decoder → trên một số
TV box (ví dụ **Amlogic**) hardware decoder lỗi và **đứng hình**
(`OMX.amlogic.avc.decoder.awesome ... ERROR`, `MediaCodecVideoRenderer error`, `ExoPlaybackException`).

**Cách xử lý:** luôn release player (và gọi `InStreamManager.release()`) trong `onDestroy`:

```kotlin
override fun onDestroy() {
    super.onDestroy()
    InStreamManager.getInstance().release()
    exoPlayer?.release()      // BẮT BUỘC: nhả hardware decoder
    exoPlayer = null
}
```

> Đây là nguyên nhân phổ biến nhất của lỗi "đứng hình quảng cáo" trên TV box. Chỉ cần release player
> đúng chỗ là hết — **không cần** đổi decoder hay cấu hình player đặc biệt.

*(Các tuỳ chọn phòng thủ khác — `setEnableDecoderFallback(true)`, `surface_type="texture_view"`,
bắt `onPlayerError` — vẫn dùng được nếu box cụ thể còn lỗi, nhưng không phải là cách fix chính.)*

---

## 2. Sửa điều hướng focus nút report khi nút skip đang đếm ngược

Ở 1.1.25, khi **nút skip đang đếm ngược** (chưa bấm được, `isEnabled = false`) thì D-pad không đưa được
focus sang nút report. Bản 1.1.26 sửa lại cho cả Welcome và InStream.

### 2.1. Welcome Ads — tự động (không cần làm gì thêm)

Đã sửa trong SDK: `AdsWelcomeManager.getInstance().dispatchKeyEvent(keyCode)` nay bắc cầu focus với **mọi
loại nút skip** (IMA hoặc custom), **kể cả lúc đang đếm ngược**. Đối tác giữ nguyên cách wire như 1.1.25:

```kotlin
override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (event.action == KeyEvent.ACTION_DOWN &&
        AdsWelcomeManager.getInstance().dispatchKeyEvent(event.keyCode)) {
        return true
    }
    return super.dispatchKeyEvent(event)
}
```

### 2.2. InStream Ads — theo dõi trạng thái nút skip

App tự sở hữu nút skip nên tự phân biệt "skip đã bấm được" hay "đang đếm ngược" để trả focus đúng chỗ.
Dùng một cờ `isSkipButtonReady`:

```kotlin
private var isSkipButtonReady = false

// trong WiAdsLoaderListener:
override fun showSkipButton(campaignId: String, duration: Int) {
    isSkipButtonReady = false     // bắt đầu đếm ngược -> skip custom chưa bấm được
    skipButton?.startCountdown(duration, object : SkipAdsButtonAds.WiSkipButtonListener {
        override fun onRequestFocus() {
            isSkipButtonReady = true   // đếm ngược xong -> đã bấm được
            skipButton?.requestFocusToSkip()
        }
    })
}
override fun hideSkipButton(campaignId: String) {
    isSkipButtonReady = false
    skipButton?.hide()
}
```

Trong hàm xử lý D-pad (gọi từ `Activity.dispatchKeyEvent`):

```kotlin
fun onDpadKey(keyCode: Int): Boolean {
    if (!isInStreamAdPlaying) return false
    val report = reportButton ?: return false
    return when (keyCode) {
        // skip (IMA/custom, kể cả đang đếm ngược) -> report: luôn chọn được report
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_LEFT -> {
            if (!report.hasFocus()) { report.requestFocusToReportButton(); true } else false
        }
        // report -> skip
        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT -> {
            if (report.hasFocus()) {
                if (isSkipButtonReady) skipButton?.requestFocusToSkip()   // skip custom đã bấm được
                else InStreamManager.getInstance().focusSkipButton()      // IMA (hoặc đang đếm ngược -> giữ report)
                true
            } else false
        }
        else -> false
    }
}
```

---

## Checklist nâng cấp nhanh từ 1.1.25

- [ ] (Bắt buộc) Release `exoPlayer` + `InStreamManager.release()` trong `onDestroy` (mục 1).
- [ ] Kiểm tra lại focus nút report khi nút skip đang đếm ngược: Welcome tự động; InStream thêm cờ `isSkipButtonReady` (mục 2).
- [ ] Không có thay đổi API — giữ nguyên phần tích hợp còn lại như 1.1.25.
