package com.kt.smartKibot;

import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.view.View;

public class FaceRectangle extends View {
    private Paint paint;
    private Vector<Rect> vector;
    private double coefWidth, coefHeight;

    public FaceRectangle(Context context) {
	super(context);
	vector = new Vector<Rect>();
	paint = new Paint();
	paint.setTextAlign(Align.CENTER);
	paint.setColor(Color.RED);
	paint.setStyle(Paint.Style.STROKE);
	paint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
	for (Rect r : vector) {
	    if (r != null) {
		canvas.drawRect(getScaledRect(r), paint);
	    }
	}
	vector.clear();
    }

    /**
     * Draw (and automatically invalidate the View) a Vector of rectangles
     * 
     * @param rect
     *            Vector of Rectangles to be drawn
     */
    public void draw(Vector<Rect> rect) {
	vector = rect;
	invalidate();
    }

    /**
     * Set the size of the current preview to calculate the scaling coefficient
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
	coefWidth = (double) width / (double) (CamConf.FRAME_WIDTH / 2);
	coefHeight = (double) height / (double) (CamConf.FRAME_HEIGHT / 2);
    }

    /**
     * 
     * @param rect
     *            Rectangle to be scaled
     * @return The same rectangle but scaled with the previously calculated
     */
    private Rect getScaledRect(Rect rect) {
	rect.left *= coefWidth;
	rect.right *= coefWidth;
	rect.top *= coefHeight;
	rect.bottom *= coefHeight;
	return rect;
    }
}
