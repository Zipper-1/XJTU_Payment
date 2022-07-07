package edu.xjtu.zipper.xjtupayment.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import edu.xjtu.zipper.xjtupayment.R;
import edu.xjtu.zipper.xjtupayment.data.TokenHolder;
import edu.xjtu.zipper.xjtupayment.tool.TempTokenManager;
import edu.xjtu.zipper.xjtupayment.data.XJTUUser;
import edu.xjtu.zipper.xjtupayment.data.XjtuUserImportException;
import edu.xjtu.zipper.xjtupayment.ui.login.LoginActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static android.view.KeyEvent.*;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private TempTokenManager tempTokenManager;
    WebSettings webSettings;
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tempTokenManager = new TempTokenManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("gzp debug","id:");
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:")||url.startsWith("https:")){
                    //view.loadUrl(url);
                    return false;
                }
                Log.d("gzp debug","unknown link type");
                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                //view.loadUrl(url);
                return true;
            }
        });
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setDisplayZoomControls(false);
        webView.addJavascriptInterface(this,"android");
        webView.setWebViewClient(new WebViewClient(){
            @Override

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                view.reload();
            }

        });
        webView.onResume();
        tempTokenManager.getCode().observe(this, new Observer<TokenHolder>() {
            @Override
            public void onChanged(TokenHolder s) {
                if(s.getErrorType() == TokenHolder.ErrorType.expired)
                {
                    Toast.makeText(getApplicationContext(), "未登录或登录已失效", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
                while(s.getErrorType() == TokenHolder.ErrorType.failed){
                    tempTokenManager.refreshCode(Objects.requireNonNull(XJTUUser.getActiveUser().getValue()).getPersonToken());
                }
                webView.loadUrl("https://org.xjtu.edu.cn/h5/campuscard.html?comeAcc="+ XJTUUser.getActiveUser().getValue().getSno() +"&code="+s.getContent());
                Log.d("url","https://org.xjtu.edu.cn/h5/campuscard.html?comeAcc="+ XJTUUser.getActiveUser().getValue().getSno() +"&code="+s.getContent());
            }
        });
        XJTUUser.getActiveUser().observe(this, new Observer<XJTUUser>() {
            @Override
            public void onChanged(XJTUUser s) {
                tempTokenManager.refreshCode(s.getPersonToken());

            }
        });
        try {
            Log.d("load saved user","loading conf");
            FileInputStream loginInfoStream = this.openFileInput("info.json");
            byte[] buf = new byte[1024];
            int len = loginInfoStream.read(buf);
            loginInfoStream.close();
            JSONObject loginInfoJson = new JSONObject(new String(buf,0,len));
            Log.d("load data",loginInfoJson.toString());
            XJTUUser.importJson(loginInfoJson,true);
        } catch (FileNotFoundException e) {
            Log.d("load f","file not found");
            Toast.makeText(getApplicationContext(), "请先登录", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } catch (IOException | JSONException e) {
            Log.d("debug", Arrays.toString(e.getStackTrace()));
        } catch (XjtuUserImportException e) {
            Toast.makeText(getApplicationContext(), "读取保存数据失败，请重新登录", Toast.LENGTH_LONG).show();
            Log.w("load error",e.getMessage());
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
