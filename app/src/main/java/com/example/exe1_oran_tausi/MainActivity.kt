package com.example.exe1_oran_tausi

import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.exe1_oran_tausi.databinding.ActivityMainBinding
import java.util.Locale
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var gameManager: GameManager

    private val tickMs = 800L
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private lateinit var coneViews: Array<Array<ImageView>>
    private lateinit var heartViews: List<ImageView>

    private lateinit var carViews: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        gameManager = GameManager()


        carViews = listOf(
            binding.imgCarLeft,
            binding.imgCarMiddle,
            binding.imgCarRight
        )
        heartViews = listOf(
            binding.hart1,
            binding.hart2,
            binding.hart3
        )


        coneViews = arrayOf(
            arrayOf(binding.imgVic1,  binding.imgVic2,  binding.imgVic3),   // row 0
            arrayOf(binding.imgVic4,  binding.imgVic5,  binding.imgVic6),   // row 1
            arrayOf(binding.imgVic7,  binding.imgVic8,  binding.imgVic9),   // row 2
            arrayOf(binding.imgVic10, binding.imgVic11, binding.imgVic12),  // row 3
            arrayOf(binding.imgVic13, binding.imgVic14, binding.imgVic15),  // row 4
            arrayOf(binding.imgVic16, binding.imgVic17, binding.imgVic18)   // row 5
        )


        updateCarUi()
        updateConesUi()
        updateHeartsUi()
        updateDistanceUi()

        setButtonsCarListeners()

    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val deltaTimeSeconds = tickMs / 1000f
            gameManager.updateDistance(deltaTimeSeconds)

            val crashed = gameManager.stepConesAndCheckCrash()
            if (crashed) {
                vibrateOnCrash()
                makeToast()
            }

            updateConesUi()
            updateHeartsUi()
            updateDistanceUi()

            if (gameManager.isGameOver()) {
                onGameOver()
                return
            }


            handler.postDelayed(this, tickMs)
        }
    }

    private fun setControlsVisible(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.INVISIBLE
        binding.btnMoveLeft.visibility = v
        binding.btnMoveRight.visibility = v
    }



    private fun makeToast(){
        Toast.makeText(this,"crashed! life left: ${gameManager.currentLives}", Toast.LENGTH_SHORT).show()
    }

    private fun updateConesUi() {
        val grid = gameManager.getConesGrid()

        for (r in 0 until 6) {
            for (c in 0 until 3) {
                coneViews[r][c].visibility =
                    if (grid[r][c] == 1) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    fun updateCarUi(){
        val lane = gameManager.carLane

        carViews.forEachIndexed { index, imageView ->
            imageView.visibility = if(index == lane) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun updateHeartsUi() {
        val lives = gameManager.currentLives

        heartViews.forEachIndexed { index, imageView ->
            imageView.visibility = if (index < lives) View.VISIBLE else View.INVISIBLE
        }
    }


    private fun setButtonsCarListeners() {
        binding.btnMoveLeft.setOnClickListener {
            gameManager.moveCarLeft()
            updateCarUi()

        }
            binding.btnMoveRight.setOnClickListener {
                gameManager.moveCarRight()
                updateCarUi()
            }
    }

    fun updateDistanceUi() {
        val km = gameManager.getDistanceKm()
        binding.lblKilometer.text = String.format(Locale.US, "%.1f km", km)
    }


    private fun startGameLoop() {
        if (isRunning) return
        isRunning = true
        handler.post(gameLoop)
    }



    private fun stopGameLoop() {
        isRunning = false
        handler.removeCallbacks(gameLoop)
    }

    private fun onGameOver() {
        isRunning = false
        handler.removeCallbacks(gameLoop)
        setControlsVisible(false)
        Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        if (!gameManager.isGameOver()) {
            setControlsVisible(true)
            startGameLoop()
        }
    }

    override fun onStop() {
        super.onStop()
        stopGameLoop()
    }

    private fun vibrateOnCrash(durationMs: Long = 120L) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    durationMs,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    }


}