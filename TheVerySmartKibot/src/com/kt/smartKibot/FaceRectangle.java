package com.kt.smartKibot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.view.View;

public class FaceRectangle extends View {
    private Paint p;
    private Rect r;

    public FaceRectangle(Context context) {
	super(context);
	r = null;
	p = new Paint();
	p.setTextAlign(Align.CENTER);
	p.setColor(Color.YELLOW);
	p.setStyle(Paint.Style.STROKE);
	p.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
	if (r != null) {
	    canvas.drawRect(r, p);
	}
    }

    public void draw(Rect rect) {
	r = rect;
	invalidate();
    }
}
