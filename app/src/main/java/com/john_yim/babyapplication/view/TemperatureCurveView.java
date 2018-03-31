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
import android.view.View;
import android.widget.TextView;

import com.john_yim.babyapplication.R;

import java.util.ArrayList;
import java.util.List;

import static com.john_yim.babyapplication.BabyService.VALUE_KEY;
import static com.john_yim.babyapplication.BabyService.WARING_KEY;
import static com.john_yim.babyapplication.MainActivity.serviceBinder;

/**
 * Created by MSI-PC on 2017/9/21.
 */

class TemperatureCurveView extends View {
    public TemperatureCurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.temperatureHandler = new Handler(Looper.getMainLooper());
        this.path = new Path();
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        this.paint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        if (!isInEditMode()) {
            this.paint.setColor(context.getResources().getColor(R.color.temperatureGreen));
        }
        this.temperatureList = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.totalHeight = h;
        this.totalWidth = w - this.paddingWidth;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (this.temperatureList) {
            if (this.temperatureList.size() <= 0) {
                return;
            }
            if (this.temperatureList.size() > this.totalWidth) {
                this.temperatureList = this.temperatureList.subList(this.temperatureList.size() - this.totalWidth, this.temperatureList.size() - 1);
            }
            canvas.setDrawFilter(this.drawFilter);
            this.path.reset();
            this.path.moveTo(this.paddingWidth, this.totalHeight);
            float lastTemperatureIndex = 0;
            for (int i = 0; i < this.temperatureList.size(); i++) {
                float temperature = temperatureList.get(i);
                this.path.lineTo(this.paddingWidth + i,( - temperature / 0.5F + 72) * 100 + 490);
                lastTemperatureIndex = i;
            }
            this.path.lineTo(this.paddingWidth + lastTemperatureIndex, this.totalHeight);
        }
        canvas.drawPath(this.path, this.paint);
    }

    void thermometric(final View temperatureFragment) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (TemperatureFragment.viewFlag && serviceBinder != null && serviceBinder.getTemperature() != null) {
                    final boolean warningFlag = (boolean) serviceBinder.getTemperature().get(WARING_KEY);
                    final String temperatureText = serviceBinder.getTemperature().get(VALUE_KEY).toString();
                    temperatureHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (warningFlag) {
                                temperatureFragment.findViewById(R.id.alertBackground).setBackground(getResources().getDrawable(R.drawable.temperature_background_senior));
                                ((TextView) temperatureFragment.findViewById(R.id.temperatureStatus)).setText("警告");
                            } else {
                                temperatureFragment.findViewById(R.id.alertBackground).setBackground(getResources().getDrawable(R.drawable.temperature_background_normal));
                                ((TextView) temperatureFragment.findViewById(R.id.temperatureStatus)).setText("正常");
                            }
                            ((TextView) temperatureFragment.findViewById(R.id.temperatureValue)).setText(temperatureText);
                        }
                    });
                    synchronized (temperatureList) {
                        temperatureList.add(Float.valueOf(temperatureText));
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    postInvalidate();
                }
            }
        }).start();
    }

    private int totalWidth;
    private int totalHeight;
    private int paddingWidth = 200;
    public List<Float> temperatureList;
    private Paint paint;
    private Path path;
    private DrawFilter drawFilter;
    private Handler temperatureHandler;
}
