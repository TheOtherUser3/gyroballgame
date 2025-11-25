package com.example.gyroballgame

import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyro: Sensor? = null

    private var tiltX by mutableStateOf(0f)
    private var tiltY by mutableStateOf(0f)
    private var lastGyroTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock to portrait orientation.  This game would SUCK to play otherwise
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GyroMazeScreen(
                        tiltX = tiltX,
                        tiltY = tiltY
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gyro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val ts = event.timestamp
            if (lastGyroTimestamp != 0L) {
                val dt = (ts - lastGyroTimestamp) / 1_000_000_000f
                val wx = event.values[0]
                val wy = event.values[1]

                val gain = 40f
                tiltX += wy * dt * gain
                tiltY += wx * dt * gain

                tiltX = tiltX.coerceIn(-1f, 1f)
                tiltY = tiltY.coerceIn(-1f, 1f)
            }
            lastGyroTimestamp = ts
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}

data class MazeWall(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class MazeGoal(
    val cx: Float,
    val cy: Float,
    val radius: Float
)

// Maze designed for phone aspect ratio (width 0-1, height 0-2.2)
val MAZE_WALLS: List<MazeWall> = listOf(
    MazeWall(left = 0f, top = 0f, right = 1f, bottom = 0.02f),
    MazeWall(left = 0f, top = 2.18f, right = 1f, bottom = 2.2f),
    MazeWall(left = 0f, top = 0f, right = 0.02f, bottom = 2.2f),
    MazeWall(left = 0.98f, top = 0f, right = 1f, bottom = 2.2f),

    MazeWall(left = 0.09f, top = 1f, right = 0.11f, bottom = 1.1f),
    MazeWall(left = 0.09f, top = 1.3f, right = 0.11f, bottom = 1.4f),
    MazeWall(left = 0.09f, top = 1.5f, right = 0.11f, bottom = 1.6f),
    MazeWall(left = 0.09f, top = 1.7f, right = 0.11f, bottom = 1.9f),

    MazeWall(left = 0.19f, top = 0.2f, right = 0.21f, bottom = 0.3f),
    MazeWall(left = 0.19f, top = 0.4f, right = 0.21f, bottom = 0.5f),
    MazeWall(left = 0.19f, top = 0.9f, right = 0.21f, bottom = 1.1f),
    MazeWall(left = 0.19f, top = 1.4f, right = 0.21f, bottom = 1.5f),
    MazeWall(left = 0.19f, top = 1.6f, right = 0.21f, bottom = 1.7f),
    MazeWall(left = 0.19f, top = 1.9f, right = 0.21f, bottom = 2.1f),

    MazeWall(left = 0.29f, top = 0.1f, right = 0.31f, bottom = 0.2f),
    MazeWall(left = 0.29f, top = 0.5f, right = 0.31f, bottom = 0.6f),
    MazeWall(left = 0.29f, top = 0.8f, right = 0.31f, bottom = 0.9f),
    MazeWall(left = 0.29f, top = 1.2f, right = 0.31f, bottom = 1.5f),
    MazeWall(left = 0.29f, top = 1.6f, right = 0.31f, bottom = 2.1f),

    MazeWall(left = 0.39f, top = 0.2f, right = 0.41f, bottom = 0.3f),
    MazeWall(left = 0.39f, top = 0.4f, right = 0.41f, bottom = 0.5f),
    MazeWall(left = 0.39f, top = 0.7f, right = 0.41f, bottom = 0.9f),
    MazeWall(left = 0.39f, top = 1.1f, right = 0.41f, bottom = 1.3f),
    MazeWall(left = 0.39f, top = 1.4f, right = 0.41f, bottom = 1.6f),
    MazeWall(left = 0.39f, top = 1.7f, right = 0.41f, bottom = 2f),

    MazeWall(left = 0.49f, top = 0f, right = 0.51f, bottom = 0.2f),
    MazeWall(left = 0.49f, top = 0.3f, right = 0.51f, bottom = 0.6f),
    MazeWall(left = 0.49f, top = 0.9f, right = 0.51f, bottom = 1.1f),
    MazeWall(left = 0.49f, top = 2f, right = 0.51f, bottom = 2.1f),

    MazeWall(left = 0.59f, top = 0.1f, right = 0.61f, bottom = 0.3f),
    MazeWall(left = 0.59f, top = 0.5f, right = 0.61f, bottom = 1.1f),
    MazeWall(left = 0.59f, top = 1.4f, right = 0.61f, bottom = 1.5f),
    MazeWall(left = 0.59f, top = 1.7f, right = 0.61f, bottom = 1.8f),
    MazeWall(left = 0.59f, top = 1.9f, right = 0.61f, bottom = 2.2f),

    MazeWall(left = 0.69f, top = 0.1f, right = 0.71f, bottom = 0.2f),
    MazeWall(left = 0.69f, top = 0.4f, right = 0.71f, bottom = 0.5f),
    MazeWall(left = 0.69f, top = 1.1f, right = 0.71f, bottom = 1.3f),
    MazeWall(left = 0.69f, top = 1.7f, right = 0.71f, bottom = 1.9f),

    MazeWall(left = 0.79f, top = 0.2f, right = 0.81f, bottom = 0.3f),
    MazeWall(left = 0.79f, top = 0.4f, right = 0.81f, bottom = 0.6f),
    MazeWall(left = 0.79f, top = 0.8f, right = 0.81f, bottom = 1.1f),
    MazeWall(left = 0.79f, top = 1.2f, right = 0.81f, bottom = 1.3f),
    MazeWall(left = 0.79f, top = 1.5f, right = 0.81f, bottom = 1.6f),
    MazeWall(left = 0.79f, top = 1.8f, right = 0.81f, bottom = 1.9f),

    MazeWall(left = 0.89f, top = 0.1f, right = 0.91f, bottom = 0.2f),
    MazeWall(left = 0.89f, top = 0.5f, right = 0.91f, bottom = 0.8f),
    MazeWall(left = 0.89f, top = 0.9f, right = 0.91f, bottom = 1f),
    MazeWall(left = 0.89f, top = 1.6f, right = 0.91f, bottom = 1.7f),
    MazeWall(left = 0.89f, top = 2f, right = 0.91f, bottom = 2.1f),

    // horizontal internal walls
    MazeWall(left = 0.2f, top = 0.09f, right = 0.3f, bottom = 0.11f),
    MazeWall(left = 0.4f, top = 0.09f, right = 0.5f, bottom = 0.11f),

    MazeWall(left = 0.1f, top = 0.19f, right = 0.2f, bottom = 0.21f),
    MazeWall(left = 0.3f, top = 0.19f, right = 0.4f, bottom = 0.21f),
    MazeWall(left = 0.9f, top = 0.19f, right = 1f, bottom = 0.21f),

    MazeWall(left = 0.2f, top = 0.29f, right = 0.3f, bottom = 0.31f),
    MazeWall(left = 0.4f, top = 0.29f, right = 0.5f, bottom = 0.31f),
    MazeWall(left = 0.6f, top = 0.29f, right = 0.7f, bottom = 0.31f),
    MazeWall(left = 0.8f, top = 0.29f, right = 0.9f, bottom = 0.31f),

    MazeWall(left = 0.2f, top = 0.39f, right = 0.4f, bottom = 0.41f),
    MazeWall(left = 0.5f, top = 0.39f, right = 0.6f, bottom = 0.41f),
    MazeWall(left = 0.7f, top = 0.39f, right = 0.9f, bottom = 0.41f),

    MazeWall(left = 0.1f, top = 0.49f, right = 0.2f, bottom = 0.51f),
    MazeWall(left = 0.6f, top = 0.49f, right = 0.7f, bottom = 0.51f),
    MazeWall(left = 0.9f, top = 0.49f, right = 1f, bottom = 0.51f),

    MazeWall(left = 0.1f, top = 0.59f, right = 0.5f, bottom = 0.61f),
    MazeWall(left = 0.6f, top = 0.59f, right = 0.7f, bottom = 0.61f),

    MazeWall(left = 0.1f, top = 0.69f, right = 0.4f, bottom = 0.71f),
    MazeWall(left = 0.7f, top = 0.69f, right = 0.8f, bottom = 0.71f),

    MazeWall(left = 0f, top = 0.79f, right = 0.3f, bottom = 0.81f),
    MazeWall(left = 0.4f, top = 0.79f, right = 0.5f, bottom = 0.81f),
    MazeWall(left = 0.8f, top = 0.79f, right = 0.9f, bottom = 0.81f),

    MazeWall(left = 0.1f, top = 0.89f, right = 0.2f, bottom = 0.91f),
    MazeWall(left = 0.5f, top = 0.89f, right = 0.6f, bottom = 0.91f),
    MazeWall(left = 0.7f, top = 0.89f, right = 0.8f, bottom = 0.91f),

    MazeWall(left = 0f, top = 0.99f, right = 0.1f, bottom = 1.01f),
    MazeWall(left = 0.2f, top = 0.99f, right = 0.4f, bottom = 1.01f),
    MazeWall(left = 0.6f, top = 0.99f, right = 0.7f, bottom = 1.01f),
    MazeWall(left = 0.9f, top = 0.99f, right = 1f, bottom = 1.01f),

    MazeWall(left = 0.4f, top = 1.09f, right = 0.5f, bottom = 1.11f),
    MazeWall(left = 0.7f, top = 1.09f, right = 0.9f, bottom = 1.11f),

    MazeWall(left = 0.1f, top = 1.19f, right = 0.4f, bottom = 1.21f),
    MazeWall(left = 0.5f, top = 1.19f, right = 0.7f, bottom = 1.21f),
    MazeWall(left = 0.9f, top = 1.19f, right = 1f, bottom = 1.21f),

    MazeWall(left = 0f, top = 1.29f, right = 0.2f, bottom = 1.31f),
    MazeWall(left = 0.5f, top = 1.29f, right = 0.6f, bottom = 1.31f),

    MazeWall(left = 0.2f, top = 1.39f, right = 0.3f, bottom = 1.41f),
    MazeWall(left = 0.4f, top = 1.39f, right = 0.5f, bottom = 1.41f),
    MazeWall(left = 0.6f, top = 1.39f, right = 0.9f, bottom = 1.41f),

    MazeWall(left = 0.5f, top = 1.49f, right = 0.6f, bottom = 1.51f),
    MazeWall(left = 0.7f, top = 1.49f, right = 0.9f, bottom = 1.51f),

    MazeWall(left = 0.1f, top = 1.59f, right = 0.2f, bottom = 1.61f),
    MazeWall(left = 0.3f, top = 1.59f, right = 0.5f, bottom = 1.61f),
    MazeWall(left = 0.6f, top = 1.59f, right = 0.8f, bottom = 1.61f),

    MazeWall(left = 0f, top = 1.69f, right = 0.1f, bottom = 1.71f),
    MazeWall(left = 0.6f, top = 1.69f, right = 0.9f, bottom = 1.71f),

    MazeWall(left = 0.2f, top = 1.79f, right = 0.3f, bottom = 1.81f),
    MazeWall(left = 0.5f, top = 1.79f, right = 0.6f, bottom = 1.81f),
    MazeWall(left = 0.8f, top = 1.79f, right = 1f, bottom = 1.81f),

    MazeWall(left = 0.1f, top = 1.89f, right = 0.2f, bottom = 1.91f),
    MazeWall(left = 0.4f, top = 1.89f, right = 0.5f, bottom = 1.91f),
    MazeWall(left = 0.8f, top = 1.89f, right = 0.9f, bottom = 1.91f),

    MazeWall(left = 0f, top = 1.99f, right = 0.1f, bottom = 2.01f),
    MazeWall(left = 0.7f, top = 1.99f, right = 0.8f, bottom = 2.01f),

    MazeWall(left = 0.1f, top = 2.09f, right = 0.5f, bottom = 2.11f),
    MazeWall(left = 0.6f, top = 2.09f, right = 0.9f, bottom = 2.11f),
)

// Goal
val MAZE_GOAL = MazeGoal(cx = 0.85f, cy = 1.85f, radius = 0.035f)

@Composable
fun GyroMazeScreen(
    tiltX: Float,
    tiltY: Float
) {
    // Ball
    var ballX by remember { mutableStateOf(0.45f) }
    var ballY by remember { mutableStateOf(0.55f) }

    var velX by remember { mutableStateOf(0f) }
    var velY by remember { mutableStateOf(0f) }

    val ballRadius = 0.025f

    val animX by animateFloatAsState(targetValue = ballX, label = "ballX")
    val animY by animateFloatAsState(targetValue = ballY, label = "ballY")

    var hasWon by remember { mutableStateOf(false) }

    val currentTiltX by rememberUpdatedState(tiltX)
    val currentTiltY by rememberUpdatedState(tiltY)

    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            val frameTime = withFrameNanos { it }
            if (lastTime != 0L) {
                val dt = (frameTime - lastTime) / 1_000_000_000f

                val accelScale = 1.5f
                velX += currentTiltX * accelScale * dt
                velY += currentTiltY * accelScale * dt

                val friction = 0.9f
                velX *= friction
                velY *= friction

                var nextX = ballX + velX * dt
                var nextY = ballY + velY * dt

                // Bounds check with proper aspect ratio
                nextX = nextX.coerceIn(0.04f + ballRadius, 0.96f - ballRadius)
                nextY = nextY.coerceIn(0.04f + ballRadius, 2.16f - ballRadius)

                var correctedX = nextX
                var correctedY = nextY

                MAZE_WALLS.forEach { w ->
                    val expandedLeft = w.left - ballRadius
                    val expandedRight = w.right + ballRadius
                    val expandedTop = w.top - ballRadius
                    val expandedBottom = w.bottom + ballRadius

                    if (correctedX in expandedLeft..expandedRight &&
                        correctedY in expandedTop..expandedBottom) {

                        val overlapLeft = correctedX - expandedLeft
                        val overlapRight = expandedRight - correctedX
                        val overlapTop = correctedY - expandedTop
                        val overlapBottom = expandedBottom - correctedY

                        val minOverlap = minOf(overlapLeft, overlapRight, overlapTop, overlapBottom)

                        when (minOverlap) {
                            overlapLeft -> {
                                correctedX = expandedLeft - 0.001f
                                velX = -velX * 0.3f
                            }
                            overlapRight -> {
                                correctedX = expandedRight + 0.001f
                                velX = -velX * 0.3f
                            }
                            overlapTop -> {
                                correctedY = expandedTop - 0.001f
                                velY = -velY * 0.3f
                            }
                            overlapBottom -> {
                                correctedY = expandedBottom + 0.001f
                                velY = -velY * 0.3f
                            }
                        }
                    }
                }

                ballX = correctedX
                ballY = correctedY

                val dx = ballX - MAZE_GOAL.cx
                val dy = ballY - MAZE_GOAL.cy
                val distance = sqrt(dx * dx + dy * dy)
                if (distance < MAZE_GOAL.radius + ballRadius) {
                    hasWon = true
                }
            }
            lastTime = frameTime
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080A0C)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasWon) "***YOU WIN!***" else "GYRO LABYRINTH",
                color = if (hasWon) Color(0xFF00E676) else Color(0xFFFFC107),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Calculate proper scaling - height coordinate system is 2.2x taller
                    val scaleX = w
                    val scaleY = h / 2.2f

                    val wallColor = Color(0xFF37474F)
                    MAZE_WALLS.forEach { wall ->
                        drawRect(
                            color = wallColor,
                            topLeft = Offset(wall.left * scaleX, wall.top * scaleY),
                            size = Size(
                                (wall.right - wall.left) * scaleX,
                                (wall.bottom - wall.top) * scaleY
                            )
                        )
                    }

                    val goalPx = MAZE_GOAL.cx * scaleX
                    val goalPy = MAZE_GOAL.cy * scaleY
                    val goalR = MAZE_GOAL.radius * scaleX

                    drawCircle(
                        color = Color(0xFF00E676),
                        radius = goalR,
                        center = Offset(goalPx, goalPy),
                        style = Fill
                    )
                    drawCircle(
                        color = Color(0xFF1B5E20),
                        radius = goalR,
                        center = Offset(goalPx, goalPy),
                        style = Stroke(width = 6f)
                    )

                    val radius = ballRadius * scaleX
                    val cx = animX * scaleX
                    val cy = animY * scaleY

                    drawCircle(
                        color = Color(0xFFFFC107),
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Fill
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 6f)
                    )
                }
            }
        }
    }
}