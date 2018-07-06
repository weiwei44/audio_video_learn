package mobile.indoorbuy.com.audio_video_learn.less7

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_aac_codec.*
import mobile.indoorbuy.com.audio_video_learn.R
import mobile.indoorbuy.com.audio_video_learn.less2.AudioRecordHelper

/**
 * Created by BMW on 2018/6/27.
 */
class AACCodecActivity : AppCompatActivity() {

    private lateinit var recordHelper: AudioAACHelper

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aac_codec)

        recordHelper = AudioAACHelper()
        requestPermission()
        recordHelper.checkMediaDecoder()
        btn_star.setOnClickListener {
            recordHelper.startRecord()
        }

        btn_stop.setOnClickListener {
            recordHelper.stopRecord()
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