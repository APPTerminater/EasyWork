package com.tongji.lisa1225.myapplication;

import com.laocaixw.layout.SuspendButtonLayout;
import com.tongji.lisa1225.myapplication.Adapter.MainRVAdapter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ADDPROGRAM = 0;
    private static final int REQUEST_ADDTASK = 1;

    private static final String NO_PROGRAM = "You have no project!";
    private static final String NO_TASK = "You have no task!";

    final int BGColorNum = 5;
    //RecyclerView相关
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private MainRVAdapter rvAdapter;
    private GridLayoutManager mLayoutManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final int PAGE_COUNT = 10;
    private int lastVisibleItem = 0;

    private AVQuery<AVObject> projectQuery = new AVQuery<>("ProjectInfo");
    private AVQuery<AVObject> taskQuery = new AVQuery<>("TaskInfo");
    //下拉框
    private ArrayAdapter<String> programAdapter;
    List<String> projectList = new ArrayList<>();
    List<String> taskList = new ArrayList<>();

    //悬浮按钮
    public String[] suspendChildButtonInfo = {"相机", "音乐", "地图", "亮度", "联系人", "短信"};

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.programName) Spinner programSpinner;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.tabs) PagerSlidingTabStrip tabStrip;
    @BindView(R.id.pager) ViewPager pager;

    //用户信息
    private String email,name,occupation,password;
    //项目信息
    private String projectName;

    private MyPagerAdapter myPagerAdapter;

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

        if(occupation.equals("Producer"))
        {
            setContentView(R.layout.activity_main);

        }
        else
        {
            setContentView(R.layout.activity_engineer);
        }
        ButterKnife.bind(this);

        //切换背景颜色
        final int[] BGColor = {getResources().getColor(R.color.pinkDarkBlue),
                getResources().getColor(R.color.pinkBlue),
                getResources().getColor(R.color.pinkGreen),
                getResources().getColor(R.color.pinkYellow),
                getResources().getColor(R.color.pinkRed)};


        //获取用户信息
        View nav_header = navigationView.inflateHeaderView(R.layout.nav_header);
        TextView user_name = nav_header.findViewById(R.id.user_name);
        TextView user_mail = nav_header.findViewById(R.id.user_mail);
        TextView user_occu = nav_header.findViewById(R.id.user_occu);
        user_name.setText(name);
        user_mail.setText(email);
        user_occu.setText(occupation);

        navigationView.setItemBackgroundResource(R.color.pinkGreen);
        //navigationView.setCheckedItem(R.id.nav_guardian);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                //各种操作
                switch (item.getItemId())
                {
                    case R.id.nav_guardian:
                        //Intent guardian = new Intent(MainActivity.this, GuardianActivity.class);
                        //startActivity(guardian);
                        break;
                    case R.id.nav_changetheme:
                        /*if (isNight)
                        {
                            isNight = false;
                            recreate();
                        }
                        else
                        {
                            isNight = true;
                            recreate();
                        }*/
                        break;
                    case R.id.tuling:
                        //Intent tuling = new Intent(MainActivity.this,TulingActivity.class);
                        //startActivity(tuling);
                        break;
                    case R.id.weather:
                        //Intent weather = new Intent(MainActivity.this,WeatherActivity.class);
                        //startActivity(weather);
                        break;
                    case R.id.nav_favorites:
                        //Intent favorites = new Intent(MainActivity.this, LoveActivity.class);
                        //startActivity(favorites);
                        break;
                    case R.id.nav_news:
                        //Intent news = new Intent(MainActivity.this, NewsActivity.class);
                        //startActivity(news);
                        break;
                    case R.id.nav_github:
                        //Intent github = new Intent(MainActivity.this, GithubActivity.class);
                        //startActivity(github);
                        break;
                    case R.id.nav_about:
                        //Intent about = new Intent(MainActivity.this, AboutActivity.class);
                        //startActivity(about);
                        break;
                    case R.id.banben:
                        //Toasty.info(MainActivity.this, "你觉得可能有新版本吗", Toast.LENGTH_SHORT, true).show();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
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

        //toolbar.setLogo(R.mipmap.home);//设置app logo
        //toolbar.setTitle(" "+name+" ");//设置主标题
        //toolbar.setTitleTextColor(getResources().getColor(R.color.darkBlue));
        //toolbar.setSubtitle(" "+occupation+" ");//设置子标题
        //toolbar.setSubtitleTextColor(getResources().getColor(R.color.pinkDarkBlue));

        toolbar.inflateMenu(R.menu.base_toolbar_menu);//设置右上角的填充菜单
        //toolbar的按钮点击
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
                if (menuItemId == R.id.action_search) {
                    Toast.makeText(MainActivity.this , " " , Toast.LENGTH_SHORT).show();

                } else if (menuItemId == R.id.action_notification) {
                    Toast.makeText(MainActivity.this, " ", Toast.LENGTH_SHORT).show();
                }else if (menuItemId == R.id.action_add) {
                    Intent intent = new Intent(getApplicationContext(), AddProgramActivity.class);
                    startActivityForResult(intent, REQUEST_ADDPROGRAM);
                }
                return true;
            }
        });

       //toolbar下拉项目选择
            //String[] ctype = new String[]{"Program1", "Program2222", "Program3333333"};
            //创建一个数组适配器
            //programAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, ctype);
            //programAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, projectList);
        projectQuery.whereEqualTo("producer", email);
        projectQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> projectInfo) {
                if(projectInfo.size()==0)
                {
                    projectList.add(NO_PROGRAM);
                    refreshProject();
                }
                else
                {
                    for(int i = 0;i<projectInfo.size();i++)
                    {
                        projectList.add(projectInfo.get(i).getString("projectName"));
                    }
                    projectName = projectInfo.get(0).getString("projectName");
                    refreshProject();
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
            //programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     //设置下拉列表框的下拉选项样式
            //programSpinner.setAdapter(programAdapter);
        //项目条目点击事件
        programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                projectName = programAdapter.getItem(position);
                Toast.makeText(MainActivity.this, projectName, Toast.LENGTH_SHORT).show();
                parent.setVisibility(View.VISIBLE);
                showTask(BGColor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setVisibility(View.VISIBLE);
            }
        });

        //悬浮按钮
        final SuspendButtonLayout suspendButtonLayout = (SuspendButtonLayout) findViewById(R.id.layout);
        suspendButtonLayout.setOnSuspendListener(new SuspendButtonLayout.OnSuspendListener() {
            @Override
            public void onButtonStatusChanged(int status) {

            }

            @Override
            public void onChildButtonClick(int index) {
                Intent intent = new Intent(getApplicationContext(), AddTaskActivity.class);
                intent.putExtra("projectName", projectName);
                startActivityForResult(intent, REQUEST_ADDTASK);

                Toast.makeText(MainActivity.this, "您点击了【"
                        + suspendChildButtonInfo[index - 1] + "】按钮！", Toast.LENGTH_SHORT).show();
            }
        });
        suspendButtonLayout.setPosition(true, 100);


               /* LinearLayoutManager layoutManager = new LinearLayoutManager(this );
                //设置布局管理器
                recyclerView.setLayoutManager(layoutManager);
                //设置为垂直布局，这也是默认的
                layoutManager.setOrientation(OrientationHelper. VERTICAL);
                //设置Adapter
                recyclerView.setAdapter(recycleAdapter);
                //设置分隔线
                recyclerView.addItemDecoration( new DividerGridItemDecoration(this ));
                //设置增加或删除条目的动画
                recyclerView.setItemAnimator( new DefaultItemAnimator());*/
        //final PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Initialize the ViewPager and set an adapter
        //final ViewPager pager = (ViewPager) findViewById(R.id.pager);

    }

    private void showTask(final int[] BGColor)
    {
        //显示任务
        taskQuery.whereEqualTo("projectName", projectName);
        taskQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> taskInfo) {
                int initsize = taskList.size();
                if(taskInfo.size()==0)
                {
                    taskList.add(NO_TASK);
                }
                else
                {
                    for(int i = 0;i<taskInfo.size();i++)
                    {
                        taskList.add(taskInfo.get(i).getString("taskName"));
                    }

                }
                taskList = taskList.subList(initsize,taskList.size());
                myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
                setPaperAdapter(BGColor);
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }

    private void setPaperAdapter(final int[] BGColor)
    {
        pager.setAdapter(myPagerAdapter);
        myPagerAdapter.notifyDataSetChanged();

        ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                pager.setBackgroundColor(BGColor[i % BGColorNum]);
                tabStrip.setBackgroundColor(BGColor[i % BGColorNum]);
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

                projectList.add(data.getStringExtra("projectName"));
                if(projectList.contains(NO_PROGRAM))
                {
                    projectList.remove(NO_PROGRAM);
                }
            }
        }
        else if(requestCode == REQUEST_ADDTASK){
            if (resultCode == RESULT_OK) {
                taskList.add(data.getStringExtra("taskName"));
                //todo

            }
        }
    }


    private void refreshProject()
    {
        programAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, projectList);
        programAdapter.notifyDataSetChanged();
        programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     //设置下拉列表框的下拉选项样式

        programSpinner.setAdapter(programAdapter);
    }

    private void findView() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

    }

    private void initRefreshLayout() {
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        refreshLayout.setOnRefreshListener(this);
    }
    private void initRecyclerView() {
        rvAdapter = new MainRVAdapter(1, this, false );

        //rvAdapter = new MainRVAdapter(getDatas(0, PAGE_COUNT), this, getDatas(0, PAGE_COUNT).size() > 0 );
        rvAdapter.setOnItemClickListener(new MainRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //tripInfoList.get(postion)
               /* Toast.makeText(MainActivity.this,"onItemClick : " + String.valueOf(position), Toast.LENGTH_SHORT).show();
                Intent commentIntent=new Intent(MainActivity.this,CommentActivity.class);
                commentIntent.putExtra("nickname",nickname);
                commentIntent.putExtra("position",position);
                startActivity(commentIntent);*/

            }
        });

        mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(rvAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!rvAdapter.isFadeTips() && lastVisibleItem + 1 == rvAdapter.getItemCount()) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //updateRecyclerView(rvAdapter.getRealLastPosition(), adapter.getRealLastPosition() + PAGE_COUNT);
                            }
                        }, 500);
                    }

                    if (rvAdapter.isFadeTips() && lastVisibleItem + 2 == rvAdapter.getItemCount()) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //updateRecyclerView(adapter.getRealLastPosition(), adapter.getRealLastPosition() + PAGE_COUNT);
                            }
                        }, 500);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

   /* private List<TripInfo> getDatas(final int firstIndex, final int lastIndex) {
        List<TripInfo> resList = new ArrayList<>();
        for (int i = firstIndex; i < lastIndex; i++) {
            if (i < tripInfoList.size()) {
                resList.add(tripInfoList.get(i));
            }
        }
        return resList;
    }*/

    private void updateRecyclerView(int fromIndex, int toIndex) {
       /* List<TripInfo> newDatas = getDatas(fromIndex, toIndex);
        if (newDatas.size() > 0) {
            rvAdapter.updateList(newDatas, true);
        } else {
            rvAdapter.updateList(null, false);
        }*/
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);
        rvAdapter.resetDatas();
        updateRecyclerView(0, PAGE_COUNT);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {name, "Home", "Top Paid", "Top Free", "Top Grossing", "Top New Paid",
                "Top New Free", "Trending"};
        private

        MyPagerAdapter(FragmentManager fm) {
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

            return SuperAwesomeCardFragment.newInstance(position);
        }
    }
}
