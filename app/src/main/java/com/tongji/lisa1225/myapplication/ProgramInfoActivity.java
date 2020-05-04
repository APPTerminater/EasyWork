package com.tongji.lisa1225.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tongji.lisa1225.myapplication.Application.MyLeanCloudApp;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.List;
public class ProgramInfoActivity extends AppCompatActivity{
    private static final String TAG = "ProgramInfoActivity";
    //用户信息
    static private String email,name,occupation,password;
    private String projectID,projectName;
    private int checkedTask = 0,totalTask;
    private Boolean testing = false,finished = false;

    //@BindView(R.id.projectName) TextView tvProject;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.producer) TextView tvProducer;
    @BindView(R.id.member1) TextView tvMember1;
    @BindView(R.id.member2) TextView tvMember2;
    @BindView(R.id.taskCount) TextView tvCount;
    @BindView(R.id.btn_change) Button btnChange;


    private AVQuery<AVObject> projectQuery = new AVQuery<>("ProjectInfo");
    private AVQuery<AVObject> taskQuery = new AVQuery<>("TaskInfo");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //获取当前用户信息
        MyLeanCloudApp app = (MyLeanCloudApp) this.getApplication();
        email = app.getEmail();
        name = app.getUserName();
        occupation = app.getOccu();
        password = app.getPassword();

        setContentView(R.layout.activity_programinfo);
        if (!occupation.equals("Producer")) //producer界面
        {
            btnChange.setVisibility(View.INVISIBLE);
        }
        ButterKnife.bind(this);
        Intent intent = getIntent();
        //设置项目名称文字
        projectID = intent.getStringExtra("projectID");
        projectName = intent.getStringExtra("projectName");

        //查询项目里的任务总数和完成的任务总数
        taskQuery.whereEqualTo("projectID", projectID);
        taskQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}

            public void onNext(List<AVObject> taskInfo) {
                totalTask = taskInfo.size();
                for(int i = 0;i<taskInfo.size();i++)
                {
                    if(taskInfo.get(i).getBoolean("checked"))
                    {
                        checkedTask++;
                    }
                }
                if(!testing&&!finished)
                {
                    tvCount.setText("Progress:" + checkedTask + "(finished)/" + totalTask + "(total)");
                }

            }

            public void onError(Throwable throwable) {}

            public void onComplete() {}
        });

        projectQuery.getInBackground(projectID).subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(AVObject ProjectInfo) {
                tvProducer.setText("Producer:"+ProjectInfo.getString("producer"));
                tvMember1.setText("Member1:"+ProjectInfo.getString("member1"));
                tvMember2.setText("Member2:"+ProjectInfo.getString("member2"));
                testing = ProjectInfo.getBoolean("testing");
                finished = ProjectInfo.getBoolean("finished");

                if(testing)
                {
                    tvCount.setText(getResources().getString(R.string.progress_testing));
                    btnChange.setText(getResources().getString(R.string.testing));
                }
                if(finished)
                {
                    tvCount.setText(getResources().getString(R.string.progress_finished));
                    btnChange.setText(getResources().getString(R.string.restart_project));
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });



        toolbar.setNavigationIcon(R.mipmap.home);//设置导航栏图标
        toolbar.setTitle(projectName);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //点击提交按钮
        btnChange.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //任务完成，重新开始任务
                if(finished)
                {
                    restartProject();
                }
                else if(testing)
                {
                    goToTest();
                }
                else if(totalTask>0&&totalTask == checkedTask)
                {
                    startTest();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Please finish all the tasks!", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void restartProject()
    {
        AVObject testObject = AVObject.createWithoutData("ProjectInfo", projectID);
        testObject.put("finished", false);
        testObject.put("testing", false);
        testObject.saveInBackground().subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(AVObject todo) {
                // 成功保存之后，执行其他逻辑
                onSubmitSuccess();
            }
            public void onError(Throwable throwable) {
                // 异常处理
            }
            public void onComplete() {}
        });
    }

    private void goToTest()
    {
        Intent testIntent = new Intent(ProgramInfoActivity.this,ProgramTestActivity.class);
        testIntent.putExtra("projectName", projectName);
        testIntent.putExtra("projectID", projectID);
        startActivity(testIntent);
        finish();
    }

    private void startTest()
    {
        AVObject testObject = AVObject.createWithoutData("ProjectInfo", projectID);
        //testObject.put("finished", false);
        testObject.put("testing", true);
        testObject.saveInBackground().subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(AVObject todo) {
                // 成功保存之后，执行其他逻辑
                testing = true;
                tvCount.setText(getResources().getString(R.string.progress_testing));
                btnChange.setText(getResources().getString(R.string.testing));
            }
            public void onError(Throwable throwable) {
                // 异常处理
            }
            public void onComplete() {}
        });

    }

    private void onSubmitSuccess()
    {
        btnChange.setEnabled(true);
        if (MainActivity.instance != null) {
            MainActivity.instance.finish();
        }
        //返回项目界面
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
