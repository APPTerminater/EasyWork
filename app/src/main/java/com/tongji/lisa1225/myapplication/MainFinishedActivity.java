package com.tongji.lisa1225.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import com.astuetz.PagerSlidingTabStrip;
import com.tongji.lisa1225.myapplication.Application.MyLeanCloudApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFinishedActivity extends AppCompatActivity{
    private static final String TAG = "MainFinishedActivity";
    private static final int REQUEST_ADDPROGRAM = 0;

    private static final String NO_PROGRAM = "You have no project!";
    private static final String NO_TASK = "There is no task!";

    static final int BGColorNum = 5;
    static int[] BGColor = new int[BGColorNum];


    private AVQuery<AVObject> projectQuery = new AVQuery<>("ProjectInfo");
    private AVQuery<AVObject> taskQuery = new AVQuery<>("TaskInfo");
    //下拉框
    private ArrayAdapter<String> programAdapter;

    List<String> projectNameList = new ArrayList<>();//存储项目标题的列表
    List<String> projectIDList = new ArrayList<>();//存储项目ID的列表
    List<Boolean> projectDoingList = new ArrayList<>();//存储项目状态的列表
    static List<String> taskList = new ArrayList<>();//存储任务名称的列表
    static List<AVObject> taskTotalInfo = new ArrayList<>();//存储leancloud格式的任务列表
    static int currentPosition;//目前任务卡是第几个
    private AlertDialog deleteTaskDialog = null;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.programName) Spinner programSpinner;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.tabs) PagerSlidingTabStrip tabStrip;
    @BindView(R.id.pager) ViewPager pager;

    //用户信息
    static private String email,name,occupation,password;
    //项目信息
    private String projectName,projectID;
    static private boolean projectDoing;


    private MainFinishedActivity.MyPagerAdapter2 myPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //获取当前用户信息
        MyLeanCloudApp app = (MyLeanCloudApp)this.getApplication();
        email = app.getEmail();
        name = app.getUserName();
        occupation = app.getOccu();
        password = app.getPassword();

        setContentView(R.layout.activity_main_finished);
        ButterKnife.bind(this);

        //切换背景颜色
        BGColor[0] = getResources().getColor(R.color.pinkDarkBlue);
        BGColor[1] = getResources().getColor(R.color.pinkBlue);
        BGColor[2] = getResources().getColor(R.color.pinkGreen);
        BGColor[3] = getResources().getColor(R.color.pinkYellow);
        BGColor[4] = getResources().getColor(R.color.pinkRed);

        //侧边栏头部获取用户信息
        View nav_header = navigationView.inflateHeaderView(R.layout.nav_header);
        TextView user_name = nav_header.findViewById(R.id.user_name);
        TextView user_mail = nav_header.findViewById(R.id.user_mail);
        TextView user_occu = nav_header.findViewById(R.id.user_occu);
        user_name.setText(name);
        user_mail.setText(email);
        user_occu.setText(occupation);
        //侧边栏按钮
        navigationView.setItemBackgroundResource(R.color.grey);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                Intent mainIntent = new Intent(MainFinishedActivity.this,MainActivity.class);
                //各种操作
                switch (item.getItemId())
                {
                    case R.id.tuling:
                        startActivity(mainIntent);
                        finish();
                        break;
                    case R.id.nav_doing:
                        startActivity(mainIntent);
                        finish();
                        break;
                    case R.id.nav_finished://本页面，不需要跳转
                        break;
                    case R.id.nav_edit:
                        if(!projectNameList.get(0).equals(NO_PROGRAM))
                        {
                            Intent editProgramIntent = new Intent(MainFinishedActivity.this, EditProgramActivity.class);
                            editProgramIntent.putExtra("projectName", projectName);
                            startActivity(editProgramIntent);
                        }
                        else
                        {
                            Toast.makeText(MainFinishedActivity.this, "There is no project related to you!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_bin:
                        Intent binIntent = new Intent(MainFinishedActivity.this,BinActivity.class);
                        startActivity(binIntent);
                        finish();
                        break;
                    case R.id.nav_detail:
                        if (!projectNameList.get(0).equals(NO_PROGRAM)) {
                            Intent detailIntent = new Intent(MainFinishedActivity.this, ProgramInfoActivity.class);
                            detailIntent.putExtra("projectName", projectName);
                            detailIntent.putExtra("projectID", projectID);
                            startActivity(detailIntent);
                        } else
                        {
                            Toast.makeText(MainFinishedActivity.this, "There is no project related to you!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_restart:
                        if(!taskTotalInfo.isEmpty()&&projectDoing)
                        {
                            showDeleteTaskDialog();
                        }
                        else
                        {
                            Toast.makeText(MainFinishedActivity.this, "You can't undo the task now!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_about:
                        //Intent about = new Intent(MainFinishedActivity.this, AboutActivity.class);
                        //startActivity(about);
                        break;
                    case R.id.logout:
                        finish();
                        break;
                    default:
                }
                return true;
            }
        });

        toolbar.setNavigationIcon(R.mipmap.home);//设置导航栏图标
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        toolbar.inflateMenu(R.menu.toolbar_no_addbtn_menu);//设置右上角的填充菜单
        //toolbar的按钮点击
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
                if (menuItemId == R.id.action_notification) {
                    Toast.makeText(MainFinishedActivity.this, " ", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        if(occupation.equals("Producer")) //producer界面
        {
            projectQuery.whereEqualTo("producer", email);
        }
        else
        {
            final AVQuery<AVObject> member1Query = new AVQuery<>("ProjectInfo");
            member1Query.whereEqualTo("member1", email);

            final AVQuery<AVObject> member2Query = new AVQuery<>("ProjectInfo");
            member2Query.whereEqualTo("member2", email);

            projectQuery = AVQuery.or(Arrays.asList(member1Query, member2Query));
        }
        projectQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> projectInfo) {
                if(projectInfo.size()==0)
                {
                    projectNameList.add(NO_PROGRAM);
                    refreshProject();
                }
                else
                {
                    for(int i = 0;i<projectInfo.size();i++)
                    {
                        projectNameList.add(projectInfo.get(i).getString("projectName"));
                        projectIDList.add(projectInfo.get(i).getObjectId());
                        projectDoingList.add(!(projectInfo.get(i).getBoolean("testing")||projectInfo.get(i).getBoolean("finished")));
                    }
                    projectID = projectInfo.get(0).getObjectId();
                    projectName = projectInfo.get(0).getString("projectName");
                    projectDoing = !(projectInfo.get(0).getBoolean("testing")||projectInfo.get(0).getBoolean("finished"));
                    refreshProject();
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });

        //项目条目点击事件
        programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                projectName = programAdapter.getItem(position);
                if(!projectName.equals(NO_PROGRAM))
                {
                    projectID = projectIDList.get(position);
                    projectDoing = projectDoingList.get(position);

                }
                //Toast.makeText(MainFinishedActivity.this, projectName, Toast.LENGTH_SHORT).show();
                parent.setVisibility(View.VISIBLE);
                showTask();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showTask()
    {
        if(!projectName.equals(NO_PROGRAM)) {
            //显示任务
            taskQuery.whereEqualTo("projectID", projectID);
            taskQuery.whereEqualTo("finished", true);
            taskQuery.whereEqualTo("deleted", false);
            taskQuery.orderByAscending("checked");
            taskQuery.addAscendingOrder("ddl");
            taskQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
                public void onSubscribe(Disposable disposable) {
                }

                public void onNext(List<AVObject> taskInfo) {
                    taskTotalInfo = taskInfo;
                    int initsize = taskList.size();
                    if (taskInfo.size() == 0) {
                        taskList.add(NO_TASK);
                    } else {
                        for (int i = 0; i < taskInfo.size(); i++) {
                            taskList.add(taskInfo.get(i).getString("taskName"));
                        }
                    }
                    taskList = taskList.subList(initsize, taskList.size());
                    myPagerAdapter = new MainFinishedActivity.MyPagerAdapter2(getSupportFragmentManager());
                    setPaperAdapter();
                }

                public void onError(Throwable throwable) {
                }

                public void onComplete() {
                }
            });
        }
        else//没有项目的情况
        {
            if(taskList.isEmpty())
            {
                taskList.add(NO_TASK);
            }
            myPagerAdapter = new MyPagerAdapter2(getSupportFragmentManager());
            setPaperAdapter();
        }
    }

    private void showDeleteTaskDialog(){
        if(deleteTaskDialog == null) {
            AlertDialog.Builder deleteTaskBuilder = new AlertDialog.Builder(this);
            //deleteTaskBuilder.setIcon(R.drawable.picture);
            deleteTaskBuilder.setTitle("Are you sure to Undo this Task?");
            deleteTaskBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteTask();
                        }
                    });
            deleteTaskBuilder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            deleteTaskDialog = deleteTaskBuilder.create();
        }
        deleteTaskDialog.show();
    }

    private void deleteTask()
    {
        AVObject del = AVObject.createWithoutData("TaskInfo", taskTotalInfo.get(currentPosition).getObjectId());
        del.put("finished", false);
        del.saveInBackground().subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(AVObject todo) {
                // 成功保存之后，执行其他逻辑
                restartActivity(MainFinishedActivity.this);
            }
            public void onError(Throwable throwable) {
                // 异常处理
            }
            public void onComplete() {}
        });
    }
    public void restartActivity(Activity activity) {

        activity.finish();
        activity.startActivity(activity.getIntent());

    }

    private void setPaperAdapter()
    {
        pager.setAdapter(myPagerAdapter);
        myPagerAdapter.notifyDataSetChanged();

        ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                pager.setBackgroundColor(BGColor[i % BGColorNum]);
                tabStrip.setBackgroundColor(BGColor[i % BGColorNum]);
                currentPosition = i;
            }

            @Override
            public void onPageSelected(int i) {
                //pager.setBackgroundColor(BGColor[i%2]);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //pager.setBackgroundColor(BGColor[i%2]);
            }
        };

        // Bind the tabs to the ViewPager
        tabStrip.setViewPager(pager);
        tabStrip.setOnPageChangeListener(mPageChangeListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADDPROGRAM) {
            if (resultCode == RESULT_OK) {

                projectNameList.add(data.getStringExtra("projectName"));
                if(projectNameList.contains(NO_PROGRAM))
                {
                    projectNameList.remove(NO_PROGRAM);
                }
            }
        }
    }

    private void refreshProject()
    {
        programAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, projectNameList);
        programAdapter.notifyDataSetChanged();
        programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     //设置下拉列表框的下拉选项样式
        programSpinner.setAdapter(programAdapter);
    }

    public class MyPagerAdapter2 extends FragmentPagerAdapter {


        private MyPagerAdapter2(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return taskList.get(position);
        }

        @Override
        public int getCount() {
            return taskList.size();
        }

        @Override
        public long getItemId(int position) {
            return taskList.get(position).hashCode();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            //return new Fragment();

            return MainFinishedActivity.SuperAwesomeCardFragment.newInstance(position);
        }
    }
    static public class SuperAwesomeCardFragment extends Fragment {
        private static final String ARG_POSITION = "position";

        @BindView(R.id.taskType) TextView tvType;
        //@BindView(R.id.taskDDL) TextView tvDDL;
        @BindView(R.id.taskMember) TextView tvMember;
        @BindView(R.id.taskContent) TextView tvContent;
        @BindView(R.id.id_checkbox) CheckBox checkBox;

        private int position;

        public static MainFinishedActivity.SuperAwesomeCardFragment newInstance(int position) {
            MainFinishedActivity.SuperAwesomeCardFragment f = new MainFinishedActivity.SuperAwesomeCardFragment();
            Bundle b = new Bundle();
            b.putInt(ARG_POSITION, position);
            f.setArguments(b);
            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            position = getArguments().getInt(ARG_POSITION);
            Log.d(TAG,String.valueOf(position));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_card,container,false);
            ButterKnife.bind(this, rootView);
            ViewCompat.setElevation(rootView, 50);
            if(!taskTotalInfo.isEmpty())
            {
                tvType.setTextColor(BGColor[position%BGColorNum]);
                tvType.setText(taskTotalInfo.get(position).getString("type"));
                tvMember.setText("Executer: "+taskTotalInfo.get(position).getString("memberName"));
                checkBox.setText("Checked\nFinish it before: "+taskTotalInfo.get(position).getString("ddl"));
                tvContent.setText("Task Describition:\n"+taskTotalInfo.get(position).getString("content"));
                checkBox.setChecked(taskTotalInfo.get(position).getBoolean("checked"));
            }
            if(!occupation.equals("Producer")||!projectDoing)
            {
                checkBox.setClickable(false);
            }

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    if (!taskTotalInfo.isEmpty()) {
                        AVObject testObject = AVObject.createWithoutData("TaskInfo", taskTotalInfo.get(currentPosition).getObjectId());
                        if (checkBox.isChecked()) {
                            testObject.put("checked", true);
                        } else {
                            testObject.put("checked", false);
                        }
                        testObject.saveInBackground().subscribe(new Observer<AVObject>() {
                            public void onSubscribe(Disposable disposable) {
                            }

                            public void onNext(AVObject todo) {
                                // 成功保存之后，执行其他逻辑
                            }

                            public void onError(Throwable throwable) {
                                // 异常处理
                            }

                            public void onComplete() {
                            }
                        });
                    }
                }
            });
            return rootView;
        }

        @Override
        public void onPause()
        {
            super.onPause();
        }
    }

}
