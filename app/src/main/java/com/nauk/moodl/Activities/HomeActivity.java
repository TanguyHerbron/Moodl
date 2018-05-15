package com.nauk.moodl.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

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

    private BottomNavigationView bottomNavigationView;
    private CustomViewPager viewPager;


    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            item.setChecked(true);
            switch (item.getItemId())
            {
                case R.id.navigation_watchlist:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.navigation_currencies_list:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.navigation_market_cap:
                    viewPager.setCurrentItem(2);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**Interface setup**/
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.activity_currency_summary);

        viewPager = findViewById(R.id.viewPager);
        final HomeActivityPagerAdapter adapter = new HomeActivityPagerAdapter(getSupportFragmentManager(), 3);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(!bottomNavigationView.getMenu().getItem(position).isChecked())
                {
                    bottomNavigationView.getMenu().getItem(position).setChecked(true);
                }

                if(position % 2 == 0)
                {
                    ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(false, true);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Objects initialization

        //Layouts setup

        bottomNavigationView = findViewById(R.id.navigationSummary);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.navigation_currencies_list);
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
