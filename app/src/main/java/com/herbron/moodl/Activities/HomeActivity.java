package com.herbron.moodl.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.herbron.moodl.Activities.HomeActivityFragments.MarketCapitalization;
import com.herbron.moodl.Activities.HomeActivityFragments.Overview;
import com.herbron.moodl.Activities.HomeActivityFragments.Summary;
import com.herbron.moodl.Activities.HomeActivityFragments.Watchlist;
import com.herbron.moodl.BalanceSwitchManagerInterface;
import com.herbron.moodl.BalanceUpdateInterface;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.PlaceholderManager;
import com.herbron.moodl.R;

import static com.herbron.moodl.MoodlBox.numberConformer;

//Use WilliamChart for charts https://github.com/diogobernardino/WilliamChart

//Auto refresh with predefined intervals
//Multiple portfolio (exchanges & custom)
//Add roadmap to buy a coin
//Add reddit link ?
//

public class HomeActivity extends AppCompatActivity implements BalanceUpdateInterface {

    private DrawerLayout drawerLayout;
    private Fragment watchlistFragment;
    private Fragment holdingsFragment;
    private Fragment marketFragment;
    private Fragment overviewFragment;
    private Fragment currentFragment;


    private BalanceSwitchManagerInterface switchInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStatusBarGradiant(this);

        /**Interface setup**/

        setContentView(R.layout.activity_currency_summary);

        watchlistFragment = new Watchlist();
        holdingsFragment = new Summary();
        marketFragment = new MarketCapitalization();
        overviewFragment = new Overview();

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setListener((BalanceSwitchManagerInterface) holdingsFragment);

        showFragment(holdingsFragment);

        navigationView.setCheckedItem(R.id.navigation_holdings);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                item.setChecked(true);

                switch (item.getItemId())
                {
                    case R.id.navigation_watchlist:
                        showFragment(watchlistFragment);
                        break;
                    case R.id.navigation_holdings:
                        showFragment(holdingsFragment);
                        break;
                    case R.id.navigation_market_cap:
                        showFragment(marketFragment);
                        break;
                    case R.id.navigation_overview:
                        showFragment(overviewFragment);
                        break;
                    case R.id.navigation_settings:
                        Intent settingIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                        settingIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.MainPreferenceFragment.class.getName() );
                        settingIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true );
                        startActivity(settingIntent);
                        item.setChecked(false);
                        break;
                }

                drawerLayout.closeDrawers();

                return false;
            }
        });

        setupBalanceSwitch();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gradient_background);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    @Override
    public void onBackPressed() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void setListener(BalanceSwitchManagerInterface switchInterface)
    {
        this.switchInterface = switchInterface;
    }

    private void setupBalanceSwitch()
    {
        Switch balanceSwitch = findViewById(R.id.switchHideBalance);

        PreferencesManager preferencesManager = new PreferencesManager(getBaseContext());

        balanceSwitch.setChecked(preferencesManager.isBalanceHidden());

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

    public Fragment getHoldingsFragment()
    {
        return holdingsFragment;
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
    public void onBalanceUpdated(float value) {
        PreferencesManager preferencesManager = new PreferencesManager(getApplicationContext());
        NavigationView navigationView = findViewById(R.id.nav_view);
        TextView drawerBalanceTextView = navigationView.getHeaderView(0).findViewById(R.id.balanceTextView);

        if(preferencesManager.isBalanceHidden())
        {
            drawerBalanceTextView.setText(PlaceholderManager.getPercentageString(numberConformer(value), getApplicationContext()));
        }
        else
        {
            drawerBalanceTextView.setText(PlaceholderManager.getValueString(numberConformer(value), getApplicationContext()));
        }
    }

    public interface IconCallBack
    {
        void onSuccess(Bitmap bitmap);
    }
}
