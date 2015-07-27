package com.lchpatners.shadal;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The main {@link android.app.Activity Activity}.
 * Displays the {@link android.support.v4.app.Fragment Fragments}.
 */
public class MainActivity extends ActionBarActivity {
    //public static String URL;
    //public static boolean SHOW_POPUP=false;
    protected static String[] drawer_str = {"주문하기", "더보기", "추천"};
    /**
     * The main {@link android.support.v4.view.ViewPager ViewPager}.
     */
    protected ViewPager viewPager;
    /**
     * Indicates the last instance of {@link com.lchpatners.shadal.RestaurantListFragment
     * RestaurantListFragment}.
     */
    RestaurantListFragment restaurantListFragmentCurrentlyOn;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        new Server(this).checkAppMinimumVersion();


        // If no campus is selected, have the user select one.
        if (Preferences.getCampusKoreanShortName(this) == null) {
            startActivity(new Intent(this, InitializationActivity.class));
            finish();
        } else {
            new Server(this).getPopupList();
        }


        // If no database, get data from the server and update.
        if (!DatabaseHelper.getInstance(this).checkDatabase(Preferences.getCampusEnglishName(this))) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new Server(this).updateAll();
            }
        }


        updateCampusMetaData();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView listView = (ListView) findViewById(R.id.navigation_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drawer_str);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    mDrawerLayout.closeDrawer(listView);
                } else if (position == 1) {

                    Intent intent = new Intent(MainActivity.this, SeeMoreActivity.class);
                    startActivity(intent);

                } else if (position == 2) {

                    Intent intent = new Intent(MainActivity.this, BookmarkFragment.class);
                    startActivity(intent);
                }
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close) {
            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // getActionBar().setTitle(mTitle);
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        viewPager = (ViewPager) findViewById(R.id.main_pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(PagerAdapter.MAX_PAGE);
        //viewPager.setCurrentItem(PagerAdapter.MAIN);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(adapter.getPageTitle(viewPager.getCurrentItem()));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // Do nothing.
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // Do nothing.
            }
        };

        for (int page = 0; page < PagerAdapter.MAX_PAGE; page++) {
            ActionBar.Tab tab =
                    actionBar.newTab()
                            .setIcon(adapter.getPageIcon(page, page == viewPager.getCurrentItem()))
                            .setText(adapter.getPageTitle(page))
                            .setTabListener(tabListener);
            actionBar.addTab(tab);


        }

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                viewPager.setCurrentItem(position);
                for (int i = 0; i < actionBar.getTabCount(); i++) {
                    actionBar.getTabAt(i).setIcon(adapter.getPageIcon(i, false))
                            .setText(adapter.getPageTitle(i));
                }
                actionBar.getTabAt(position)
                        .setIcon(adapter.getPageIcon(position, true));

                actionBar.setTitle("주문하기");
            }
        });

        viewPager.setCurrentItem(PagerAdapter.MAIN);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == PagerAdapter.MAIN) {
            if (restaurantListFragmentCurrentlyOn != null) {
                AnalyticsHelper helper = new AnalyticsHelper(getApplication());
                helper.sendScreen("메인 화면");
            }
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(PagerAdapter.MAIN, true);
        }
    }


    /**
     * Update campus meta data of currently selected campus.
     *
     * @see com.lchpatners.shadal.Server.CampusesLoadingTask CampusesLoadingTask
     */
    public void updateCampusMetaData() {
        new Server.CampusesLoadingTask() {
            @Override
            public void onPostExecute(Void aVoid) {
                if (results == null) return;
                for (int i = 0; i < results.length(); i++) {
                    try {
                        JSONObject result = results.getJSONObject(i);
                        if (result.getString("name_eng").equals(
                                Preferences.getCampusEnglishName(MainActivity.this))) {
                            Preferences.setCampus(MainActivity.this, result);
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            protected Void doInBackground(Void... params) {

                try {
                    serviceCall = Server.makeServiceCall(
                            Server.BASE_URL + Server.CAMPUSES, Server.GET, null);
                    if (serviceCall == null) {
                        return null;
                    }
                    results = new JSONArray(serviceCall);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    /**
     * The {@link android.support.v4.app.FragmentStatePagerAdapter
     * FragmentStatePagerAdapter} of {@link #viewPager}.
     */
    private class PagerAdapter extends FragmentStatePagerAdapter {

        /**
         * Indicates the main {@link android.support.v4.app.Fragment Fragment}.
         *
         * @see com.lchpatners.shadal.CategoryListFragment CategoryListFragment
         */
        public static final int MAIN = 1;
        /**
         * Indicates the bookmark {@link android.support.v4.app.Fragment Fragment}.
         *
         * @see com.lchpatners.shadal.BookmarkFragment BookmarkFragment
         */
        public static final int BOOKMARK = 0;
        /**
         * Indicates the random {@link android.support.v4.app.Fragment Fragment}.
         *
         * @see com.lchpatners.shadal.RandomFragment RandomFragment
         */
        // public static final int RANDOM = 2;
        /**
         * Indicates the see-more {@link android.support.v4.app.Fragment Fragment}.
         *
         * @see com.lchpatners.shadal.SeeMoreFragment SeeMoreFragment
         */
        public static final int SEE_MORE = 2;
        /**
         * The number of {@link android.support.v4.app.Fragment Fragments}.
         */
        public static final int MAX_PAGE = 3;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case MAIN:
                    fragment = CategoryListFragment.newInstance();
                    break;
                case BOOKMARK:
                    fragment = CallListFragment.newInstance();
                    break;
//                case RANDOM:
//                    fragment = RandomFragment.newInstance();
//                    break;
                case SEE_MORE:
                    fragment = SeeMoreFragment.newInstance();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return MAX_PAGE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            switch (position) {
                case MAIN:
                    title = getString(R.string.app_name);
                    break;
                case BOOKMARK:
                    title = "최근주문";
                    break;
//                case RANDOM:
//                    title = getString(R.string.random);
//                    break;
                case SEE_MORE:
                    title = getString(R.string.see_more);
                    break;
            }
            return title;
        }

        public Drawable getPageIcon(int page, boolean selected) {
            Drawable icon = null;
            switch (page) {
                case MAIN:
                    icon = getResources().getDrawable(
                            selected ? R.drawable.tab_main_selected : R.drawable.tab_main_unselected
                    );
                    break;
                case BOOKMARK:
                    icon = getResources().getDrawable(
                            selected ? R.drawable.tab_star_selected : R.drawable.tab_star_unselected
                    );
                    break;
//                case RANDOM:
//                    icon = getResources().getDrawable(
//                            selected ? R.drawable.tab_dice_selected : R.drawable.tab_dice_unselected
//                    );
//                    break;
                case SEE_MORE:
                    icon = getResources().getDrawable(
                            selected ? R.drawable.tab_more_selected : R.drawable.tab_more_unselected
                    );
                    break;
            }
            return icon;
        }


    }

}