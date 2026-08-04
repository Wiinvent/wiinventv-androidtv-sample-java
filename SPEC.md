# SPEC — Nâng cấp sample lên Wiinvent SDK Android TV 1.1.26

Nguồn: `docs/readme_1.1.26.md`. Nền: sample đã ở 1.1.25 (Java, 25 file). 1.1.26 **không đổi API công khai** — chỉ 2 bug fix.

## Mục tiêu
Áp dụng 2 fix của 1.1.26 vào sample. Không có class/method mới → không cần javap.

1. **(Bắt buộc) Fix đứng hình InStream trên TV box** — release ExoPlayer + `InStreamManager.release()` trong `onDestroy`.
   Nguyên nhân: player không release → giữ hardware video decoder → lần phát sau đứng hình (Amlogic…).
2. **Fix focus nút report khi nút skip đang đếm ngược** — InStream thêm cờ `isSkipButtonReady`. Welcome tự động (SDK lo, không sửa).

## Lệnh
```bash
./gradlew assembleDebug     # PASS = build sạch với 1.1.26
./gradlew installDebug      # chạy tay (tùy chọn)
```

## Phạm vi thay đổi (chỉ 2 file)

**Dependency** — `app/build.gradle`: `1.1.25` → `1.1.26`.

**InStream** — `fragment/PlaybackVideoFragment.java` (màn duy nhất bị ảnh hưởng):
- **Mục 1:** thêm `onDestroy()`:
  ```java
  @Override public void onDestroy() {
    super.onDestroy();
    InStreamManager.Companion.getInstance().release();
    if (exoPlayer != null) { exoPlayer.release(); exoPlayer = null; } // BẮT BUỘC: nhả hardware decoder
  }
  ```
- **Mục 2.2:** thêm field `boolean isSkipButtonReady`:
  - `showSkipButton`: set `false` trước `startCountdown`; trong `onRequestFocus` set `true`.
  - `hideSkipButton`: set `false`.
  - `onDpadKey` nhánh DOWN/RIGHT (từ report về skip): `if (isSkipButtonReady) skipButton.requestFocusToSkip(); else InStreamManager...focusSkipButton();`

**Không đụng:** Welcome (`MainActivity` — SDK tự bắc cầu focus 1.1.26), Display/Overlay banner (không liên quan fix này).

## Code style
Java 8, comment/log tiếng Việt. Manager qua `Companion.getInstance()`. Diff tối thiểu — chỉ đúng 2 fix.

## Kiểm thử
Không có unit test. PASS = `./gradlew assembleDebug` build sạch với 1.1.26. Kiểm đứng hình/focus là thủ công trên TV box (tùy chọn).

## Boundaries
- **Always**: giữ tiếng Việt; bump version + build; chỉ làm đúng 2 fix trong doc.
- **Ask first**: đụng player release ở màn khác (Overlay) — ngoài phạm vi doc; thêm option phòng thủ (decoderFallback, texture_view).
- **Never**: đổi API/behavior ngoài 2 fix; sửa `docs/readme_1.1.26.md`; commit/publish khi chưa được yêu cầu.
