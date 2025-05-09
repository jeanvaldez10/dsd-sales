package dsd.dsdvansales.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;

public class MintIFrameActivity extends Activity {

    WebView mWebView;
    boolean bFinished = false;
    String mStrReference = "";
    float mRequestedAmount = 0f;
    boolean bCallbackFinished = false;
    String mStrCallbackURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mint_iframe);
        //Picasso.with(this).load(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.DSDVanSales/logo/Cmpylogo.bmp")).into((ImageView)findViewById(R.id.imgLogo));
        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Here put your code
                Log.d("My Webview", url);
                if(Uri.parse(url).getHost().equals("www.samsbooks.com") && bFinished)
                {
                    Intent intent = new Intent();
                    intent.putExtra("url", url);
                    intent.putExtra("reference", mStrReference);
                    intent.putExtra("amount", mRequestedAmount);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    return false;
                }
                // return true; //Indicates WebView to NOT load the url;
                return false; //Allow WebView to load url
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                bFinished = true;
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        Intent intent = getIntent();
        if(intent != null)
        {
            mStrReference = intent.getStringExtra("reference");
            mRequestedAmount = intent.getFloatExtra("amount", 0);
            if(mRequestedAmount == 0)
            {
                setResult(Activity.RESULT_CANCELED);
                finish();
                return;
            }

            String strBaseUrl = Utils.getStringSetting(this, Global.PREF_MINTHPPURL);
            String strUserName = Utils.getStringSetting(this, Global.PREF_MINTHPPUSER);
            String strCurrency = Utils.getStringSetting(this, Global.PREF_MINTHPPCURR);
            String strAmount = String.format(Locale.ENGLISH, "%.2f", mRequestedAmount);
            String strSecretKey = Utils.getStringSetting(this, Global.PREF_MINTHPPSECRET);
            String hashValue = mStrReference + ":" + strAmount + ":" + strCurrency + ":" + "https://www.samsbooks.com/payment/callback:true";
            String hash = "";
            try {
                hash = calcHmac(hashValue, strSecretKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String strUrl = strBaseUrl + "/" + strUserName + "/" + mStrReference + "/" + strCurrency + "/" + strAmount + "/" + hash + "?return_path=https://www.samsbooks.com/payment/callback&show_email=false&show_extras=false&surcharge=true&iframe=true";
            String strImgElement = "data:image/png;base64,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";
            mWebView.loadData("<body><center><img src=\"" + strImgElement + "\" height=\"100\"><center><br><iframe src=\""+ strUrl+"\" width=\"100%\" height=\"100%\" style=\"border:none;\"></iframe></body>", "text/html; charset=UTF-8", null);
            //mWebView.loadUrl(strUrl);
        }
        else
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    public String calcHmac(String src, String secret) throws Exception {

        Mac mac = Mac.getInstance("HmacMD5");
        SecretKeySpec sk = new SecretKeySpec(secret.getBytes(),mac.getAlgorithm());
        mac.init(sk);
        byte[] result = mac.doFinal(src.getBytes());

        StringBuffer hash = new StringBuffer();

        for (int i=0; i<result.length; i++) {
            String hex = Integer.toHexString(0xFF &  result[i]);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }
        String sEncodedString = hash.toString();

        return sEncodedString;
    }
}
