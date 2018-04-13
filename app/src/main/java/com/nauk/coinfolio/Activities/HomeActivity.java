package com.nauk.coinfolio.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.nauk.coinfolio.DataManagers.BalanceManager;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.MarketCapManager;
import com.nauk.coinfolio.DataManagers.PreferencesManager;
import com.nauk.coinfolio.LayoutManagers.HomeLayoutGenerator;
import com.nauk.coinfolio.PagerAdapter;
import com.nauk.coinfolio.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

//Use WilliamChart for charts https://github.com/diogobernardino/WilliamChart

//Auto refresh with predefined intervals
//Adding manually currencies (date, purchased price)
//Multiple portfolio (exchanges & custom)
//Add currency details (market cap, 1h, 3h, 1d, 3d, 1w, 1m, 3m, 1y)
//Add roadmap to buy a coin
//Add reddit link ?
//

public class HomeActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;

    private CollapsingToolbarLayout toolbarLayout;
    private ViewFlipper viewFlipper;
    private HomeLayoutGenerator layoutGenerator;
    private BottomNavigationView bottomNavigationView;

    private ViewPager viewPager;


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

        //Setup main interface
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_currency_summary);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        viewPager = findViewById(R.id.viewPager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), 3);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Objects initialization
        preferencesManager = new PreferencesManager(this);

        //Layouts setup
        toolbarLayout = findViewById(R.id.toolbar_layout);
        viewFlipper = findViewById(R.id.viewFlipperSummary);

        bottomNavigationView = findViewById(R.id.navigationSummary);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.navigation_currencies_list);
        toolbarLayout.setForegroundGravity(Gravity.CENTER);

        ImageButton settingsButton = findViewById(R.id.settings_button);

        settingsButton.setBackground(this.getResources().getDrawable(R.drawable.ic_settings_black_24dp));
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingIntent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                //overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });

        updateViewButtonIcon();

    }

    private void updateViewButtonIcon()
    {
        ImageButton imgButton = findViewById(R.id.switch_button);

        imgButton.setBackgroundColor(this.getResources().getColor(R.color.buttonColor));

        if(preferencesManager.getDetailOption())
        {
            imgButton.setBackground(this.getResources().getDrawable(R.drawable.ic_unfold_less_black_24dp));
            preferencesManager.setDetailOption(true);
        }
        else
        {
            imgButton.setBackground(this.getResources().getDrawable(R.drawable.ic_details_black_24dp));
            preferencesManager.setDetailOption(false);
        }
    }



    private void switchMainView()
    {
        Log.d("coinfolio", "Should");
        ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(true, true);
        findViewById(R.id.swiperefresh).setNestedScrollingEnabled(true);

        findViewById(R.id.toolbar_layout).setFocusable(true);
        ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(true, true);
        ((AppBarLayout) findViewById(R.id.app_bar)).setActivated(true);
        findViewById(R.id.app_bar).setClickable(true);
        findViewById(R.id.nestedScrollViewLayout).setNestedScrollingEnabled(true);

        findViewById(R.id.app_bar).setEnabled(true);
        findViewById(R.id.toolbar_layout).setNestedScrollingEnabled(true);
        findViewById(R.id.coordinatorLayout).setNestedScrollingEnabled(true);

        findViewById(R.id.switch_button).setVisibility(View.VISIBLE);

        viewFlipper.setDisplayedChild(1);
    }

    private void switchSecondaryViews(int itemIndex)
    {
        Log.d("coinfolio", "Should not");
        ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(false, true);
        findViewById(R.id.swiperefresh).setNestedScrollingEnabled(false);

        findViewById(R.id.toolbar_layout).setFocusable(false);
        ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(false, true);
        ((AppBarLayout) findViewById(R.id.app_bar)).setActivated(false);
        findViewById(R.id.app_bar).setClickable(false);
        findViewById(R.id.nestedScrollViewLayout).setNestedScrollingEnabled(false);

        findViewById(R.id.app_bar).setEnabled(false);
        findViewById(R.id.toolbar_layout).setNestedScrollingEnabled(false);
        findViewById(R.id.coordinatorLayout).setNestedScrollingEnabled(false);

        findViewById(R.id.switch_button).setVisibility(View.GONE);

        viewFlipper.setDisplayedChild(itemIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void addTestWatchlistCardview()
    {
        View view = LayoutInflater.from(this).inflate(R.layout.cardview_watchlist, null);

        ((TextView) view.findViewById(R.id.currencyFluctuationPercentageTextView)).setText("3%");
        ((TextView) view.findViewById(R.id.currencyFluctuationTextView)).setText("$3");
        ((TextView) view.findViewById(R.id.currencyNameTextView)).setText("TanguyCoin");
        ((TextView) view.findViewById(R.id.currencySymbolTextView)).setText("TGC");
        ((TextView) view.findViewById(R.id.currencyValueTextView)).setText("$100");

        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("coinfolio", "Clicked !");
            }
        });

        ((LinearLayout) findViewById(R.id.linearLayoutWatchlist)).addView(view);
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
