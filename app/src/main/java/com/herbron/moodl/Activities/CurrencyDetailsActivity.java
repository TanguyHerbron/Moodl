package com.herbron.moodl.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DetailsActivityPagerAdapter;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.R;

import java.util.Objects;

import static com.herbron.moodl.MoodlBox.numberConformer;
import static java.lang.Math.abs;

/**Create a Parcelable**/

public class CurrencyDetailsActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private Currency currency;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if(viewPager.getCurrentItem() == 0)
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        finishAfterTransition();
                    }
                }
                else
                {
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_details);

        currency = getIntent().getParcelableExtra(getBaseContext().getString(R.string.currency));
        viewPager = findViewById(R.id.vfCurrencyDetails);
        final DetailsActivityPagerAdapter adapter = new DetailsActivityPagerAdapter(getSupportFragmentManager(), 3);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        BottomNavigationView navigation = findViewById(R.id.navigation_details);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setupActionBar();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(!navigation.getMenu().getItem(position).isChecked())
                {
                    navigation.getMenu().getItem(position).setChecked(true);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setupActionBar()
    {
        if(currency.getBalance() == 0)
        {
            setTitle(" " + currency.getName());
        }
        else
        {
            setTitle(" " + currency.getName() + " | " + numberConformer(currency.getBalance()));
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);

        if(currency.getIcon() != null)
        {
            Bitmap result = Bitmap.createBitmap(150, 150, currency.getIcon().getConfig());

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(ContextCompat.getColor(this, R.color.white));

            Canvas canvas = new Canvas(result);
            canvas.drawCircle(result.getHeight()/2, result.getWidth()/2, 75, paint);
            canvas.drawBitmap(Bitmap.createScaledBitmap(currency.getIcon(), 100, 100, false), result.getHeight()/2 - 50, result.getWidth()/2 - 50, null);

            getSupportActionBar()
                    .setIcon(new BitmapDrawable(getResources(),
                            Bitmap.createScaledBitmap(result
                                    , (int) MoodlBox.convertDpToPx(25, getResources())
                                    , (int) MoodlBox.convertDpToPx(25, getResources())
                                    , false)));
        }
    }
}
/*for(int i = 0; i < dataChartList.size(); i++)
        {*/
            /*if(counter == offset)
            {
                calendar.setTimeInMillis(dataChartList.get(i).getTimestamp()*1000);

                switch (pointFormat)
                {
                    case HOUR:
                        hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                        minute = String.valueOf(calendar.get(Calendar.MINUTE));

                        if(hour.length() < 2)
                        {
                            hour = "0" + hour;
                        }

                        if(minute.length() < 2)
                        {
                            minute = "0" + minute;
                        }

                        lineSet.addPoint(hour + ":" + minute, (float) dataChartList.get(i).getOpen());
                        break;
                    case DAY:
                        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK)+1;

                        switch (dayIndex)
                        {
                            case Calendar.MONDAY:
                                dayName = "Mon";
                                break;
                            case Calendar.TUESDAY:
                                dayName = "Tue";
                                break;
                            case Calendar.WEDNESDAY:
                                dayName = "Wed";
                                break;
                            case Calendar.THURSDAY:
                                dayName = "Thu";
                                break;
                            case Calendar.FRIDAY:
                                dayName = "Fri";
                                break;
                            case Calendar.SATURDAY:
                                dayName = "Sat";
                                break;
                            case Calendar.SUNDAY:
                                dayName = "Sun";
                                break;
                        }

                        lineSet.addPoint(dayName, (float) dataChartList.get(i).getOpen());
                        break;
                    case MONTH:
                        dayNumber = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)+1);
                        monthNumber = String.valueOf(calendar.get(Calendar.MONTH)+1);

                        if(dayNumber.length() < 2)
                        {
                            dayNumber = '0' + dayNumber;
                        }

                        if(monthNumber.length() < 2)
                        {
                            monthNumber = '0' + monthNumber;
                        }

                        lineSet.addPoint(dayNumber + "/" + monthNumber, (float) dataChartList.get(i).getOpen());
                        break;
                    case YEAR:
                        int mb = calendar.get(Calendar.MONTH);

                        switch (mb)
                        {
                            case Calendar.JANUARY:
                                monthName = "Jan";
                                break;
                            case Calendar.FEBRUARY:
                                monthName = "Feb";
                                break;
                            case Calendar.MARCH:
                                monthName = "Mar";
                                break;
                            case Calendar.APRIL:
                                monthName = "Apr";
                                break;
                            case Calendar.MAY:
                                monthName = "May";
                                break;
                            case Calendar.JUNE:
                                monthName = "Jun";
                                break;
                            case Calendar.JULY:
                                monthName = "Jul";
                                break;
                            case Calendar.AUGUST:
                                monthName = "Aug";
                                break;
                            case Calendar.SEPTEMBER:
                                monthName = "Sep";
                                break;
                            case Calendar.OCTOBER:
                                monthName = "Oct";
                                break;
                            case Calendar.NOVEMBER:
                                monthName = "Nov";
                                break;
                            case Calendar.DECEMBER:
                                monthName = "Dec";
                                break;
                        }

                        lineSet.addPoint(monthName, (float) dataChartList.get(i).getOpen());
                        break;
                }
                counter = 0;
            }
            else
            {
                counter++;
                lineSet.addPoint("", (float) dataChartList.get(i).getOpen());
            }*/
            /*values.add(new Entry(i, (float) dataChartList.get(i).getOpen()));
        }*/