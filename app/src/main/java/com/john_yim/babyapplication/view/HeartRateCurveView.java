package com.john_yim.babyapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.john_yim.babyapplication.R;

import java.util.Arrays;

import static com.john_yim.babyapplication.BabyService.VALUE_KEY;
import static com.john_yim.babyapplication.MainActivity.serviceBinder;

/**
 * Created by MSI-PC on 2017/9/15.
 */

class HeartRateCurveView extends View {


    public HeartRateCurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.isHeartBeat = false;
        this.heartRateHandler = new Handler(Looper.getMainLooper());
        this.path = new Path();
        this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        this.drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        this.practicalSpeed = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, this.SPEED, context.getResources().getDisplayMetrics());
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(5);
        if (!isInEditMode()) {
            this.paint.setColor(context.getResources().getColor(R.color.backgroundWhite));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.totalHeight = h;
        this.heartBeatWidth = w - this.paddingWidth;
        this.amplitude = this.totalHeight / 4;
        this.originalYPoition = new float[this.heartBeatWidth];
        Arrays.fill(this.originalYPoition, 0);
        this.periodFraction = (float) (Math.PI / this.heartBeatWidth * 6);
        for (int i = this.heartBeatWidth / 3; i < this.heartBeatWidth * 2 / 3; i++) {
            this.originalYPoition[i] = (float) (this.amplitude * Math.sin(this.periodFraction * i) + this.OFFSET_Y);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.isHeartBeat) {
            canvas.setDrawFilter(this.drawFilter);
            this.path.reset();
            this.path.moveTo(this.paddingWidth + this.offset, this.totalHeight / 2);
            boolean drawLine = false;
            for (int i = 0, j = this.paddingWidth + 1; i < this.heartBeatWidth - this.offset; i++) {
                if (this.heartBeatWidth - this.offset > this.heartBeatWidth / 3) {
                    if (!drawLine) {
                        path.lineTo(this.paddingWidth + this.offset + this.heartBeatWidth / 3, this.totalHeight / 2);
                        j += this.offset + this.heartBeatWidth / 3;
                        i = this.heartBeatWidth / 3;
                        drawLine = true;
                    } else if (drawLine && i >= this.heartBeatWidth * 2 / 3) {
                        this.path.lineTo(j, this.originalYPoition[i] + this.totalHeight / 2);
                        this.path.lineTo(this.heartBeatWidth, this.totalHeight / 2);
                        this.x = this.heartBeatWidth;
                        this.y = this.totalHeight / 2;
                        break;
                    } else {
                        this.path.lineTo(j, this.originalYPoition[i] + this.totalHeight / 2);
                        if (++j > this.heartBeatWidth) {
                            this.x = j;
                            this.y = this.originalYPoition[i] + this.totalHeight / 2;
                            break;
                        }
                    }
                } else if (!drawLine && this.originalYPoition[this.heartBeatWidth - this.offset - 1] == 0) {
                    this.path.lineTo(this.heartBeatWidth, this.totalHeight / 2);
                    this.x = this.heartBeatWidth;
                    this.y = this.totalHeight / 2;
                    break;
                }
            }
            canvas.drawPath(this.path, this.paint);
            canvas.drawCircle(this.x + 10, this.y, 10, this.paint);
        }
    }

    void heartBeat(final View heartRateValue) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (HeartFragment.viewFlag && serviceBinder != null && serviceBinder.getHeartRate() != null) {
                    final String rateText = serviceBinder.getHeartRate().get(VALUE_KEY).toString();
                    if (!rateText.equals("") && rateText != null) {
                        float rate = Float.parseFloat(rateText);
                        if (rate > 0) {
                            isHeartBeat = true;
                        }
                    }
                    heartRateHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) heartRateValue).setText(rateText);
                        }
                    });
                    for (offset = heartBeatWidth - paddingWidth; offset - practicalSpeed > 0; offset -= practicalSpeed) {
                        postInvalidate();
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    isHeartBeat = false;
                }
            }
        }).start();
    }

    private int totalHeight;
    private int heartBeatWidth;
    private float x;
    private float y;
    private final int paddingWidth = 100;
    private float[] originalYPoition;
    private float periodFraction = 0;
    private final int OFFSET_Y = 0;
    private volatile int offset = 1;
    private float amplitude = 200;
    private final int SPEED = 9;
    private int practicalSpeed;
    private boolean isHeartBeat;
    private Path path;
    private Paint paint;
    private DrawFilter drawFilter;
    private Handler heartRateHandler;
}
