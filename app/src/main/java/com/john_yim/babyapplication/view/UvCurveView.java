package com.john_yim.babyapplication.view;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.john_yim.babyapplication.R;

import java.util.Hashtable;

import static com.john_yim.babyapplication.BabyService.VALUE_KEY;
import static com.john_yim.babyapplication.MainActivity.serviceBinder;

/**
 * Created by MSI-PC on 2017/9/26.
 */

class UvCurveView extends View {
    public UvCurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.uvHandler = new Handler(Looper.getMainLooper());
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        this.intervalAngle = 26F;
        this.arcWidth = 20;
        this.padding = 25;
        this.argbEvaluator = new ArgbEvaluator();
        this.drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        this.initTextPaint(context);
        this.initArcPaint(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.totalHeight = h;
        this.totalWidth = w;
        if (this.totalWidth < this.totalHeight) {
            this.radius = this.totalWidth / 2 - this.padding;
        } else {
            this.radius = this.totalHeight / 2 - this.padding;
        }
        this.rectF = new RectF(this.totalWidth / 2 - this.radius, this.totalHeight / 2 - this.radius,
                this.totalWidth / 2 + this.radius, this.totalHeight / 2 + this.radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(this.drawFilter);
        super.onDraw(canvas);
        this.drawText(canvas);
        this.drawProgress(canvas);
    }

    private void drawText(Canvas canvas) {
        for (int i = 0; i <= 10; i++) {
            canvas.save();
            canvas.rotate(this.intervalAngle * i - 130, totalWidth / 2, totalHeight / 2);
            canvas.drawText(i + "", this.totalWidth / 2, this.arcWidth * 4 + this.padding, this.textPaint);
            canvas.restore();
        }
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawArc(this.rectF, 140, 260, false, this.arcBackgroundPaint);
        Integer uvColor;
        for (int i = 0, startAngle = 140; i < 260; i++, startAngle++) {
            uvColor = (Integer) this.argbEvaluator.evaluate(i / 260f, Color.YELLOW, Color.RED);
            this.arcPiant.setColor(uvColor);
            canvas.drawArc(this.rectF, startAngle, 1, false, this.arcPiant);
            if (i >= this.uv / 10 * 260f) {
                break;
            }
        }
    }

    private void initTextPaint(Context context) {
        this.textPaint = new Paint();
        if (!isInEditMode()) {
            this.textPaint.setColor(context.getResources().getColor(R.color.backgroundWhite));
        }
        this.textPaint.setStrokeWidth(1);
        this.textPaint.setTextSize(50);
        this.textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initArcPaint(Context context) {
        this.arcBackgroundPaint = new Paint();
        if (!isInEditMode()) {
            this.arcBackgroundPaint.setColor(context.getResources().getColor(R.color.backgroundWhite));
        }
        this.arcBackgroundPaint.setStyle(Paint.Style.STROKE);
        this.arcBackgroundPaint.setStrokeWidth(this.arcWidth);
        this.arcBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        this.arcPiant = new Paint(this.arcBackgroundPaint);
    }

    public void uvDetection(final View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (UvFragment.viewFlag) {
                    try {
                        uvTable = serviceBinder.getUv();
                        uv = Float.valueOf(uvTable.get(VALUE_KEY).toString());
                        uvHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) view.findViewById(R.id.uvValue)).setText(uvTable.get(VALUE_KEY).toString());
                                if (uv < 3) {
                                    ((TextView) view.findViewById(R.id.uvStatus)).setText("低");
                                } else if (uv >=3 && uv < 5) {
                                    ((TextView) view.findViewById(R.id.uvStatus)).setText("中等");
                                } else if (uv >= 5 && uv < 7) {
                                    ((TextView) view.findViewById(R.id.uvStatus)).setText("高");
                                } else if (uv >= 7 && uv < 10) {
                                    ((TextView) view.findViewById(R.id.uvStatus)).setText("甚高");
                                } else {
                                    ((TextView) view.findViewById(R.id.uvStatus)).setText("极高");
                                }
                            }
                        });
                        Thread.sleep(500);
                        postInvalidate();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private int totalWidth;
    private int totalHeight;
    private int radius;
    private int arcWidth;
    private int padding;
    private float intervalAngle;
    private float uv;
    private Paint textPaint;
    private Paint arcBackgroundPaint;
    private Paint arcPiant;
    private DrawFilter drawFilter;
    private RectF rectF;
    private ArgbEvaluator argbEvaluator;
    private Hashtable uvTable;
    private Handler uvHandler;
}
