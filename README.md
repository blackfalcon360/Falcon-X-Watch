# Falcon X Watch
By: Black Falcon 🦅

Java-only, no AndroidX or external libraries. Portrait splash and main activity.
Black cards and tabs, Urdu (default), English, Arabic app UI with RTL support.
External X content retains the language and layout supplied by X.

## What it does
Opens the official X website in an internal Android WebView, with Latest search:
Bajaur OR responsibility OR TTP OR ISKP OR IMP
All means ANY keyword, not all five in one post. Individual keyword filters are
provided. Refresh, back and search controls are inside the app. HTTPS links,
including t.co redirects, remain inside the browser. Non-HTTPS app/deep links,
downloads, external apps and popup authentication flows are not supported.
This is a web search viewer, NOT an API aggregator or scraping system.

## Internet and privacy
First launch shows offline instructions; Start online opts into network use.
Live posts require internet. Only INTERNET permission is requested. X may
require login, limit results, change its website, or reject embedded WebViews.
No guarantee of all/latest posts or login compatibility. No background polling,
alerts, scraping, censorship bypass, private-post access or offline post archive.
No X API token required. An official API-based feed would separately require
X developer credentials and paid usage. No API secrets bundled in this APK.
Cookies and web storage persist locally until cleared. Only sign into genuine
X domains after checking the full displayed URL. No native JavaScript bridge,
file access, content access or cleartext HTTP; SSL errors are not bypassed.
Clear-browser-data removes cookies/cache/local web storage; it does not revoke
remote account sessions or erase provider-side records. No analytics added.

## GitHub build
Upload CONTENTS of this folder, including the hidden .github directory, to the
repository root. .github/workflows/build.yml runs on push or workflow_dispatch.
It uses Java 17, Gradle 8.7 and AGP 8.5.2; compile/target SDK 34, min SDK 24.
Download FalconXWatch-APK from the successful Actions run's artifacts.
Local build requires Java 17, Android SDK 34 and Gradle 8.7:
gradle assembleDebug
No Gradle wrapper is required by the provided workflow. Debug APK only;
production signing is not configured. Keep Android System WebView up to date.

## Checks
Project XML parsed, ZIP structure and workflow presence verified. Query URL
encoding checked separately. Android method signatures reviewed against their
API-24-compatible forms. No SDK_INT-gated APIs are used. Android SDK / Gradle
compilation and device/X login testing were not available in generation environment.

Sources: https://docs.x.com/x-api/posts/search/introduction
https://docs.x.com/x-api/introduction
