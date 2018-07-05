package mobile.indoorbuy.com.audio_video_learn.less3

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.widget.FrameLayout

/**
 * Created by BMW on 2018/7/4.
 */
class CameraTextureView: FrameLayout, TextureView.SurfaceTextureListener{


    private val textureView = TextureView(context)
    private val camera: ACamera
    private var supportCameraBack = false // 是否支持后置

    constructor(context: Context) : super(context) {
        addPreview()
        camera = Came1Helper(textureView)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        addPreview()
        camera = Came1Helper(textureView)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        addPreview()
        //根据系统版本使用Camera或者Camera2
        camera = Came1Helper(textureView)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        camera.close()
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if(supportCameraBack){
            camera.open(1,surface)  //支持前置
        }else{
            camera.open(0,surface)  //不支持钱置
        }
    }


    private fun addPreview() {
        textureView.surfaceTextureListener = this
        this.addView(textureView)
    }

    fun takePicture(){
        camera.takePicture()
    }
}