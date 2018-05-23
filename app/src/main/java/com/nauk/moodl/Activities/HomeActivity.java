package com.nauk.moodl.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.nauk.moodl.Activities.HomeActivityFragments.MarketCapitalization;
import com.nauk.moodl.Activities.HomeActivityFragments.Summary;
import com.nauk.moodl.Activities.HomeActivityFragments.Watchlist;
import com.nauk.moodl.HideBalanceSwitch;
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


    private HideBalanceSwitch switchInterface;

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

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setListener((HideBalanceSwitch) holdingsFragment);

        showFragment(holdingsFragment);

        navigationView.setCheckedItem(R.id.navigation_currencies_list);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                item.setChecked(true);

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
                        item.setChecked(false);
                        break;
                }

                drawerLayout.closeDrawers();

                return false;
            }
        });

        setupBalanceSwitch();

        //Objects initialization

        //Layouts setup
    }

    public void setListener(HideBalanceSwitch switchInterface)
    {
        this.switchInterface = switchInterface;
    }

    private void setupBalanceSwitch()
    {
        Switch balanceSwitch = findViewById(R.id.switchHideBalance);

        balanceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switchInterface.buttonCheckedChange();
            }
        });
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(currentFragment != null)
        {
            fragmentTransaction.hide(currentFragment);
        }

        if(fragment.isAdded())
        {
            fragmentTransaction.show(fragment);
        }
        else
        {
            fragmentTransaction.add(R.id.content_frame, fragment).addToBackStack(null);
        }

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

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
            case R.id.navigation_settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface IconCallBack
    {
        void onSuccess(Bitmap bitmap);
    }
}
