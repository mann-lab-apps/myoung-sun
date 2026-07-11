# Myoung Soon

Myoung Soon is a large-text accessibility helper for using ChatGPT on a Samsung Galaxy Z Fold7.

The app is not trying to clone ChatGPT or replace the ChatGPT product. It helps a low-vision user open the real ChatGPT web experience and follow a calm, step-by-step guide around it.

See [docs/product-purpose.md](docs/product-purpose.md) for the product direction, security boundaries, and banned approaches.

## Current Direction

The app is moving from a static web/PWA experiment to an Android WebView app:

- ChatGPT opens as the top-level page in an Android `WebView`.
- The app shows its own large Korean guide UI next to or below that WebView.
- The app does not use iframe, object, or embed to load `chatgpt.com`.
- The app does not use ChatKit or direct OpenAI API calls as the default path.
- The app does not ask for or store OpenAI API keys, ChatGPT passwords, verification codes, cookies, or session values.
- If WebView login fails, the app offers a clear Chrome/external-browser fallback.

## Why Not ChatKit Or API By Default

ChatGPT web subscriptions and OpenAI API billing are separate. API usage needs a server-side key and a billing-enabled API account. That is not a good default for this product because the target user should not need to understand API keys or platform billing.

## Development

```bash
npm install
npm run dev
```

## Web Build

```bash
npm run build
```

The web build remains as a lightweight direction/preview shell. It is not the primary runtime for the final ChatGPT-in-app experience.

## Android

Android Studio 또는 Android SDK가 필요합니다. 로컬 빌드가 SDK 경로를 찾지 못하면 `ANDROID_HOME`을 설정하거나 `android/local.properties`에 `sdk.dir=/path/to/android/sdk`를 지정하세요.

```bash
npm run android:sync
npm run android:build
```

Open the Android project in Android Studio with:

```bash
npm run android:open
```

The native Android entry point is:

```text
android/app/src/main/java/com/mannlab/myoungsun/MainActivity.java
```

## Deployment

The existing GitHub Pages deployment can still serve the web shell:

https://mann-lab-apps.github.io/myoung-sun/

Android distribution requires building and installing the Android app separately.
