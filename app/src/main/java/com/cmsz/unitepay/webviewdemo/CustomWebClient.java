package com.cmsz.unitepay.webviewdemo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.MailTo;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by clark on 17/9/20.
 */

public class CustomWebClient extends WebViewClient {
    private Activity mContext;
    private WebView mWebView;

    public CustomWebClient(Activity context, WebView webView) {
        this.mContext = context;
        this.mWebView = webView;
    }



    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("about:")) {
            return super.shouldOverrideUrlLoading(view, url);
        }
        if (url.contains("mailto:")) {
            MailTo mailTo = MailTo.parse(url);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailTo.getTo()});
            intent.putExtra(Intent.EXTRA_TEXT, mailTo.getBody());
            intent.putExtra(Intent.EXTRA_SUBJECT, mailTo.getSubject());
            intent.putExtra(Intent.EXTRA_CC, mailTo.getCc());
            intent.setType("message/rfc822");
            mContext.startActivity(intent);
            view.reload();
            return true;
        } else if (url.startsWith("intent://")) {
            Intent intent;

            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException ex) {
                return false;
            }
            if (intent != null) {
                try {
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
//                            Log.e(Constants.TAG, "ActivityNotFoundException");
                }
                return true;
            }
        }

        boolean ret = startActivityForUrl(mWebView, url);
        if (!ret) {
                /*
                mWebView.onPause();
				mWebView.pauseTimers();
				if(lastWebView!=null) {
					lastWebView.destroy();
				}

				lastWebView=mWebView;
				cachedWebView.add(mWebView);
				openNewView(url);
				return true;
				*/
        }

        if (url.endsWith(".mp3")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "audio/*");
            view.getContext().startActivity(intent);
            return true;
        } else if (url.endsWith(".mp4") || url.endsWith(".3gp")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            view.getContext().startActivity(intent);
            return true;
        }
        return ret;
    }


    public boolean startActivityForUrl(WebView tab, String url) {


        Intent intent;
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException ex) {
            Log.w("Browser", "Bad URI " + url + ": " + ex.getMessage());
            return false;
        }
        //应用未安装打开应用市场
        if (mContext.getPackageManager().resolveActivity(intent, 0) == null) {
            String packagename = intent.getPackage();
            if (packagename != null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:"
                        + packagename));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                mContext.startActivity(intent);
                return true;
            } else {
                return false;
            }
        }
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setComponent(null);
        if (tab != null) {
            intent.putExtra(mContext.getPackageName() + ".Origin", 1);
        }

        Matcher m = ACCEPTED_URI_SCHEMA.matcher(url);
        if (m.matches() && !isSpecializedHandlerAvailable(intent)) {

            return false;
        }
        try {
            if (mContext.startActivityIfNeeded(intent, -1)) {

                return true;
            }
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean isSpecializedHandlerAvailable(Intent intent) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> handlers = pm.queryIntentActivities(intent,
                PackageManager.GET_RESOLVED_FILTER);
        if (handlers == null || handlers.isEmpty()) {
            return false;
        }
        for (ResolveInfo resolveInfo : handlers) {
            IntentFilter filter = resolveInfo.filter;
            if (filter == null) {
                // No intent filter matches this intent?
                // Error on the side of staying in the browser, ignore
                continue;
            }
            // NOTICE: Use of && instead of || will cause the browser
            // to launch a new intent for every URL, using OR only
            // launches a new one if there is a non-browser app that
            // can handle it.
            if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) {
                // Generic handler, skip
                continue;
            }
            return true;
        }
        return false;
    }

    static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)"
            + // switch on case insensitive matching
            "("
            + // begin group for schema
            "(?:http|https|file):\\/\\/" + "|(?:inline|data|about|javascript):" + "|(?:.*:.*@)"
            + ")" + "(.*)");
}
