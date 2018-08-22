package com.herbron.moodl.Activities.DetailsActivityFragments;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.Utils.PlaceholderUtils;
import com.herbron.moodl.R;

import static com.herbron.moodl.MoodlBox.numberConformer;

/**
 * Created by Tiji on 13/05/2018.
 */

public class InformationFragment extends Fragment {

    private Currency currency;
    private View view;
    private PreferencesManager preferencesManager;
    private boolean isSnapshotUpdated;
    private boolean isTickerUpdated;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.detailsactivity_fragment_informations, container, false);

        isSnapshotUpdated = false;
        isTickerUpdated = false;

        currency = getActivity().getIntent().getParcelableExtra("currency");
        preferencesManager = new PreferencesManager(getActivity().getBaseContext());

        updateInfoTab();

        return view;
    }

    private void updateInfoTab()
    {
        currency.updateSnapshot(getActivity().getBaseContext(), new Currency.CurrencyCallBack() {
            @Override
            public void onSuccess(final Currency currency) {
                isSnapshotUpdated = true;
                dataCounter();
            }
        });

        currency.updateTicker(getActivity().getBaseContext(), preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
            @Override
            public void onSuccess(Currency currency) {
                isTickerUpdated = true;
                dataCounter();
            }
        });
    }

    private void dataCounter()
    {
        if(isTickerUpdated && isSnapshotUpdated)
        {
            if(getActivity() != null)
            {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshInfoTab();
                    }
                });
            }
        }
    }

    private void refreshInfoTab()
    {
        Drawable progressBarDrawable = ((ProgressBar) view.findViewById(R.id.percentageCoinEmited)).getProgressDrawable();
        progressBarDrawable.mutate();
        progressBarDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        progressBarDrawable.invalidateSelf();

        ((ProgressBar) view.findViewById(R.id.percentageCoinEmited))
                .setProgress((int) Math.round(currency.getMinedCoinSupply() / currency.getMaxCoinSupply() * 100));

        if(currency.getAlgorithm() != null && !currency.getAlgorithm().equals(""))
        {
            ((TextView) view.findViewById(R.id.txtViewAlgorithm))
                    .setText(currency.getAlgorithm());
        }

        if(currency.getProofType() != null && !currency.getProofType().equals(""))
        {
            ((TextView) view.findViewById(R.id.txtViewProofType))
                    .setText(currency.getProofType());
        }

        if(currency.getStartDate() != null && !currency.getStartDate().equals(""))
        {
            ((TextView) view.findViewById(R.id.txtViewStartDate))
                    .setText(currency.getStartDate());
        }

        if(currency.getDescription() != null)
        {
            ((TextView) view.findViewById(R.id.txtViewDescription))
                    .setText(Html.fromHtml(currency.getDescription()));
        }

        ((TextView) view.findViewById(R.id.txtViewDescription))
                .setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.txtViewPercentageCoinEmited))
                .setText(PlaceholderUtils.getEmitedPercentageString(numberConformer(currency.getMinedCoinSupply() / currency.getMaxCoinSupply() * 100), getActivity().getBaseContext()));

        if(currency.getMarketCapitalization() != 0)
        {
            ((TextView) view.findViewById(R.id.txtViewMarketCapitalization))
                    .setText(PlaceholderUtils.getValueString(numberConformer(currency.getMarketCapitalization()), getActivity().getBaseContext()));
            view.findViewById(R.id.linearMarketCap).setVisibility(View.VISIBLE);
        }

        if(currency.getRank() != 0)
        {
            ((TextView) view.findViewById(R.id.txtViewRank))
                    .setText(String.valueOf(currency.getRank()));
            view.findViewById(R.id.linearRank).setVisibility(View.VISIBLE);
        }

        if(currency.getMaxCoinSupply() == 0)
        {
            ((TextView) view.findViewById(R.id.txtViewTotalSupply))
                    .setText(PlaceholderUtils.getSymbolString(getString(R.string.infinity), getActivity()));
        }
        else
        {
            ((TextView) view.findViewById(R.id.txtViewTotalSupply))
                    .setText(PlaceholderUtils.getSymbolString(numberConformer(currency.getMaxCoinSupply()), getActivity()));
        }
        ((TextView) view.findViewById(R.id.txtViewCirculatingSupply))
                .setText(PlaceholderUtils.getSymbolString(numberConformer(currency.getMinedCoinSupply()), getActivity()));
    }

}
