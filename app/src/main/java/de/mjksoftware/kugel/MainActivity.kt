package de.mjksoftware.kugel


import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.system.exitProcess


// get stored score
var oldHighscore: Long = 0
// will store the pixel relations
var dp: Float = 50f

// the view for the game field for drawing and touch events while the game's running
private lateinit var gameFieldView: GameFieldView

// game objects
lateinit var player: GameObject
// simple little green ball to catch
lateinit var bonus: GameObject
// list of enemies, so we can add and remove red balls
var enemies: MutableList<GameObject> = mutableListOf(
        GameObject(100, 100, 25, 25,
                Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_8888),
                Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_8888))
)

// when the title image was clicked it will be movable
//private var titleKugelIsMovable: Boolean = false

// amount of seconds have to left for next level
var timePerRound: Long = 20
// already played time
var timePlaying: Long = 0
// current game level
var gameLevel: Int = 1

// indicates if we're playing
var gameIsRunning: Boolean = false
var gameOver: Boolean = false
// true to request exit game
var exitGame: Boolean = false
// for restarting the game. Not first start!
var startNewGame: Boolean = false
//screen brightness will be loaded
var screenBrightness: Int = 80

// labels to be translated
lateinit var lblTimePlaying: String
lateinit var lblScore:String
lateinit var lblRemainingLifes: String
lateinit var lblLevel: String
lateinit var lblMessageInfo: String
lateinit var lblTapToPause: String
lateinit var lblYouHave: String
lateinit var lblPoints: String
lateinit var lblYouCanDoBetter: String
lateinit var lblYourHighestScoreWas: String
lateinit var lblCongratulations: String
lateinit var lblNextLevelIn: String

// screen dimensions
var screenWidth: Int = 1
var screenHeight: Int = 1

// enemy color
var enemyResID: Int = 0

// all the bitmaps
lateinit var bmpBackground: Bitmap
lateinit var bmpGameOver: Bitmap
lateinit var bmpBallGreen: Bitmap
lateinit var bmpBallRed: Bitmap
lateinit var bmpBallShadow: Bitmap
lateinit var bmpIcoPowerOff: Bitmap
lateinit var bmpIcoPowerOffSmall: Bitmap
lateinit var bmpIcoRestart: Bitmap
lateinit var bmpIcoPause: Bitmap

// paint settings
var paintFg: Paint = Paint()
var paintBg: Paint = Paint()

// indicates if (x,y) is inside the rectangle (x1,y1)-(x2,y2)
fun isWithin(x: Int, y: Int, x1: Int, y1: Int, x2: Int, y2: Int): Boolean
    = ((x > x1) && (x < x2) && (y > y1) && (y < y2))


class MainActivity: AppCompatActivity(), SensorEventListener {
    // sensor stuff
    private lateinit var mSensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor

    // layout stuff
    private lateinit var btnStart: Button
    private lateinit var btnQuit: Button
    private lateinit var btnInfo: Button
    private lateinit var imgTitleKugel: ImageView
    private lateinit var btnSettings: ImageButton
    private var timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // sensors
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // we want landscape display orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // set the content to our activity_main
        setContentView(R.layout.activity_main)

        // some phones activated energy saving mode while playing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        applyScreenBrightness()
        
        // get display metrics to calculate dpi-independent dps and screen dimensions
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        dp = displayMetrics.density

        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        //configure paint and custom font
        //custom font makes problems. not important now.
        //val customTypeface: Typeface = Typeface.createFromAsset(assets, "luckiest_guy.ttf")
        paintFg.apply {
            color = Color.rgb(128, 255, 0)
            style = Paint.Style.STROKE
            textSize = 18 * dp
            //typeface = customTypeface
            strokeWidth = 1f*dp
            isAntiAlias = true
            isDither = true
            setShadowLayer(5 * dp, 3 * dp, 3 * dp, Color.rgb(0, 0, 0))
        }
        paintBg.apply {
            color = Color.rgb(0, 0, 0)
            style = Paint.Style.FILL_AND_STROKE
        }

        // load images from resources
        loadEnemyKugelColor()
        bmpBallRed = BitmapFactory.decodeResource(resources, enemyResID)
        bmpBallGreen = BitmapFactory.decodeResource(resources, R.drawable.kugel_green)
        bmpBallShadow = BitmapFactory.decodeResource(resources, R.drawable.kugel_shadow)
        bmpBackground = BitmapFactory.decodeResource(resources, R.drawable.bkstonehwarning)
        bmpGameOver = BitmapFactory.decodeResource(resources, R.drawable.bkgameover)
        bmpIcoPowerOff = BitmapFactory.decodeResource(resources, R.drawable.icopoweroff)
        bmpIcoRestart = BitmapFactory.decodeResource(resources, R.drawable.icorestart)
        bmpIcoPause = BitmapFactory.decodeResource(resources, R.drawable.icopause)

        // resize icons
        bmpIcoPowerOffSmall = Bitmap.createScaledBitmap(
                bmpIcoPowerOff,
                (40 * dp).toInt(),
                (40 * dp).toInt(),
                true
        )
        bmpIcoPowerOff = Bitmap.createScaledBitmap(
                bmpIcoPowerOff,
                (150 * dp).toInt(),
                (150 * dp).toInt(),
                true
        )
        bmpIcoRestart = Bitmap.createScaledBitmap(
                bmpIcoRestart,
                (150 * dp).toInt(),
                (150 * dp).toInt(),
                true
        )
        bmpIcoPause = Bitmap.createScaledBitmap(
                bmpIcoPause,
                (140 * dp).toInt(),
                (140 * dp).toInt(),
                true
        )

        player = GameObject(
                screenWidth / 2, screenHeight / 2,
                (35 * dp).toInt(), (35 * dp).toInt(),
                bmpBallGreen, bmpBallShadow
        )

        bonus = GameObject(0, 0, (15 * dp).toInt(), (15 * dp).toInt(), bmpBallGreen, bmpBallShadow)
        setRandomPosition(bonus)
        bonus.speed = 4f

        // get translation strings from resources
        lblTimePlaying = getString(R.string.timePlaying)
        lblScore = getString(R.string.playerScore)
        lblRemainingLifes = getString(R.string.remainingLifes)
        lblLevel = getString(R.string.level)
        lblMessageInfo = getString(R.string.messageInfo)
        lblPoints = getString(R.string.points)
        lblYouHave = getString(R.string.youHave_POINTS)
        lblTapToPause = getString(R.string.lblTapToPause)
        lblYouCanDoBetter = getString(R.string.lblYouCanDoBetter)
        lblYourHighestScoreWas = getString(R.string.lblBestHighscore)
        lblCongratulations = getString(R.string.congratulations)
        lblNextLevelIn = getString(R.string.lblNextLevel)

        // set up instances for ui items
        btnStart = findViewById(R.id.btnStart)
        btnQuit = findViewById(R.id.btnQuit)
        btnInfo = findViewById(R.id.btnInfo)
        btnSettings = findViewById(R.id.btnSettings)
        imgTitleKugel = findViewById(R.id.imgTitleKugel)

        // set game field view
        gameFieldView = GameFieldView(this)

        // set OnClickListeners for the buttons
        btnQuit.setOnClickListener { leaveApp() }
        btnStart.setOnClickListener { startGame() }
        btnInfo.setOnClickListener { showInfo() }
        btnSettings.setOnClickListener {
            finish()
            val i = Intent(this@MainActivity, ConfigActivity::class.java)
            startActivity(i)
        }

        // stuff to do with the title screen - does not work yet
        //imgTitleKugel.setOnClickListener { titleKugelIsMovable = !titleKugelIsMovable }


        ////////////////////////////////////////////////////////////////////////////////////////////
        // TIMER
        ////////////////////////////////////////////////////////////////////////////////////////////
        // timer counting seconds and check game states
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (exitGame) leaveApp()
                if (startNewGame) startGame()
                if (gameOver) {
                    gameFieldView.invalidate()
                }
                if (gameIsRunning) timePlaying++
                if (timePlaying > timePerRound) {
                    // time for a new level!
                    // add a new enemy
                    if (gameLevel > 23) enemies.clear()
                    val newEnemy = GameObject(1, 1, (20 * dp).toInt(), (20 * dp).toInt(), bmpBallRed, bmpBallShadow)
                    setRandomPositionOnLeft(newEnemy)
                    enemies.add(newEnemy)
                    // increase game level
                    gameLevel++
                    if (gameLevel < 7) enemies.last().speed += gameLevel
                    // avoid first enemy from staying inside bonus when placed unlucky
                    if (enemies.first() == enemies.last()) enemies.last().dX = -bonus.dX
                    // reset game time for the next level
                    timePlaying = 0
                }
            }
        }, 1000, 2000)
    }


    override fun onBackPressed() {
        super.onBackPressed()

        leaveApp()
    }


    override fun onResume() {
        super.onResume()

        loadState()
        mSensorManager.registerListener(
                this, sensorAccelerometer,
                SensorManager.SENSOR_DELAY_GAME
        )
    }


    override fun onPause() {
        super.onPause()

        saveState()
        gameIsRunning = false
        mSensorManager.unregisterListener(this)
    }


    private fun leaveApp() {
        writeHighscore()
        finish()
        exitProcess(0)
        // I really tried all - the app still remains in the background :-(
    }


    private fun showInfo() {
        val msg = AlertDialog.Builder(this)
        with(msg) {
            setIcon(R.drawable.kugel_green)
            setNeutralButton("okay", null)
            setTitle(getString(R.string.lblGameTitle))
            setMessage(getString(R.string.infoMessage))
            show()
        }
    }


    private fun startGame() {
        //gameFieldView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        if (!startNewGame) setContentView(gameFieldView)
        else enemies.clear()  // kill all enemies

        writeHighscore()
        startNewGame = false
        gameOver = false
        gameIsRunning = true
        timePlaying = 0
        gameLevel = 1
        player.lifes = 3
        player.score = 0
    }


    override fun onSensorChanged(event: SensorEvent) {
        // don't do anything when the game is not running
        if (gameIsRunning) {
            val x: Int = (event.values[1] * dp).toInt()
            val y: Int = (event.values[0] * dp).toInt()

            player.move(x, y)
            bonus.move()

            if (collision(player, bonus)) {
                player.addScore(100)
                setRandomPosition(bonus)
            }

            for (i in enemies.indices) enemies.elementAt(i).move()

            if (enemies.isNotEmpty())
                for (i in enemies.indices) {
                    if (collision(player, enemies.elementAt(i))) {
                        player.lifes--
                        enemies.removeAt(i)
                        if (player.lifes == 0) {
                            gameIsRunning = false
                            gameOver = true
                        }
                        return
                    }
                }
            // game field changed -> has to be redrawn
            gameFieldView.invalidate()
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}


    private fun saveState() {
        val prefs = getSharedPreferences("de.mjksoftware.Kugel", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.apply {
            putLong("score", player.score)
            putInt("level", gameLevel)
            putInt("lifes", player.lifes)
            putInt("enemy_res_id", enemyResID)
            putInt("screen_brightness", screenBrightness)
            putString("player_name", player.name)
        }.apply()
    }


    private fun loadState() {
        val prefs = getSharedPreferences("de.mjksoftware.Kugel", Context.MODE_PRIVATE)
        prefs.apply {
            getLong("score", player.score)
            getInt("level", gameLevel)
            getInt("lifes", player.lifes)
            getInt("enemy_res_id", enemyResID)
            getInt("screen_brightness", screenBrightness)
            getString("player_name", player.name)
        }
        applyScreenBrightness()
    }


    private fun writeHighscore() {
        val prefs = getSharedPreferences("de.mjksoftware.Kugel", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        oldHighscore = prefs.getLong("highscore", 0)
        if(player.score > oldHighscore) {
            editor.putLong("highscore", player.score)
            // use commit() instead of apply() to write the data immediately! My mom just lost her score!!
            editor.commit()
            oldHighscore = player.score
        }
    }


    private fun loadEnemyKugelColor() {
        // editor for preferences (highscore)
        val prefs = getSharedPreferences("de.mjksoftware.Kugel", Context.MODE_PRIVATE)
        enemyResID = prefs.getInt("enemy_res_id", R.drawable.kugel_red)
    }


    private fun applyScreenBrightness() {
        val prefs = getSharedPreferences("de.mjksoftware.Kugel", Context.MODE_PRIVATE)
        screenBrightness = prefs.getInt("screen_brightness", 50)

        val lp: WindowManager.LayoutParams = window.attributes
        lp.screenBrightness = screenBrightness.toFloat() / 100
        window.attributes = lp
    }
}
