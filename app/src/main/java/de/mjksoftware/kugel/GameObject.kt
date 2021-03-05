package de.mjksoftware.kugel


import android.graphics.Bitmap
import android.graphics.Canvas


class GameObject(l: Int, t: Int, w: Int, h: Int, bitmap: Bitmap, shadow: Bitmap) {
    var left: Int = l
    var top: Int = t
    var width: Int = w
    var height: Int = h
    var score: Long = 0
    var lifes: Int = 3
    var speed: Float = 2f
    var dX: Int = -1
    var dY: Int = 1
    var name: String = "name"

    private var bitmapScaled: Bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    private var shadowScaled: Bitmap = Bitmap.createScaledBitmap(shadow, width, height, true)

    fun draw(canvas: Canvas) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) canvas.drawBitmap(shadowScaled, left + 5*dp, top + 5*dp, paintFg)
        canvas.drawBitmap(bitmapScaled, left.toFloat(), top.toFloat(), paintFg)
    }


    fun addScore(s: Int) {
        score += s
        if (score < 0) score = 0
    }


    fun move(l: Int, t: Int) {
        left += (l * speed).toInt()
        top += (t * speed).toInt()
        if (left < 0) left = 0
        if (top < 40*dp) top = (40*dp).toInt()
        if (left + width  > screenWidth) left = screenWidth - width
        if (top + height  > screenHeight) top = screenHeight - height
    }


    fun move() {
        if ((left < 0) || (left + width  > screenWidth)) dX = -dX
        if ((top < 40*dp) || (top + height > screenHeight)) dY = -dY
        left += (dX * speed).toInt()
        top += (dY * speed).toInt()
    }
}


fun collision (o1: GameObject, o2: GameObject): Boolean =
    (o1.left + o1.width - (4*dp).toInt() > o2.left) &&
            (o1.left < o2.left + o2.width - (4*dp).toInt() ) &&
            (o1.top + o1.height  - (4*dp).toInt() > o2.top) &&
            (o1.top < o2.top + o2.height - (4*dp).toInt())


// randomized appearance of objects at every border
fun setRandomPosition(o: GameObject) {
    val border: Int = (0..3).random()
    val randomX: Int = ((5*dp).toInt()..screenWidth - o.width - (5*dp).toInt()).random()
    val randomY: Int = ((45*dp).toInt()..screenHeight - o.height - (5*dp).toInt()).random()

    if (border == 0) { o.left = (5*dp).toInt(); o.top = randomY }
    if (border == 1) { o.left = screenWidth - o.width - (5*dp).toInt(); o.top = randomY }
    if (border == 2) { o.left = randomX; o.top = (45*dp).toInt() }
    if (border == 3) { o.left = randomX; o.top = screenHeight - o.height - (5*dp).toInt() }

    o.dX = (0..1).random()
    o.dY = (0..1).random()
    if (o.dX == 0) o.dX = -1
    if (o.dY == 0) o.dY = -1
}

// let object appear on the left side only
fun setRandomPositionOnLeft(o: GameObject) {
    val maxY: Int = screenHeight - o.height - (50*dp).toInt()

    o.left =  2
    o.top = (0..maxY).random() + (45*dp).toInt()
    o.dX = (0..1).random()
    o.dY = (0..1).random()
    if (o.dX == 0) o.dX = -1
    if (o.dY == 0) o.dY = -1
}
