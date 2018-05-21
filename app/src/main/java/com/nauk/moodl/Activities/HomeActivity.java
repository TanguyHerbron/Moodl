package com.nauk.moodl.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.nauk.moodl.Activities.DetailsActivityFragments.Home;
import com.nauk.moodl.Activities.HomeActivityFragments.MarketCapitalization;
import com.nauk.moodl.Activities.HomeActivityFragments.Summary;
import com.nauk.moodl.Activities.HomeActivityFragments.Watchlist;
import com.nauk.moodl.HomeActivityPagerAdapter;
import com.nauk.moodl.LayoutManagers.CustomViewPager;
import com.nauk.moodl.R;

//Use WilliamChart for charts https://github.com/diogobernardino/WilliamChart

//Auto refresh with predefined intervals
//Multiple portfolio (exchanges & custom)
//Add roadmap to buy a coin
//Add reddit link ?
//

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Fragment watchlistFragment;
    private Fragment holdingsFragment;
    private Fragment marketFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**Interface setup**/
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.activity_currency_summary);

        watchlistFragment = new Watchlist();
        holdingsFragment = new Summary();
        marketFragment = new MarketCapitalization();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, watchlistFragment)
                .addToBackStack(null)
                .add(R.id.content_frame, marketFragment)
                .addToBackStack(null)
                .add(R.id.content_frame, holdingsFragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        showFragment(holdingsFragment);

        navigationView.setCheckedItem(R.id.navigation_currencies_list);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.navigation_watchlist:
                        showFragment(watchlistFragment);
                        break;
                    case R.id.navigation_currencies_list:
                        showFragment(holdingsFragment);
                        break;
                    case R.id.navigation_market_cap:
                        showFragment(marketFragment);
                        break;
                    case R.id.navigation_settings:
                        Intent settingIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(settingIntent);
                        break;
                }
                item.setChecked(true);
                drawerLayout.closeDrawers();

                return false;
            }
        });

        //Objects initialization

        //Layouts setup
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if(currentFragment != null)
        {
            fragmentManager.beginTransaction()
                    .hide(currentFragment)
                    .show(fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
        else
        {
            fragmentManager.beginTransaction()
                    .show(fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }



        currentFragment = fragment;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_currency_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            /*case R.id.action_settings:
                Log.d(this.getResources().getString(R.string.debug), "Setting button toggled");
                break;*/
        }

        return super.onOptionsItemSelected(item);
    }

    public interface IconCallBack
    {
        void onSuccess(Bitmap bitmap);
    }
}
