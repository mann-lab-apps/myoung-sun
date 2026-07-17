package com.mannlab.myoungsun;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String CHATGPT_URL = "https://chatgpt.com/?locale=ko-KR";
    private static final String FEEDBACK_EMAIL = "daga4242@gmail.com";
    private static final String STATE_SCREEN = "screen";
    private static final String STATE_CURRENT_URL = "currentUrl";
    private static final String STATE_GUIDE_STEP_INDEX = "guideStepIndex";
    private static final String STATE_INTRO_STEP_INDEX = "introStepIndex";
    private static final String STATE_MAIN_FRAME_LOAD_ERROR = "mainFrameLoadError";
    private static final Map<String, String> KOREAN_HEADERS = new HashMap<String, String>() {{
        put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.6,en;q=0.5");
    }};
    private static final String[] GUIDE_TITLES = {
            "첫 대화 시작해보기",
            "요즘 날씨 물어보기",
            "내 기분 알려주기",
            "고민 털어놓기",
            "축하합니다"
    };
    private static final String[] GUIDE_BODIES = {
            "\"안녕\"하고 인사해보세요.",
            "\"요즘 제주도 날씨는 어때?\"하고 물어보세요.",
            "내 기분을 직접 입력해보세요!",
            "고민을 직접 입력해보세요!",
            "GPT와 대화하기에 성공했습니다!\nGPT와 자유롭게 대화해보세요 :)"
    };
    private static final String[] GUIDE_AUTO_PROMPTS = {
            "안녕",
            "요즘 제주도 날씨는 어때?"
    };

    private enum Screen {
        HOME,
        INTRO,
        GPT
    }

    private Screen currentScreen = Screen.HOME;
    private WebView chatWebView;
    private LinearLayout errorPanel;
    private String currentUrl = CHATGPT_URL;
    private boolean hasMainFrameLoadError = false;
    private long lastPromptSubmitAtMillis = 0L;
    private int guideStepIndex = 0;
    private int introStepIndex = 0;
    private TextView guideProgressText;
    private TextView guideTitleText;
    private TextView guideBodyText;
    private Button guidePrevButton;
    private Button guideNextButton;
    private Button guideSelectionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(Locale.KOREA);
        if (!restoreScreen(savedInstanceState)) {
            showHomeScreen();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveCurrentWebUrl();
        outState.putString(STATE_SCREEN, currentScreen.name());
        outState.putString(STATE_CURRENT_URL, currentUrl);
        outState.putInt(STATE_GUIDE_STEP_INDEX, guideStepIndex);
        outState.putInt(STATE_INTRO_STEP_INDEX, introStepIndex);
        outState.putBoolean(STATE_MAIN_FRAME_LOAD_ERROR, hasMainFrameLoadError);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showHomeScreen();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        saveCurrentWebUrl();

        if (currentScreen == Screen.GPT) {
            showGptScreen(false, hasMainFrameLoadError);
        } else if (currentScreen == Screen.INTRO) {
            showIntroScreen();
        } else {
            showHomeScreen();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentScreen == Screen.GPT && chatWebView != null && chatWebView.canGoBack()) {
            chatWebView.goBack();
            return;
        }

        if (currentScreen == Screen.GPT || currentScreen == Screen.INTRO) {
            showHomeScreen();
            return;
        }

        super.onBackPressed();
    }

    private void showHomeScreen() {
        currentScreen = Screen.HOME;
        destroyChatWebView();
        errorPanel = null;
        hasMainFrameLoadError = false;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(251, 248, 241));
        root.setPadding(dp(18), dp(22), dp(18), dp(22));

        LinearLayout homeHeader = new LinearLayout(this);
        homeHeader.setOrientation(LinearLayout.HORIZONTAL);
        homeHeader.setGravity(Gravity.CENTER_VERTICAL);
        homeHeader.setPadding(0, 0, 0, dp(12));

        TextView title = createText("명순님, 무엇을 도와드릴까요?", 28, Color.rgb(23, 33, 31));
        homeHeader.addView(title, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));

        Button introButton = createTinyButton("앱 소개");
        introButton.setOnClickListener(v -> {
            introStepIndex = 0;
            showIntroScreen();
        });
        homeHeader.addView(introButton);
        root.addView(homeHeader);

        TextView intro = createText(
                "필요한 가이드를 하나만 골라 주세요.",
                22,
                Color.rgb(51, 64, 61)
        );
        intro.setPadding(0, 0, 0, dp(10));
        root.addView(intro);

        TextView chooseTitle = createText("가이드를 선택하세요", 24, Color.rgb(23, 33, 31));
        chooseTitle.setPadding(0, dp(8), 0, dp(4));
        root.addView(chooseTitle);

        ScrollView guideScrollView = new ScrollView(this);
        guideScrollView.setFillViewport(false);
        guideScrollView.setBackgroundColor(Color.rgb(251, 248, 241));

        LinearLayout guideList = new LinearLayout(this);
        guideList.setOrientation(LinearLayout.VERTICAL);
        guideList.setPadding(0, 0, 0, dp(92));

        guideList.addView(createHomeItem(
                "GPT와 대화하기",
                "대화 화면을 크게 엽니다.",
                true,
                v -> showGptScreen(true)
        ));
        guideList.addView(createHomeItem(
                "카카오톡 길라잡이",
                "곧 추가됩니다.",
                false,
                null
        ));
        guideScrollView.addView(guideList, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        root.addView(guideScrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        setContentView(wrapWithFeedbackButton(root));
    }

    private void showIntroScreen() {
        currentScreen = Screen.INTRO;
        destroyChatWebView();
        errorPanel = null;
        hasMainFrameLoadError = false;
        introStepIndex = clampIntroStepIndex(introStepIndex);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(Color.rgb(251, 248, 241));
        screen.addView(createIntroHeader());

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(251, 248, 241));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(110));

        TextView title = createText("이 앱은 이렇게 써요", 30, Color.rgb(23, 33, 31));
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title);

        TextView intro = createText(
                "한 화면씩 천천히 넘겨 보세요.",
                22,
                Color.rgb(51, 64, 61)
        );
        intro.setPadding(0, 0, 0, dp(16));
        root.addView(intro);

        root.addView(createCurrentIntroSection());

        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        screen.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        setContentView(screen);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showGptScreen(boolean resetToChatGptHome) {
        showGptScreen(resetToChatGptHome, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showGptScreen(boolean resetToChatGptHome, boolean restoreErrorPanel) {
        currentScreen = Screen.GPT;
        if (resetToChatGptHome) {
            currentUrl = CHATGPT_URL;
            guideStepIndex = 0;
            hasMainFrameLoadError = false;
        }
        destroyChatWebView();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(251, 248, 241));

        root.addView(createStackHeader("GPT와 대화하기", ""));

        LinearLayout content = new LinearLayout(this);
        boolean wideLayout = getResources().getConfiguration().screenWidthDp >= 600;
        content.setOrientation(wideLayout ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        content.setPadding(dp(10), dp(10), dp(10), dp(10));
        content.setGravity(Gravity.CENTER);

        FrameLayout webFrame = new FrameLayout(this);
        LinearLayout.LayoutParams webParams = wideLayout
                ? new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.25f)
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        webParams.setMargins(0, 0, wideLayout ? dp(10) : 0, wideLayout ? 0 : dp(10));

        TextView loading = createLoadingMessage();
        webFrame.addView(loading, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        chatWebView = createChatWebView(loading);
        webFrame.addView(chatWebView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        errorPanel = createErrorPanel();
        errorPanel.setVisibility(restoreErrorPanel ? View.VISIBLE : View.GONE);
        webFrame.addView(errorPanel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        content.addView(webFrame, webParams);
        content.addView(createGuideScrollPanel(wideLayout), wideLayout
                ? new LinearLayout.LayoutParams(dp(330), ViewGroup.LayoutParams.MATCH_PARENT)
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(250)));

        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        setContentView(root);
        if (restoreErrorPanel) {
            hasMainFrameLoadError = true;
            loading.setVisibility(View.GONE);
        } else {
            webFrame.postDelayed(() -> {
                if (chatWebView != null && currentScreen == Screen.GPT) {
                    loadChatGpt(currentUrl != null ? currentUrl : CHATGPT_URL);
                }
            }, 250);
        }
    }

    private WebView createChatWebView(TextView loading) {
        WebView webView = new WebView(this);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.addJavascriptInterface(new GuideBridge(), "MyoungSoonGuide");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();

                if ("http".equals(scheme) || "https".equals(scheme)) {
                    return false;
                }

                openExternal(uri.toString());
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    hasMainFrameLoadError = true;
                    loading.setVisibility(View.GONE);
                    showErrorPanel();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                currentUrl = url;
                loading.setVisibility(View.GONE);
                injectPromptSubmitWatcher(view);
                if (hasMainFrameLoadError) {
                    showErrorPanel();
                } else {
                    hideErrorPanel();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                if (resultMsg == null || resultMsg.obj == null) {
                    return false;
                }

                WebView popupWebView = new WebView(MainActivity.this);
                popupWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView popupView, WebResourceRequest request) {
                        return openPopupUrl(popupView, request.getUrl().toString());
                    }

                    @Override
                    public void onPageStarted(WebView popupView, String url, android.graphics.Bitmap favicon) {
                        openPopupUrl(popupView, url);
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popupWebView);
                resultMsg.sendToTarget();
                return true;
            }
        });

        return webView;
    }

    private void injectPromptSubmitWatcher(WebView webView) {
        if (webView == null) {
            return;
        }

        String script = "(function(){"
                + "if(window.__myoungSoonSubmitWatcherInstalled){return;}"
                + "window.__myoungSoonSubmitWatcherInstalled=true;"
                + "var last=0;"
                + "function notify(reason){"
                + "var now=Date.now();"
                + "if(now-last<1200){return;}"
                + "last=now;"
                + "try{window.MyoungSoonGuide.onPromptSubmitted(reason);}catch(e){}"
                + "}"
                + "function isEditable(target){"
                + "if(!target){return false;}"
                + "return target.tagName==='TEXTAREA'||target.tagName==='INPUT'||target.isContentEditable||"
                + "(target.closest&&target.closest('[contenteditable=\"true\"]'));"
                + "}"
                + "document.addEventListener('keydown',function(event){"
                + "if(event.key==='Enter'&&!event.shiftKey&&!event.isComposing&&isEditable(event.target)){notify('enter');}"
                + "},true);"
                + "document.addEventListener('submit',function(){notify('submit');},true);"
                + "document.addEventListener('click',function(event){"
                + "var target=event.target;"
                + "var button=target&&target.closest?target.closest('button,[role=\"button\"]'):null;"
                + "if(!button){return;}"
                + "var label=[button.getAttribute('aria-label'),button.getAttribute('data-testid'),button.getAttribute('title'),button.getAttribute('type'),button.textContent]"
                + ".filter(Boolean).join(' ').toLowerCase();"
                + "if(label.indexOf('send')>-1||label.indexOf('submit')>-1||label.indexOf('보내')>-1){notify('button');}"
                + "},true);"
                + "})();";
        webView.evaluateJavascript(script, null);
    }

    private final class GuideBridge {
        @JavascriptInterface
        public void onPromptSubmitted(String reason) {
            runOnUiThread(() -> handlePromptSubmitted());
        }
    }

    private void handlePromptSubmitted() {
        if (currentScreen != Screen.GPT) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastPromptSubmitAtMillis < 1500) {
            return;
        }
        lastPromptSubmitAtMillis = now;

        int lastIndex = GUIDE_TITLES.length - 1;
        if (guideStepIndex < lastIndex) {
            guideStepIndex++;
            updateGuideText();
        }
    }

    private void sendGuidePromptForCurrentStep() {
        String prompt = getAutoPromptForStep(guideStepIndex);
        if (chatWebView == null || prompt == null) {
            return;
        }

        String script = "(function(){"
                + "var prompt=" + JSONObject.quote(prompt) + ";"
                + "function fire(target,type){target.dispatchEvent(new Event(type,{bubbles:true,cancelable:true}));}"
                + "function findEditor(){"
                + "var selectors=['#prompt-textarea','textarea','[contenteditable=\"true\"]','[role=\"textbox\"]'];"
                + "for(var i=0;i<selectors.length;i++){var el=document.querySelector(selectors[i]);if(el){return el;}}"
                + "return null;"
                + "}"
                + "function setPrompt(editor){"
                + "editor.focus();"
                + "var tag=(editor.tagName||'').toUpperCase();"
                + "if(tag==='TEXTAREA'||tag==='INPUT'){editor.value=prompt;fire(editor,'input');fire(editor,'change');return true;}"
                + "try{document.execCommand('selectAll',false,null);document.execCommand('insertText',false,prompt);}catch(e){}"
                + "if((editor.innerText||editor.textContent||'').trim()!==prompt.trim()){editor.textContent=prompt;}"
                + "fire(editor,'input');fire(editor,'change');"
                + "return true;"
                + "}"
                + "function findSendButton(){"
                + "var selectors=['button[data-testid=\"send-button\"]','button[aria-label*=\"Send\"]','button[aria-label*=\"보내\"]','button[type=\"submit\"]'];"
                + "for(var i=0;i<selectors.length;i++){var button=document.querySelector(selectors[i]);if(button&&!button.disabled){return button;}}"
                + "var buttons=[].slice.call(document.querySelectorAll('button,[role=\"button\"]'));"
                + "for(var j=0;j<buttons.length;j++){var b=buttons[j];var label=[b.getAttribute('aria-label'),b.getAttribute('data-testid'),b.getAttribute('title'),b.textContent].filter(Boolean).join(' ').toLowerCase();if(!b.disabled&&(label.indexOf('send')>-1||label.indexOf('submit')>-1||label.indexOf('보내')>-1)){return b;}}"
                + "return null;"
                + "}"
                + "var editor=findEditor();"
                + "if(!editor){return false;}"
                + "setPrompt(editor);"
                + "setTimeout(function(){var button=findSendButton();if(button){button.click();}},450);"
                + "return true;"
                + "})();";

        chatWebView.evaluateJavascript(script, value -> {
            if (!"true".equals(value)) {
                Toast.makeText(this, "입력창을 찾지 못했어요. 직접 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getAutoPromptForStep(int stepIndex) {
        if (stepIndex < 0 || stepIndex >= GUIDE_AUTO_PROMPTS.length) {
            return null;
        }

        return GUIDE_AUTO_PROMPTS[stepIndex];
    }

    private View createHomeItem(String title, String body, boolean enabled, View.OnClickListener listener) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(18), dp(18), dp(18), dp(16));
        item.setMinimumHeight(dp(132));
        item.setBackground(createGuideItemBackground(enabled));
        item.setElevation(enabled ? dp(4) : 0);
        item.setClickable(enabled);
        item.setFocusable(enabled);
        if (enabled && listener != null) {
            item.setOnClickListener(listener);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(10), 0, dp(10));
        item.setLayoutParams(params);

        TextView itemTitle = createText(title, 26, enabled ? Color.rgb(23, 33, 31) : Color.rgb(72, 78, 76));
        TextView itemBody = createText(body, 19, enabled ? Color.rgb(51, 64, 61) : Color.rgb(86, 94, 90));
        itemBody.setPadding(0, dp(8), 0, 0);

        item.addView(itemTitle);
        item.addView(itemBody);

        TextView action = createPillLabel(enabled ? "시작하기" : "준비 중", enabled);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionParams.gravity = Gravity.END;
        actionParams.setMargins(0, dp(12), 0, 0);
        item.addView(action, actionParams);
        return item;
    }

    private GradientDrawable createGuideItemBackground(boolean enabled) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(8));
        drawable.setColor(enabled ? Color.rgb(255, 253, 248) : Color.rgb(240, 238, 232));
        drawable.setStroke(enabled ? dp(3) : dp(1), enabled ? Color.rgb(216, 91, 59) : Color.rgb(180, 176, 166));
        return drawable;
    }

    private TextView createPillLabel(String text, boolean enabled) {
        TextView label = createText(text, 18, enabled ? Color.WHITE : Color.rgb(72, 78, 76));
        label.setPadding(dp(16), dp(8), dp(16), dp(8));
        label.setGravity(Gravity.CENTER);

        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(8));
        background.setColor(enabled ? Color.rgb(216, 91, 59) : Color.rgb(225, 221, 212));
        label.setBackground(background);
        return label;
    }

    private View createCurrentIntroSection() {
        if (introStepIndex == 1) {
            return createIntroScreenSection(
                    "2. GPT 가이드 첫 화면",
                    "ChatGPT 화면을 보면서 오른쪽 안내를 읽습니다. 준비되면 다음을 눌러요.",
                    createGuideFirstStepPreview()
            );
        }

        if (introStepIndex == 2) {
            return createIntroScreenSection(
                    "3. 가이드 완료 화면",
                    "마지막 단계에서는 털어놓고 싶은 고민을 편하게 이야기해봅니다.",
                    createGuideCompletePreview()
            );
        }

        return createIntroScreenSection(
                "1. 가이드 선택 화면",
                "홈에서 필요한 도움을 고릅니다. 지금은 GPT와 대화하기를 눌러 시작해요.",
                createGuideSelectionPreview()
        );
    }

    private View createIntroScreenSection(String title, String body, View preview) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(14), dp(14), dp(14), dp(14));
        section.setBackground(createGuideItemBackground(true));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(10));
        section.setLayoutParams(params);

        TextView sectionTitle = createText(title, 24, Color.rgb(23, 33, 31));
        TextView sectionBody = createText(body, 18, Color.rgb(51, 64, 61));
        sectionBody.setPadding(0, dp(6), 0, dp(12));

        section.addView(sectionTitle);
        section.addView(sectionBody);
        section.addView(preview);
        return section;
    }

    private View createGuideSelectionPreview() {
        LinearLayout preview = createScreenshotPreview();
        preview.addView(createPreviewHeader("명순님, 무엇을 도와드릴까요?", "앱 소개"));
        preview.addView(createPreviewCaption("가이드를 선택하세요", 16, Color.rgb(23, 33, 31)));
        preview.addView(createPreviewCard("GPT와 대화하기", "대화 화면을 크게 엽니다.", "시작하기", true));
        preview.addView(createPreviewCard("카카오톡 길라잡이", "곧 추가됩니다.", "준비 중", false));
        return preview;
    }

    private View createGuideFirstStepPreview() {
        LinearLayout preview = createScreenshotPreview();
        preview.addView(createPreviewHeader("GPT와 대화하기", ""));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(0, dp(8), 0, 0);

        TextView webArea = createPreviewPane("ChatGPT 화면");
        LinearLayout guideArea = createPreviewGuidePane("1 / 5", "첫 대화 시작해보기", "\"안녕\"하고 인사해보세요.", true, "\"안녕\" 전송", true);
        content.addView(webArea, new LinearLayout.LayoutParams(0, dp(154), 1.15f));

        LinearLayout.LayoutParams guideParams = new LinearLayout.LayoutParams(0, dp(154), 0.85f);
        guideParams.setMargins(dp(8), 0, 0, 0);
        content.addView(guideArea, guideParams);
        preview.addView(content);
        return preview;
    }

    private View createGuideCompletePreview() {
        LinearLayout preview = createScreenshotPreview();
        preview.addView(createPreviewHeader("GPT와 대화하기", "마지막 단계"));

        LinearLayout guideArea = createPreviewGuidePane("5 / 5", "축하합니다", "GPT와 대화하기에 성공했습니다!\nGPT와 자유롭게 대화해보세요 :)", false, "완료", false);
        TextView selectionButton = createPreviewPill("가이드 선택하기", true);
        LinearLayout.LayoutParams selectionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        selectionParams.setMargins(0, dp(8), 0, 0);
        guideArea.addView(selectionButton, selectionParams);

        LinearLayout.LayoutParams guideParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(210)
        );
        guideParams.setMargins(0, dp(8), 0, 0);
        preview.addView(guideArea, guideParams);
        return preview;
    }

    private LinearLayout createScreenshotPreview() {
        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setPadding(dp(10), dp(10), dp(10), dp(10));
        preview.setBackground(createPreviewBackground(Color.rgb(251, 248, 241), Color.rgb(180, 176, 166)));
        preview.setContentDescription("앱 화면 미리보기");
        return preview;
    }

    private View createPreviewHeader(String title, String action) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = createPreviewCaption(title, 16, Color.rgb(23, 33, 31));
        header.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        if (action != null && !action.trim().isEmpty()) {
            TextView actionView = createPreviewPill(action, false);
            header.addView(actionView);
        }
        return header;
    }

    private View createPreviewCard(String title, String body, String action, boolean enabled) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(8), dp(10), dp(8));
        card.setBackground(createPreviewBackground(
                enabled ? Color.rgb(255, 253, 248) : Color.rgb(240, 238, 232),
                enabled ? Color.rgb(216, 91, 59) : Color.rgb(180, 176, 166)
        ));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, 0);
        card.setLayoutParams(params);

        card.addView(createPreviewCaption(title, 18, enabled ? Color.rgb(23, 33, 31) : Color.rgb(72, 78, 76)));
        card.addView(createPreviewCaption(body, 14, Color.rgb(51, 64, 61)));

        TextView actionView = createPreviewPill(action, enabled);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionParams.gravity = Gravity.END;
        actionParams.setMargins(0, dp(6), 0, 0);
        card.addView(actionView, actionParams);
        return card;
    }

    private TextView createPreviewPane(String text) {
        TextView pane = createPreviewCaption(text, 18, Color.rgb(51, 64, 61));
        pane.setGravity(Gravity.CENTER);
        pane.setBackground(createPreviewBackground(Color.rgb(255, 253, 248), Color.rgb(180, 176, 166)));
        return pane;
    }

    private LinearLayout createPreviewGuidePane(String progress, String title, String body, boolean firstStep, String nextLabel, boolean nextEnabled) {
        LinearLayout guide = new LinearLayout(this);
        guide.setOrientation(LinearLayout.VERTICAL);
        guide.setPadding(dp(10), dp(8), dp(10), dp(8));
        guide.setBackground(createPreviewBackground(Color.rgb(255, 253, 248), Color.rgb(216, 91, 59)));

        guide.addView(createPreviewCaption(progress, 15, Color.rgb(143, 47, 29)));
        guide.addView(createPreviewCaption(title, 18, Color.rgb(23, 33, 31)));
        guide.addView(createPreviewCaption(body, 14, Color.rgb(51, 64, 61)));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(8), 0, 0);
        buttons.addView(createPreviewPill("←", !firstStep), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        nextParams.setMargins(dp(6), 0, 0, 0);
        buttons.addView(createPreviewPill(nextLabel, nextEnabled), nextParams);
        guide.addView(buttons);
        return guide;
    }

    private TextView createPreviewCaption(String text, int size, int color) {
        TextView view = createText(text, size, color);
        view.setLineSpacing(0, 1f);
        return view;
    }

    private TextView createPreviewPill(String text, boolean enabled) {
        TextView label = createPreviewCaption(text, 13, enabled ? Color.WHITE : Color.rgb(86, 94, 90));
        label.setGravity(Gravity.CENTER);
        label.setPadding(dp(9), dp(5), dp(9), dp(5));
        label.setBackground(createPreviewBackground(
                enabled ? Color.rgb(216, 91, 59) : Color.rgb(232, 226, 214),
                enabled ? Color.rgb(216, 91, 59) : Color.rgb(232, 226, 214)
        ));
        return label;
    }

    private GradientDrawable createPreviewBackground(int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(8));
        drawable.setColor(fillColor);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private FrameLayout wrapWithFeedbackButton(View content) {
        FrameLayout frame = new FrameLayout(this);
        frame.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        Button feedbackButton = createFloatingButton("의견");
        feedbackButton.setOnClickListener(v -> showFeedbackDialog());

        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                dp(112),
                dp(68),
                Gravity.BOTTOM | Gravity.END
        );
        buttonParams.setMargins(0, 0, dp(18), dp(18));
        frame.addView(feedbackButton, buttonParams);
        return frame;
    }

    private Button createFloatingButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(23, 91, 79));
        button.setAllCaps(false);
        button.setMinHeight(dp(68));
        button.setElevation(dp(8));
        return button;
    }

    private void showFeedbackDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(22), dp(22), dp(18));
        panel.setBackgroundColor(Color.rgb(255, 253, 248));

        TextView title = createText("의견 보내기", 28, Color.rgb(23, 33, 31));
        panel.addView(title);

        TextView body = createText("의견은 이메일 작성 화면으로 열립니다. 적은 내용을 확인한 뒤 보내세요.", 19, Color.rgb(51, 64, 61));
        body.setPadding(0, dp(8), 0, dp(12));
        panel.addView(body);

        TextView privacyNote = createText("비밀번호, 인증번호, 전화번호 같은 개인 정보는 적지 마세요.", 18, Color.rgb(143, 47, 29));
        privacyNote.setPadding(0, 0, 0, dp(12));
        panel.addView(privacyNote);

        EditText input = new EditText(this);
        input.setTextSize(22);
        input.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        input.setTextColor(Color.rgb(23, 33, 31));
        input.setHint("예: 버튼이 너무 작아요.");
        input.setHintTextColor(Color.rgb(98, 105, 101));
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setMinLines(4);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setSingleLine(false);
        input.setBackgroundColor(Color.rgb(240, 238, 232));
        input.setPadding(dp(12), dp(12), dp(12), dp(12));
        panel.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(12), 0, 0);

        Button cancelButton = createSecondaryButton("닫기");
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        Button sendButton = createButton("이메일 작성하기");
        sendButton.setOnClickListener(v -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty()) {
                input.setError("내용을 적어주세요.");
                return;
            }

            showFeedbackConfirmDialog(dialog, message);
        });

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cancelParams.setMargins(0, 0, dp(6), 0);
        actions.addView(cancelButton, cancelParams);

        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sendParams.setMargins(dp(6), 0, 0, 0);
        actions.addView(sendButton, sendParams);
        panel.addView(actions);

        dialog.setContentView(panel);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setLayout(getDialogWidthWithSideMargins(), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        input.requestFocus();
        input.postDelayed(() -> {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }

    private void showFeedbackConfirmDialog(Dialog feedbackDialog, String message) {
        Dialog confirmDialog = new Dialog(this);
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(22), dp(22), dp(18));
        panel.setBackgroundColor(Color.rgb(255, 253, 248));

        TextView title = createText("이메일로 보낼게요", 28, Color.rgb(23, 33, 31));
        panel.addView(title);

        TextView body = createText("이메일 작성 화면으로 이동합니다. 비밀번호, 인증번호, 개인 정보가 없으면 계속하세요.", 19, Color.rgb(51, 64, 61));
        body.setPadding(0, dp(8), 0, dp(12));
        panel.addView(body);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button cancelButton = createSecondaryButton("다시 쓰기");
        cancelButton.setOnClickListener(v -> confirmDialog.dismiss());

        Button continueButton = createButton("이메일 열기");
        continueButton.setOnClickListener(v -> {
            confirmDialog.dismiss();
            feedbackDialog.dismiss();
            openFeedbackEmail(message);
        });

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cancelParams.setMargins(0, 0, dp(6), 0);
        actions.addView(cancelButton, cancelParams);

        LinearLayout.LayoutParams continueParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        continueParams.setMargins(dp(6), 0, 0, 0);
        actions.addView(continueButton, continueParams);
        panel.addView(actions);

        confirmDialog.setContentView(panel);
        Window window = confirmDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        confirmDialog.show();

        Window shownWindow = confirmDialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setLayout(getDialogWidthWithSideMargins(), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private int getDialogWidthWithSideMargins() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int horizontalMargin = dp(48);
        return Math.max(dp(320), screenWidth - (horizontalMargin * 2));
    }

    private void openFeedbackEmail(String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + FEEDBACK_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "[명순 앱 의견] " + getCurrentScreenName());
        intent.putExtra(Intent.EXTRA_TEXT, "피드백\n\n" + message + "\n\n화면\n\n" + getCurrentScreenName());

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException ignored) {
            showErrorPanel();
        }
    }

    private String getCurrentScreenName() {
        if (currentScreen == Screen.GPT) {
            return "GPT와 대화하기 / " + (guideStepIndex + 1) + "단계";
        }
        if (currentScreen == Screen.INTRO) {
            return "앱 소개";
        }
        return "홈";
    }

    private View createIntroHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(10), dp(14), dp(10));
        header.setBackgroundColor(Color.rgb(255, 253, 248));

        Button backButton = createHeaderBackButton();
        backButton.setOnClickListener(v -> showHomeScreen());
        header.addView(backButton);

        TextView title = createText("앱 소개", 22, Color.rgb(23, 33, 31));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.setMargins(dp(10), 0, dp(10), 0);
        header.addView(title, titleParams);

        Button prevButton = createHeaderArrowButton("←", false);
        boolean canGoBack = introStepIndex > 0;
        prevButton.setEnabled(canGoBack);
        prevButton.setAlpha(canGoBack ? 1f : 0.45f);
        prevButton.setContentDescription(canGoBack ? "이전 소개 화면으로 돌아갑니다" : "이전 소개 화면이 없습니다");
        prevButton.setOnClickListener(v -> {
            if (introStepIndex > 0) {
                introStepIndex--;
                showIntroScreen();
            }
        });

        Button nextButton = createHeaderArrowButton("→", true);
        boolean canGoNext = introStepIndex < 2;
        nextButton.setEnabled(canGoNext);
        nextButton.setAlpha(canGoNext ? 1f : 0.45f);
        nextButton.setContentDescription(canGoNext ? "다음 소개 화면으로 갑니다" : "마지막 소개 화면입니다");
        nextButton.setOnClickListener(v -> {
            if (introStepIndex < 2) {
                introStepIndex++;
                showIntroScreen();
            }
        });

        header.addView(prevButton);
        header.addView(nextButton);
        return header;
    }

    private View createStackHeader(String titleText, String subtitleText) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(10), dp(14), dp(10));
        header.setBackgroundColor(Color.rgb(255, 253, 248));

        Button backButton = createHeaderBackButton();
        backButton.setOnClickListener(v -> showHomeScreen());
        header.addView(backButton);

        TextView title = createText(titleText, 22, Color.rgb(23, 33, 31));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.addView(title);
        if (subtitleText != null && !subtitleText.trim().isEmpty()) {
            TextView subtitle = createText(subtitleText, 16, Color.rgb(51, 64, 61));
            copy.addView(subtitle);
        }

        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.setMargins(dp(10), 0, 0, 0);
        header.addView(copy, copyParams);

        return header;
    }

    private TextView createLoadingMessage() {
        TextView loading = createText("여는 중", 24, Color.rgb(51, 64, 61));
        loading.setGravity(Gravity.CENTER);
        loading.setBackgroundColor(Color.rgb(255, 253, 248));
        return loading;
    }

    private ScrollView createGuideScrollPanel(boolean wideLayout) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(255, 253, 248));
        scrollView.addView(createGuidePanel(wideLayout), new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return scrollView;
    }

    private LinearLayout createGuidePanel(boolean wideLayout) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(14), dp(14), dp(14));
        panel.setBackgroundColor(Color.rgb(255, 253, 248));

        guideProgressText = createText("", 18, Color.rgb(143, 47, 29));
        guideProgressText.setPadding(0, 0, 0, dp(6));
        panel.addView(guideProgressText);

        guideTitleText = createText("", wideLayout ? 30 : 25, Color.rgb(23, 33, 31));
        guideTitleText.setPadding(0, 0, 0, dp(10));
        panel.addView(guideTitleText);

        guideBodyText = createText("", wideLayout ? 22 : 20, Color.rgb(51, 64, 61));
        guideBodyText.setPadding(0, 0, 0, dp(14));
        panel.addView(guideBodyText);

        LinearLayout guideNav = new LinearLayout(this);
        guideNav.setOrientation(LinearLayout.HORIZONTAL);
        guideNav.setPadding(0, dp(4), 0, 0);

        guidePrevButton = createGuideArrowButton("←", false);
        guidePrevButton.setContentDescription("이전 단계로 돌아갑니다");
        guidePrevButton.setOnClickListener(v -> {
            if (guideStepIndex > 0) {
                guideStepIndex--;
                updateGuideText();
            }
        });

        guideNextButton = createGuideArrowButton("→", true);
        guideNextButton.setContentDescription("다음 단계로 갑니다");
        guideNextButton.setOnClickListener(v -> {
            if (getAutoPromptForStep(guideStepIndex) != null) {
                sendGuidePromptForCurrentStep();
            }
        });

        guideNav.addView(guidePrevButton);
        guideNav.addView(guideNextButton);
        panel.addView(guideNav);

        guideSelectionButton = createButton("가이드 선택하기");
        guideSelectionButton.setContentDescription("가이드 선택 화면으로 돌아갑니다");
        guideSelectionButton.setOnClickListener(v -> showHomeScreen());
        panel.addView(guideSelectionButton);

        updateGuideText();
        return panel;
    }

    private LinearLayout createErrorPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(24), dp(24), dp(24), dp(24));
        panel.setBackgroundColor(Color.rgb(255, 253, 248));

        TextView title = createText("화면을 불러오지 못했어요.", 28, Color.rgb(143, 47, 29));
        title.setGravity(Gravity.CENTER);

        TextView body = createText("네트워크가 불안정하거나, 로그인 화면이 앱 안에서 멈췄을 수 있어요. 다시 시도하거나 외부 브라우저에서 이어가세요.", 19, Color.rgb(51, 64, 61));
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, dp(14), 0, dp(14));

        Button externalButton = createButton("외부 브라우저에서 열기");
        externalButton.setOnClickListener(v -> openExternal(getExternalChatUrl()));

        Button reloadButton = createSecondaryButton("다시 시도");
        reloadButton.setOnClickListener(v -> {
            hideErrorPanel();
            if (chatWebView != null) {
                loadChatGpt(currentUrl != null ? currentUrl : CHATGPT_URL);
            }
        });

        panel.addView(title);
        panel.addView(body);
        panel.addView(externalButton);
        panel.addView(reloadButton);
        return panel;
    }

    private TextView createText(String text, int size, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setLineSpacing(dp(2), 1.05f);
        view.setContentDescription(text);
        return view;
    }

    private void updateGuideText() {
        if (guideProgressText == null || guideTitleText == null || guideBodyText == null) {
            return;
        }

        int lastIndex = GUIDE_TITLES.length - 1;
        if (guideStepIndex < 0) {
            guideStepIndex = 0;
        } else if (guideStepIndex > lastIndex) {
            guideStepIndex = lastIndex;
        }

        String progressText = (guideStepIndex + 1) + " / " + GUIDE_TITLES.length;
        String progressDescription = (guideStepIndex + 1) + "단계, 전체 " + GUIDE_TITLES.length + "단계";
        guideProgressText.setText(progressText);
        guideProgressText.setContentDescription(progressDescription);
        guideTitleText.setText(GUIDE_TITLES[guideStepIndex]);
        guideTitleText.setContentDescription("현재 단계. " + GUIDE_TITLES[guideStepIndex]);
        guideBodyText.setText(GUIDE_BODIES[guideStepIndex]);
        guideBodyText.setContentDescription(GUIDE_BODIES[guideStepIndex]);

        boolean isLastStep = guideStepIndex == lastIndex;
        if (guidePrevButton != null) {
            boolean canGoBack = guideStepIndex > 0;
            guidePrevButton.setEnabled(canGoBack);
            guidePrevButton.setAlpha(canGoBack ? 1f : 0.45f);
            guidePrevButton.setContentDescription(canGoBack ? "이전 단계로 돌아갑니다" : "이전 단계가 없습니다");
        }

        if (guideNextButton != null) {
            String autoPrompt = getAutoPromptForStep(guideStepIndex);
            boolean canSendPrompt = autoPrompt != null;
            guideNextButton.setEnabled(canSendPrompt);
            guideNextButton.setAlpha(canSendPrompt ? 1f : 0.45f);
            if (canSendPrompt) {
                String buttonText = "\"" + autoPrompt + "\" 전송하기";
                guideNextButton.setText(buttonText);
                guideNextButton.setTextSize(autoPrompt.length() > 8 ? 16 : 20);
                guideNextButton.setContentDescription(buttonText);
            } else if (guideStepIndex == 2) {
                guideNextButton.setText("직접 입력해보세요!");
                guideNextButton.setTextSize(18);
                guideNextButton.setContentDescription("내 기분을 직접 입력하면 다음 단계로 넘어갑니다");
            } else if (guideStepIndex == 3) {
                guideNextButton.setText("직접 입력해보세요!");
                guideNextButton.setTextSize(18);
                guideNextButton.setContentDescription("고민을 직접 입력하면 완료 화면으로 넘어갑니다");
            } else {
                guideNextButton.setText("완료");
                guideNextButton.setTextSize(20);
                guideNextButton.setContentDescription("마지막 단계입니다");
            }
        }

        if (guideSelectionButton != null) {
            guideSelectionButton.setVisibility(isLastStep ? View.VISIBLE : View.GONE);
            guideSelectionButton.setContentDescription("가이드 선택 화면으로 돌아갑니다");
        }
    }

    private Button createHeaderBackButton() {
        Button button = createSecondaryButton("뒤로");
        button.setMinWidth(dp(92));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button createHeaderArrowButton(String text, boolean primary) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(28);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(primary ? Color.WHITE : Color.rgb(23, 33, 31));
        button.setBackgroundColor(primary ? Color.rgb(216, 91, 59) : Color.rgb(232, 226, 214));
        button.setAllCaps(false);
        button.setMinWidth(dp(68));
        button.setMinHeight(dp(68));
        button.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(68), dp(68));
        params.setMargins(dp(8), 0, 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button createGuideArrowButton(String text, boolean primary) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(text.length() > 2 ? 20 : 28);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(primary ? Color.WHITE : Color.rgb(23, 33, 31));
        button.setBackgroundColor(primary ? Color.rgb(216, 91, 59) : Color.rgb(232, 226, 214));
        button.setAllCaps(false);
        button.setMinHeight(dp(58));
        button.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(primary ? dp(6) : 0, dp(10), primary ? 0 : dp(6), 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button createTinyButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(Color.rgb(23, 33, 31));
        button.setBackgroundColor(Color.rgb(232, 226, 214));
        button.setAllCaps(false);
        button.setMinHeight(dp(56));
        button.setContentDescription(text);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(8));
        button.setLayoutParams(params);
        return button;
    }

    private Button createButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(216, 91, 59));
        button.setAllCaps(false);
        button.setMinHeight(dp(58));
        button.setContentDescription(text);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(10), 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = createButton(text);
        button.setTextColor(Color.rgb(23, 33, 31));
        button.setBackgroundColor(Color.rgb(232, 226, 214));
        return button;
    }

    private void destroyChatWebView() {
        if (chatWebView == null) {
            return;
        }

        chatWebView.stopLoading();
        chatWebView.setWebChromeClient(null);
        chatWebView.setWebViewClient(null);
        chatWebView.destroy();
        chatWebView = null;
    }

    private void loadChatGpt(String url) {
        if (chatWebView == null) {
            return;
        }

        hasMainFrameLoadError = false;
        chatWebView.loadUrl(url, KOREAN_HEADERS);
    }

    private String getExternalChatUrl() {
        if (currentUrl == null || currentUrl.trim().isEmpty()) {
            return CHATGPT_URL;
        }

        return currentUrl;
    }

    private boolean openPopupUrl(WebView popupView, String url) {
        if (url == null || url.trim().isEmpty() || "about:blank".equals(url)) {
            return false;
        }

        openExternal(url);
        popupView.stopLoading();
        popupView.destroy();
        return true;
    }

    private void openExternal(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException ignored) {
            showErrorPanel();
        }
    }

    private void showErrorPanel() {
        if (errorPanel != null) {
            errorPanel.setVisibility(View.VISIBLE);
        }
    }

    private void hideErrorPanel() {
        if (errorPanel != null) {
            errorPanel.setVisibility(View.GONE);
        }
    }

    private boolean restoreScreen(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return false;
        }

        try {
            String savedScreen = savedInstanceState.getString(STATE_SCREEN, Screen.HOME.name());
            currentScreen = Screen.valueOf(savedScreen);
            currentUrl = savedInstanceState.getString(STATE_CURRENT_URL, CHATGPT_URL);
            guideStepIndex = clampGuideStepIndex(savedInstanceState.getInt(STATE_GUIDE_STEP_INDEX, 0));
            introStepIndex = clampIntroStepIndex(savedInstanceState.getInt(STATE_INTRO_STEP_INDEX, 0));
            hasMainFrameLoadError = savedInstanceState.getBoolean(STATE_MAIN_FRAME_LOAD_ERROR, false);

            if (currentScreen == Screen.GPT) {
                showGptScreen(false, hasMainFrameLoadError);
            } else if (currentScreen == Screen.INTRO) {
                showIntroScreen();
            } else {
                showHomeScreen();
            }
            return true;
        } catch (RuntimeException ignored) {
            currentScreen = Screen.HOME;
            currentUrl = CHATGPT_URL;
            guideStepIndex = 0;
            introStepIndex = 0;
            hasMainFrameLoadError = false;
            return false;
        }
    }

    private int clampGuideStepIndex(int index) {
        if (index < 0) {
            return 0;
        }

        int lastIndex = GUIDE_TITLES.length - 1;
        return Math.min(index, lastIndex);
    }

    private int clampIntroStepIndex(int index) {
        if (index < 0) {
            return 0;
        }

        return Math.min(index, 2);
    }

    private void saveCurrentWebUrl() {
        if (chatWebView != null && chatWebView.getUrl() != null) {
            currentUrl = chatWebView.getUrl();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
