# PLAN — Migrate sample lên SDK 1.1.24 (ponytail)

Nguồn: [SPEC.md](../SPEC.md). API đã verify từ AAR 1.1.24.

## Nguyên tắc lazy
- Không thêm abstraction. Nút báo cáo bám đúng pattern `TV360SkipAdsButtonAds` đã có.
- Tạo report button per-row **bằng code** trong `DisplayBannerAdapter` (giống `BannerAdView` hiện tại) — không đụng XML item.
- Không có test framework trong repo → tiêu chí = `./gradlew assembleDebug` build sạch.

## Dependency graph
```
T1 (bump 1.1.24) ──> mọi thứ (đổi signature = vỡ compile toàn bộ)
T2 (TV360ReportAdsButton + resource) ──> T3, T4, T5, T6
T3 InStream ─┐
T4 Display  ─┼─> CHECKPOINT 1 (build sạch, 3 màn cũ đã migrate)
T5 Overlay  ─┘
T6 Welcome Ads (additive, mới) ──> CHECKPOINT 2
T7 README.md (viết lại, phản ánh code cuối) ──> CHECKPOINT 3
```
T1+T2 phải land cùng T3/T4/T5 mới có build đầu tiên (không thể build trung gian sau 1 màn vì signature đổi đồng loạt).

## Phase A — Core migration (land cùng nhau)
Slice dọc, nhưng compile chỉ xanh khi cả A xong.

**T1 — Bump version**
- `app/build.gradle`: `1.1.17` → `1.1.24`.

**T2 — Nút báo cáo dùng chung**
- `ui/TV360ReportAdsButton.java`: extends `ReportButtonAds`, override `init()` → inflate `layout_report_ads_button`, `setReportButton(findViewById(R.id.report_ads_button))`.
- `res/layout/layout_report_ads_button.xml`, `res/drawable/report_button_bg.xml`, `report_button_selected.xml`, `repot_button_normal.xml` (theo doc mục 1.1, giữ nguyên tên file kể cả typo `repot_`).

**T3 — InStream** `fragment/PlaybackVideoFragment.java` + `res/layout/playback_video_fragment.xml`
- Layout: thêm `TV360ReportAdsButton` id `instream_report_button`, visibility gone.
- Field + `findViewById` trong `onViewCreated`.
- `AdsRequestData`: thêm `.uid("123123123").userImpressionLimit(5)`.
- `requestAds(..., reportButton)` (arg thứ 6).
- Friendly obstruction cho report button (`FriendlyObstructionPurpose.OTHER`).
- `WiAdsLoaderListener`: thêm `showReportButton(id)` → `reportButton.show(getActivity())`; `hideReportButton(id)` → `reportButton.hide()`.

**T4 — Display Banner** `activity/DisplayBannerActivity.java` + `ui/DisplayBannerAdapter.java`
- Listener `DisplayBannerActivity`: sửa sang 7-method (thêm `reportButton` param ở onDisplay/onDismiss/onError; thêm `onShowReportButton`→`show`, `onHideReportButton`→`hide`).
- `DisplayBannerAdapter`: tạo `TV360ReportAdsButton` per-row (add vào `ctlBanner` như `bannerAdView`); `requestAds(activity, view, reportButton, data)`; `.userImpressionLimit(5)`.

**T5 — Overlay Banner** `activity/OverlayBannerActivity.java` + `res/layout/activity_overlays_banner.xml`
- Layout: thêm `TV360ReportAdsButton` id `overlay_report_button` neo theo `banner_ad_overlay_view`.
- Listener 7-method; field report button.
- `requestAds(activity, view, data, cacheTimeSec, reportButton)`; `releaseBanner(view, reportButton)` (**2 arg**); `.userImpressionLimit(5)`.

### ✅ CHECKPOINT 1
`./gradlew assembleDebug` build sạch. 3 màn cũ đã có nút báo cáo + `uil`. **Dừng, xác nhận trước khi sang B.**

## Phase B — Welcome Ads (mới, additive)
**T6** — `activity/WelcomeAdsActivity.java`, `res/layout/activity_welcome_ads.xml`,
sửa `res/layout/wisdk_welcome_tvc_detail.xml` (thêm `TV360ReportAdsButton` id `wisdk_report_button`),
`AndroidManifest.xml` (đăng ký activity), `MainFragment.java` (ô grid + nhánh click),
`res/values/strings.xml` (`test_welcome_ads`).
- `AdsWelcomeManager.init(...)` → `addWelcomeListener(new WelcomeAdsEventListener{4 method})` → `requestAds(activity, R.id.welcome_ad_view, R.layout.wisdk_welcome_tvc_detail, R.id.wisdk_exo_player_view, R.id.wisdk_skip_button, "Bỏ qua quảng cáo", R.drawable.skip_icon_button, data, R.id.wisdk_report_button)` → `release()` trong onDestroy.

### ✅ CHECKPOINT 2
Build sạch, grid hiện "Test Welcome Ads" mở được màn mới. **Xác nhận trước khi sang C.**

## Phase C — Docs
**T7** — Viết lại `README.md` bằng Java cho 1.1.24: 4 loại quảng cáo + Report button + `userImpressionLimit` + bảng tham số. Snippet khớp code vừa viết. Không sửa `docs/readme_1.1.24.md`.

### ✅ CHECKPOINT 3
README review xong. Không commit/publish trừ khi được yêu cầu.
