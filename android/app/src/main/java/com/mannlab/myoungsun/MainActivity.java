package com.mannlab.myoungsun;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String CHATGPT_URL = "https://chatgpt.com/";
    private WebView chatWebView;
    private LinearLayout errorPanel;
    private String currentUrl = CHATGPT_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (chatWebView != null && chatWebView.getUrl() != null) {
            currentUrl = chatWebView.getUrl();
        }
        buildLayout();
    }

    @Override
    public void onBackPressed() {
        if (chatWebView != null && chatWebView.canGoBack()) {
            chatWebView.goBack();
            return;
        }

        super.onBackPressed();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(251, 248, 241));

        root.addView(createHeader());

        LinearLayout content = new LinearLayout(this);
        boolean wideLayout = getResources().getConfiguration().screenWidthDp >= 700;
        content.setOrientation(wideLayout ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        content.setPadding(dp(10), dp(10), dp(10), dp(10));
        content.setGravity(Gravity.CENTER);

        FrameLayout webFrame = new FrameLayout(this);
        LinearLayout.LayoutParams webParams = wideLayout
                ? new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.25f)
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        webParams.setMargins(0, 0, wideLayout ? dp(10) : 0, wideLayout ? 0 : dp(10));

        chatWebView = new WebView(this);
        WebSettings settings = chatWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(chatWebView, true);

        chatWebView.setWebViewClient(new WebViewClient() {
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
                hideErrorPanel();
            }
        });

        chatWebView.setWebChromeClient(new WebChromeClient() {
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
        chatWebView.loadUrl(currentUrl != null ? currentUrl : CHATGPT_URL);

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
        content.addView(createGuidePanel(wideLayout), wideLayout
                ? new LinearLayout.LayoutParams(dp(330), ViewGroup.LayoutParams.MATCH_PARENT)
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(250)));

        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        setContentView(root);
    }

    private View createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(10), dp(14), dp(10));
        header.setBackgroundColor(Color.rgb(255, 253, 248));

        TextView title = new TextView(this);
        title.setText("명순  ·  GPT와 대화하기");
        title.setTextColor(Color.rgb(23, 33, 31));
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("ChatGPT 웹 화면을 크게 열고, 아래 안내를 보며 천천히 진행하세요.");
        subtitle.setTextColor(Color.rgb(51, 64, 61));
        subtitle.setTextSize(16);
        subtitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.addView(title);
        copy.addView(subtitle);

        header.addView(copy, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        return header;
    }

    private LinearLayout createGuidePanel(boolean wideLayout) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(14), dp(14), dp(14));
        panel.setBackgroundColor(Color.rgb(255, 253, 248));

        TextView heading = new TextView(this);
        heading.setText("지금 할 일");
        heading.setTextColor(Color.rgb(23, 33, 31));
        heading.setTextSize(wideLayout ? 28 : 23);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        panel.addView(heading);

        addStep(panel, "1. ChatGPT 화면이 열렸는지 확인하기");
        addStep(panel, "2. 로그인 또는 계속하기 버튼 누르기");
        addStep(panel, "3. 입력칸에 한 문장 적기");
        addStep(panel, "4. 이전 대화는 목록에서 다시 열기");
        addStep(panel, "5. 마지막에 쉬운 말로 정리 요청하기");

        Button externalButton = createButton("Chrome에서 열기");
        externalButton.setOnClickListener(v -> openExternal(CHATGPT_URL));
        panel.addView(externalButton);

        Button reloadButton = createButton("다시 불러오기");
        reloadButton.setOnClickListener(v -> {
            hideErrorPanel();
            chatWebView.reload();
        });
        panel.addView(reloadButton);

        TextView safety = new TextView(this);
        safety.setText("비밀번호, 인증번호, API key는 이 앱이 받거나 저장하지 않습니다.");
        safety.setTextColor(Color.rgb(143, 47, 29));
        safety.setTextSize(16);
        safety.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        safety.setPadding(0, dp(10), 0, 0);
        panel.addView(safety);

        return panel;
    }

    private LinearLayout createErrorPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(24), dp(24), dp(24), dp(24));
        panel.setBackgroundColor(Color.rgb(255, 253, 248));

        TextView title = new TextView(this);
        title.setText("화면을 불러오지 못했어요.");
        title.setTextColor(Color.rgb(143, 47, 29));
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView body = new TextView(this);
        body.setText("네트워크 또는 로그인 보안 문제일 수 있습니다. Chrome에서 열기를 눌러 계속 진행하세요.");
        body.setTextColor(Color.rgb(51, 64, 61));
        body.setTextSize(19);
        body.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, dp(14), 0, dp(14));

        Button externalButton = createButton("Chrome에서 열기");
        externalButton.setOnClickListener(v -> openExternal(CHATGPT_URL));

        panel.addView(title);
        panel.addView(body);
        panel.addView(externalButton);
        return panel;
    }

    private void addStep(LinearLayout panel, String text) {
        TextView step = new TextView(this);
        step.setText(text);
        step.setTextColor(Color.rgb(23, 33, 31));
        step.setTextSize(18);
        step.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        step.setPadding(0, dp(8), 0, dp(8));
        panel.addView(step);
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
