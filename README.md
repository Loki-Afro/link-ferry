# LinkFerry

LinkFerry opens ordinary web links in the Android browser you choose. It uses Android’s default browser role and needs Android 10 or later.

## Install and set up

1. Download the debug APK and open it on your Android phone. If Android asks, allow the app that opened the APK to install unknown apps, then finish installation.
2. Open **LinkFerry** and tap **Choose browser**. Select an installed browser such as Chrome or Firefox.
3. Tap **Set as default browser** and confirm **LinkFerry** in Android’s prompt. You can also change this from Android’s **Settings → Apps → Default apps → Browser app**.
4. Tap a web link in an app that hands the link to Android. If the preferred browser has been removed or disabled, LinkFerry lets you pick another browser for that link when one is available.

To turn off routing, change **Browser app** in Android’s default app settings back to your browser.

## Which links it can open

| Link path | Expected behavior |
| --- | --- |
| Ordinary Android `ACTION_VIEW` for a web URL | LinkFerry can forward it when Android resolves the link to LinkFerry. The selected browser gets the original URL in a new, package-scoped intent. |
| Chrome Custom Tab | An app can select a provider and launch its tab directly. LinkFerry registers no Custom Tabs service. |
| Embedded WebView | The host app controls navigation. LinkFerry only receives a link if the host hands it to Android and Android resolves it to LinkFerry. |
| Verified Android App Link | Android may route the link straight to the app associated with the domain. |

Android matches manifest schemes case-sensitively, so link handling uses lowercase `http` and `https` schemes. Browser task reuse, Back and Recents depend on Android and the selected browser. LinkFerry requests no app permissions and does not record URLs. Build using Android Studio, JDK 17 or later, Android SDK 36 and Build Tools 36.0.0.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Gradle writes the installable debug APK to `app/build/outputs/apk/debug/app-debug.apk`. See the [test report](TESTING.md) for the Android version matrix and Signal and OsmAnd checks.

## GitHub Actions

**Android CI** builds, tests and lints pushes and pull requests targeting `main` and provides the APK under the run’s **Artifacts**. **APK release** publishes the tested APK on GitHub Releases when you push a version tag that matches `versionName` in `app/build.gradle.kts`.

```sh
git tag v0.1.2
git push origin v0.1.2
```

To sign releases with the same key, set the optional `ANDROID_DEBUG_KEYSTORE_BASE64` GitHub Actions secret to the base64-encoded Android debug keystore you use for those APKs. Android requires the same signing key for an in-place update. CI artifacts and releases without the secret use a fresh debug signing key.

## License

This project is licensed under the GNU Affero General Public License v3.0 only (AGPL-3.0-only). See [LICENSE](LICENSE).

## References

- [Android role requests](https://developer.android.com/reference/android/app/role/RoleManager)
- [Android intents and intent filters](https://developer.android.com/guide/components/intents-filters)
- [Android package visibility for browsers](https://developer.android.com/training/package-visibility/use-cases)
- [Android App Links](https://developer.android.com/training/app-links)
- [Chrome Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs)
