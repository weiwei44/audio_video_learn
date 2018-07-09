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
 * 实时录音，保存aac
 */
class AudioAACHelper : Runnable {
    private val dirPath = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei"
    private lateinit var file: File
    private var isRecording = false
    private var maxBufferSize = 0
    private lateinit var audioRecord: AudioRecord
    private var thread: Thread? = null

    private lateinit var mBufferInfo: MediaCodec.BufferInfo

    fun initRecord(){
        maxBufferSize = AudioRecord.getMinBufferSize(sampleSize,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,  //手机麦克风输入的音频
                sampleSize,         //采样率
                AudioFormat.CHANNEL_IN_STEREO,   //双通道
                AudioFormat.ENCODING_PCM_16BIT, //16bit数据位宽，就是一个short
                maxBufferSize                  //缓冲区大小,必须为一帧的2到N倍大小
        )

        file = File(dirPath).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * 手机支持的mine
    "video/x-vnd.on2.vp8" - VP8 video (i.e. video in .webm)
    "video/x-vnd.on2.vp9" - VP9 video (i.e. video in .webm)
    "video/avc" - H.264/AVC video
    "video/hevc" - H.265/HEVC video
    "video/mp4v-es" - MPEG4 video
    "video/3gpp" - H.263 video
    "audio/3gpp" - AMR narrowband audio
    "audio/amr-wb" - AMR wideband audio
    "audio/mpeg" - MPEG1/2 audio layer III
    "audio/mp4a-latm" - AAC audio (note, this is raw AAC packets, not packaged in LATM!)
    "audio/vorbis" - vorbis audio
    "audio/g711-alaw" - G.711 alaw audio
    "audio/g711-mlaw" - G.711 ulaw audio
     */
    //private val mime = "audio/mp4a-latm"
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val mime = MediaFormat.MIMETYPE_AUDIO_AAC
    private var mMediaCodec: MediaCodec? = null
    private lateinit var inputBufferArray: Array<ByteBuffer>
    private lateinit var outputBufferArray: Array<ByteBuffer>
    private lateinit var outputStream: FileOutputStream

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun run() {
        val audioFile = File(dirPath, "luyin.aac")
                .apply {
                    if (exists()) delete()
                }
        audioFile.createNewFile()

        initEcodec()

        try {
            outputStream = FileOutputStream(audioFile)

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
                    //outputStream.write(buffer, 0, ret)
                    encodeData(buffer)
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
        val wavHelper = WAVHelper(sampleSize.toLong(),1,maxBufferSize,dirPath)
        wavHelper.convertWaveFile()
    }


    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    val kSampleRates = intArrayOf(8000, 11025, 22050, 44100, 48000)
    //比特率 声音中的比特率是指将模拟声音信号转换成数字声音信号后，单位时间内的二进制数据量，是间接衡量音频质量的一个指标
    val kBitRates = intArrayOf(64000, 96000, 128000)

    private val sampleSize = kSampleRates[3]
    private val bitRate = kBitRates[1]


    /**
     * 初始化编码器
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initEcodec(){
        try {
            mMediaCodec = MediaCodec.createEncoderByType(mime)
            val mediaFormat = MediaFormat.createAudioFormat(mime,sampleSize,2)  //参数对应-> mime type、采样率、声道数

            mediaFormat.setString(MediaFormat.KEY_MIME,mime)
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC)  //低复杂度规格, MPEG-4 AAC LC
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,1024 * 1024)  //作用于inputBuffer的大小
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,bitRate)  //比特率

            /*
              第四个参数 编码的时候是MediaCodec.CONFIGURE_FLAG_ENCODE
                         解码的时候是0
            */
            mMediaCodec!!.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            //start（）后进入执行状态，才能做后续的操作
            mMediaCodec!!.start()

            inputBufferArray = mMediaCodec!!.inputBuffers
            outputBufferArray = mMediaCodec!!.outputBuffers

            mBufferInfo = MediaCodec.BufferInfo()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    //pts时间基数
    var presentationTimeUs: Long = 0

    /**
     * 编码
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun encodeData(data:ByteArray){
        //dequeueInputBuffer（time）需要传入一个时间值，
        // -1表示一直等待，0表示不等待有可能会丢帧，其他表示等待多少毫秒
        val inputIndex = mMediaCodec!!.dequeueInputBuffer(-1)
        if(inputIndex >= 0){
            val inputByteBuf = inputBufferArray[inputIndex]
            inputByteBuf.clear()
            inputByteBuf.put(data) // 添加数据
            inputByteBuf.limit(data.size) //限制ByteBuffer的访问长度

            //计算pts,实际上这个pts对应音频来说作用并不大，设置成0也是没有问题的
            val pts = computePresentationTime(presentationTimeUs)
            //通知编码器 编码
            mMediaCodec!!.queueInputBuffer(inputIndex, 0, data.size, pts, 0)
            presentationTimeUs += 1
        }

        //同解码器
        var outputIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo,0)
        while (outputIndex >= 0 ){
            //获取缓存信息的长度
            val byteBufSize = mBufferInfo.size

            val outPutBuf = outputBufferArray[outputIndex]
            outPutBuf.position(mBufferInfo.offset)
            outPutBuf.limit(mBufferInfo.offset + mBufferInfo.size)

            //添加ADTS头部后的长度
            val bytePacketSize = byteBufSize + 7
            val targetByte = ByteArray(bytePacketSize)
            //添加ADTS头部
            addADTStoPacket(targetByte,bytePacketSize)

            /*
            get（byte[] dst,int offset,int length）:ByteBuffer从position位置开始读，
            读取length个byte，并写入dst下标从offset到offset + length的区域
             */
            outPutBuf.get(targetByte,7,byteBufSize) //将编码得到的AAC数据 取出到byte[]中 偏移量offset=7
            outPutBuf.position(mBufferInfo.offset)

            try {
                outputStream.write(targetByte)
            }catch (e:IOException){
                e.printStackTrace()
            }

            //释放
            mMediaCodec!!.releaseOutputBuffer(outputIndex,false)
            outputIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo,10000)
        }
    }

    /**
     * 给编码出的aac裸流添加adts头字段
     *ADTS可以在任意帧解码，也就是说它每一帧都有头信息
     * @param packet    要空出前7个字节，否则会搞乱数据
     * @param packetLen
     */
    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        val profile = 2  //AAC LC
        val freqIdx = 4  //44.1KHz
        val chanCfg = 2  //CPE
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

    /**
     * 查看手机支持的硬编解码器
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun checkMediaDecoder() {
        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfos = mediaCodecList.codecInfos
        for (codecInfo in codecInfos) {
            Log.e("TAG", "codecInfo =" + codecInfo.getName())
        }
    }

    //计算PTS，
    private fun computePresentationTime(frameIndex: Long): Long {
        return frameIndex * 90000 * 1024 / 44100
    }

}