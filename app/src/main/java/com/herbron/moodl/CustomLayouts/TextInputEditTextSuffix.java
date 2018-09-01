package com.herbron.moodl.CustomLayouts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.design.widget.TextInputEditText;
import android.text.TextPaint;
import android.util.AttributeSet;

public class TextInputEditTextSuffix extends TextInputEditText {

    private TextPaint textPaint = new TextPaint();
    private String suffix = "";

    public TextInputEditTextSuffix(Context context) {
        super(context);
    }

    public TextInputEditTextSuffix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextInputEditTextSuffix(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(!getText().toString().equals(""))
        {
            int suffixXPosition = (int) textPaint.measureText(getText().toString() + getPaddingLeft());
            canvas.drawText(suffix, Math.max(suffixXPosition, 0), getBaseline(), textPaint);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        textPaint.setColor(getCurrentTextColor());
        textPaint.setTextSize(getTextSize());
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }
}
