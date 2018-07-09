package mobile.indoorbuy.com.audio_video_learn.less7

import android.content.ContentValues.TAG
import android.media.*
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.util.Log
import mobile.indoorbuy.com.audio_video_learn.less2.WAVHelper
import java.io.*
import java.nio.ByteBuffer
import android.media.MediaCodec


/**
 * Created by BMW on 2018/7/2.
 * 实时录音，保存aac，最初版本，有点小问题
 */
class AudioPCMHelper : Runnable {
    private val filePath = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei/luyin.aac"
    private var isPlaying = false
    private var maxBufferSize = 0
    private lateinit var audioPlay: AudioTrack
    private lateinit var extractor: MediaExtractor
    private var thread: Thread? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val mime = MediaFormat.MIMETYPE_AUDIO_AAC
    private var mMediaCodec: MediaCodec? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun run() {
        initPlay()
        initDecodec()

        while (isPlaying){
            dencodeData()
        }

        release()

    }


    /**
     * 开始播放
     */
    fun startPlay() {
        destroyThread()
        isPlaying = true
        if (thread == null) {
            thread = Thread(this)
        }
        thread!!.start()
    }

    /**
     * 停止播放
     */
    fun stopPlay() {
        isPlaying = false
        Log.e(TAG, "停止播放")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun release() {
        if(mMediaCodec != null){
            mMediaCodec!!.stop()
            mMediaCodec!!.release()
        }

        audioPlay.stop()
        audioPlay.release()

        extractor.release()
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


    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    val kSampleRates = intArrayOf(8000, 11025, 22050, 44100, 48000)
    //比特率 声音中的比特率是指将模拟声音信号转换成数字声音信号后，单位时间内的二进制数据量，是间接衡量音频质量的一个指标
    val kBitRates = intArrayOf(64000, 96000, 128000)

    private val sampleSize = kSampleRates[3]
    private val bitRate = kBitRates[1]

    /**
     * 初始化播放器
     */
    private fun initPlay() {
        maxBufferSize = AudioRecord.getMinBufferSize(sampleSize,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT)

        audioPlay = AudioTrack(
                AudioManager.STREAM_MUSIC,  //手机麦克风输入的音频
                sampleSize,         //采样率
                AudioFormat.CHANNEL_OUT_STEREO,   //双通道
                AudioFormat.ENCODING_PCM_16BIT, //16bit数据位宽，就是一个short
                maxBufferSize,                  //缓冲区大小,必须为一帧的2到N倍大小
                AudioTrack.MODE_STREAM
        )

        audioPlay.play()
    }


    private lateinit var inputBufferArray: Array<out ByteBuffer>
    private lateinit var outputBufferArray: Array<out ByteBuffer>
    private lateinit var mBufferInfo: MediaCodec.BufferInfo

    /**
     * 初始化解码器
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initDecodec() {
        try {
            mMediaCodec = MediaCodec.createDecoderByType(mime)

            extractor = MediaExtractor()
            extractor.setDataSource(filePath)

            var mediaFormat = MediaFormat.createAudioFormat(mime, sampleSize, 2)

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val filemime = format.getString(MediaFormat.KEY_MIME)
                if (filemime.startsWith("audio/")) {
                    extractor.selectTrack(i)
                    mediaFormat = format
                    Log.e("weiwei","MediaFormat.KEY_MIME = ${mediaFormat.getString(MediaFormat.KEY_MIME)}")
                    Log.e("weiwei","MediaFormat.KEY_SAMPLE_RATE = ${mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)}")
                    Log.e("weiwei","MediaFormat.KEY_CHANNEL_COUNT = ${mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)}")
                }
            }

            mMediaCodec!!.configure(mediaFormat, null, null, 0)
            //start（）后进入执行状态，才能做后续的操作
            mMediaCodec!!.start()

            inputBufferArray = mMediaCodec!!.inputBuffers
            outputBufferArray = mMediaCodec!!.outputBuffers

            mBufferInfo = MediaCodec.BufferInfo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 解码
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun dencodeData() {


        var sawInputEOS = false
        var sawOutputEOS = false
        var totalRawSize = 0

        try {
            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    val inputIndex = mMediaCodec!!.dequeueInputBuffer(-1)
                    if (inputIndex >= 0) {
                        val inputByteBuf = inputBufferArray[inputIndex]
                        val sampleSize = extractor.readSampleData(inputByteBuf, 0)
                        if (sampleSize < 0) {
                            Log.e("weiwei", "saw input eos")
                            sawInputEOS = true
                            mMediaCodec!!.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        } else {
                            //pts时间基数
                            val presentationTimeUs: Long = extractor.sampleTime
                            mMediaCodec!!.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo, 0)
                if (outputIndex >= 0) {
                    if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        mMediaCodec!!.releaseOutputBuffer(outputIndex, false)
                        continue
                    }

                    if (mBufferInfo.size != 0) {
                        val outputBuffer = outputBufferArray[outputIndex]
                        outputBuffer.position(mBufferInfo.offset)
                        outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size)

                        //用来保存解码后的数据
                        val outData = ByteArray(mBufferInfo.size)
                        outputBuffer.get(outData)
                        totalRawSize += outData.size
                        //清空缓存
                        outputBuffer.clear()

                        //播放解码后的数据
                        audioPlay.write(outData, 0, outData.size)

                    }

                    //释放已经解码的buffer
                    mMediaCodec!!.releaseOutputBuffer(outputIndex, false)

                    if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true
                    }
                    //解码未解完的数据
                    //outputIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo,0)
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBufferArray = mMediaCodec!!.outputBuffers
                    Log.e("weiwei", "output buffers have changed")
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val oformat = mMediaCodec!!.outputFormat
                    Log.e("weiwei", "output format has changed to $oformat")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            extractor.release()
        }

    }
}
