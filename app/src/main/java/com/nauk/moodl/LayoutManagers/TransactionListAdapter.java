package com.nauk.moodl.LayoutManagers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.nauk.moodl.Activities.CurrencyDetailsActivity;
import com.nauk.moodl.DataManagers.CurrencyData.Transaction;
import com.nauk.moodl.DataManagers.DatabaseManager;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static java.lang.Math.abs;

/**
 * Created by Guitoune on 24/04/2018.
 */

public class TransactionListAdapter extends ArrayAdapter<Transaction> {

    private Context context;

    public TransactionListAdapter(Context context, ArrayList<Transaction> transactions)
    {
        super(context, android.R.layout.simple_list_item_1, transactions);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Transaction transaction = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_transaction_row, parent, false);
        }

        TextView amountTxtView = convertView.findViewById(R.id.amountPurchased);
        TextView valueTxtView = convertView.findViewById(R.id.puchasedValue);
        TextView dateTxtView = convertView.findViewById(R.id.purchaseDate);

        amountTxtView.setText(String.valueOf(transaction.getAmount()));
        valueTxtView.setText(numberConformer(transaction.getPurchasedPrice() * transaction.getAmount()));
        dateTxtView.setText(getDate(transaction.getTimestamp()));

        LinearLayout deleteLayout = convertView.findViewById(R.id.deleteTransactionLayout);
        deleteLayout.setTag(transaction.getTransactionId());

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesManager preferencesManager = new PreferencesManager(context);
                DatabaseManager databaseManager = new DatabaseManager(context);
                preferencesManager.setMustUpdateSummary(true);
                databaseManager.deleteTransactionFromId(Integer.parseInt(view.getTag().toString()));
                collapse((View) view.getParent().getParent().getParent());
            }
        });

        setupSwipeView(convertView);

        return convertView;
    }

    private static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
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


    private String getDate(long timeStamp){

        try{
            SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm dd/MM/yyyy", Locale.getDefault());
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    private String numberConformer(double number)
    {
        String str;

        if(abs(number) > 1)
        {
            str = String.format( Locale.UK, "%.2f", number).replaceAll("\\.?0*$", "");
        }
        else
        {
            str = String.format( Locale.UK, "%.4f", number).replaceAll("\\.?0*$", "");
        }

        if(!str.equals("Infinity"))
        {
            int counter = 0;
            int i = str.indexOf(".");
            if(i <= 0)
            {
                i = str.length();
            }
            for(i -= 1; i > 0; i--)
            {
                counter++;
                if(counter == 3)
                {
                    str = str.substring(0, i) + " " + str.substring(i, str.length());
                    counter = 0;
                }
            }
        }

        return str;
    }
}
