package mobile.indoorbuy.com.audio_video_learn.less4

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Created by BMW on 2018/7/5.
 *
 * 目前只能支持一个audio track和一个video track，而且仅支持mp4输出。即把H264和AAC封装为Mp4格式
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
/**
 * type 参数预留着
 */
class MediaHelper{
    private val path = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei/test.mp4"
    private val outPath = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei/test_out.mp4"
    private val outaudioPath = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei/test_audio_out.aac"
    private val newPath = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei/test_new.mp4"

    private lateinit var mMediaExtractor:MediaExtractor

    private var videoIndex = -1
    private var audioIndex = -1

    /**
     * type占时没用
     */
    private constructor(type:Int){
        try {
            mMediaExtractor = MediaExtractor()
            mMediaExtractor.setDataSource(path)
            //获取通道个数
            val trackCount = mMediaExtractor.trackCount
            //过程跟ffmpeg获取音视频流好像的说
            for(i in 0 until trackCount){
                val trackFormat = mMediaExtractor.getTrackFormat(i)
                val typeMimb = trackFormat.getString(MediaFormat.KEY_MIME)
                if(typeMimb.startsWith("video/")){
                    videoIndex = i
                }
                if (typeMimb.startsWith("audio/")) {
                    audioIndex = i
                }
            }
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    private lateinit var videoExtractor:MediaExtractor
    private lateinit var audioExtractor:MediaExtractor
    private constructor(){
        try {
            videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(outPath)
            var trackCount  = videoExtractor.trackCount
            for(i in 0 until trackCount){
                val trackFormat = videoExtractor.getTrackFormat(i)
                val typeMimb = trackFormat.getString(MediaFormat.KEY_MIME)
                if(typeMimb.startsWith("video/")){
                    videoIndex = i
                    break
                }
            }

            audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(outaudioPath)
            trackCount  = audioExtractor.trackCount
            for(i in 0 until trackCount){
                val trackFormat = audioExtractor.getTrackFormat(i)
                val typeMimb = trackFormat.getString(MediaFormat.KEY_MIME)
                if(typeMimb.startsWith("audio/")){
                    audioIndex = i
                    break
                }
            }



        }catch (e:IOException){
            e.printStackTrace()
        }
    }


    companion object {
        fun extracMedia(){
            MediaHelper(0).extracMedia()
        }

        fun extracAudio(){
            MediaHelper(0).extracAudio()
        }

        fun combineVideo(){
            MediaHelper().combineVideo()
        }
    }


    /**
     * 抽取视频流
     */

    fun extracMedia(){
        if(videoIndex == -1)return
        try {
            // 切换到视频流
            mMediaExtractor.selectTrack(videoIndex)
            val trackFormat = mMediaExtractor.getTrackFormat(videoIndex)
            //初始化视频合成器
            val mediaMuxer = MediaMuxer(outPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            //添加给合成器的通道
            val writeVideo = mediaMuxer.addTrack(trackFormat)

            val allocate = ByteBuffer.allocate(500 * 1024)
            mediaMuxer.start()

            //根据源视频相邻帧之间的时间间隔
            var videoSameTime = 0L
            mMediaExtractor.readSampleData(allocate,0)
            //判断是否是I帧,并跳过第一个I帧
            if(mMediaExtractor.sampleFlags == MediaExtractor.SAMPLE_FLAG_SYNC){
                mMediaExtractor.advance()
            }
            mMediaExtractor.readSampleData(allocate,0)
            val firstTime = mMediaExtractor.sampleTime
            //下一帧
            mMediaExtractor.advance()
            mMediaExtractor.readSampleData(allocate,0)
            val senondTime = mMediaExtractor.sampleTime
            videoSameTime = Math.abs(senondTime - firstTime)

            val bufferInfo = MediaCodec.BufferInfo()
            //回退到第一帧
            mMediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            while (true){
                val readSamSize = mMediaExtractor.readSampleData(allocate,0)
                if (readSamSize < 0){ //读取完毕
                    //释放之前选中的
                    mMediaExtractor.unselectTrack(videoIndex)
                    break
                }
                mMediaExtractor.advance()
                bufferInfo.flags = mMediaExtractor.sampleFlags
                bufferInfo.size = readSamSize
                bufferInfo.offset = 0
                bufferInfo.presentationTimeUs += videoSameTime  //时间戳
                mediaMuxer.writeSampleData(writeVideo,allocate,bufferInfo)
            }

            mediaMuxer.stop()
            mediaMuxer.release()
            mMediaExtractor.release()

        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    /**
     * 抽取音频流
     */
    fun extracAudio(){
        if(audioIndex == -1)return
        try{
            mMediaExtractor.selectTrack(audioIndex)
            val trackFormat = mMediaExtractor.getTrackFormat(audioIndex)
            val mediaMuxer = MediaMuxer(outaudioPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val writeAudio  = mediaMuxer.addTrack(trackFormat)
            mediaMuxer.start()
            val buffer = ByteBuffer.allocate(1024 * 500)
            val bufferInfo = MediaCodec.BufferInfo()

            var sampleTime = 0L
            mMediaExtractor.readSampleData(buffer,0)
            if (mMediaExtractor.sampleFlags == MediaExtractor.SAMPLE_FLAG_SYNC) {
                mMediaExtractor.advance()
            }
            mMediaExtractor.readSampleData(buffer,0)
            val firstTime = mMediaExtractor.sampleTime

            mMediaExtractor.advance()
            val secondeTime = mMediaExtractor.sampleTime
            sampleTime = Math.abs(secondeTime - firstTime)

            //回退到第一帧
            mMediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

            while (true) {
                val  readSize = mMediaExtractor.readSampleData(buffer, 0)
                if (readSize < 0) {
                    mMediaExtractor.unselectTrack(audioIndex)
                    break
                }
                mMediaExtractor.advance()
                bufferInfo.size = readSize
                bufferInfo.flags = mMediaExtractor.sampleFlags
                bufferInfo.offset = 0
                bufferInfo.presentationTimeUs += sampleTime
                mediaMuxer.writeSampleData(writeAudio, buffer, bufferInfo)
            }

            mediaMuxer.stop()
            mediaMuxer.release()
            mMediaExtractor.release()

        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    /**
     * 音视频合成
     */
    fun combineVideo(){
        try {
            videoExtractor.selectTrack(videoIndex)
            audioExtractor.selectTrack(audioIndex)

            val videoFormat = videoExtractor.getTrackFormat(videoIndex)
            val audioFormat = audioExtractor.getTrackFormat(audioIndex)

            val videoBufferInfo = MediaCodec.BufferInfo()
            val audioBufferInfo = MediaCodec.BufferInfo()

            val mediaMuxer = MediaMuxer(newPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat)
            val writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat)
            mediaMuxer.start()

            //获取video 帧间时间
            val byteBuffer = ByteBuffer.allocate(500 * 1024)
            var videoSampleTime = 0L
            videoExtractor.readSampleData(byteBuffer,0)
            if (videoExtractor.sampleFlags == MediaExtractor.SAMPLE_FLAG_SYNC) {
                videoExtractor.advance()
            }
            videoExtractor.readSampleData(byteBuffer, 0)
            val videoTime1 = videoExtractor.sampleTime
            videoExtractor.advance()
            val videoTime2 = videoExtractor.sampleTime
            videoSampleTime = Math.abs(videoTime2 - videoTime1)


            //获取audio 帧间时间
            var audioSampleTime = 0L
            val byteBufferAudio = ByteBuffer.allocate(500 * 1024)
            audioExtractor.readSampleData(byteBufferAudio,0)
            if (audioExtractor.sampleFlags == MediaExtractor.SAMPLE_FLAG_SYNC) {
                audioExtractor.advance()
            }
            audioExtractor.readSampleData(byteBufferAudio, 0)
            val audioTime1 = audioExtractor.sampleTime
            audioExtractor.advance()
            val audioTime2 = audioExtractor.sampleTime
            audioSampleTime = Math.abs(audioTime2 - audioTime1)

            videoExtractor.seekTo(0,MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            audioExtractor.seekTo(0,MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

            while (true){
                val  readSize = videoExtractor.readSampleData(byteBuffer, 0)
                if (readSize < 0) {
                    videoExtractor.unselectTrack(videoIndex)
                    break
                }
                videoBufferInfo.size = readSize
                videoBufferInfo.flags = videoExtractor.sampleFlags
                videoBufferInfo.offset = 0
                videoBufferInfo.presentationTimeUs += videoSampleTime
                mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo)
                videoExtractor.advance()
            }

            while (true){
                val  readSize = audioExtractor.readSampleData(byteBufferAudio, 0)
                if (readSize < 0) {
                    audioExtractor.unselectTrack(audioIndex)
                    break
                }
                audioBufferInfo.size = readSize
                audioBufferInfo.flags = audioExtractor.sampleFlags
                audioBufferInfo.offset = 0
                audioBufferInfo.presentationTimeUs += audioSampleTime
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBufferAudio, audioBufferInfo)
                audioExtractor.advance()
            }

            mediaMuxer.stop()
            mediaMuxer.release()
            videoExtractor.release()
            audioExtractor.release()
        }catch (e:IOException){
            e.printStackTrace()
        }
    }
}