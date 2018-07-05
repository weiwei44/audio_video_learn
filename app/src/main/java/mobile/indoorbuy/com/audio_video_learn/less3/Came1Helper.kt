package mobile.indoorbuy.com.audio_video_learn.less3

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import mobile.indoorbuy.com.audio_video_learn.common.ScreenUtils
import java.io.IOException

/**
 * Created by BMW on 2018/7/4.
 */
class Came1Helper(val view: View) : ACamera() {

    private var cameraId = 0
    private var camera: Camera? = null
    private var surface: SurfaceTexture? = null

    override fun open(type: Int,surface: SurfaceTexture?) {
        this.cameraId = type
        this.surface = surface

        val rotation = (view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.rotation

        if (!openCamera(type)) return

        setParameters(camera, ScreenUtils.getScreenWidth(view.context), ScreenUtils.getScreenHeight(view.context))
        resizeDisplayView()
        setDisplayOrientation(camera,rotation)
        setPreviewDisplay(camera)

        camera!!.setDisplayOrientation(90)
        camera!!.startPreview()

    }

    override fun close() {
        camera!!.stopPreview()
        camera!!.release()
    }

    override fun takePicture() {
        camera!!.takePicture(
                { Log.e("weiwei","快门按下") },  //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
                { _, _ ->  Log.e("weiwei","源数据")},  // 拍摄的未压缩原数据的回调,可以为null
                { _, _ -> Log.e("weiwei","拍摄完成") }   //拍照完成的回调，这里可以保存图片,这里数据默认为yuv420sp
        )
    }

    /**
     * 检查是否支持相机
     * Camera.getNumberOfCameras()可以获得当前设备的Camera的个数 0.不支持相机 1.只有后置
     */
    private fun checkCameraId(cameraId: Int): Boolean =
            cameraId >= 0 && cameraId < Camera.getNumberOfCameras()

    /**
     * 打开相机，获取相机实例
     */
    private fun openCamera(cameraId: Int): Boolean {
        Log.e("weiwei","Camera.getNumberOfCameras() = ${Camera.getNumberOfCameras()}")
        if (!checkCameraId(cameraId)) return false
        camera = Camera.open(cameraId)
        return true
    }

    /**
     * 设置相机实例参数
     */
    private fun setParameters(camera: Camera?, screenWidth: Int, screenHeight: Int) {
        val parameters = camera!!.parameters

        //PreviewSize设置为设备支持的最高分辨率
        val previewSize = getBestSize(parameters.supportedPreviewSizes, screenWidth, screenHeight)
        Log.e("weiwei","previewSize = ${previewSize!!.width},${previewSize.height}")
        parameters.setPreviewSize(previewSize!!.width, previewSize.height)

        //PictureSize设置为和预览大小最近的
        val pictureSize = getBestSize(parameters.supportedPictureSizes, screenWidth, screenHeight)
        Log.e("weiwei","pictureSize = ${pictureSize!!.width},${pictureSize.height}")
        parameters.setPictureSize(pictureSize!!.width, pictureSize.height)

        //如果相机支持自动聚焦，则设置相机自动聚焦，否则不设置
        if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        }

        //设置拍照后存储的图片格式
        parameters.previewFormat = ImageFormat.NV21

        camera.parameters = parameters
    }

    /**
     * 获取最佳尺寸
     */
    private fun getBestSize(supportedPreviewSizes: List<Camera.Size>, screenWidth: Int, screenHeight: Int): Camera.Size? {
        var bestSize: Camera.Size? = null
        var largestArea = screenWidth * screenHeight
        for (size in supportedPreviewSizes) {
            if (size.width <= screenWidth && size.height <= screenHeight) {
                if (bestSize == null) {
                    bestSize = size
                } else {
                    val resultArea = bestSize.width * bestSize.height
                    val newArea = size.width * size.height

                    if (newArea > resultArea) {
                        bestSize = size
                    }
                }
            }
        }
        return bestSize
    }

    //调整SurfaceView的大小
    private fun resizeDisplayView() {
        val parameters = camera!!.parameters
        val size = parameters.previewSize
        val p = view.layoutParams as FrameLayout.LayoutParams
        val scale = size.width / size.height.toFloat()
        val displayScale = view.height.toFloat() / view.width.toFloat()
        if (scale > displayScale) {
            p.height = (scale * view.width).toInt()
            p.width = view.width
        } else {
            p.width = (view.height / scale).toInt()
            p.height = view.height
        }
        Log.e("weiwei","view : ${p.width},${p.height}")
        view.layoutParams = p
        view.invalidate()
    }


    //设置相机预览方向
    private fun setDisplayOrientation(camera: Camera?, rotation: Int) =
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
                camera!!.setDisplayOrientation(90)
            } else {
                camera!!.setDisplayOrientation(0)
            }

    //设置相机预览载体SurfaceHolder
    private fun setPreviewDisplay(camera: Camera?) {
        try {
            if(view is SurfaceView) {
                val surfaceView:SurfaceView = view
                camera!!.setPreviewDisplay(surfaceView.holder)
            }else if(view is TextureView){
                camera!!.setPreviewTexture(surface)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

}