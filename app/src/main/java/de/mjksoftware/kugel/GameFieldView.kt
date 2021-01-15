package de.mjksoftware.kugel


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View


class GameFieldView(context: Context) : View(context) {

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        screenWidth = width
        screenHeight = height

        bmpBackground = Bitmap.createScaledBitmap(bmpBackground, width, height, true)
        bmpGameOver = Bitmap.createScaledBitmap(bmpGameOver, width, height, true)
    }


    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_UP -> {
                if (gameOver) {
                    if (isWithin(e.x.toInt(), e.y.toInt(), 0, (screenHeight / 2), screenWidth / 2, screenHeight))
                        startNewGame = true
                    if (isWithin(e.x.toInt(), e.y.toInt(), screenWidth / 2, (screenHeight / 2), screenWidth ,screenHeight))
                        exitGame = true
                }
                if (isWithin(e.x.toInt(), e.y.toInt(), (50*dp).toInt(), (50*dp).toInt(),(screenWidth - 50*dp).toInt(), (screenHeight - 50*dp).toInt()))
                    gameIsRunning = !gameIsRunning
                if ((!gameIsRunning) && (isWithin(e.x.toInt(), e.y.toInt() , (screenWidth - 65*dp).toInt(), (screenHeight - 65*dp).toInt(), screenWidth, screenHeight)))
                    exitGame = true
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            //game over screen
            if (gameOver) {
                drawBitmap(bmpGameOver, 0f, 0f, paintFg)
                drawText("$lblYouHave ${player.score} $lblPoints!", screenWidth / 2 - 120*dp, screenHeight  / 2 - 30*dp, paintFg)
                drawText("$lblYourHighestScoreWas $oldHighscore.", screenWidth / 2 - 120*dp, (screenHeight  / 2).toFloat(), paintFg)
                if (player.score > oldHighscore)
                    drawText(lblCongratulations, screenWidth / 2 - 120*dp, (screenHeight  / 2 + 30*dp).toFloat(), paintFg)
                else
                    drawText(lblYouCanDoBetter, screenWidth / 2 - 120*dp, (screenHeight  / 2 + 30*dp).toFloat(), paintFg)
            }
            // game field
            else {
                drawBitmap(bmpBackground, 0f, 0f, paintFg)

                bonus.draw(canvas)
                enemies.forEach { i -> i.draw(canvas) }
                player.draw(canvas)

                drawText("$lblLevel: $gameLevel   |   $lblRemainingLifes: ${player.lifes}   |   $lblScore: ${player.score}   |   $lblTapToPause", 20*dp, 25*dp, paintFg)
                drawRoundRect(5*dp, 5*dp, screenWidth - 5*dp, 39*dp, 5*dp, 5*dp, paintFg)
                if (!gameIsRunning) {
                    drawBitmap(bmpIcoPause, screenWidth / 2 - 70*dp, screenHeight / 2 - 70*dp, paintFg)
                    drawBitmap(bmpIcoPowerOffSmall, screenWidth - 60*dp, screenHeight - 60*dp, paintFg)
                }
                if (timePlaying > timePerRound - 4)
                    drawText("$lblNextLevelIn ${timePerRound - timePlaying + 1}...", screenWidth / 2 - 50*dp, screenHeight - 20*dp, paintFg)
            }
        }
    }
}
