# SPEC — Nâng cấp sample lên Wiinvent SDK Android TV 1.1.24

## 1. Mục tiêu

Áp dụng các thay đổi của SDK **1.1.24** (từ `docs/readme_1.1.24.md`) vào sample Android TV
(Java), rồi viết lại `README.md` gốc bằng Java cho 1.1.24.

Hai tính năng mới của SDK cần demo:
1. **Report Ads Button** — nút "báo cáo quảng cáo" dùng chung cho cả 4 loại quảng cáo.
2. **`userImpressionLimit` (`uil`)** — giới hạn số lần hiển thị/người dùng, thêm vào mọi request builder.

Đối tượng: đối tác copy pattern tích hợp → ưu tiên rõ ràng, code Java (SDK là Kotlin nên
dùng `Manager.Companion.getInstance()`). Comment/log giữ tiếng Việt.

**API 1.1.24 đã verify trực tiếp từ AAR** (không tin snippet Kotlin trong doc):
- `BannerAdEventListener` = 7 method: `onDisplayAds(pos, view, reportButton)`, `onNoAds(pos, view)`,
  `onAdsBannerDismiss(pos, view, reportButton)`, `onAdsBannerError(pos, view, reportButton)`,
  `onAdsBannerClick(pos, link)`, `onShowReportButton(pos, reportButton)`, `onHideReportButton(pos, reportButton)`.
- `DisplayBannerManager.requestAds(activity, bannerAdView, reportButton, requestData)` — reportButton là arg #3.
- `DisplayBannerManager.releaseBanner(view)` — 1 arg (giữ nguyên).
- `OverlayBannerManager.requestAds(activity, view, requestData, cacheTimeSec(long), reportButton)`.
- `OverlayBannerManager.releaseBanner(view, reportButton)` — **2 arg** (khác Display).
- `InStreamManager.requestAds(requestData, mediaSource, adViewProvider, player, mediaSourceFactory, reportButton)`.
- `InStreamManager.WiAdsLoaderListener` thêm `showReportButton(String)` / `hideReportButton(String)`.
- `AdsWelcomeManager.requestAds(activity, viewId, tvcLayoutId, tvcPlayerViewId, tvcSkipButtonId, skipLabel(String), iconDrawable(int), requestData, reportButtonId(int))`.
- `ReportButtonAds` (abstract ConstraintLayout): override `init()`, `setReportButton(View)`; runtime `show(Activity)` / `hide()` / `addListener(ReportListener)`.
- 3 builder (`WelcomeAdsRequestData`, `AdsRequestData`, `DisplayBannerAdsRequestData`) đều có
  `.uid(String)` và `.userImpressionLimit(Integer)` dạng fluent (giữ style hiện tại, không đổi constructor).

## 2. Lệnh

```bash
./gradlew assembleDebug     # tiêu chí PASS chính: build thành công với 1.1.24
./gradlew installDebug      # cài lên TV/emulator để chạy tay (tùy chọn)
```

## 3. Phạm vi thay đổi (file)

**Dependency**
- `app/build.gradle`: `wiinvent-sdk-android-tv:1.1.17` → `1.1.24`.

**Nút báo cáo (mới, dùng chung)**
- `ui/TV360ReportAdsButton.java` — kế thừa `ReportButtonAds`, y hệt pattern `TV360SkipAdsButtonAds`.
- `res/layout/layout_report_ads_button.xml`, `res/drawable/report_button_bg.xml`,
  `report_button_selected.xml`, `repot_button_normal.xml` (theo doc mục 1.1).

**InStream** — `fragment/PlaybackVideoFragment.java`
- Thêm field `TV360ReportAdsButton reportButton`; `requestAds(..., reportButton)`.
- `WiAdsLoaderListener`: thêm `showReportButton`/`hideReportButton` → `reportButton.show(activity)` / `hide()`.
- Đăng ký report button làm friendly obstruction (`FriendlyObstructionPurpose.OTHER`).
- `.uid("123123123").userImpressionLimit(5)` trong `AdsRequestData`.
- `res/layout/playback_video_fragment.xml`: thêm `TV360ReportAdsButton` (visibility gone).

**Display Banner** — `activity/DisplayBannerActivity.java` + `ui/DisplayBannerAdapter.java`
- Cập nhật listener sang 7-method; `requestAds(activity, view, reportButton, data)`.
- `onShow/onHideReportButton` → show/hide. `.userImpressionLimit(5)`.
- Report button tạo per-row trong `DisplayBannerAdapter` (song song với `BannerAdView`).

**Overlay Banner** — `activity/OverlayBannerActivity.java`
- Listener 7-method; `requestAds(activity, view, data, cacheTimeSec, reportButton)`;
  `releaseBanner(view, reportButton)` (2 arg). `.userImpressionLimit(5)`.
- `res/layout/activity_overlays_banner.xml`: thêm `TV360ReportAdsButton` neo theo banner view.

**Welcome Ads (MỚI — screen demo thứ 4)**
- `activity/WelcomeAdsActivity.java` — init `AdsWelcomeManager`, `addWelcomeListener`, `requestAds(...)`, `release()` trong onDestroy.
- `res/layout/activity_welcome_ads.xml` — container `welcome_ad_view`.
- `res/layout/wisdk_welcome_tvc_detail.xml` (đã tồn tại) — thêm `TV360ReportAdsButton id=wisdk_report_button`.
- `AndroidManifest.xml` — đăng ký `.activity.WelcomeAdsActivity`.
- `MainFragment.java` — thêm ô grid "Test Welcome Ads" + nhánh click mở activity.
- `res/values/strings.xml` — `test_welcome_ads`.
- *Ghi chú:* `WelcomeAdsEventListener` sẽ xác minh signature từ AAR khi implement (chưa javap).

**Tài liệu**
- `README.md` — **viết lại toàn bộ** cho 1.1.24, snippet Java, 4 loại quảng cáo + Report + `uil` + bảng tham số.
- `docs/readme_1.1.24.md` giữ nguyên (nguồn).

## 4. Code style
- Java 8, comment/log tiếng Việt như code hiện có. Manager qua `Companion.getInstance()`.
- Không thêm abstraction mới; nút báo cáo bám đúng pattern `TV360SkipAdsButtonAds` đã có.
- `userImpressionLimit(0)` = không giới hạn (theo doc); sample dùng `5` để demo.

## 5. Kiểm thử
- Không có unit test trong repo. Tiêu chí PASS = `./gradlew assembleDebug` build sạch với 1.1.24
  (link đúng mọi signature mới). Chạy tay `installDebug` để xác nhận 4 màn hình hiện nút báo cáo (tùy chọn).

## 6. Boundaries
- **Always**: verify signature từ AAR trước khi viết; giữ tiếng Việt trong sample; bump version + build.
- **Ask first**: đổi behavior ngoài phạm vi Report/`uil`; thêm dependency mới.
- **Never**: đụng vào logic không liên quan; publish/commit khi chưa được yêu cầu; sửa `docs/readme_1.1.24.md`.
