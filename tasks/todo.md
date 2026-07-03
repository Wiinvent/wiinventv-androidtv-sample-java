# TODO — SDK 1.1.24 migration

## Phase A — Core migration (build xanh chỉ khi cả A xong) ✅
- [x] T1 — `app/build.gradle`: bump `1.1.17` → `1.1.24` (+ thêm `com.google.android.material:material:1.6.1` — SDK bắt buộc cho hộp thoại report)
- [x] T2 — `ui/TV360ReportAdsButton.java` + `layout_report_ads_button.xml` + 3 drawable
- [x] T3 — InStream: `PlaybackVideoFragment.java` + `playback_video_fragment.xml`
- [x] T4 — Display Banner: `DisplayBannerActivity.java` + `DisplayBannerAdapter.java`
- [x] T5 — Overlay Banner: `OverlayBannerActivity.java` + `activity_overlays_banner.xml`
- [x] ✅ CHECKPOINT 1 — `./gradlew assembleDebug` PASS

## Phase B — Welcome Ads
> PHÁT HIỆN: Welcome Ads **đã tồn tại** trong `MainActivity` (chạy lúc mở app), không phải chưa có.
> Đã wire nút báo cáo vào integration sẵn có + `wisdk_welcome_tvc_detail.xml` (làm luôn ở Phase A để build xanh).
- [x] Welcome report button + `uil` trong `MainActivity` + layout TVC ✅
- [x] ~~Màn Welcome demo RIÊNG~~ — user quyết BỎ (trùng với MainActivity). Phase B xong.

## Phase C — Docs
- [ ] T7 — Viết lại `README.md` (Java, 1.1.24)
- [ ] ✅ CHECKPOINT 3 — review README
