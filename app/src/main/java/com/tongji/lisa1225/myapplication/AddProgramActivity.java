package com.tongji.lisa1225.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import butterknife.ButterKnife;
import butterknife.BindView;

public class AddProgramActivity extends AppCompatActivity {
    private static final String TAG = "AddProgramActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.programName) EditText _programNameText;
    @BindView(R.id.member1) EditText _memberText1;
    @BindView(R.id.member2) EditText _memberText2;
    @BindView(R.id.member3) EditText _memberText3;
    @BindView(R.id.btn_submit) Button _submitButton;
    //@BindView(R.id.link_signup) TextView _signupLink;

    private AVObject testObject = new AVObject("ProjectInfo");
    private AVQuery<AVObject> query = new AVQuery<>("ProjectInfo");
    private String email,name,occupation,password;
    private String projectName,member1,member2,member3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_addprogram);
        ButterKnife.bind(this);

        MyLeanCloudApp app = (MyLeanCloudApp)this.getApplication();
        email = app.getEmail();
        name = app.getUserName();
        occupation = app.getOccu();
        password = app.getPassword();

        //点击提交按钮
        _submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    public void submit() {
        Log.d(TAG, "Submit");

        if (!validate()) {
            onSubmitFailed();
            return;
        }

        _submitButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(AddProgramActivity.this,
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        projectName = _programNameText.getText().toString();
        member1 = _memberText1.getText().toString();
        member2 = _memberText2.getText().toString();
        member3 = _memberText3.getText().toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // 判断输入的账号密码是否正确
                        //todo
                        queryProjectName(projectName,member1,member2,member3);
                        // On complete call either onLoginSuccess or onLoginFailed
                        progressDialog.dismiss();
                    }
                }, 1000);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    //成功添加项目
    public void onSubmitSuccess() {
        _submitButton.setEnabled(true);
        //返回项目界面
        Intent intent = new Intent();
        intent.putExtra("projectName", projectName);
        setResult(RESULT_OK, intent);
        finish();

        // finish();
    }

    public void onSubmitFailed() {
        Toast.makeText(getBaseContext(), "Add project error!", Toast.LENGTH_LONG).show();
        _submitButton.setEnabled(true);
    }

    public void onDuplicateProject() {
        Toast.makeText(getBaseContext(), "This project name has been existed!", Toast.LENGTH_LONG).show();

        _submitButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String projectName = _programNameText.getText().toString();
        String email1 = _memberText1.getText().toString();
        String email2 = _memberText2.getText().toString();
        String email3 = _memberText3.getText().toString();

        if (email1.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _memberText1.setError("enter a valid email address");
            valid = false;
        } else {
            _memberText1.setError(null);
        }
        if (!email2.isEmpty()) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _memberText2.setError("enter a valid email address");
                valid = false;
            } else {
                _memberText2.setError(null);
            }
        }
        if (!email3.isEmpty()) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _memberText3.setError("enter a valid email address");
                valid = false;
            } else {
                _memberText3.setError(null);
            }
        }

        if (projectName.isEmpty() || password.length() < 4 || password.length() > 10) {
            _programNameText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _programNameText.setError(null);
        }

        return valid;
    }

    //查询此项目是否已经登记过，若没有则进行登记
    public void queryProjectName(final String projectName, final String member1, final String member2, final String member3)
    {
        query.whereEqualTo("projectName", projectName);
        query.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> projectInfo) {
                if(projectInfo.size()==0)
                {
                    addProject(projectName,member1,member2,member3);
                }
                else
                {
                    onDuplicateProject();
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }
    public void addProject(final String projectName, final String member1, final String member2, final String member3)
    {
        testObject.put("projectName", projectName);
        testObject.put("producer",email);
        testObject.put("member1", member1);
        testObject.put("member2",member2);
        testObject.put("member3",member3);
        testObject.put("finished",false);

        // 将对象保存到云端
        testObject.saveInBackground().subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(AVObject todo) {
                // 成功保存之后，执行其他逻辑
                onSubmitSuccess();
            }
            public void onError(Throwable throwable) {
                // 异常处理
                onSubmitFailed();
            }
            public void onComplete() {}
        });
    }
}
