package com.tongji.lisa1225.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.tongji.lisa1225.myapplication.Application.MyLeanCloudApp;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.List;

public class ProgramTestActivity extends AppCompatActivity {
    private static final String TAG = "ProgramTestActivity";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.test1) CheckBox ckTest1;
    @BindView(R.id.test2) CheckBox ckTest2;
    @BindView(R.id.test3) CheckBox ckTest3;
    @BindView(R.id.test4) CheckBox ckTest4;
    @BindView(R.id.test5) CheckBox ckTest5;
    @BindView(R.id.btn_submit) Button _submitButton;

    private AVObject testObject = new AVObject("ProjectInfo");
    private AVQuery<AVObject> projectQuery = new AVQuery<>("ProjectInfo");
    private AVQuery<AVObject> queryUser = new AVQuery<>("UserInfo");

    AlertDialog dialog = null;

    private String email,name,occupation,password;
    private String projectName, email1,email2;

    private String oriProjectName,projectID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        MyLeanCloudApp app = (MyLeanCloudApp)this.getApplication();
        email = app.getEmail();
        name = app.getUserName();
        occupation = app.getOccu();
        password = app.getPassword();

        Intent intent = getIntent();
        //设置项目名称文字
        projectID = intent.getStringExtra("projectID");
        oriProjectName = intent.getStringExtra("projectName");

        projectQuery.whereEqualTo("projectName", oriProjectName);
        projectQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> projectInfo) {
                if(projectInfo.size()==1)
                {
                    ckTest1.setChecked(projectInfo.get(0).getBoolean("test1"));
                    ckTest2.setChecked(projectInfo.get(0).getBoolean("test2"));
                    ckTest3.setChecked(projectInfo.get(0).getBoolean("test3"));
                    ckTest4.setChecked(projectInfo.get(0).getBoolean("test4"));
                    ckTest5.setChecked(projectInfo.get(0).getBoolean("test5"));
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });


        toolbar.setNavigationIcon(R.mipmap.home);//设置导航栏图标
        toolbar.setTitle(oriProjectName);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ckTest1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                AVObject testObject = AVObject.createWithoutData("ProjectInfo", projectID);
                if (ckTest1.isChecked()) {
                    testObject.put("test1", true);
                } else {
                    testObject.put("test1", false);
                }
                testObject.saveInBackground().subscribe(new Observer<AVObject>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(AVObject todo) {// 成功保存之后，执行其他逻辑
                    }
                    public void onError(Throwable throwable) {// 异常处理
                    }
                    public void onComplete() {}
                });
            }
        });
        ckTest2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                AVObject testObject = AVObject.createWithoutData("ProjectInfo", projectID);
                if (ckTest2.isChecked()) {
                    testObject.put("test2", true);
                } else {
                    testObject.put("test2", false);
                }
                testObject.saveInBackground().subscribe(new Observer<AVObject>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(AVObject todo) {// 成功保存之后，执行其他逻辑
                    }
                    public void onError(Throwable throwable) {// 异常处理
                    }
                    public void onComplete() {}
                });
            }
        });
        ckTest3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                AVObject testObject = AVObject.createWithoutData("ProjectInfo", projectID);
                if (ckTest3.isChecked()) {
                    testObject.put("test3", true);
                } else {
                    testObject.put("test3", false);
                }
                testObject.saveInBackground().subscribe(new Observer<AVObject>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(AVObject todo) {// 成功保存之后，执行其他逻辑
                    }
                    public void onError(Throwable throwable) {// 异常处理
                    }
                    public void onComplete() {}
                });
            }
        });
        ckTest4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                AVObject testObject = AVObject.createWithoutData("ProjectInfo", projectID);
                if (ckTest4.isChecked()) {
                    testObject.put("test4", true);
                } else {
                    testObject.put("test4", false);
                }
                testObject.saveInBackground().subscribe(new Observer<AVObject>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(AVObject todo) {// 成功保存之后，执行其他逻辑
                    }
                    public void onError(Throwable throwable) {// 异常处理
                    }
                    public void onComplete() {}
                });
            }
        });
        ckTest5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                AVObject testObject = AVObject.createWithoutData("ProjectInfo", projectID);
                if (ckTest5.isChecked()) {
                    testObject.put("test5", true);
                } else {
                    testObject.put("test5", false);
                }
                testObject.saveInBackground().subscribe(new Observer<AVObject>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(AVObject todo) {// 成功保存之后，执行其他逻辑
                    }
                    public void onError(Throwable throwable) {// 异常处理
                    }
                    public void onComplete() {}
                });
            }
        });


        //点击提交按钮
        _submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ckTest1.isChecked()&&ckTest2.isChecked()&&ckTest3.isChecked()&&ckTest4.isChecked()&&ckTest5.isChecked()) {
                    submit();
                }
                else
                {
                    onNotFinished();
                }
            }
        });
    }

    public void submit() {
        Log.d(TAG, "Submit");


        _submitButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(ProgramTestActivity.this,
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        editProject();
                        // On complete call either onLoginSuccess or onLoginFailed
                        progressDialog.dismiss();
                    }
                }, 1000);
    }

    private void onNotFinished()
    {
        Toast.makeText(getBaseContext(), "Please check all these items!", Toast.LENGTH_LONG).show();
    }

    //成功添加项目
    private void onSubmitSuccess() {
        _submitButton.setEnabled(true);
        if (MainActivity.instance != null) {
            MainActivity.instance.finish();
        }
        //返回项目界面
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onSubmitFailed() {
        Toast.makeText(getBaseContext(), "Edit project error!", Toast.LENGTH_LONG).show();
        _submitButton.setEnabled(true);
    }

    private void editProject()
    {
        AVObject testObject = AVObject.createWithoutData("ProjectInfo", projectID);
        testObject.put("testing", false);
        testObject.put("finished", true);
        testObject.put("test1",false);
        testObject.put("test2",false);
        testObject.put("test3",false);
        testObject.put("test4",false);
        testObject.put("test5",false);
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
