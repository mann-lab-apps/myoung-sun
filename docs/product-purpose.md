# Product Purpose

## Summary

명순 is a large-text execution manual for helping a low-vision family member use ChatGPT with less fear and less confusion.

This app is not a replacement for ChatGPT itself. This app is an accessibility helper that makes the real ChatGPT web experience easier to open, follow, and recover from when something goes wrong.

## Primary User

The primary user is an older family member whose eyesight is declining.

Important assumptions:

- Reading small text is difficult and may become harder over time.
- Switching between many apps, tabs, and browser states can be confusing.
- Login and account screens are stressful.
- The user should not need to understand API keys, iframes, billing systems, browser security headers, or developer settings.
- Mistakes should feel recoverable.

## Product Goal

The product goal is to provide a calm, large-text guide around the real ChatGPT web page.

The app should:

- Open `https://chatgpt.com` in an Android WebView as a top-level page.
- Keep a large guide panel visible beside or below the WebView.
- Show one practical step at a time.
- Use high contrast, large type, and large touch targets.
- Offer Chrome/external-browser fallback when WebView login fails.
- Avoid asking for or storing secrets.

## Core Direction

The product direction is Android WebView first.

On Samsung Galaxy Z Fold7:

- Unfolded screen: show ChatGPT WebView and the guide panel side by side when space allows.
- Cover screen: prioritize the ChatGPT WebView and show the guide as a bottom panel or large control area.
- Orientation and folded/unfolded changes must not break the layout.

## Why Not iframe

The original idea was to show ChatGPT inside a web page using iframe and place guide UI around it. That approach is not reliable.

Major web services, including ChatGPT, can block embedding with security headers such as `X-Frame-Options` and `Content-Security-Policy: frame-ancestors`. Those headers are controlled by the external service, not by this app.

Changing from `iframe` to `object` or `embed` does not solve the problem because those are still embedding mechanisms.

## Why Not ChatKit Or OpenAI API By Default

ChatKit and direct OpenAI API integration are not the default direction for this product.

Reasons:

- ChatGPT subscriptions and OpenAI API billing are separate.
- API usage requires a billing-enabled API account.
- API keys must live on a backend, not inside browser code.
- A static GitHub Pages deployment cannot safely create ChatKit sessions.
- Asking the target user to paste an API key would be a poor and risky experience.

This app should not require OpenAI API billing for the primary flow.

## Why Android WebView

Android WebView can load `https://chatgpt.com` as the top-level page inside the Android app. This avoids the iframe embedding problem because ChatGPT is no longer being framed by another website.

This does not guarantee that every login path will work. Some identity providers block embedded WebViews for security. Therefore the app must always provide an external browser fallback.

## Security Boundaries

The app must not:

- Store OpenAI API keys.
- Ask users to paste OpenAI API keys.
- Store ChatGPT passwords.
- Store verification codes.
- Store ChatGPT cookies or session tokens outside the WebView/browser system.
- Force-click or automatically type into ChatGPT.
- Scrape, inspect, or manipulate the ChatGPT DOM.
- Try to bypass iframe, CSP, or WebView login restrictions.

User login should happen only inside the ChatGPT WebView or an external browser controlled by the user.

## Required Fallbacks

The app must provide obvious recovery paths:

- Reload ChatGPT.
- Open ChatGPT in Chrome or the default browser.
- Explain that social login may fail in embedded WebViews.
- Keep the guide visible even when ChatGPT fails to load.

## Guide Steps

The primary guide should cover:

1. ChatGPT 접속하기
2. 로그인하기
3. 비로그인/로그인 상태 확인하기
4. 새 대화 시작하기
5. 이전 대화 이어가기
6. 대화 내용 정리/복사하기

## Design Principles

- One current action at a time.
- Large Korean labels before explanatory text.
- High contrast over decorative styling.
- Big touch targets.
- No thin gray instructional text.
- Recovery action is always visible.
- The app should feel like a calm helper, not a test.
