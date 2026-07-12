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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String CHATGPT_URL = "https://chatgpt.com/?locale=ko-KR";
    private static final String GITHUB_ISSUE_URL = "https://github.com/mann-lab-apps/myoung-sun/issues/new";
    private static final Map<String, String> KOREAN_HEADERS = new HashMap<String, String>() {{
        put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.6,en;q=0.5");
    }};
    private static final String[] GUIDE_TITLES = {
            "화면 확인",
            "첫 대화 시작하기",
            "로그인하기",
            "이전 대화 열기",
            "쉽게 정리하기",
            "안 되면 Chrome"
    };
    private static final String[] GUIDE_BODIES = {
            "ChatGPT 화면이 보이면 준비됐습니다.",
            "먼저 입력칸에 한 문장만 적어보세요.",
            "이전 대화가 필요할 때만 로그인하세요.",
            "왼쪽 위 메뉴에서 지난 대화를 찾으세요.",
            "마지막에 “쉽게 정리해줘”라고 부탁하세요.",
            "로그인이 막히면 Chrome으로 여세요."
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
    private int guideStepIndex = 0;
    private TextView guideProgressText;
    private TextView guideTitleText;
    private TextView guideBodyText;
    private Button guidePrevButton;
    private Button guideNextButton;
    private Button guideExternalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(Locale.KOREA);
        showHomeScreen();
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
        if (chatWebView != null && chatWebView.getUrl() != null) {
            currentUrl = chatWebView.getUrl();
        }

        if (currentScreen == Screen.GPT) {
            showGptScreen(false);
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
        introButton.setOnClickListener(v -> showIntroScreen());
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
        guideList.addView(createHomeItem(
                "순서 정리",
                "곧 추가됩니다.",
                false,
                null
        ));
        guideList.addView(createHomeItem(
                "도움 요청",
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

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(Color.rgb(251, 248, 241));
        screen.addView(createStackHeader("앱 소개", "이 앱은 이렇게 써요"));

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
                "어려운 일을 고르면, 큰 글씨로 한 단계씩 도와드려요.",
                22,
                Color.rgb(51, 64, 61)
        );
        intro.setPadding(0, 0, 0, dp(16));
        root.addView(intro);

        root.addView(createIntroExample());

        root.addView(createIntroStep("1. 가이드 선택", "홈에서 필요한 도움을 고릅니다."));
        root.addView(createIntroStep("2. 가이드", "큰 글씨를 보며 천천히 따라 합니다."));
        root.addView(createIntroStep("3. 의견 전달", "불편한 점은 오른쪽 아래 의견 버튼으로 보냅니다."));

        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        screen.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        setContentView(wrapWithFeedbackButton(screen));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showGptScreen(boolean resetToChatGptHome) {
        currentScreen = Screen.GPT;
        if (resetToChatGptHome) {
            currentUrl = CHATGPT_URL;
            guideStepIndex = 0;
        }
        destroyChatWebView();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(251, 248, 241));

        root.addView(createStackHeader("GPT와 대화하기", "한 단계씩 천천히"));

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
        errorPanel.setVisibility(View.GONE);
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

        setContentView(wrapWithFeedbackButton(root));
        webFrame.postDelayed(() -> {
            if (chatWebView != null && currentScreen == Screen.GPT) {
                loadChatGpt(currentUrl != null ? currentUrl : CHATGPT_URL);
            }
        }, 250);
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
                    showErrorPanel();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                currentUrl = url;
                loading.setVisibility(View.GONE);
                hideErrorPanel();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView popupWebView = new WebView(MainActivity.this);
                popupWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView popupView, WebResourceRequest request) {
                        openExternal(request.getUrl().toString());
                        return true;
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

    private View createIntroExample() {
        LinearLayout example = new LinearLayout(this);
        example.setOrientation(LinearLayout.VERTICAL);
        example.setPadding(0, dp(4), 0, dp(14));

        TextView heading = createText("예시로 보면", 24, Color.rgb(23, 33, 31));
        heading.setPadding(0, 0, 0, dp(8));
        example.addView(heading);

        example.addView(createExampleBox("가이드 선택", "GPT와 대화하기", "시작하기"));
        example.addView(createExampleBox("가이드", "1 / 6  화면 확인", "다음"));
        example.addView(createExampleBox("의견 전달", "오른쪽 아래 의견 버튼", "만마에에게 보내기"));
        return example;
    }

    private View createExampleBox(String title, String body, String action) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackground(createGuideItemBackground(true));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(5), 0, dp(5));
        box.setLayoutParams(params);

        TextView titleView = createText(title, 20, Color.rgb(143, 47, 29));
        TextView bodyView = createText(body, 22, Color.rgb(23, 33, 31));
        bodyView.setPadding(0, dp(4), 0, dp(8));

        box.addView(titleView);
        box.addView(bodyView);
        box.addView(createPillLabel(action, true));
        return box;
    }

    private View createIntroStep(String title, String body) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(14), dp(10), dp(14), dp(10));
        row.setBackgroundColor(Color.rgb(255, 253, 248));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(5), 0, dp(5));
        row.setLayoutParams(params);

        TextView stepTitle = createText(title, 21, Color.rgb(23, 33, 31));
        TextView stepBody = createText(body, 17, Color.rgb(51, 64, 61));
        stepBody.setPadding(0, dp(4), 0, 0);

        row.addView(stepTitle);
        row.addView(stepBody);
        return row;
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

        TextView body = createText("불편한 점을 적으면 개발자 만마에에게 전달됩니다.", 19, Color.rgb(51, 64, 61));
        body.setPadding(0, dp(8), 0, dp(12));
        panel.addView(body);

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

        Button sendButton = createButton("만마에에게 보내기");
        sendButton.setOnClickListener(v -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty()) {
                input.setError("내용을 적어주세요.");
                return;
            }

            dialog.dismiss();
            openExternal(createFeedbackIssueUrl(message));
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
            shownWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        input.requestFocus();
        input.postDelayed(() -> {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }

    private String createFeedbackIssueUrl(String message) {
        String body = "## 피드백\n\n" + message + "\n\n## 화면\n\n" + getCurrentScreenName();
        return Uri.parse(GITHUB_ISSUE_URL)
                .buildUpon()
                .appendQueryParameter("title", "[피드백] 앱 사용 의견")
                .appendQueryParameter("body", body)
                .build()
                .toString();
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
        TextView subtitle = createText(subtitleText, 16, Color.rgb(51, 64, 61));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.addView(title);
        copy.addView(subtitle);

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

        guidePrevButton = createSecondaryButton("이전");
        guidePrevButton.setOnClickListener(v -> {
            if (guideStepIndex > 0) {
                guideStepIndex--;
                updateGuideText();
            }
        });

        guideNextButton = createButton("다음");
        guideNextButton.setOnClickListener(v -> {
            if (guideStepIndex < GUIDE_TITLES.length - 1) {
                guideStepIndex++;
                updateGuideText();
            }
        });

        LinearLayout.LayoutParams prevParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        prevParams.setMargins(0, dp(4), dp(6), 0);
        guideNav.addView(guidePrevButton, prevParams);

        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        nextParams.setMargins(dp(6), dp(4), 0, 0);
        guideNav.addView(guideNextButton, nextParams);
        panel.addView(guideNav);

        guideExternalButton = createButton("Chrome에서 열기");
        guideExternalButton.setOnClickListener(v -> openExternal(CHATGPT_URL));
        panel.addView(guideExternalButton);

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

        TextView body = createText("네트워크 또는 로그인 보안 문제일 수 있습니다. Chrome에서 열기를 눌러 계속 진행하세요.", 19, Color.rgb(51, 64, 61));
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, dp(14), 0, dp(14));

        Button externalButton = createButton("Chrome에서 열기");
        externalButton.setOnClickListener(v -> openExternal(CHATGPT_URL));

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

        guideProgressText.setText((guideStepIndex + 1) + " / " + GUIDE_TITLES.length);
        guideTitleText.setText(GUIDE_TITLES[guideStepIndex]);
        guideBodyText.setText(GUIDE_BODIES[guideStepIndex]);

        if (guidePrevButton != null) {
            boolean canGoBack = guideStepIndex > 0;
            guidePrevButton.setEnabled(canGoBack);
            guidePrevButton.setAlpha(canGoBack ? 1f : 0.45f);
        }

        if (guideNextButton != null) {
            boolean canGoNext = guideStepIndex < lastIndex;
            guideNextButton.setEnabled(canGoNext);
            guideNextButton.setAlpha(canGoNext ? 1f : 0.45f);
            guideNextButton.setText(canGoNext ? "다음" : "끝");
        }

        if (guideExternalButton != null) {
            guideExternalButton.setVisibility(guideStepIndex == lastIndex ? View.VISIBLE : View.GONE);
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

    private Button createTinyButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(Color.rgb(23, 33, 31));
        button.setBackgroundColor(Color.rgb(232, 226, 214));
        button.setAllCaps(false);
        button.setMinHeight(dp(44));

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

        chatWebView.loadUrl(url, KOREAN_HEADERS);
    }

    private void openExternal(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
