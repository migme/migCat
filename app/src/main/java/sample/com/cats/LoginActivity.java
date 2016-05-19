package sample.com.cats;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import sample.com.cats.HttpTask.HTTP_TASK;

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String MyTOKEN = "MyToken";



    public static final String CLIENT_ID = "309f818242abae8fdd1b";
    public static final String REDIRECT_URI = "http://localhost:34876/oauth/callback";
    private static final String SCOPES = "test-scope";

    private WebView mWebView;
    private String mAuthCode;
    private String mToken;

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Uri uri = new Uri.Builder().scheme("https")
                .authority("oauth.mig.me")
                .appendPath("oauth")
                .appendPath("auth")
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("scope", SCOPES)
                .appendQueryParameter("response_type", "code")
                .build();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, getApplicationContext().MODE_PRIVATE);
        mToken = sharedpreferences.getString(MyTOKEN, "");

        if (mToken.length() > 0) {
            Log.d(TAG, "Get token from DB, " + mToken);
            toMainActivity();
        } else {
            mWebView = (WebView) findViewById(R.id.webview);
            mWebView.loadUrl(uri.toString());
            mWebView.setWebViewClient(mWebViewClient);
        }
    }

    private void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setToken(String token) {
        mToken = token;
        mHandler.sendEmptyMessage(HTTP_TASK.TOKEN.ordinal());
    }

    private void writeTokenToSharePreference() {
        sharedpreferences.edit().putString(MyTOKEN, mToken).commit();
    }

    private void handleMessage2(Message msg) {
        HTTP_TASK what = HTTP_TASK.values()[msg.what];
        switch (what) {
            case AUTH:
                Log.i(TAG, "Get AuthCode than request Token");
                new requestToken().executeOnExecutor(Executors.newCachedThreadPool());
                break;
            case TOKEN:
                writeTokenToSharePreference();
                toMainActivity();
                break;
        }
    }

    WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "url: " + url);
            if (getAuthCode(url))
                return true;
            else
                return super.shouldOverrideUrlLoading(view, url);
        }

        public void onPageFinished(WebView view, String url) {

        }
    };

    private Boolean getAuthCode(String url) {
        if (url.contains(REDIRECT_URI + "?code")) {
            mAuthCode = url.substring((REDIRECT_URI + "?code").length() + 1);
            mHandler.sendEmptyMessage(HTTP_TASK.AUTH.ordinal());
            return true;
        }
        return false;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleMessage2(msg);
        }
    };

    private AlertDialog getAlertDialog(String message) {
        LinearLayout layout = new LinearLayout(this);
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(10);
        LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(tv, pm);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(20, 20, 20, 20);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    public class requestToken extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
                NetworkManager networkManager = new NetworkManager();
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("code", mAuthCode.trim()));
                nameValuePair.add(new BasicNameValuePair("client_id", LoginActivity.CLIENT_ID));
                nameValuePair.add(new BasicNameValuePair("redirect_uri", LoginActivity.REDIRECT_URI));
                nameValuePair.add(new BasicNameValuePair("grant_type", "authorization_code"));
                ServerResponse response = networkManager.postData("https://oauth.mig.me/oauth/token", nameValuePair);
                if (response.getStatusCode() == HttpStatus.SC_OK) {
                    Log.d(TAG, "OK");
                    Log.d(TAG, response.getJsonObj().toString());
                    if (response.getJsonObj() instanceof JSONObject) {
                        JSONObject jObj = (JSONObject) response.getJsonObj();
                        result = jObj.getString("access_token");
                    }
                } else {
                    Log.d(TAG, "NOT OK");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String token) {
            Log.i(TAG, "Get token");
            setToken(token);
        }
    }

}
