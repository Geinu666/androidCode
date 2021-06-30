package com.example.splashdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import WebKit.Bean.LoginBean;
import WebKit.Bean.LoginData;
import WebKit.Service.LoginService;
import WebKit.RetrofitFactory;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    //供SignupActivity操作的实例
    public static LoginActivity instance = null;
    private EditText accountLoginText;
    private EditText passwordLoginText;
    private TextView signupText;
    private Button loginButton;
    private String act;
    private String psw;
    private LoginService loginService;
    private Boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        instance = this;
        accountLoginText = (EditText) findViewById(R.id.accountLoginText);
        passwordLoginText = (EditText) findViewById(R.id.passwordLoginText);
        signupText = (TextView) findViewById(R.id.signup_text);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginService = RetrofitFactory.getLoginService(getApplicationContext());
        LightStatusBarUtils.setAndroidNativeLightStatusBar(this, true);
        //跳转注册的逻辑
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String transAct = accountLoginText.getText().toString().trim();
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                intent.putExtra("actTrans", transAct);
                startActivity(intent);
                overridePendingTransition(R.anim.rightin_enter, R.anim.no_anim);
            }
        });

        //设置账号栏输入限制
        accountLoginText.setKeyListener(new DigitsKeyListener(Locale.CHINA) {
            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }

            @Override
            protected char[] getAcceptedChars() {
                char[] data = getStringData(R.string.login_only_can_input).toCharArray();
                return data;
            }
        });


    }

    /**
     * 登录按钮的点击事件
     *
     * @param view Button
     */
    public void onClick(View view) {
        act = accountLoginText.getText().toString().trim();
        psw = passwordLoginText.getText().toString().trim();
        login(act, psw);
    }

    /**
     * 发送登录请求
     * @param account 账号栏输入
     * @param password 密码栏输入
     */
    public void login(String account, String password) {
        Call<LoginBean> call = loginService.postLogin(account, password);
        call.enqueue(new Callback<LoginBean>() {
            @Override
            public void onResponse(Call<LoginBean> call, Response<LoginBean> response) {
                if (response.isSuccessful()) {
                    LoginBean result = response.body();
                    String cookies = response.headers().get("Set-Cookie");
                    SharedPreferences.Editor config = getApplicationContext().getSharedPreferences("config", getApplicationContext().MODE_PRIVATE).edit();
                    config.putString("cookie", cookies);
                    config.commit();
                    //同步cookies到全局WebView
                    syncCookie("http://119.91.130.198/api/", cookies);
                    loginButton = (Button) findViewById(R.id.loginButton);
                    loginButton.setText("login success");
                }
            }

            @Override
            public void onFailure(Call<LoginBean> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "登陆失败！", Toast.LENGTH_SHORT).show();
            }
        });
        Log.i("loginPOST", String.valueOf(isLogin));
        Log.i("loginPOST", "before return");
    }

    /**
     * 修改过场动画
     */
    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.rightout_exit);
    }

    /**
     * 登陆成功，抓取Cookie同步到全局WebView
     * @param url
     * @param cookie
     */
    public void syncCookie(String url, String cookie) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookie();
        cookieManager.setCookie(url, cookie);
//        CookieSyncManager.getInstance().sync();
        Log.i("test", "cookie sync" + cookie);
    }

    public String getStringData(int id) {
        return getResources().getString(id);
    }
}