package de.mjksoftware.kugel

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class ConfigActivity : AppCompatActivity() {
    private lateinit var seekbarBrightness: SeekBar
    private lateinit var btnBack: Button
    private lateinit var btnKugelYellow: ImageButton
    private lateinit var btnKugelWhite: ImageButton
    private lateinit var btnKugelBlack: ImageButton
    private lateinit var btnKugelBlue: ImageButton
    private lateinit var btnKugelCyan: ImageButton
    private lateinit var btnKugelGreen: ImageButton
    private lateinit var btnKugelPink: ImageButton
    private lateinit var btnKugelRed: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // we want landscape display orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_config)

        // some phones activated energy saving mode while playing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        btnBack = findViewById(R.id.btnBack)
        seekbarBrightness = findViewById(R.id.seekbarBrightness)
        btnKugelYellow = findViewById(R.id.btnKugelYellow)
        btnKugelWhite = findViewById(R.id.btnKugelWhite)
        btnKugelBlack = findViewById(R.id.btnKugelBlack)
        btnKugelBlue = findViewById(R.id.btnKugelBlue)
        btnKugelCyan = findViewById(R.id.btnKugelCyan)
        btnKugelGreen = findViewById(R.id.btnKugelGreen)
        btnKugelPink = findViewById(R.id.btnKugelPink)
        btnKugelRed = findViewById(R.id.btnKugelRed)

        getScreenBrightness()

        btnBack.setOnClickListener {
            navigateBackToMainActivity()
        }

        btnKugelYellow.setOnClickListener { unScaleAll(); btnKugelYellow.scaleType = ImageView.ScaleType.FIT_XY; enemyResID = R.drawable.kugel_yellow }
        btnKugelWhite.setOnClickListener  { unScaleAll(); btnKugelWhite.scaleType =  ImageView.ScaleType.FIT_XY; enemyResID = R.drawable.kugel_white }
        btnKugelBlack.setOnClickListener  { unScaleAll(); btnKugelBlack.scaleType =  ImageView.ScaleType.FIT_XY; enemyResID = R.drawable.kugel_black }
        btnKugelBlue.setOnClickListener   { unScaleAll(); btnKugelBlue.scaleType =   ImageView.ScaleType.FIT_XY; enemyResID = R.drawable.kugel_blue }
        btnKugelCyan.setOnClickListener   { unScaleAll(); btnKugelCyan.scaleType =   ImageView.ScaleType.FIT_XY; enemyResID = R.drawable.kugel_cyan }
        btnKugelGreen.setOnClickListener  { unScaleAll(); btnKugelGreen.scaleType =  ImageView.ScaleType.FIT_XY; enemyResID = R.drawable.kugel_green }
        btnKugelPink.setOnClickListener   { unScaleAll(); btnKugelPink.scaleType =   ImageView.ScaleType.FIT_XY; enemyResID = R.drawable.kugel_pink }
        btnKugelRed.setOnClickListener    { unScaleAll(); btnKugelRed.scaleType =    ImageView.ScaleType.FIT_XY; enemyResID = R.drawable.kugel_red }

        seekbarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                screenBrightness = i
                applyScreenBrightness()
                }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    override fun onBackPressed() {
        super.onBackPressed()

        startNewGame = true
        navigateBackToMainActivity()
    }


    private fun navigateBackToMainActivity() {
        val i = Intent(this@ConfigActivity, MainActivity::class.java)
        // editor for preferences (enemy kugel drawable resource id)
        val prefs = getSharedPreferences("de.mjksoftware.Kugel", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.apply {
            putInt("enemy_res_id", enemyResID)
            putInt("screen_brightness", screenBrightness)
        }.apply()
        finish()
        startActivity(i)
    }


    private fun unScaleAll() {
        btnKugelYellow.scaleType = ImageView.ScaleType.CENTER
        btnKugelWhite.scaleType =  ImageView.ScaleType.CENTER
        btnKugelBlack.scaleType =  ImageView.ScaleType.CENTER
        btnKugelBlue.scaleType =   ImageView.ScaleType.CENTER
        btnKugelCyan.scaleType =   ImageView.ScaleType.CENTER
        btnKugelGreen.scaleType =  ImageView.ScaleType.CENTER
        btnKugelPink.scaleType =   ImageView.ScaleType.CENTER
        btnKugelRed.scaleType =    ImageView.ScaleType.CENTER
    }


    private fun applyScreenBrightness() {
        val lp: WindowManager.LayoutParams = window.attributes
        lp.screenBrightness = screenBrightness.toFloat() / 100
        window.attributes = lp
    }


    private fun getScreenBrightness() {
        val prefs = getSharedPreferences("de.mjksoftware.Kugel", Context.MODE_PRIVATE)
        screenBrightness = prefs.getInt("screen_brightness", 50)
        seekbarBrightness.progress = screenBrightness
        applyScreenBrightness()
    }
}