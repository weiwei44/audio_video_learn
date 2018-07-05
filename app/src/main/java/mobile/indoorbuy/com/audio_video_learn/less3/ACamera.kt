package mobile.indoorbuy.com.audio_video_learn.less3

import android.graphics.SurfaceTexture

/**
 * Created by BMW on 2018/7/4.
 */
abstract class ACamera{
    abstract fun open(type: Int,surfaceTexture: SurfaceTexture? = null)
    abstract fun close()
    abstract fun takePicture()
}