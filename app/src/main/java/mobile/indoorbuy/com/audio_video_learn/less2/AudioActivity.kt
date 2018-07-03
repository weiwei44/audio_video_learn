package mobile.indoorbuy.com.audio_video_learn.less2

import android.Manifest
import android.content.ContentValues

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_audio.*
import mobile.indoorbuy.com.audio_video_learn.R

/**
 * Created by BMW on 2018/6/27.
 */
class AudioActivity : AppCompatActivity() {


    private lateinit var recordHelper: AudioRecordHelper

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        recordHelper = AudioRecordHelper()
        requestPermission()

        btn_star.setOnClickListener{
            recordHelper.startRecord()
        }

        btn_stop.setOnClickListener {
            recordHelper.stopRecord()
        }

        btn_play.setOnClickListener {
            AudioPlayHelper().playPCM()
        }

        btn_wav.setOnClickListener {
            recordHelper.convertWaveFile()
        }

        btn_wav_play.setOnClickListener {
            AudioPlayHelper().playWav()
        }
    }

    /**
     * 权限获取
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun requestPermission(){
        RxPermissions(this)
                .requestEach(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                        )
                .subscribe{
                    when{
                        it.granted -> {
                            // 用户已经同意该权限
                            recordHelper.initRecord()
                        }
                        it.shouldShowRequestPermissionRationale -> {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                        }
                        else -> {
                            // 用户拒绝了该权限，并且选中『不再询问』
                        }
                    }
                }
    }

}