package com.kt.smartKibot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.view.View;

public class FaceRectangle extends View {
    private Paint paint;
    private Rect[] array;
    private double coefWidth, coefHeight;

    public FaceRectangle(Context context) {
	super(context);
	array = null;
	paint = new Paint();
	paint.setTextAlign(Align.CENTER);
	paint.setColor(Color.RED);
	paint.setStyle(Paint.Style.STROKE);
	paint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
	if (array != null) {
	    for (Rect r : array) {
		if (r != null) {
		    canvas.drawRect(getScaledRect(r), paint);
		}
	    }
	}
    }

    public void draw(Rect[] rect) {
	array = rect;
	invalidate();
    }

    public void setSize(int width, int height) {
	coefWidth = (double) width / (double) (CamConf.FRAME_WIDTH / 2);
	coefHeight = (double) height / (double) (CamConf.FRAME_HEIGHT / 2);
    }

    private Rect getScaledRect(Rect rect) {
	rect.left *= coefWidth;
	rect.right *= coefWidth;
	rect.top *= coefHeight;
	rect.bottom *= coefHeight;
	return flip(rect);
    }

    private Rect flip(Rect rect) {
	int rectWidth = rect.width();
	rect.left = getWidth() - rect.right;
	rect.right = rect.left + rectWidth;
	return rect;
    }
}
