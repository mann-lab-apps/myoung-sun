# Low-Vision Onboarding

## Goal

The first-run experience should help a low-vision user open ChatGPT without needing to understand API keys, billing, browser embedding, or technical setup.

## Flow

1. Start with the largest action: `GPT와 대화하기`.
2. Open the Android WebView screen for `https://chatgpt.com/`.
3. Show one large guide step at a time.
4. Offer `외부 브라우저로 열기` when WebView login or loading fails.
5. Return to the same guide flow after the user finishes login or browser fallback.

## Interaction Rules

- Show one primary action at a time.
- Keep Back, Skip, and Restart visible in plain language.
- Keep copy practical and calm; avoid scoring or correction language.
- Keep touch targets at least 56 px tall.
- Prefer short Korean labels that describe the next action.
- Do not ask the user for an OpenAI API key.
- Do not ask the user to type passwords or verification codes into app-owned UI.

## Completion Criteria

- The home screen leads directly to `GPT와 대화하기`.
- The user can reach ChatGPT WebView or external browser fallback without outside help.
- The final state prepares the user to follow the first large-text ChatGPT guide step.
