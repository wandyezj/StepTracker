package megacorp.steptracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;
import java.util.Random;

public class CreativeView extends View {

    public abstract class Shape {

        public abstract void draw(Canvas canvas);
    }

    public class ShapeCircle extends Shape {

        private PointF center = new PointF();
        private float radius = 0;
        private Paint paint;

        public ShapeCircle(float x, float y, float r, Paint color) {
            center.set(x, y);
            radius = r;
            paint = color;
        }


        @Override
        public void draw(Canvas canvas) {
            canvas.drawCircle(center.x, center.y, radius, paint);
        }
    }




    public CreativeView(Context context) {
        super(context);
    }

    public CreativeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CreativeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr) {

    }


    private LinkedList<Shape> shapes = new LinkedList<>();


    private Random _random = new Random();

    private static int MIN_CIRCLE_RADIUS = 50;
    private static int MAX_CIRCLE_RADIUS = 200 + 1;
    private static int MAX_ARGB = 255 + 1;

    public void addShape()
    {


        // Generate random paint color
        Paint paint = new Paint();

        int a = _random.nextInt(MAX_ARGB);
        int r = _random.nextInt(MAX_ARGB);
        int g = _random.nextInt(MAX_ARGB);
        int b = _random.nextInt(MAX_ARGB);

        paint.setARGB(a, r, g, b);


        // generate random circle size and location
        int x = _random.nextInt(_width);
        int y = _random.nextInt(_height);
        int radius = MIN_CIRCLE_RADIUS + _random.nextInt(MAX_CIRCLE_RADIUS - MIN_CIRCLE_RADIUS);


        ShapeCircle circle  = new ShapeCircle(x, y, radius, paint);

        shapes.add(circle);
        invalidate();
    }


    private int _width = 0;
    private int _height = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        _width = w;
        _height = h;

    }

    @Override
    public void onDraw(Canvas canvas) {

        for ( Shape shape :shapes) {
            shape.draw(canvas);
        }
    }

}
