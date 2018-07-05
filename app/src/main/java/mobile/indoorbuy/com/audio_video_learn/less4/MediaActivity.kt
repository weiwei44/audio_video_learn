package mobile.indoorbuy.com.audio_video_learn.less4

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_media.*
import mobile.indoorbuy.com.audio_video_learn.R

/**
 * Created by BMW on 2018/6/27.
 */
class MediaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        btn_video_stream.setOnClickListener {
            MediaHelper.extracMedia()
        }

        btn_audio_stream.setOnClickListener{
            MediaHelper.extracAudio()
        }

        btn_com.setOnClickListener{
            MediaHelper.combineVideo()
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
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .subscribe{
                    when{
                        it.granted -> {
                            // 用户已经同意该权限
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