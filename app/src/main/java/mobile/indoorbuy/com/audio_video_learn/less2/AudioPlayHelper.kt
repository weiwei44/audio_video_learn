package mobile.indoorbuy.com.audio_video_learn.less2

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Environment
import java.io.*

/**
 * Created by BMW on 2018/7/2.
 */
class AudioPlayHelper{
    private val dirPath = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei"
    private lateinit var file: File
    private var isRecording = false
    private var maxBufferSize = 0
    private var audioPlay: AudioTrack?
    private var thread: Thread? = null
    init {
        maxBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioPlay = AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxBufferSize,
                AudioTrack.MODE_STREAM
                )
    }

    fun play(){
        val audioFile = File(dirPath, "w.pcm")
        var dis: DataInputStream? = null
        try {
            //从音频文件中读取声音
            dis = DataInputStream(BufferedInputStream(FileInputStream(audioFile)))

            val buffer = ByteArray(maxBufferSize)
            audioPlay!!.play()
            while (true){
                var i = 0
                try{
                    while (dis.available() > 0 && i < buffer.size){
                        buffer[i] = dis.readByte()
                        i++
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
                audioPlay!!.write(buffer,0,buffer.size)
                //读完了
                if(i != maxBufferSize){
                    audioPlay!!.stop()
                    audioPlay!!.release()
                    break
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}