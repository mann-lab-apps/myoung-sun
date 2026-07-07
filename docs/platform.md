# Platform Decision

## Decision

Start with a Progressive Web App.

## Why PWA First

- It works on Samsung Android phones through Chrome or Samsung Internet.
- It can be added to the home screen, which keeps the app feeling simple and familiar.
- It lets us improve the product quickly while the interaction model is still being learned.
- It keeps the core experience portable if we later wrap it as an Android app.

## MVP Platform Requirements

- Mobile-first layout for one-handed phone use.
- Installable web app manifest with a clear Korean app name.
- Offline fallback for the app shell so the user does not see a broken blank screen on weak networks.
- Large text and touch targets from the first implementation.
- Browser APIs kept behind small adapters so Android-native capabilities can be added later.

## Android Transition Notes

If the app needs deeper Android integration, prefer wrapping the existing PWA before rewriting the product:

- Use Trusted Web Activity or Capacitor if the web experience remains the main surface.
- Consider native Android only if we need reliable system-level voice, notifications, contacts, or accessibility-service integrations.
- Keep AI prompts, tutorial content, and safety rules in data modules so they can be shared across web and native shells.

## Open Questions

- Whether voice input should be browser-based first or Android-native.
- Whether family members need a separate caregiver/admin view.
- Whether the app should work fully offline for tutorial content.
