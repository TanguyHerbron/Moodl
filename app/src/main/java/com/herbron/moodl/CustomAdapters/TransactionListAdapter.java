package com.herbron.moodl.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.herbron.moodl.Activities.RecordTransactionActivity;
import com.herbron.moodl.DataManagers.CurrencyData.Trade;
import com.herbron.moodl.DataManagers.CurrencyData.Transaction;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.R;
import com.herbron.moodl.Utils.PlaceholderUtils;
import com.herbron.moodl.Utils.TransferUtils;

import java.util.ArrayList;

import static com.herbron.moodl.MoodlBox.collapseH;
import static com.herbron.moodl.MoodlBox.getDateFromTimestamp;
import static com.herbron.moodl.MoodlBox.numberConformer;
import static java.lang.Math.abs;

import static com.herbron.moodl.Utils.TransferUtils.isBalanceRelated;

/**
 * Created by Guitoune on 24/04/2018.
 */

public class TransactionListAdapter extends ArrayAdapter<Object> {

    private Context context;

    public TransactionListAdapter(Context context, ArrayList<Object> transactions)
    {
        super(context, android.R.layout.simple_list_item_1, transactions);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(getItem(position) instanceof Transaction)
        {
            return generateTransactionLayout(position, parent);
        }
        else
        {
            return generateTradeLayout(position, parent);
        }
    }

    private View generateTradeLayout(int position, ViewGroup parent)
    {
        Trade trade = (Trade) getItem(position);

        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_trade_row, parent, false);

        TextView amountTxtView = convertView.findViewById(R.id.amountPurchased);
        TextView purchasedPrice = convertView.findViewById(R.id.purchasePrice);
        TextView tradePair = convertView.findViewById(R.id.pair);
        TextView dateTxtView = convertView.findViewById(R.id.tradeDate);
        View tradeIndicator = convertView.findViewById(R.id.tradeIndicator);

        amountTxtView.setText(String.valueOf(trade.getQty()));
        purchasedPrice.setText(trade.getPrice());
        dateTxtView.setText(getDateFromTimestamp(trade.getTime()));
        tradePair.setText(trade.getSymbol() + "/" + trade.getPairSymbol());

        if(trade.isBuyer())
        {
            tradeIndicator.setBackgroundColor(context.getResources().getColor(R.color.green));
        }
        else
        {
            tradeIndicator.setBackgroundColor(context.getResources().getColor(R.color.red));
        }

        return convertView;
    }

    private View generateTransactionLayout(int position, ViewGroup parent)
    {
        final Transaction transaction = (Transaction) getItem(position);

        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_transaction_row, parent, false);

        TextView topLeftTextView = convertView.findViewById(R.id.transactionTLTV);
        TextView bottomLeftTextView = convertView.findViewById(R.id.transactionBLTV);
        TextView dateTxtView = convertView.findViewById(R.id.transactionDateTextView);
        TextView amountTxtView = convertView.findViewById(R.id.transactionAmountTextView);

        amountTxtView.setText(String.valueOf(transaction.getAmount()));
        dateTxtView.setText(getDateFromTimestamp(transaction.getTimestamp()));

        LinearLayout deleteLayout = convertView.findViewById(R.id.deleteTransactionLayout);
        deleteLayout.setTag(transaction.getTransactionId());

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesManager preferencesManager = new PreferencesManager(context);
                DatabaseManager databaseManager = new DatabaseManager(context);
                preferencesManager.setMustUpdateSummary(true);
                databaseManager.deleteTransactionFromId(Integer.parseInt(view.getTag().toString()));
                collapseH((View) view.getParent().getParent().getParent());
            }
        });

        LinearLayout editLayout = convertView.findViewById(R.id.editTransactionLayout);
        editLayout.setTag(transaction.getTransactionId());

        editLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = ((Activity) context).getTitle().toString();
                name = name.substring(1, name.indexOf("|") - 1);
                Intent intent = new Intent(context, RecordTransactionActivity.class);
                intent.putExtra("coin", name);
                intent.putExtra("symbol", transaction.getSymbol());
                intent.putExtra("transactionId", transaction.getTransactionId());
                context.startActivity(intent);
            }
        });

        View transactionIndicator = convertView.findViewById(R.id.transactionIndicator);

        switch (transaction.getType())
        {
            case "b":
                transactionIndicator.setBackgroundColor(context.getResources().getColor(R.color.increaseCandle));
                topLeftTextView.setText(transaction.getSource());
                bottomLeftTextView.setText(PlaceholderUtils.getToPairString(transaction.getSymbol(), transaction.getSymPair(), context));
                break;
            case "s":
                transactionIndicator.setBackgroundColor(context.getResources().getColor(R.color.decreaseCandle));
                topLeftTextView.setText(transaction.getSource());
                bottomLeftTextView.setText(PlaceholderUtils.getToPairString(transaction.getSymPair(), transaction.getSymbol(), context));
                break;
            case "t":
                transactionIndicator.setBackgroundColor(context.getResources().getColor(R.color.blue));

                if(isBalanceRelated(transaction.getDestination()) && isBalanceRelated(transaction.getSource()))
                {
                    topLeftTextView.setText(context.getString(R.string.transferText));

                    bottomLeftTextView.setText(PlaceholderUtils.getFromToString(TransferUtils.getLabelFor(context, transaction.getSource()), TransferUtils.getLabelFor(context, transaction.getDestination()), context));
                }
                else
                {
                    if(isBalanceRelated(transaction.getDestination()))
                    {
                        topLeftTextView.setText(context.getString(R.string.depositText));
                        bottomLeftTextView.setText(PlaceholderUtils.getFromString(TransferUtils.getLabelFor(context, transaction.getSource()), context));
                    }
                    else
                    {
                        if(isBalanceRelated(transaction.getSource()))
                        {
                            topLeftTextView.setText(context.getString(R.string.withdrawText));
                            bottomLeftTextView.setText(PlaceholderUtils.getToString(TransferUtils.getLabelFor(context, transaction.getDestination()), context));
                        }
                    }
                }

                break;
        }

        setupSwipeView(convertView);

        return convertView;
    }



    private void setupSwipeView(View view)
    {
        SwipeLayout swipeLayout =  view.findViewById(R.id.swipeLayout);

        //set show mode.
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, view.findViewById(R.id.bottom_wrapper));

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //when user's hand released.
            }
        });
    }
}
