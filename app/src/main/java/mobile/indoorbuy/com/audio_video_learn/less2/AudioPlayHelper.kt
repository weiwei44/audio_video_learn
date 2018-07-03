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
class AudioPlayHelper : Runnable{


    private val dirPath = "${Environment.getExternalStorageDirectory().absolutePath}/weiwei"
    private lateinit var file: File
    private var isRecording = false
    private var maxBufferSize = 0
    private var audioPlay: AudioTrack?
    private var thread: Thread? = null
    private var disWAV:DataInputStream? = null

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

    /**
     * 销毁线程
     */
    private fun destroyThread() {
        if (thread != null && Thread.State.RUNNABLE == thread!!.state) {
            Thread.sleep(500)
            thread!!.interrupt()
        }
    }

    fun playWav(){
        val audioFile = File(dirPath, "w.wav")
        destroyThread()
        try {
            disWAV = DataInputStream(FileInputStream(audioFile))
            val wavHelper = WAVHelper()
            wavHelper.readWavHeader(disWAV)
            if (thread == null) {
                thread = Thread(this)
            }
            thread!!.start()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun playPCM(){
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

    private fun readData(buffer:ByteArray,offset:Int,count:Int):Int{
        try {
            val nbytes = disWAV!!.read(buffer,offset,count)
            if(nbytes == -1){
                return 0
            }
            return nbytes
        }catch (e:Exception){
            e.printStackTrace()
        }
        return -1
    }

    override fun run() {
        val buffer = ByteArray(1024 * 2)
        while (readData(buffer,0,buffer.size) > 0){
            if(audioPlay!!.write(buffer,0,buffer.size) != buffer.size){

            }
            audioPlay!!.play()
        }
        audioPlay!!.stop()
        audioPlay!!.release()
        try {
            if(disWAV != null){
                disWAV!!.close()
                disWAV = null
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}