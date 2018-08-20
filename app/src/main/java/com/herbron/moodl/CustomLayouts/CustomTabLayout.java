package com.herbron.moodl.CustomLayouts;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.herbron.moodl.R;

public class CustomTabLayout extends TabLayout {

    private LinearLayout linearLayout;
    private Context context;

    public CustomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        linearLayout = (LinearLayout) getChildAt(0);
        this.context = context;
    }

    private StateListDrawable getSellStateListDrawable() {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[] {android.R.attr.state_pressed},
                ContextCompat.getDrawable(context, R.drawable.unselected_tab_background));
        sld.addState(new int[] {android.R.attr.state_selected},
                ContextCompat.getDrawable(context, R.drawable.record_transaction_tab_background_sell));
        sld.addState(new int[] {android.R.attr.state_enabled },
                ContextCompat.getDrawable(context, R.drawable.unselected_tab_background));
        sld.addState(new int[] { },
                ContextCompat.getDrawable(context, R.drawable.disabled_tab_background));
        return sld;
    }

    private StateListDrawable getBuyStateListDrawable() {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[] {android.R.attr.state_pressed},
                ContextCompat.getDrawable(context, R.drawable.unselected_tab_background));
        sld.addState(new int[] {android.R.attr.state_selected},
                ContextCompat.getDrawable(context, R.drawable.record_transaction_tab_background_buy));
        sld.addState(new int[] {android.R.attr.state_enabled },
                ContextCompat.getDrawable(context, R.drawable.unselected_tab_background));
        sld.addState(new int[] { },
                ContextCompat.getDrawable(context, R.drawable.disabled_tab_background));
        return sld;
    }

    private StateListDrawable getTransferStateListDrawable() {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[] {android.R.attr.state_pressed},
                ContextCompat.getDrawable(context, R.drawable.unselected_tab_background));
        sld.addState(new int[] {android.R.attr.state_selected},
                ContextCompat.getDrawable(context, R.drawable.record_transaction_tab_background_transfer));
        sld.addState(new int[] {android.R.attr.state_enabled },
                ContextCompat.getDrawable(context, R.drawable.unselected_tab_background));
        sld.addState(new int[] { },
                ContextCompat.getDrawable(context, R.drawable.disabled_tab_background));
        return sld;
    }

    public void addTab(int index, String label) {
        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(label);
        textView.setGravity(GRAVITY_CENTER);
        textView.setTextColor(getResources().getColor(R.color.separationColor));
        addTab(newTab().setCustomView(textView));

        View tabView = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
        params.setMargins(2, 0, 2, 0);
        tabView.setLayoutParams(params);

        switch (index)
        {
            case 0:
                tabView.setBackground(getBuyStateListDrawable());
                break;
            case 1:
                tabView.setBackground(getSellStateListDrawable());
                break;
            case 2:
                tabView.setBackground(getTransferStateListDrawable());
                break;
        }
    }
}