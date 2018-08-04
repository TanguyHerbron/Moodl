package com.herbron.moodl.Activities.DetailsActivityFragments;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.herbron.moodl.CurrencyInfoUpdateNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.CurrencyDataChart;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.CustomLayouts.CustomViewPager;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.PlaceholderManager;
import com.herbron.moodl.R;

import java.util.ArrayList;
import java.util.List;

import static com.herbron.moodl.MoodlBox.getDateFromTimestamp;
import static com.herbron.moodl.MoodlBox.numberConformer;

/**
 * Created by Tiji on 13/05/2018.
 */

public class Charts extends Fragment implements CurrencyInfoUpdateNotifierInterface {

    private final static int HOUR = 0;
    private final static int DAY = 1;
    private final static int WEEK = 2;
    private final static int MONTH = 3;
    private final static int YEAR = 4;

    private View view;
    private Currency currency;
    private LineChart lineChart;
    private CandleStickChart candleStickChart;
    private BarChart barChart;
    private List<CurrencyDataChart> dataChartList;
    private PreferencesManager preferencesManager;

    private boolean displayLineChart;

    private Button lineChartButton;
    private Button candleStickChartButton;

    private Spinner timeIntervalSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_charts_detailsactivity, container, false);

        currency = getActivity().getIntent().getParcelableExtra("currency");

        currency.setListener(this);

        lineChart = view.findViewById(R.id.chartPriceView);
        candleStickChart = view.findViewById(R.id.chartCandleStickView);
        lineChartButton = view.findViewById(R.id.lineChartButton);
        candleStickChartButton = view.findViewById(R.id.candleStickChartButton);
        barChart = view.findViewById(R.id.chartVolumeView);
        preferencesManager = new PreferencesManager(getActivity().getBaseContext());

        displayLineChart = true;

        lineChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lineChartButton.setEnabled(false);
                candleStickChartButton.setEnabled(true);

                lineChart.setVisibility(View.VISIBLE);
                candleStickChart.setVisibility(View.GONE);

                displayLineChart = true;
            }
        });

        candleStickChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lineChartButton.setEnabled(true);
                candleStickChartButton.setEnabled(false);

                lineChart.setVisibility(View.GONE);
                candleStickChart.setVisibility(View.VISIBLE);

                displayLineChart = false;
            }
        });

        initializeLineChart(lineChart);
        initializeCandleStickChart(candleStickChart);

        initializeSpinners();

        return view;
    }

    private void initializeSpinners()
    {
        timeIntervalSpinner = view.findViewById(R.id.timeIntervalSinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
                R.array.time_interval_string_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        timeIntervalSpinner.setAdapter(adapter);

        timeIntervalSpinner.setSelection(2);

        timeIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateCharts(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void updateCharts(int index)
    {
        view.findViewById(R.id.chartPriceView).setVisibility(View.GONE);
        view.findViewById(R.id.chartCandleStickView).setVisibility(View.GONE);
        view.findViewById(R.id.chartVolumeView).setVisibility(View.GONE);
        view.findViewById(R.id.progressLayoutChart).setVisibility(View.VISIBLE);

        switch (index)
        {
            case 0:
                currency.updateHistoryMinutes(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
            case 1:
                currency.updateHistoryMinutes(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
            case 2:
                currency.updateHistoryMinutes(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
            case 3:
                currency.updateHistoryHours(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
            case 4:
                currency.updateHistoryHours(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
            case 5:
                currency.updateHistoryHours(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
            case 6:
                currency.updateHistoryDays(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
            case 7:
                currency.updateHistoryDays(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
            case 8:
                currency.updateHistoryDays(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency());
                break;
        }
    }

    private void initializeCandleStickChart(CandleStickChart candleStickChart)
    {
        candleStickChart.setDrawGridBackground(false);
        candleStickChart.setDrawBorders(false);
        candleStickChart.setDrawMarkers(true);
        candleStickChart.getDescription().setEnabled(false);
        candleStickChart.getAxisLeft().setEnabled(true);
        candleStickChart.getAxisRight().setEnabled(true);
        candleStickChart.getLegend().setEnabled(false);
        candleStickChart.getXAxis().setEnabled(true);
        candleStickChart.setViewPortOffsets(0, 0, 0, 0);
    }

    private void initializeLineChart(LineChart lineChart)
    {
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.setDrawMarkers(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setScaleEnabled(false);
        lineChart.setDragEnabled(true);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.setViewPortOffsets(0, 0, 0, 0);
        lineChart.setNoDataTextColor(currency.getChartColor());
    }

    private void updateChartTab(int timeUnit, int amount)
    {
        updateChartsData(timeUnit, amount);

        if(currency.getHistoryMinutes() != null)
        {
            drawPriceLineChart();
            drawPriceCandleStickChart();
            drawVolumeChart();
            updateGeneralData(lineChart.getData().getDataSets().get(0).getEntryForIndex(0).getY(), lineChart.getData().getDataSets().get(0).getEntryForIndex(lineChart.getData().getDataSets().get(0).getEntryCount() - 1).getY());
        }

        if(displayLineChart)
        {
            view.findViewById(R.id.chartPriceView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.progressLayoutChart).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.chartCandleStickView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.progressLayoutChart).setVisibility(View.GONE);
        }
    }

    private void updateGeneralData(float start, float end)
    {
        double totalVolume = dataChartList.get(0).getVolumeTo();
        double highestPrice = dataChartList.get(0).getOpen();
        double lowestPrice = dataChartList.get(0).getOpen();

        updateFluctuation(start, end);

        ((TextView) view.findViewById(R.id.txtViewPriceStart)).setText(PlaceholderManager.getValueString(numberConformer(start), getActivity().getBaseContext()));
        ((TextView) view.findViewById(R.id.txtViewPriceNow)).setText(PlaceholderManager.getValueString(numberConformer(end), getActivity().getBaseContext()));

        for(int i = 1; i < dataChartList.size(); i++)
        {
            totalVolume += dataChartList.get(i).getVolumeTo();

            if(highestPrice < dataChartList.get(i).getOpen())
            {
                highestPrice = dataChartList.get(i).getOpen();
            }

            if(lowestPrice > dataChartList.get(i).getOpen())
            {
                lowestPrice = dataChartList.get(i).getOpen();
            }
        }

        ((TextView) view.findViewById(R.id.totalVolume)).setText(PlaceholderManager.getValueString(numberConformer(totalVolume), getActivity().getBaseContext()));
        ((TextView) view.findViewById(R.id.highestPrice)).setText(PlaceholderManager.getValueString(numberConformer(highestPrice), getActivity().getBaseContext()));
        ((TextView) view.findViewById(R.id.lowestPrice)).setText(PlaceholderManager.getValueString(numberConformer(lowestPrice), getActivity().getBaseContext()));
    }

    private void updateFluctuation(float start, float end)
    {
        float fluctuation = end - start;
        float percentageFluctuation = (fluctuation / start * 100);

        if(percentageFluctuation < 0)
        {
            ((TextView) view.findViewById(R.id.txtViewPercentage)).setTextColor(getResources().getColor(R.color.red));
        }
        else
        {
            ((TextView) view.findViewById(R.id.txtViewPercentage)).setTextColor(getResources().getColor(R.color.green));
        }

        ((TextView) view.findViewById(R.id.txtViewPercentage)).setText(getResources().getString(R.string.currencyPercentagePlaceholder, numberConformer(percentageFluctuation)));
    }

    private void drawVolumeChart()
    {
        initializeBarChart(barChart);

        barChart.setData(generateVolumeChartSet());
        barChart.animateY(1000);
        barChart.invalidate();

        view.findViewById(R.id.chartVolumeView).setVisibility(View.VISIBLE);
    }

    private BarData generateVolumeChartSet()
    {
        BarDataSet dataSet;
        ArrayList<BarEntry> values = new ArrayList<>();

        int offset = (int) Math.floor(dataChartList.size() / 200);

        if(offset < 1)
        {
            offset = 1;
        }

        for(int i = 0, j = 0; i < dataChartList.size(); i += offset, j++)
        {
            values.add(new BarEntry(j, (float) dataChartList.get(j).getVolumeTo()));
        }

        dataSet = new BarDataSet(values, "Volume");
        dataSet.setDrawIcons(false);
        dataSet.setColor(Color.GRAY);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(currency.getChartColor());

        return new BarData(dataSet);
    }

    private void initializeBarChart(BarChart barChart)
    {
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setDrawMarkers(true);
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.setPinchZoom(true);
        barChart.setScaleEnabled(false);
        barChart.setDragEnabled(true);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getXAxis().setEnabled(false);
        barChart.setViewPortOffsets(0, 0, 0, 0);
        barChart.setFitBars(true);
        barChart.setHighlightFullBarEnabled(true);

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                barChartValueSelected(e);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        barChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                return toucheEvent(motionEvent);
            }
        });
    }

    private void barChartValueSelected(Entry e)
    {
        int index = barChart.getData().getDataSets().get(0).getEntryIndex((BarEntry) e);

        lineChart.highlightValue(lineChart.getData().getDataSets().get(0).getEntryForIndex(index).getX(), lineChart.getData().getDataSets().get(0).getEntryForIndex(index).getY(), 0);
        generatePlaceHoldersFromIndex(index);
    }

    private boolean toucheEvent(MotionEvent motionEvent)
    {
        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                ((CustomViewPager) view.getParent()).setPagingEnabled(false);
                break;

            case MotionEvent.ACTION_UP:
                ((CustomViewPager) view.getParent()).setPagingEnabled(true);
                lineChart.highlightValue(null);
                updateFluctuation(lineChart.getData().getDataSets().get(0).getEntryForIndex(0).getY(), lineChart.getData().getDataSets().get(0).getEntryForIndex(lineChart.getData().getDataSets().get(0).getEntryCount() - 1).getY());
                barChart.highlightValues(null);
                hideDataIndicators();
                break;
        }

        return false;
    }

    private void hideDataIndicators()
    {
        ((TextView) view.findViewById(R.id.volumeHightlight)).setText(".\n.");
        view.findViewById(R.id.volumeHightlight).setVisibility(View.INVISIBLE);
        ((TextView) view.findViewById(R.id.priceHightlight)).setText(".\n.");
        view.findViewById(R.id.priceHightlight).setVisibility(View.INVISIBLE);
        ((TextView) view.findViewById(R.id.timestampHightlight)).setText(".\n.");
        view.findViewById(R.id.timestampHightlight).setVisibility(View.INVISIBLE);
    }

    private void drawPriceLineChart()
    {
        lineChart.setData(generatePriceLineChartSet());
        lineChart.getAxisLeft().setAxisMinValue(lineChart.getData().getYMin());

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                lineChartValueSelected(e);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        lineChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                return toucheEvent(motionEvent);
            }
        });
    }

    private void lineChartValueSelected(Entry e)
    {
        int index = lineChart.getData().getDataSets().get(0).getEntryIndex(e);

        barChart.highlightValue(barChart.getData().getDataSets().get(0).getEntryForIndex(index).getX(), 0, index);
        generatePlaceHoldersFromIndex(index);
    }

    private void generatePlaceHoldersFromIndex(int index)
    {
        String date;
        String volumePlaceholder;
        String pricePlaceholder;
        String timestampPlaceholder;

        if(dataChartList.size() > 200)
        {
            date = getDateFromTimestamp(dataChartList.get((int) Math.floor(dataChartList.size() / 200) * index).getTimestamp() * 1000);
        }
        else
        {
            date = getDateFromTimestamp(dataChartList.get(index).getTimestamp() * 1000);
        }

        volumePlaceholder = PlaceholderManager.getVolumeString(numberConformer(barChart.getData().getDataSets().get(0).getEntryForIndex(index).getY()), getActivity().getBaseContext());
        pricePlaceholder = PlaceholderManager.getPriceString(numberConformer((lineChart.getHighlighted())[0].getY()), getActivity().getBaseContext());
        timestampPlaceholder = PlaceholderManager.getTimestampString(date, getActivity().getBaseContext());

        ((TextView) view.findViewById(R.id.volumeHightlight)).setText(volumePlaceholder);
        view.findViewById(R.id.volumeHightlight).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.priceHightlight)).setText(pricePlaceholder);
        view.findViewById(R.id.priceHightlight).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.timestampHightlight)).setText(timestampPlaceholder);
        view.findViewById(R.id.timestampHightlight).setVisibility(View.VISIBLE);
    }

    private LineData generatePriceLineChartSet()
    {
        LineDataSet dataSet;
        ArrayList<Entry> values = new ArrayList<>();

        int offsetRange = (int) Math.floor(dataChartList.size() / 200);

        if(offsetRange < 1)
        {
            offsetRange = 1;
        }

        for(int i = 0, j = 0; i < dataChartList.size(); i += offsetRange, j++)
        {
            values.add(new Entry(j, (float) dataChartList.get(i).getOpen()));
        }

        dataSet = new LineDataSet(values, "History");
        dataSet.setDrawIcons(false);
        dataSet.setColor(currency.getChartColor());
        dataSet.setLineWidth(1);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getColorWithAlpha(currency.getChartColor(), 0.5f));
        dataSet.setFormLineWidth(1);
        dataSet.setFormSize(15);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setHighLightColor(currency.getChartColor());
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        return new LineData(dataSet);
    }

    private int getColorWithAlpha(int color, float ratio)
    {
        int transColor;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        transColor = Color.argb(alpha, r, g, b);

        return transColor;
    }

    private void drawPriceCandleStickChart()
    {
        candleStickChart.setData(generatePriceCandleStickChartSet());
    }

    private CandleData generatePriceCandleStickChartSet()
    {
        CandleDataSet dataSet;
        ArrayList<CandleEntry> values = new ArrayList<>();

        int offsetRange = (int) Math.floor(dataChartList.size() / 200);

        if(offsetRange < 1)
        {
            offsetRange = 1;
        }

        for(int i = 0, j = 0; i < dataChartList.size(); i+= offsetRange, j++)
        {
            values.add(new CandleEntry(j, (float) dataChartList.get(i).getHigh()
                    , (float) dataChartList.get(i).getLow()
                    , (float) dataChartList.get(i).getOpen()
                    , (float) dataChartList.get(i).getClose()));
        }

        dataSet = new CandleDataSet(values, "");
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setDecreasingColor(MoodlBox.getColor(R.color.decreaseCandle, getActivity().getBaseContext()));
        dataSet.setShowCandleBar(true);
        dataSet.setShadowColorSameAsCandle(true);
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(MoodlBox.getColor(R.color.increaseCandle, getActivity().getBaseContext()));
        dataSet.setIncreasingPaintStyle(Paint.Style.STROKE);
        dataSet.setNeutralColor(MoodlBox.getColor(R.color.increaseCandle, getActivity().getBaseContext()));
        dataSet.setHighLightColor(MoodlBox.getColor(R.color.colorAccent, getActivity().getBaseContext()));
        dataSet.setDrawHorizontalHighlightIndicator(false);

        return new CandleData(dataSet);
    }

    private void updateChartsData(int timeUnit, int amount)
    {
        dataChartList = null;

        switch (timeUnit)
        {
            case HOUR:
                if(currency.getHistoryMinutes() != null)
                {
                    dataChartList = currency.getHistoryMinutes().subList(currency.getHistoryMinutes().size()-(60*amount), currency.getHistoryMinutes().size());
                }
                break;
            case DAY:
                if(amount == 1)
                {
                    dataChartList = currency.getHistoryMinutes();
                }
                else
                {
                    if(currency.getHistoryHours() != null)
                    {
                        dataChartList = currency.getHistoryHours().subList(currency.getHistoryHours().size()-(24*amount), currency.getHistoryHours().size());
                    }
                }
                break;
            case WEEK:
                if(currency.getHistoryHours() != null)
                {
                    dataChartList = currency.getHistoryHours().subList(currency.getHistoryHours().size()-168, currency.getHistoryHours().size());
                }
                break;
            case MONTH:
                switch (amount)
                {
                    case 1:
                        dataChartList = currency.getHistoryHours();
                        break;
                    case 3:
                        if(currency.getHistoryDays() != null)
                        {
                            dataChartList = currency.getHistoryDays().subList(currency.getHistoryDays().size()-93, currency.getHistoryDays().size());
                        }
                        break;
                    case 6:
                        if(currency.getHistoryDays() != null)
                        {
                            dataChartList = currency.getHistoryDays().subList(currency.getHistoryDays().size()-186, currency.getHistoryDays().size());
                        }
                        break;
                }
                break;
            case YEAR:
                dataChartList = currency.getHistoryDays();
                break;
        }
    }

    @Override
    public void onTimestampPriveUpdated(String price) {

    }

    @Override
    public void onHistoryDataUpdated() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (timeIntervalSpinner.getSelectedItemPosition())
                {
                    case 0:
                        updateChartTab(Charts.HOUR, 1);
                        break;
                    case 1:
                        updateChartTab(Charts.HOUR, 3);
                        break;
                    case 2:
                        updateChartTab(Charts.DAY, 1);
                        break;
                    case 3:
                        updateChartTab(Charts.DAY, 3);
                        break;
                    case 4:
                        updateChartTab(Charts.WEEK, 11);
                        break;
                    case 5:
                        updateChartTab(Charts.MONTH, 1);
                        break;
                    case 6:
                        updateChartTab(Charts.MONTH, 3);
                        break;
                    case 7:
                        updateChartTab(Charts.MONTH, 6);
                        break;
                    case 8:
                        updateChartTab(Charts.YEAR, 1);
                        break;
                }
            }
        });
    }

    @Override
    public void onPriceUpdated(Currency currency) {

    }
}
