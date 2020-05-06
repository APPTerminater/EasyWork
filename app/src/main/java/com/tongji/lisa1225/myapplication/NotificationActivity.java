package com.tongji.lisa1225.myapplication;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tongji.lisa1225.myapplication.Application.MyLeanCloudApp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class NotificationActivity extends AppCompatActivity{
    private static final String TAG = "NotificationActivity";

    private String pattern = "yyyy-MM-dd";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.taskType) TextView tvType;
    @BindView(R.id.projectName) TextView tvProject;
    @BindView(R.id.taskName) TextView tvName;
    @BindView(R.id.taskMember) TextView tvMember;
    @BindView(R.id.taskDDL) TextView tvDDL;
    @BindView(R.id.Subscribe) TextView tvContent;
    private String projectID,projectName,taskType,taskName,taskMemberEmail,taskDDL,taskContent,taskID;
    private String email,occupation;

    private SimpleDateFormat sdf = new SimpleDateFormat(pattern);

    private AVObject testObject = new AVObject("TaskInfo");
    private AVQuery<AVObject> taskQuery = new AVQuery<>("TaskInfo");
    private AVQuery<AVObject> projectQuery = new AVQuery<>("ProjectInfo");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);

        MyLeanCloudApp app = (MyLeanCloudApp)this.getApplication();
        email = app.getEmail();
        occupation = app.getOccu();

        if(occupation.equals("Producer"))
        {
            taskQuery.whereEqualTo("producer", email);
            taskQuery.whereEqualTo("finished", true);
            taskQuery.whereEqualTo("checked", false);
        }
        else
        {
            taskQuery.whereEqualTo("member", email);
            taskQuery.whereEqualTo("finished", false);
        }
        taskQuery.whereEqualTo("deleted",false);
        taskQuery.orderByAscending("ddl");

        taskQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> taskInfo) {
                int i = 0;
                while (i<taskInfo.size())
                {
                    try {
                        taskDDL = taskInfo.get(i).getString("ddl");
                        Date ddl = sdf.parse(taskDDL);
                        Date now = new Date();
                        if((ddl.getTime() + 1000*60*60*24) >= now.getTime()||occupation.equals("Producer")){
                            projectID = taskInfo.get(i).getString("projectID");
                            tvProject.setText("ProjectName:"+taskInfo.get(i).getString("projectName"));
                            tvType.setText("TaskType:"+taskInfo.get(i).getString("type"));
                            tvName.setText("TaskName:"+taskInfo.get(i).getString("taskName"));
                            tvDDL.setText("Finish it before:"+taskInfo.get(i).getString("ddl"));
                            tvDDL.setTextColor(getResources().getColor(R.color.pinkRed));
                            tvMember.setText("Executor:"+taskInfo.get(i).getString("member"));
                            tvContent.setText("Task Describition:"+taskInfo.get(i).getString("content"));
                            break;
                        }
                        else
                        {
                            i++;
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });

        toolbar.setNavigationIcon(R.mipmap.home);//设置导航栏图标
        toolbar.setTitle("Notification");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
