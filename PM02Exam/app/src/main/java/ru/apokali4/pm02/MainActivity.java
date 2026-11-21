package ru.apokali4.pm02;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.view.ViewGroup;

public class MainActivity extends Activity {
    private WebView exam;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exam = new WebView(this);
        exam.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        exam.getSettings().setJavaScriptEnabled(true);
        exam.getSettings().setDomStorageEnabled(true);
        exam.getSettings().setAllowFileAccess(true);
        exam.getSettings().setAllowContentAccess(false);
        exam.setWebViewClient(new WebViewClient());
        exam.setWebChromeClient(new WebChromeClient());
        setContentView(exam);
        exam.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onBackPressed() {
        if (exam != null && exam.canGoBack()) exam.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (exam != null) {
            exam.stopLoading();
            exam.destroy();
        }
        super.onDestroy();
    }
}
