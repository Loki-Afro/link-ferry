# Test report

## Build checks

The Gradle wrapper assembled a signed debug APK. The URL validation tests cover escaped paths, repeated query keys, fragments, IPv6, internationalized domains, malformed URLs and unsupported schemes. Android lint found no errors and flagged the pinned Android Gradle Plugin version as older than the latest release. The APK signature and packaged manifest were checked with Android SDK tools.

## Android compatibility

The manifest declares exported, browsable `ACTION_VIEW` handling for HTTP and HTTPS without a host restriction. Browser discovery queries web handlers using package visibility declarations, requires support for both schemes, and excludes Redirector. The setup screen requests the browser role using Android’s confirmation UI. Android and its Permission Controller evaluate role eligibility on each device.

| Android version | API | Runtime coverage |
| --- | --- | --- |
| Android 10 | 29 | Device or emulator run pending |
| Android 12 | 31 | Device or emulator run pending |
| Android 14 | 34 | Device or emulator run pending |
| Android 15 | 35 | Device or emulator run pending |
| Android 16 | 36 | Emulator startup failed before APK installation. Device run pending. |
| Android 17 | 37 | Device or emulator run pending |

No physical Android device was connected. The Android 16 emulator ran in software mode and its system server watchdog stopped the first boot. The install attempt was rejected while Android was still booting.

## Manual test cases

Run with a second browser installed where the case calls for one. Use a test URL such as `https://example.com/a%2Fb?x=%2B&x=2#part`.

| Case | Steps and expected outcome |
| --- | --- |
| Onboarding | Pick a browser, agree to the Android role prompt, reopen Redirector and confirm both statuses. Repeat after declining the prompt and after changing the default in Android settings. |
| Ordinary web link | Tap an HTTP URL and an HTTPS URL from a separate app. Verify the selected browser opens and receives the original path, query and fragment. Try a test URL containing an escaped slash and repeated query keys. |
| Back and task navigation | Repeat with the browser cold, running and foregrounded. Open multiple distinct links. Check Back, Recents, and link delivery when Redirector is backgrounded. Try tapping a link after unlocking the phone. |
| Missing browser | Disable or uninstall the preferred browser and tap a normal link. Verify the one-time browser picker or the configuration action appears. Confirm Redirector never appears as a target. |
| Invalid input | Explicitly invoke the exported link entry with a missing URL, an invalid HTTP URI and a disallowed scheme. Verify the activity finishes safely. |
| Verified App Link | Tap a known verified link and record which domain-associated app Android launches. |
| Explicit Custom Tab | Open a link from an app that names a Custom Tabs provider. Check whether the provider starts directly or the caller falls back to ordinary link routing. |
| Embedded WebView | Navigate within an app-owned WebView, then use any exposed **Open in browser** action. Observe whether the host hands off the link to Android. |

You can send the ordinary-link case over ADB after setting the browser default.

```sh
adb shell 'am start -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d "https://example.com/a%2Fb?x=%2B&x=2#part"'
```

## Signal and OsmAnd

| Path | Physical-device result |
| --- | --- |
| Signal message link | Pending. The public conversation code sends ordinary message URLs and link previews through an unscoped `ACTION_VIEW`. Signal handles some links itself, including Signal group and proxy links. Verify the specific message tap in the installed version. [Signal ConversationFragment](https://github.com/signalapp/Signal-Android/blob/8959dc6e1329f882951b16ae87e22e60d1c11516/app/src/main/java/org/thoughtcrime/securesms/conversation/v2/ConversationFragment.kt#L1377) |
| OsmAnd general external link | Pending. One public path creates a Custom Tabs intent without selecting a package at that call site. Test which activity Android launches with Redirector as the default. [OsmAnd AndroidUtils.openUrl](https://github.com/osmandapp/OsmAnd/blob/master/OsmAnd/src/net/osmand/plus/utils/AndroidUtils.java) |
| OsmAnd Wikipedia article | Pending. The [Wikipedia WebView client](https://github.com/osmandapp/OsmAnd/blob/master/OsmAnd/src/net/osmand/plus/wikipedia/WikipediaWebViewClient.java) has in-app navigation paths. |
| OsmAnd Wikipedia external link | Pending. Accepting the warning invokes the same Custom Tabs helper. Test the warning and which activity Android launches afterward. [OsmAnd WikiArticleHelper](https://github.com/osmandapp/OsmAnd/blob/master/OsmAnd/src/net/osmand/plus/wikipedia/WikiArticleHelper.java#L108) |

When testing on a real device, compare the same taps with Chrome, Firefox and a third browser. Use dummy URLs when recording ADB activity launches.

**Decision:** The APK is suitable for trial as an ordinary web-link handler. A claim that it materially improves Signal or OsmAnd remains a no-go until those tap paths are confirmed on real devices. If the apps keep links in their own UI, use an app-provided **Open in browser** action where available or ask the app maintainers for an external-browser setting.
