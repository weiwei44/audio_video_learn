package mobile.indoorbuy.com.audio_video_learn.less1;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import mobile.indoorbuy.com.audio_video_learn.R;

/**
 * Created by BMW on 2018/6/27.
 */

public class ImageShowView extends SurfaceView implements SurfaceHolder.Callback,Runnable {


    private final SurfaceHolder surfaceHolder;
    private final Bitmap src;
    private final Paint paint;

    public ImageShowView(Context context) {
        this(context,null);
    }

    public ImageShowView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ImageShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        src = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint.setStrokeWidth(2);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // 对画布锁定，获取SurfaceView的Canvas
        Canvas canvas = surfaceHolder.lockCanvas();
        //画图
        canvas.drawBitmap(src, new Matrix(), paint);
        //解锁画布
        surfaceHolder.unlockCanvasAndPost(canvas);
        //重新锁一次，"持久化"上次所绘制的内容
        surfaceHolder.lockCanvas(new Rect(0, 0, 0, 0));
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void run() {

    }
}
