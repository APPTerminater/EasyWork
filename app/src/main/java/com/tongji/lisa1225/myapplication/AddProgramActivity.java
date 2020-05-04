package com.tongji.lisa1225.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tongji.lisa1225.myapplication.Application.MyLeanCloudApp;

import java.util.List;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import butterknife.ButterKnife;
import butterknife.BindView;

public class AddProgramActivity extends AppCompatActivity {
    private static final String TAG = "AddProgramActivity";

    @BindView(R.id.programName) EditText _programNameText;
    @BindView(R.id.member1) EditText _memberText1;
    @BindView(R.id.member2) EditText _memberText2;
    @BindView(R.id.btn_submit) Button _submitButton;

    private AVObject testObject = new AVObject("ProjectInfo");
    private AVQuery<AVObject> query = new AVQuery<>("ProjectInfo");
    private AVQuery<AVObject> queryUser = new AVQuery<>("UserInfo");

    AlertDialog dialog = null;

    private String email,name,occupation,password;
    private String projectName,member1,member2;
    private String email1,email2;

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
                projectName = _programNameText.getText().toString();
                email1 = _memberText1.getText().toString();
                email2 = _memberText2.getText().toString();
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


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // 判断输入的账号密码是否正确
                        //todo
                        checkEmail1();
                        // On complete call either onLoginSuccess or onLoginFailed
                        progressDialog.dismiss();
                    }
                }, 1000);
    }

    public boolean validate() {
        boolean valid = true;

        String projectName = _programNameText.getText().toString();
        String email1 = _memberText1.getText().toString();
        String email2 = _memberText2.getText().toString();

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

        if (projectName.isEmpty() || projectName.length() < 4 || projectName.length() > 10) {
            _programNameText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _programNameText.setError(null);
        }

        return valid;
    }

    private void checkEmail1()
    {
        queryUser.whereEqualTo("email", email1);
        queryUser.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> UserInfo) {
                if(UserInfo.size()==1)
                {
                    member1 = UserInfo.get(0).getString("name");
                    if(!email2.isEmpty())
                    {
                        checkEmail2();
                    }
                    else
                    {
                        showDialog();
                    }
                }
                else
                {
                    onNoUser(email1);
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }

    private void checkEmail2()
    {
        queryUser.whereEqualTo("email", email2);
        queryUser.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> UserInfo) {
                if(UserInfo.size()==1)
                {
                    member2 = UserInfo.get(0).getString("name");
                    showDialog();

                }
                else
                {
                    onNoUser(email2);
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }

    private void onNoUser(String email)
    {
        Toast.makeText(getBaseContext(), "No user uses email:"+email, Toast.LENGTH_LONG).show();
        _submitButton.setEnabled(true);
    }

    //todo:https://blog.csdn.net/weixin_34194551/article/details/94238275
    private void showDialog(){
        if(dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setIcon(R.drawable.picture);
            builder.setTitle("Are you sure to add this Program?");
            builder.setMessage("Program Name: " + projectName + "\n"
                    + "Producer: " + name + "\n"
                    + "Member1:" + member1 + "\n"
                    + "Member2:" + member2 + "\n");
            builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            queryProjectName(projectName, email1, email2);
                            _submitButton.setEnabled(true);
                        }
                    });
            builder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            _submitButton.setEnabled(true);
                        }
                    });
            dialog = builder.create();
        }
        dialog.show();

    }


    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        finish();
        //moveTaskToBack(true);
    }

    //成功添加项目
    private void onSubmitSuccess() {
        _submitButton.setEnabled(true);
        //返回项目界面
        if (MainActivity.instance != null) {
            MainActivity.instance.finish();
        }
        Intent mainIntent = new Intent(AddProgramActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();

        // finish();
    }

    private void onSubmitFailed() {
        Toast.makeText(getBaseContext(), "Add project error!", Toast.LENGTH_LONG).show();
        _submitButton.setEnabled(true);
    }

    public void onDuplicateProject() {
        Toast.makeText(getBaseContext(), "This project name has been existed!", Toast.LENGTH_LONG).show();

        _submitButton.setEnabled(true);
    }

    //查询此项目是否已经登记过，若没有则进行登记
    public void queryProjectName(final String projectName, final String member1, final String member2)
    {
        query.whereEqualTo("projectName", projectName);
        query.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> projectInfo) {
                if(projectInfo.size()==0)
                {
                    addProject(projectName,member1,member2);
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
    public void addProject(final String projectName, final String member1, final String member2)
    {
        testObject.put("projectName", projectName);
        testObject.put("producer",email);
        testObject.put("member1", member1);
        testObject.put("member2",member2);
        testObject.put("finished",false);
        testObject.put("testing",false);
        testObject.put("test1",false);
        testObject.put("test2",false);
        testObject.put("test3",false);
        testObject.put("test4",false);
        testObject.put("test5",false);


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
