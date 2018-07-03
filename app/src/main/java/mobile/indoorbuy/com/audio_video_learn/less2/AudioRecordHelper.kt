package mobile.indoorbuy.com.audio_video_learn.less2

import android.content.ContentValues.TAG
import android.media.*
import android.os.Environment
import android.util.Log
import java.io.*

/**
 * Created by BMW on 2018/7/2.
 */
class AudioRecordHelper : Runnable {
    private val dirPath = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei"
    private lateinit var file: File
    private var isRecording = false
    private var maxBufferSize = 0
    private lateinit var audioRecord: AudioRecord
    private var thread: Thread? = null


    fun initRecord(){
        maxBufferSize = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,  //手机麦克风输入的音频
                44100,         //采样率
                AudioFormat.CHANNEL_IN_MONO,   //单通道
                AudioFormat.ENCODING_PCM_16BIT, //16bit数据位宽，就是一个short
                maxBufferSize                  //缓冲区大小,必须为一帧的2到N倍大小
        )

        file = File(dirPath).apply {
            if (!exists()) mkdirs()
        }
    }

    override fun run() {
        val audioFile = File(dirPath, "w.pcm")
                .apply {
                    if (exists()) delete()
                }
        audioFile.createNewFile()
        val outputStream: DataOutputStream
        try {
            outputStream = DataOutputStream(
                            FileOutputStream(audioFile))
            val buffer = ByteArray(maxBufferSize)
            //开始录音
            audioRecord.startRecording()
            while (isRecording) {
                //返回读取到的个数
                val ret = audioRecord.read(buffer, 0, maxBufferSize)
                if (ret == AudioRecord.ERROR_INVALID_OPERATION || ret == AudioRecord.ERROR_BAD_VALUE) {
                    continue
                }
                if(ret != 0 && ret != -1 ) {
                    outputStream.write(buffer, 0, ret)
                    Log.e(TAG, "录制...")
                }
            }
            outputStream.close()
            release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 开始录音
     */
    fun startRecord() {
        destroyThread()
        isRecording = true
        if (thread == null) {
            thread = Thread(this)
        }
        thread!!.start()
    }

    /**
     * 停止录音
     */
    fun stopRecord() {
        isRecording = false
        Log.e(TAG, "停止录音")
    }

    /**
     * 释放资源
     */
    fun release() {
        audioRecord.stop()
        audioRecord.release()
    }

    /**
     * 销毁线程
     */
    private fun destroyThread() {
        if (thread != null && Thread.State.RUNNABLE == thread!!.state) {
            Thread.sleep(500)
            thread!!.interrupt()
        }
    }

    public fun convertWaveFile(){
        val wavHelper = WAVHelper(44100L,1,maxBufferSize,dirPath)
        wavHelper.convertWaveFile()
    }


}