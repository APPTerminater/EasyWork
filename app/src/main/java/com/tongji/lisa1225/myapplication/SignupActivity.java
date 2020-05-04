package com.tongji.lisa1225.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tongji.lisa1225.myapplication.Application.MyLeanCloudApp;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    @BindView(R.id.spinner) Spinner _occuSpinner;
    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    private ArrayAdapter<String> adapter;
    private String email,name,occupation,password;
    private AVQuery<AVObject> query = new AVQuery<>("UserInfo");
    private AVObject testObject = new AVObject("UserInfo");



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        String[] ctype = new String[]{"Producer", "Engineer", "Artist"};
        //创建一个数组适配器
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ctype);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     //设置下拉列表框的下拉选项样式

        _occuSpinner.setAdapter(adapter);
        //条目点击事件
        _occuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                occupation = adapter.getItem(position);
                Toast.makeText(SignupActivity.this, occupation, Toast.LENGTH_SHORT).show();
                parent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setVisibility(View.VISIBLE);
            }
        });



        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        name = _nameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        queryEmailExist(email,name,occupation,password);

                        progressDialog.dismiss();
                    }
                }, 1000);
    }


    public void onSignupSuccess() {

        MyLeanCloudApp app = (MyLeanCloudApp)this.getApplication();
        app.setUserInfo(email, name, occupation, password);
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        //Intent mainIntent = new Intent(SignupActivity.this, MainActivity.class);
        //启动
        //startActivity(mainIntent);
        finish();
    }

    public void onEmailExist()
    {
        Toast.makeText(getBaseContext(), "This email has already existed!", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

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

    //查询此邮箱是否已经注册过，若没有则进行注册
    public void queryEmailExist(final String email, final String name, final String occu, final String password)
    {
        query.whereEqualTo("email", email);
        query.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> user) {
                // user 是包含满足条件的 UserInfo 对象的数组
                if(user.size() == 1)
                {
                    onEmailExist();
                }
                else
                {
                    insertUserInfo(email, name, occu, password);
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }

    //进行注册
    public void insertUserInfo(final String email, final String name, final String occu, final String password)
    {
        testObject.put("email", email);
        testObject.put("name", name);
        testObject.put("occu",occu);
        testObject.put("password",password);

        //testObject.saveInBackground().blockingSubscribe();
        // 将对象保存到云端
        testObject.saveInBackground().subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(AVObject todo) {
                // 成功保存之后，执行其他逻辑
                onSignupSuccess();
            }
            public void onError(Throwable throwable) {
                // 异常处理
                onSignupFailed();
            }
            public void onComplete() {}
        });
    }
}
