package com.tongji.lisa1225.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import butterknife.ButterKnife;
import butterknife.BindView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;

    private AVQuery<AVObject> query = new AVQuery<>("UserInfo");
    private String email,name,occupation,password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //点击登录按钮
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        //点击去注册按钮
        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // 判断输入的账号密码是否正确
                        queryEmailAndPassword(email,password);
                        // On complete call either onLoginSuccess or onLoginFailed
                        progressDialog.dismiss();
                        _loginButton.setEnabled(true);
                    }
                }, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // By default we just finish the Activity and log them in automatically
                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                //启动
                startActivity(mainIntent);
                //this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    //账号密码正确
    public void onLoginSuccess() {
        MyLeanCloudApp app = (MyLeanCloudApp)this.getApplication();
        app.setUserInfo(email, name, occupation, password);
        _loginButton.setEnabled(true);
        //不同职业进入不同界面
        if(occupation.equals("Producer"))
        {
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            //启动
            startActivity(mainIntent);
        }
        else
        {
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            //启动
            startActivity(mainIntent);
        }
       // finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "This email or password is not correct!", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public void onNoAccount() {
        Toast.makeText(getBaseContext(), "This account is not existed!", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    //查询此邮箱是否已经注册过，若注册过则判断密码是否正确
    public void queryEmailAndPassword(final String mail, final String pass)
    {
        query.whereEqualTo("email", mail);
        query.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> userInfo) {
                if(userInfo.size()!=0)
                {
                    if(pass.equals(userInfo.get(0).getString("password")))
                    {
                        email = userInfo.get(0).getString("email");
                        name = userInfo.get(0).getString("name");
                        occupation = userInfo.get(0).getString("occu");
                        password = userInfo.get(0).getString("password");
                        onLoginSuccess();
                    }
                    else
                    {
                        onLoginFailed();
                    }
                }
                else
                {
                    onNoAccount();
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }
}