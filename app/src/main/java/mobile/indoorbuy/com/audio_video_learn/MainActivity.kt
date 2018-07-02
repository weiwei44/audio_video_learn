package mobile.indoorbuy.com.audio_video_learn

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import mobile.indoorbuy.com.audio_video_learn.less1.ImageActivity
import mobile.indoorbuy.com.audio_video_learn.less2.AudioActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sample_text.text = stringFromJNI()

        learn1.setOnClickListener {
            startActivity(Intent(this,ImageActivity::class.java))
        }

        learn2.setOnClickListener {
            startActivity(Intent(this,AudioActivity::class.java))
        }
    }

    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
