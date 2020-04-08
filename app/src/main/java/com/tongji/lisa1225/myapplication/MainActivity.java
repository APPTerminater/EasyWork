package com.tongji.lisa1225.myapplication;

import com.tongji.lisa1225.myapplication.Adapter.MainRVAdapter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    //RecyclerView相关
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private MainRVAdapter rvAdapter;
    private GridLayoutManager mLayoutManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final int PAGE_COUNT = 10;
    private int lastVisibleItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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

        private final String[] TITLES = {"Categories", "Home", "Top Paid", "Top Free", "Top Grossing", "Top New Paid",
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
