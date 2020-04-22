package com.tongji.lisa1225.myapplication;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class AddTaskActivity extends AppCompatActivity {
    private static final String TAG = "AddTaskActivity";

    private String pattern = "yyyy-MM-dd";

    @BindView(R.id.projectName) TextView tvProject;
    @BindView(R.id.taskName) EditText etName;
    @BindView(R.id.taskMember) EditText etMember;
    @BindView(R.id.taskDDL) EditText etDDL;
    @BindView(R.id.Subscribe) EditText etContent;
    @BindView(R.id.btn_submit) Button _submitButton;
    private String projectName,taskName,taskMemberEmail,taskDDL,taskContent;

    private AVObject testObject = new AVObject("TaskInfo");
    private AVQuery<AVObject> taskQuery = new AVQuery<>("TaskInfo");
    private AVQuery<AVObject> projectQuery = new AVQuery<>("ProjectInfo");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtask);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        projectName = intent.getStringExtra("projectName");
        tvProject.setText(projectName);

        final Calendar c = Calendar.getInstance();     //创建日期选择对象
        //点击输入日期事件
        etDDL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示弹窗
                DatePickerDialog dialog = new DatePickerDialog(AddTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c.set(year, monthOfYear, dayOfMonth);
                        etDDL.setText(DateFormat.format(pattern, c));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        //点击提交按钮
        _submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                taskName = etName.getText().toString();
                taskMemberEmail = etMember.getText().toString();
                taskDDL = etDDL.getText().toString();
                taskContent = etContent.getText().toString();
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
        //_submitButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(AddTaskActivity.this,
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // 判断输入的任务名在项目中是否有重复、完成的成员是否在项目中
                        //todo
                        isNameDuplicate();
                        progressDialog.dismiss();
                    }
                }, 1000);
    }
    public boolean validate() {
        boolean convertSuccess = true;
        //第一步，判断任务名是否有效
        if (taskName.isEmpty() || taskName.length() < 3 || taskName.length() > 10) {
            etName.setError("between 4 and 10 alphanumeric characters");
            convertSuccess = false;
        } else {
            etName.setError(null);
        }
        //第二步，判断邮箱是否有效
        if (taskMemberEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(taskMemberEmail).matches()) {
            etMember.setError("enter a valid email address");
            convertSuccess = false;
        } else {
            etMember.setError(null);
        }
        // 第三步，判断截止日期是否有效//指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            format.setLenient(false);// 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            format.parse(taskDDL);

        } catch (ParseException e) {
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            etDDL.setError("enter a valid date");
            convertSuccess = false;
        }
        return convertSuccess;
    }
    // 判断输入的任务名在项目中是否有重复
    private void isNameDuplicate()
    {
        taskQuery.whereEqualTo("taskName", taskName);
        taskQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> TaskInfo) {
                if(TaskInfo.size() != 0)
                {
                    for (int i = 0;i < TaskInfo.size();i++)
                    {
                        if(projectName.equals(TaskInfo.get(i).getString("projectName")))
                        {
                            onDuplicateName();
                            return;
                        }
                    }
                }
                isMemberInProject();
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }
    // 判断完成的成员是否在项目中
    private void isMemberInProject()
    {
        projectQuery.whereEqualTo("projectName", projectName);
        projectQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> ProjectInfo) {
                if(taskMemberEmail.equals(ProjectInfo.get(0).getString("member1"))
                        ||taskMemberEmail.equals(ProjectInfo.get(0).getString("member2"))
                        ||taskMemberEmail.equals(ProjectInfo.get(0).getString("member3")))
                {
                    addProject();
                }
                else
                {
                    onNoUser();
                }

            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }

    public void addProject()
    {
        testObject.put("projectName", projectName);
        testObject.put("taskName",taskName);
        testObject.put("member", taskMemberEmail);
        testObject.put("ddl",taskDDL);
        testObject.put("content",taskContent);
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

    //成功添加任务，返回主界面
    private void onSubmitSuccess() {
        _submitButton.setEnabled(true);
        //返回项目界面
        Intent intent = new Intent();
        intent.putExtra("taskName", taskName);
        setResult(RESULT_OK, intent);
        finish();
    }
    //添加失败
    private void onSubmitFailed() {
        Toast.makeText(getBaseContext(), "Add task error!", Toast.LENGTH_LONG).show();
        _submitButton.setEnabled(true);
    }
    //项目中已有这个任务名了
    private void onDuplicateName()
    {
        Toast.makeText(getBaseContext(), "This task name has been existed!", Toast.LENGTH_LONG).show();
        _submitButton.setEnabled(true);
    }
    //项目中没有这个成员
    private void onNoUser()
    {
        Toast.makeText(getBaseContext(), "No one in this project uses email:"+taskMemberEmail, Toast.LENGTH_LONG).show();
        _submitButton.setEnabled(true);
    }


}
