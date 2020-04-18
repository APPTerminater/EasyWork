package com.tongji.lisa1225.myapplication;

import com.tongji.lisa1225.myapplication.Adapter.MainRVAdapter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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
import android.text.Layout;
import android.view.LayoutInflater;
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

    //RecyclerView相关
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private MainRVAdapter rvAdapter;
    private GridLayoutManager mLayoutManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final int PAGE_COUNT = 10;
    private int lastVisibleItem = 0;

    private AVQuery<AVObject> query = new AVQuery<>("ProjectInfo");

    private ArrayAdapter<String> programAdapter;
    List<String> projectList = new ArrayList<>();

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.programName) Spinner programSpinner;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    //@BindView(R.id.user_mail) TextView user_mail;
    //@BindView(R.id.user_name) TextView user_name;

    //用户信息
    private String email,name,occupation,password;
    //项目信息
    private String programName;

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

        //TextView user_name = (TextView) LayoutInflater.from(MainActivity.this).inflate(R.layout.nav_header, null).findViewById(R.id.user_name);
        View nav_header = navigationView.inflateHeaderView(R.layout.nav_header);
        TextView user_name = nav_header.findViewById(R.id.user_name);
        TextView user_mail = nav_header.findViewById(R.id.user_mail);
        user_name.setText(name);
        user_mail.setText(email);

        //获取用户信息
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
        query.whereEqualTo("producer", email);
        query.findInBackground().subscribe(new Observer<List<AVObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<AVObject> projectInfo) {
                if(projectInfo.size()==0)
                {
                    projectList.add("You have no project!");
                    refreshProject();
                }
                else
                {
                    for(int i = 0;i<projectInfo.size();i++)
                    {
                        projectList.add(projectInfo.get(i).getString("projectName"));
                    }
                    refreshProject();
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
        //programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     //设置下拉列表框的下拉选项样式

        //programSpinner.setAdapter(programAdapter);
        //条目点击事件
        programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                programName = programAdapter.getItem(position);
                Toast.makeText(MainActivity.this, programName, Toast.LENGTH_SHORT).show();
                parent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                parent.setVisibility(View.VISIBLE);
            }
        });

        //切换背景颜色
        final int[] BGColor = {getResources().getColor(R.color.pinkDarkBlue),
                getResources().getColor(R.color.pinkBlue),
                getResources().getColor(R.color.pinkGreen),
                getResources().getColor(R.color.pinkYellow),
                getResources().getColor(R.color.pinkRed)};
        final int BGColorNum = 5;

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


        final PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Initialize the ViewPager and set an adapter
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

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
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(mPageChangeListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADDPROGRAM) {
            if (resultCode == RESULT_OK) {

                projectList.add(data.getStringExtra("projectName"));

            }
        }
    }

    //todo
    private void findProject()
    {
        projectList.clear();

        //查询此邮箱是否有关联项目
        if(occupation.equals("producer"))
        {
            query.whereEqualTo("producer", email);
            query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                public void onSubscribe(Disposable disposable) {}
                public void onNext(List<AVObject> projectInfo) {
                    if(projectInfo.size()==0)
                    {
                        projectList.add("You have no project!");
                        refreshProject();
                    }
                    else
                    {
                        for(int i = 0;i<projectInfo.size();i++)
                        {
                            projectList.add(projectInfo.get(i).getString("projectName"));
                        }
                        refreshProject();
                    }
                }
                public void onError(Throwable throwable) {}
                public void onComplete() {}
            });
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

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            //return new Fragment();

            return SuperAwesomeCardFragment.newInstance(position);
        }
    }
}
