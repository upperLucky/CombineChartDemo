package com.yunkunwen.chartdemo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.Calendar;


/**
 * Created by yunkun.wen on 2017/10/11.
 */

public class FinanceChartView extends View {

    private Context mContext;
    private Paint mPaint;
    private ChartParam mChartParam;
    private String baseNumber; // 基线处的值
    private float baseLineY; // 基线纵坐标
    private float yearY; // 年份纵坐标
    private float rectLeft; // 矩形left
    private float lastX, lastY;
    private float[] rectStartX; // 柱状图的startX
    private float[] rectStopX; // 柱状图的stopX
    private float[] rectStopY; // 柱状图的stopY
    private int touchSlop;
    private int touchRectPoi = -1; // 触摸的柱状图

    private static final int BASE_LINE = 1;
    private static final int NUMBER_TEXT = 2;
    private static final int RECTANGLE = 3;
    private static final int BROKE_LINE = 4;
    private static final int CARD_LINE = 5;
    private static final int ROUND_RECT = 6;
    private static final int CARD_TEXT = 7;
    private static final int ROUND_RECT_IN = 8;


    public FinanceChartView(Context context) {
        super(context);
        init();
    }


    public FinanceChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FinanceChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mContext = getContext();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChartParam = new ChartParam();
        mChartParam.baseLineWidth = getWidth(mContext) - dpToPx(mContext, 38 * 2);
        mChartParam.screenWidth = dpToPx(mContext, 375);
        mChartParam.horNumberSpace = dpToPx(mContext, 28);
        mChartParam.verNumberSpace = dpToPx(mContext, 23);
        mChartParam.rectangleWidth = dpToPx(mContext, 12);
        mChartParam.circleRadius = dpToPx(mContext, 2);
        mChartParam.numberSize = dpToPx(mContext, 11);
        mChartParam.sideSpace = dpToPx(mContext, 15);
        mChartParam.leftNumSpace = dpToPx(mContext, 30);
        mChartParam.leftToBaseLine = dpToPx(mContext, 8);
        mChartParam.baseLineToYear = dpToPx(mContext, 5);
        mChartParam.baseLineToLeft = dpToPx(mContext, 38);
        mChartParam.firstRectCenterX = dpToPx(mContext, 79);
        mChartParam.firstRectLeft = dpToPx(mContext, 73);
        mChartParam.firstRextRight = dpToPx(mContext, 85);
        mChartParam.rectSpace = dpToPx(mContext, 40);
        mChartParam.overRectHeight = dpToPx(mContext, 20);
        mChartParam.cardToBaseLine = dpToPx(mContext, 58);
        mChartParam.cardToRect = dpToPx(mContext, 8);
        mChartParam.roundRectAngle = dpToPx(mContext, 2);
        mChartParam.cardWidth = dpToPx(mContext, 127);
        mChartParam.cardHeight = dpToPx(mContext, 63);
        mChartParam.card_textMarginTop = dpToPx(mContext, 8);
        mChartParam.card_textMarginLeft = dpToPx(mContext, 8);
        mChartParam.card_text_1to2 = dpToPx(mContext, 8);
        mChartParam.card_text_2to3 = dpToPx(mContext, 6);
        mChartParam.card_text_1toRect = dpToPx(mContext,20);
        mChartParam.card_text_2toRect = dpToPx(mContext,38);
        mChartParam.card_text_3toRect = dpToPx(mContext,55);

        rectStartX = new float[5];
        rectStopX = new float[5];
        rectStopY = new float[5];

        touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLeftNumber(canvas);
        drawBaseLine(canvas);
        drawRightNumber(canvas);
        drawYear(canvas);
        drawRectangle(canvas);
        drawPointAndLine(canvas);
        drawVerLine(canvas);
        drawTipsCard(canvas);
    }


    /**
     * 基线
     *
     * @param canvas
     */
    private void drawBaseLine(Canvas canvas) {

        setPaint(BASE_LINE);

        canvas.drawLine(mChartParam.baseLineToLeft, baseLineY - mChartParam.baseLineToYear, mChartParam.baseLineToLeft + mChartParam.baseLineWidth, baseLineY - mChartParam.baseLineToYear, mPaint);
    }


    /**
     * 左边数字栏
     *
     * @param canvas
     */
    private void drawLeftNumber(Canvas canvas) {

        for (int i = 0; i < 6; i++) {
            setPaint(NUMBER_TEXT);
            String text = ((5.0 - i) + "").equals("0.0") ? "0" : 5.0 - i + "";
            float number = Float.parseFloat(text);
            int textWidth = (int) mPaint.measureText(text);
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            canvas.drawText(text, mChartParam.leftNumSpace - textWidth,
                    mChartParam.verNumberSpace * i + (Math.abs(fontMetrics.ascent) + fontMetrics.descent) * (i + 1),
                    mPaint);
            if (number == 0 || Math.abs(number) <= 0.2) {
                baseNumber = number + "";
                // 该值是基线相关数字最上面的值
                baseLineY = mChartParam.verNumberSpace * i + (Math.abs(fontMetrics.ascent) + fontMetrics.descent) * i;
            }
            if (i == 5) {
                yearY = mChartParam.verNumberSpace * i + (Math.abs(fontMetrics.ascent) + fontMetrics.descent) * (i + 1);
            }
        }
    }

    /**
     * 右边数字栏
     *
     * @param canvas
     */
    private void drawRightNumber(Canvas canvas) {

        for (int i = 0; i < 6; i++) {
            setPaint(NUMBER_TEXT);
            String text = ((100 - 20 * i) + "").equals("0") ? "0" : (100 - 20 * i + "%");
            int textWidth = (int) mPaint.measureText(text);
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            canvas.drawText(text, getWidth(mContext) - mChartParam.sideSpace - textWidth,
                    mChartParam.verNumberSpace * i + (Math.abs(fontMetrics.ascent) + fontMetrics.descent) * (i + 1),
                    mPaint);
        }
    }


    /**
     * 年份
     *
     * @param canvas
     */
    private void drawYear(Canvas canvas) {

        setPaint(NUMBER_TEXT);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        for (int i = 0; i < 5; i++) {
            String text = year - (4 - i) + "";
            int textWidth = (int) mPaint.measureText(text);
            canvas.drawText(text, mChartParam.firstRectCenterX - textWidth / 2 + (textWidth + mChartParam.horNumberSpace) * i, yearY, mPaint);
            rectLeft = mChartParam.firstRectCenterX - textWidth / 2 + (textWidth + mChartParam.horNumberSpace) * i + textWidth / 2 - dpToPx(mContext, 6);
        }
    }

    /**
     * 柱状图
     *
     * @param canvas
     */
    private void drawRectangle(Canvas canvas) {
        setPaint(RECTANGLE);
        for (int i = 0; i < 5; i++) {
            canvas.drawRect(mChartParam.firstRectLeft + (mChartParam.rectangleWidth + mChartParam.rectSpace) * i,
                    dpToPx(mContext, 60) - i * 2,
                    mChartParam.firstRextRight + (mChartParam.rectangleWidth + mChartParam.rectSpace) * i,
                    baseLineY - mChartParam.baseLineToYear, mPaint);
            rectStartX[i] = mChartParam.firstRectLeft + (mChartParam.rectangleWidth + mChartParam.rectSpace) * i;
            rectStopX[i] = mChartParam.firstRextRight + (mChartParam.rectangleWidth + mChartParam.rectSpace) * i;
            rectStopY[i] = dpToPx(mContext, 60) - i * 2;
        }
    }

    /**
     * 点和折线
     *
     * @param canvas
     */
    private void drawPointAndLine(Canvas canvas) {
        setPaint(BROKE_LINE);
        for (int i = 0; i < 5; i++) {
            canvas.drawCircle(mChartParam.firstRectLeft + (mChartParam.rectangleWidth + mChartParam.rectSpace) * i + mChartParam.rectangleWidth / 2,
                    50 * i + 20, mChartParam.circleRadius, mPaint);
            if (i != 4) {
                float startX = mChartParam.firstRectLeft + (mChartParam.rectangleWidth + mChartParam.rectSpace) * i + mChartParam.rectangleWidth / 2;
                float stopX = mChartParam.firstRectLeft + (mChartParam.rectangleWidth + mChartParam.rectSpace) * (i + 1) + mChartParam.rectangleWidth / 2;
                float startY = 50 * i + 20;
                float stopY = 50 * (i + 1) + 20;
                canvas.drawLine(startX, startY, stopX, stopY, mPaint);
            }
        }
    }

    /**
     * 触摸柱状图时中间的竖线
     * @param canvas
     */
    private void drawVerLine(Canvas canvas) {
        if (touchRectPoi != -1) {
            setPaint(CARD_LINE);
            float lineX = rectStartX[touchRectPoi] + (rectStopX[touchRectPoi] - rectStartX[touchRectPoi]) / 2;
            canvas.drawLine(lineX, baseLineY - mChartParam.baseLineToYear, lineX, rectStopY[touchRectPoi] - mChartParam.overRectHeight, mPaint);
        }
    }

    /**
     * 卡片
     * @param canvas
     */
    private void drawTipsCard(Canvas canvas) {
        // 圆角矩形与其中的文字
        float roundRectLeft = 0, roundRectTop = 0;
        if (touchRectPoi != -1) {
            setPaint(ROUND_RECT);
            float lineX = rectStartX[touchRectPoi] + (rectStopX[touchRectPoi] - rectStartX[touchRectPoi]) / 2;
            if (touchRectPoi >= 0 && touchRectPoi < 3) {
                // 显示在柱状图右边
                canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.roundrect), lineX + mChartParam.cardToRect, baseLineY - mChartParam.cardToBaseLine - mChartParam.cardHeight,mPaint);
                roundRectLeft = lineX + mChartParam.cardToRect;
                roundRectTop = baseLineY - mChartParam.cardToBaseLine - mChartParam.cardHeight;
            } else {
                // 显示在柱状图左边
                canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.roundrect), lineX - mChartParam.cardToRect - mChartParam.cardWidth, baseLineY - mChartParam.cardToBaseLine - mChartParam.cardHeight,mPaint);
                roundRectLeft = lineX - mChartParam.cardToRect - mChartParam.cardWidth;
                roundRectTop = baseLineY - mChartParam.cardToBaseLine - mChartParam.cardHeight;
            }

            // 文字
            setPaint(CARD_TEXT);
            String text1 = "2017年年报";
            String text2 = "每股收益：1.83元";
            String text3 = "同比增长率：-12.00%";
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            canvas.drawText(text1, roundRectLeft + mChartParam.card_textMarginLeft,
                    roundRectTop + mChartParam.card_text_1toRect, mPaint);
            canvas.drawText(text2, roundRectLeft + mChartParam.card_textMarginLeft,
                    roundRectTop + mChartParam.card_text_2toRect, mPaint);
            canvas.drawText(text3, roundRectLeft + mChartParam.card_textMarginLeft,
                    roundRectTop + mChartParam.card_text_3toRect, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchToRect(touchX);
                lastX = touchX;
                return true;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                if (Math.abs(moveX - lastX) > touchSlop) {
                    lastX = moveX;
                    touchToRect(moveX);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 在柱状图范围
     * @param x
     */
    private void touchToRect(float x) {
        if (x >= rectStartX[0] && x <= rectStopX[0]) {
            touchRectPoi = 0;
            Log.d("wyk", "第一个柱状图");
        } else if (x >= rectStartX[1] && x <= rectStopX[1]) {
            touchRectPoi = 1;
            Log.d("wyk", "第二个柱状图");
        } else if (x >= rectStartX[2] && x <= rectStopX[2]) {
            touchRectPoi = 2;
            Log.d("wyk", "第三个柱状图");
        } else if (x >= rectStartX[3] && x <= rectStopX[3]) {
            touchRectPoi = 3;
            Log.d("wyk", "第四个柱状图");
        } else if (x >= rectStartX[4] && x <= rectStopX[4]) {
            touchRectPoi = 4;
            Log.d("wyk", "第五个柱状图");
        }
        invalidate();
    }

    private void setPaint(int type) {

        mPaint.reset();
        switch (type) {
            case BASE_LINE:
                mPaint.setAntiAlias(true);
                mPaint.setColor(getResources().getColor(R.color.hs_text_929292));
                mPaint.setStrokeWidth(1);
                break;
            case NUMBER_TEXT:
                mPaint.setAntiAlias(true);
                mPaint.setColor(getResources().getColor(R.color.hs_text_666666));
                mPaint.setTextSize(mChartParam.numberSize);
                break;
            case RECTANGLE:
                mPaint.setAntiAlias(true);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(getResources().getColor(R.color.hszq_viewbg_green));
                break;
            case BROKE_LINE:
                mPaint.setAntiAlias(true);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(getResources().getColor(R.color.finance_line_ff8200));
                break;
            case CARD_LINE:
                mPaint.setAntiAlias(true);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(getResources().getColor(R.color.finance_line_c9c9ca));
                break;
            case ROUND_RECT:
                mPaint.setAntiAlias(true);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(getResources().getColor(R.color.finance_line_c9c9ca));
                break;
            case CARD_TEXT:
                mPaint.setAntiAlias(true);
                mPaint.setTextSize(mChartParam.numberSize);
                mPaint.setColor(getResources().getColor(R.color.hs_font_color_333333));
                break;
            case ROUND_RECT_IN:
                mPaint.setAntiAlias(true);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(getResources().getColor(R.color.htjc_white));
                break;
        }
    }

    class ChartParam {
        private int verNumberSpace;
        private int horNumberSpace;
        private int numberSize;
        private int rectangleWidth;
        private int circleRadius; // 小圆半径
        private int baseLineWidth;
        private int screenWidth;
        private int sideSpace; // 距两边边距
        private int leftNumSpace;// 左边数字终点距y轴的距离
        private int leftToBaseLine; // 左边数字终点距基线的距离
        private int baseLineToYear; // 基线到年份的距离
        private int baseLineToLeft;
        private int firstRectCenterX; // 第一个矩形中点距边界距离
        private int firstRectLeft; // 第一个矩形left属性
        private int firstRextRight;
        private int rectSpace;
        private int overRectHeight;  // 高出相对柱状图的高度


        private int cardToBaseLine; // 提示相对基线的高度
        private int cardToRect; // 提示相对柱状图的边距
        private int roundRectAngle; // card圆角大小
        private int cardWidth;// card宽
        private int cardHeight;// card高

        private int card_textMarginTop;
        private int card_textMarginLeft;
        private int card_text_1to2;// 第一行文字距第二行文字距离
        private int card_text_2to3; // 第二行文字距第三行文字距离
        private int card_text_1toRect;// 第一行文字的x距圆角矩形的距离
        private int card_text_2toRect;//
        private int card_text_3toRect;//
    }

    /**
     * dp单位转换成像素px
     */
    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
                .getDisplayMetrics());
    }

    /**
     * @return 屏幕宽度
     */
    private int getWidth(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        //activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
