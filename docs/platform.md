# Platform Decision

## Decision

Move the product direction to an Android WebView app for Samsung Galaxy Z Fold7.

The web build remains useful as a lightweight preview and documentation surface, but it is no longer the primary ChatGPT runtime. The primary app surface should be Android: a native shell opens `https://chatgpt.com/` as the top-level WebView page and keeps the app's own large-text guide UI outside the ChatGPT page.

## Why Not PWA First

The earlier PWA direction was useful while exploring the product shape, but it does not solve the core runtime problem:

- `chatgpt.com` cannot be reliably embedded inside iframe, object, or embed tags because of browser security policies.
- ChatKit or OpenAI API flows require separate API billing, server-side session handling, and secure key management.
- Asking a low-vision end user to provide an OpenAI API key does not match the accessibility-helper product goal.
- A static web page cannot safely reproduce the user's logged-in ChatGPT web session.

## Chosen Transition

Use a Capacitor-generated Android project, but keep the first native runtime simple:

- Android `MainActivity` owns a native `WebView`.
- The WebView loads `https://chatgpt.com/` as the top-level page.
- The guide UI is app-owned native UI around or below the WebView.
- An external browser fallback opens ChatGPT in Chrome or another installed browser.
- The React/Vite web app is retained as an explanatory shell and future shared UI/content surface.

This is more realistic than trying to bypass iframe restrictions, while still allowing the repository to keep its current React build and documentation flow.

## Runtime Principles

- The app does not call the OpenAI API for GPT conversations.
- The app does not require, store, or transmit OpenAI API keys.
- The app does not store ChatGPT passwords, verification codes, session values, or cookies outside normal WebView/browser handling.
- The app does not force-click, auto-fill, scrape, or mutate the ChatGPT DOM.
- Login happens only inside the ChatGPT WebView page or external browser fallback.
- If WebView login, social login, popup, network, or security behavior fails, the user must see a large fallback UI instead of a blank screen.

## Fold7 Layout Direction

Folded cover screen:

- Prioritize the ChatGPT WebView.
- Show the guide as a large bottom panel or collapsible guide area.
- Keep buttons tall and labels short.

Unfolded main screen:

- Show WebView and guide side by side when width allows.
- Keep the current step, next action, and fallback button visible without crowding.
- Avoid dense text blocks; show only the current step's instruction.

## Web And Android Distribution

GitHub Pages or another static web deploy can only host the web shell. It should explain the direction and offer a browser link, but it should not be treated as the final ChatGPT-in-app experience.

Android distribution requires an Android build artifact such as an APK or AAB. That path is separate from static web deployment and must be tested on real Android devices, especially Fold7 folded and unfolded states.

## Known Risks

- ChatGPT may change login behavior or WebView compatibility.
- Social login providers may open popups, new windows, or browser handoff flows.
- Some authentication steps may require external browser fallback.
- Play Store distribution may require review of WebView behavior, privacy disclosures, and brand usage.
- Real-device testing is required because desktop browser emulation cannot fully reproduce Android WebView behavior.

## Commands

```bash
npm run build
npm run android:sync
npm run android:build
```

If Android SDK or Java configuration is missing locally, run the web build and sync first, then complete the Android build in Android Studio.
