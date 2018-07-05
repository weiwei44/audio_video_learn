package mobile.indoorbuy.com.audio_video_learn.less3

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import mobile.indoorbuy.com.audio_video_learn.common.ScreenUtils

/**
 * Created by BMW on 2018/7/4.
 */
class CameraSurfaceView: FrameLayout, SurfaceHolder.Callback{


    private val surfaceView = SurfaceView(context)
    private val camera: ACamera
    private var supportCameraBack = false // 是否支持后置

    constructor(context: Context) : super(context) {
        addPreview()
        camera = Came1Helper(surfaceView)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        addPreview()
        camera = Came1Helper(surfaceView)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        addPreview()
        //根据系统版本使用Camera或者Camera2
        camera = Came1Helper(surfaceView)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        if(supportCameraBack){
            camera.open(1)  //支持前置
        }else{
            camera.open(0)  //不支持钱置
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        camera.close()
    }

    //大多数手机：前摄像头预览数据旋转了90度，并且左右镜像了,后摄像头旋转了270度
    override fun surfaceCreated(holder: SurfaceHolder?) {

    }

    private fun addPreview() {
        surfaceView.holder.addCallback(this)
        this.addView(surfaceView)
    }

    fun takePicture(){
        camera.takePicture()
    }
}