package mobile.indoorbuy.com.audio_video_learn.less3

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.*
import java.util.*
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.graphics.ImageFormat




@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
/**
 * Created by BMW on 2018/7/4.
 */
class Camera2Helper(val view: View) : ACamera() {

    private val mCameraManager: CameraManager = (view.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
    private var surface: SurfaceTexture? = null

    private var mFlashSupported:Boolean = false
    private var mCameraId:String? = null
    private var mCameraDevice:CameraDevice? = null

    private var surfaceHolder:SurfaceHolder? = null
    private var mCaptureSession:CameraCaptureSession? = null

    private val mStateCallback = object :CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice?) {  //打开摄像头
            mCameraDevice = camera

            //开始预览
            takePreview()

        }

        override fun onDisconnected(camera: CameraDevice?) { //关闭摄像头
            camera!!.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            camera!!.close()
            mCameraDevice = null
        }

    }



    override fun open(type: Int, surfaceTexture: SurfaceTexture?) {
        this.surface = surface
        initCameraThread()

        openCamera()


    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun takePicture() {
       if(mCameraDevice == null)return
        // 创建拍照需要的CaptureRequest.Builder
        val captureRequestBuilder: CaptureRequest.Builder
        try{
            captureRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)

        }catch (e:CameraAccessException){
            e.printStackTrace()
        }
    }

    private var mCamThread:HandlerThread? = null
    private var mCamHandler:Handler? = null
    /**
     * 创建相机线程
     */
    private fun initCameraThread(){
        if (mCamThread==null){
            mCamThread = HandlerThread("CameraThread")
            mCamThread!!.start()
            mCamHandler = Handler(mCamThread!!.getLooper())
            return
        }
        if (mCamThread!!.isAlive()){
            Log.d("weiwei", "initCameraThread: camera thread is alive.")
            return
        }else{
            Log.d("weiwei", "initCameraThread: camera thread isn't null , and start it now.")
            mCamThread!!.start()
        }

    }


    private fun checkCameraId(): Boolean{
        try{
            //获取可用摄像头列表
            for(cameraId in mCameraManager.cameraIdList){
                //获取相机的相关参数
                val characteristics = mCameraManager.getCameraCharacteristics(cameraId)
                // 不使用前置摄像头。
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                mCameraId = cameraId

                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        ?: continue
                //获取默认配置文件中JPG的最大尺寸给ImageReader
               // val largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea())
                

                Log.e("weiwei"," 相机可用 ")
                return true

            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(): Boolean{
        if (!checkCameraId()) return false
        try {
            mCameraManager.openCamera(mCameraId,mStateCallback,mCamHandler)
        }catch (e:Exception){
            e.printStackTrace()
        }
       return true
    }


    private fun takePreview() {
        try {
            //设置了一个具有输出Surface的CaptureRequest.Builder。
            val mPreviewRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)


            if(view is SurfaceView) {
                val surfaceView:SurfaceView = view

                //设置Surface作为预览数据的显示界面
                mPreviewRequestBuilder.addTarget(surfaceView.holder.surface)
            }else if(view is TextureView){
                val textureView:TextureView = view
                val texture = textureView.surfaceTexture
                //设置TextureView的缓冲区大小
             //   texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight())

                //设置Surface作为预览数据的显示界面
                mPreviewRequestBuilder.addTarget(Surface(texture))
            }


            //创建相机捕获会话，
            // 第一个参数是捕获数据的输出Surface列表，
            // 第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，
            // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice!!.createCaptureSession(Arrays.asList(surfaceHolder!!.surface),
                    object :CameraCaptureSession.StateCallback(){
                        override fun onConfigureFailed(session: CameraCaptureSession?) {
                            Log.e("weiwei"," onConfigureFailed 开启预览失败")
                        }

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession?) {
                            // 相机已经关闭
                            if (null == mCameraDevice) {
                                return
                            }
                            // 会话准备好后，我们开始显示预览
                            mCaptureSession = cameraCaptureSession
                            try {
                                //自动对焦
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                                // 打开闪光灯
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                // 设置自动曝光模式
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                // 开启相机预览并添加事件
                                val mPreviewRequest = mPreviewRequestBuilder.build()
                                //发送请求
//                                mCaptureSession!!.setRepeatingRequest(mPreviewRequest,
//                                        null, mBackgroundHandler)
                                Log.e("weiwei"," 开启相机预览并添加事件")

                            }catch (e: CameraAccessException){
                                e.printStackTrace()
                            }
                        }

                    },null)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}