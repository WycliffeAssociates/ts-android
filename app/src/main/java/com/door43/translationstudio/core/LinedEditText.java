package com.door43.translationstudio.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by blm on 11/25/15.
 */

public class LinedEditText extends EditText {
    private Rect mRect;
    private Paint mPaint;
    private boolean mEnableLines;

    // we need this constructor for LayoutInflater
    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0xFFC4E7FF); // same color as in GIF
        mEnableLines = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(mEnableLines) {
            Rect r = mRect;
            Paint paint = mPaint;

            Rect bounds = canvas.getClipBounds();
            int bottom = bounds.bottom;

            int lineHeight = (int) getLineHeight();
            int offset = lineHeight / 8; // offset so that text is above line

            int position = getLineBounds(0, r) + offset;

            for (int i = 0; i < 100; i++) {

                if(position > bottom) {
                    break;
                }

                canvas.drawLine(bounds.left, position, bounds.right, position, paint);
                position += lineHeight;
            }
        }

        super.onDraw(canvas);
    }

    public boolean isEnableLines() {
        return mEnableLines;
    }

    public void setEnableLines(boolean mEnableLines) {
        this.mEnableLines = mEnableLines;
    }

}