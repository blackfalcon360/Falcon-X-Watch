package com.blackfalcon.falconxwatch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String[] WORDS = {"Bajaur", "responsibility", "TTP", "ISKP", "IMP"};
    private static final String QUERY = "Bajaur OR responsibility OR TTP OR ISKP OR IMP";
    private WebView web;
    private FrameLayout body;
    private LinearLayout browser;
    private ProgressBar progress;
    private TextView address;
    private SharedPreferences prefs;
    private int lang;
    private int tab;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("watch", MODE_PRIVATE);
        lang = prefs.getInt("language", 0);
        if (lang < 0 || lang > 2) lang = 0;
        LinearLayout root = column();
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutDirection(lang == 1 ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);
        TextView title = text("Falcon X Watch", 23);
        title.setTextColor(0xFFFFD54F);
        root.addView(title);
        LinearLayout tabs = new LinearLayout(this);
        Button feed = button(t("تازہ ٹویٹس", "Latest", "الأحدث"));
        Button info = button(t("معلومات", "Info", "معلومات"));
        feed.setOnClickListener(v -> showFeed());
        info.setOnClickListener(v -> showInfo());
        tabs.addView(feed, new LinearLayout.LayoutParams(0, -2, 1));
        tabs.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(tabs);
        ScrollView filterScroll = new ScrollView(this);
        LinearLayout filters = new LinearLayout(this);
        filters.setOrientation(LinearLayout.HORIZONTAL);
        android.widget.HorizontalScrollView horizontal = new android.widget.HorizontalScrollView(this);
        horizontal.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        Button all = button(t("سب", "All", "الكل"));
        all.setOnClickListener(v -> search(QUERY));
        filters.addView(all);
        for (String word : WORDS) {
            Button b = button(word);
            b.setOnClickListener(v -> search(word));
            filters.addView(b);
        }
        horizontal.addView(filters);
        root.addView(horizontal, new LinearLayout.LayoutParams(-1, -2));
        body = new FrameLayout(this);
        root.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView footer = text("By: Black Falcon 🦅", 15);
        footer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        root.addView(footer);
        setContentView(root);
        buildBrowser();
        showFeed();
        if (state == null || web.restoreState(state) == null) {
            if (prefs.getBoolean("online", false)) search(QUERY);
            else showInfo();
        }
    }

    private void buildBrowser() {
        browser = column();
        LinearLayout nav = new LinearLayout(this);
        Button back = button("‹");
        back.setContentDescription(t("واپس", "Back", "رجوع"));
        back.setOnClickListener(v -> { if (web.canGoBack()) web.goBack(); });
        Button refresh = button(t("تازہ کریں", "Refresh", "تحديث"));
        refresh.setOnClickListener(v -> web.reload());
        Button home = button(t("سرچ", "Search", "بحث"));
        home.setOnClickListener(v -> search(QUERY));
        nav.addView(back);
        nav.addView(refresh);
        nav.addView(home);
        browser.addView(nav);
        address = text("x.com", 12);
        address.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        browser.addView(address);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        browser.addView(progress, new LinearLayout.LayoutParams(-1, dp(3)));
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String scheme = request.getUrl().getScheme();
                if ("https".equalsIgnoreCase(scheme)) return false;
                Toast.makeText(MainActivity.this, t("صرف HTTPS لنکس کھلتے ہیں", "Only HTTPS links are supported", "روابط HTTPS فقط"), Toast.LENGTH_SHORT).show();
                return true;
            }
            @Override public void onPageFinished(WebView view, String url) {
                address.setText(url);
                CookieManager.getInstance().flush();
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    Toast.makeText(MainActivity.this, t("صفحہ نہیں کھلا؛ انٹرنیٹ چیک کریں اور تازہ کریں۔", "Page unavailable. Check internet and refresh.", "تعذر فتح الصفحة؛ تحقق من الإنترنت ثم حدث."), Toast.LENGTH_LONG).show();
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value == 100 ? View.GONE : View.VISIBLE);
            }
        });
        browser.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private void search(String query) {
        prefs.edit().putBoolean("online", true).apply();
        showFeed();
        Uri uri = Uri.parse("https://x.com/search").buildUpon()
                .appendQueryParameter("q", query)
                .appendQueryParameter("src", "typed_query")
                .appendQueryParameter("f", "live").build();
        web.loadUrl(uri.toString());
    }

    private void showFeed() {
        tab = 0;
        body.removeAllViews();
        body.addView(browser, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showInfo() {
        tab = 1;
        body.removeAllViews();
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = column();
        page.setPadding(dp(14), dp(12), dp(14), dp(12));
        page.setBackground(shape(0xFF161616));
        page.addView(text(t("X کی تازہ سرچ", "X latest search", "بحث X الأحدث"), 22));
        page.addView(text(t(
            "سب میں پانچوں الفاظ میں سے کوئی ایک تلاش ہوگا۔ الگ لفظ کا بٹن صرف اسی لفظ کو تلاش کرتا ہے۔ شروع کرنے سے انٹرنیٹ استعمال ہوگا۔ X لاگ اِن مانگ سکتا ہے یا اندرونی براؤزر روک سکتا ہے۔ لاگ اِن صرف اصل x.com صفحے پر کریں؛ اوپر مکمل URL دیکھیں۔",
            "All searches for any one of the five keywords. A keyword button searches only that word. Starting uses internet. X may require login or block embedded browsers. Only enter X credentials on the genuine x.com page; inspect the full URL above.",
            "الكل يبحث عن أي كلمة من الكلمات الخمس. زر الكلمة يبحث عنها وحدها. البدء يستخدم الإنترنت. قد يتطلب X تسجيل الدخول أو يمنع المتصفح المدمج. أدخل بيانات X في صفحة x.com الأصلية فقط وتحقق من الرابط أعلاه."), 16));
        page.addView(text(t(
            "نتائج X اپنی پالیسی، اکاؤنٹ اور دستیابی کے مطابق آتی ہیں؛ تمام ٹویٹس کی ضمانت نہیں۔ یہ خودکار الرٹ یا مسلسل نگرانی نہیں۔ نئے نتائج کے لیے تازہ کریں۔ آف لائن لائیو ٹویٹس دستیاب نہیں۔",
            "X controls results according to availability, account and policy. Not a complete archive or automatic alert service. Refresh to see newer results. Live posts are unavailable offline.",
            "يتحكم X بالنتائج حسب التوفر والحساب والسياسة. لا ضمان لجميع المنشورات ولا تنبيهات آلية. حدث لعرض النتائج الجديدة. المنشورات المباشرة غير متاحة دون الإنترنت."), 16));
        Button start = button(t("انٹرنیٹ کے ساتھ شروع کریں", "Start online", "ابدأ عبر الإنترنت"));
        start.setOnClickListener(v -> search(QUERY));
        page.addView(start);
        Button clear = button(t("لاگ آؤٹ / براؤزر ڈیٹا مٹائیں", "Log out / clear browser data", "خروج / مسح بيانات المتصفح"));
        clear.setOnClickListener(v -> {
            web.stopLoading();
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            android.webkit.WebStorage.getInstance().deleteAllData();
            web.clearCache(true);
            web.clearHistory();
            web.loadUrl("about:blank");
            prefs.edit().putBoolean("online", false).apply();
            Toast.makeText(this, t("ڈیٹا صاف کر دیا", "Browser data cleared", "تم مسح البيانات"), Toast.LENGTH_SHORT).show();
        });
        page.addView(clear);
        String[] langs = {"اردو", "English", "العربية"};
        for (int i = 0; i < langs.length; i++) {
            final int chosen = i;
            Button b = button(langs[i]);
            b.setOnClickListener(v -> { prefs.edit().putInt("language", chosen).apply(); recreate(); });
            page.addView(b);
        }
        scroll.addView(page);
        body.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        web.saveState(out);
        super.onSaveInstanceState(out);
    }
    @Override protected void onPause() {
        web.onPause();
        CookieManager.getInstance().flush();
        super.onPause();
    }
    @Override protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
    }
    @Override protected void onDestroy() {
        if (web != null) {
            browser.removeView(web);
            web.stopLoading();
            web.destroy();
        }
        super.onDestroy();
    }
    @Override public void onBackPressed() {
        if (tab == 1) showFeed();
        else if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
    private String t(String ur, String en, String ar) { return lang == 1 ? en : (lang == 2 ? ar : ur); }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }
    private GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(12));
        return d;
    }
    private TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
        return t;
    }
    private Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackground(shape(0xFF252525));
        b.setPadding(dp(10), dp(8), dp(10), dp(8));
        return b;
    }
}
