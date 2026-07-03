# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A demonstration Android TV (Leanback) app showing how to integrate the closed-source
**Wiinvent Android TV ad SDK** (`tv.wiinvent:wiinvent-sdk-android-tv`, currently 1.1.17).
The SDK is pulled from a private Maven repo (`https://maven.wiinvent.tv/repository/maven-releases/`,
declared in `settings.gradle`). This repo is sample/integration code, not the SDK itself —
the primary audience is partner developers copying integration patterns, so clarity of the
demo flow matters more than app polish.

## Build & run

```bash
./gradlew assembleDebug          # build APK
./gradlew installDebug           # build + install to connected TV/emulator
./gradlew clean
```

Single module: `:app`. Java only (no Kotlin sources, though the SDK is Kotlin — hence the
`Companion.getInstance()` calls). Java 8, `compileSdk`/`targetSdk` 33, `minSdk` 23, Gradle
plugin 7.4.2. There are no unit/instrumentation tests in this project.

`local.properties` holds the Android SDK path and is machine-specific (gitignored).
`android:usesCleartextTraffic="true"` + a permissive `network_security_config.xml` are set
because ad/stream endpoints are hit over HTTP in the sample.

## SDK integration — the three ad products

Each ad product has its own SDK manager (a Kotlin singleton reached via
`Manager.Companion.getInstance()`) with the same lifecycle shape: `init(...)` once →
attach a listener → `requestAds(...)` → `release()` in `onDestroy`. The sample demonstrates
each in a dedicated screen, launched from `MainFragment` (`fragment/MainFragment.java:206+`).

1. **Instream (pre-roll/mid-roll TVC)** — `InStreamManager`, in
   `fragment/PlaybackVideoFragment.java`. This is the most involved integration and the one
   the root `README.md` documents step-by-step. Key points that are easy to get wrong:
   - Player view must be the SDK's `tv.wiinvent.androidtv.ui.FriendlyPlayerView`, not a plain ExoPlayer view.
   - **Friendly obstructions are mandatory**: every view drawn on top of the player (skip
     button, overlays) must be registered via `createFriendlyObstruction(...)` and passed to
     `playerView.addFriendlyObstructionList(...)`, or IMA viewability breaks.
   - `requestAds(...)` returns an `AdsMediaSource` that you add to the ExoPlayer yourself
     (`exoPlayer.addMediaSource(...)`) — the SDK wraps your content media source, it does not
     own the player.
   - Skip button is a custom view (`ui/TV360SkipAdsButtonAds`); `showSkipButton`/`hideSkipButton`
     callbacks drive its countdown and TV focus.

2. **Display banner (list of static banners)** — `DisplayBannerManager`, in
   `activity/DisplayBannerActivity.java` + `ui/DisplayBannerAdapter.java`. Banners render into
   `BannerAdView` instances, one per RecyclerView row. Critical lifecycle detail: the adapter
   calls `releaseBanner(bannerAdView)` in `onViewRecycled` — banner views are a pooled/leakable
   resource and must be released on recycle, dismiss, and error.

3. **Overlay banner (pause-screen banner)** — `OverlayBannerManager`, in
   `activity/OverlayBannerActivity.java`. Same `requestAds`/listener API as display banners,
   but shown/hidden in response to ExoPlayer play/pause state (`showOverlayBanner` on pause,
   `dismissOverlayBanner` on resume).

Shared conventions across all three: request parameters are built with a fluent
`...RequestData.Builder()`; the `BannerAdEventListener` callbacks (`onDisplayAds`, `onNoAds`,
`onAdsBannerDismiss`, `onAdsBannerError`, `onAdsBannerClick`) always touch views inside
`runOnUiThread`, and set the `BannerAdView` visibility + call `releaseBanner` on dismiss/error.

## Non-SDK scaffolding

The rest is standard Leanback TV browse-app boilerplate and mostly irrelevant to SDK work:
`MainFragment` (browse grid), `presenter/CardPresenter`, `VideoDetailsFragment`,
`pojo/Movie`+`MovieList` (hardcoded catalog), `util/VideoCache` (ExoPlayer SimpleCache used by
the instream demo's read-only cache data source).

## Docs & versioning

`README.md` (Vietnamese) is the canonical integration guide and parameter reference (tenantId,
channelId, contentType, adSize enums, event types, etc.) — note it still shows an older SDK
version in its snippets; trust the source files and `app/build.gradle` for the current version.
`docs/README_1.1.x.md` are per-version changelogs/guides. When bumping the SDK version, update
`app/build.gradle`, add a `docs/README_<version>.md`, and reconcile the code samples in `README.md`.
Comments and log strings throughout the code are in Vietnamese — keep that style when editing
sample code so it stays consistent for the partner audience.
