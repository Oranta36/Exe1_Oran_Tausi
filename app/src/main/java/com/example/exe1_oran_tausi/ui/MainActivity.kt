package com.example.exe1_oran_tausi.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.exe1_oran_tausi.logic.CarTiltController
import com.example.exe1_oran_tausi.logic.GameManager
import com.example.exe1_oran_tausi.R
import com.example.exe1_oran_tausi.callback.TiltCallback
import com.example.exe1_oran_tausi.databinding.ActivityMainBinding
import java.util.Locale
import com.example.exe1_oran_tausi.data.TopTenStore
import com.example.exe1_oran_tausi.ui.MenuActivity.Companion.GAME_TYPE_SENSORS
import com.example.exe1_oran_tausi.utilities.BackgroundSoundPlayer
import com.example.exe1_oran_tausi.utilities.LocationHelper
import com.example.exe1_oran_tausi.utilities.SoundEffectsManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameManager: GameManager
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private lateinit var locationHelper: LocationHelper
    private var lat: Double = 32.0853
    private var lon: Double = 34.7818

    private lateinit var coneViews: Array<Array<ImageView>>
    private lateinit var heartViews: List<ImageView>
    private lateinit var carViews: List<ImageView>

    private var gameType: Int = 1
    private  var playerName: String = "Player"
    private var isFast: Boolean = false
    private var carTiltController: CarTiltController? = null


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
        initLocation()
        loadData()
        setUpArrays()
        applyControlGameType()
        updateCarUi(gameManager.carLane)
        updateItemsUi(gameManager.getItemsGrid())
        updateHeartsUi(gameManager.currentLives)
        updateDistanceUi(gameManager.getDistanceKm())
        setupBackPressHandler()
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val state = gameManager.tick()

            if (state.crashedThisTick) {
                SoundEffectsManager.playExclusive(R.raw.car_crash_sound)
                com.example.exe1_oran_tausi.utilities.VibrationHelper.vibrateOneShot(
                    this@MainActivity,
                    120L
                )
                makeToast()
            }

            if (state.coinCollectedThisTick) {
                SoundEffectsManager.playExclusive(R.raw.coin_sound)
            }

            binding.lblCoins.text = state.coins.toString()
            updateItemsUi(state.grid)
            updateHeartsUi(state.lives)
            updateDistanceUi(state.distanceKm)
            updateCarUi(state.carLane)

            if (state.isGameOver) {
                onGameOver()
                return
            }

            handler.postDelayed(this, state.nextTickMs)
        }
    }

    private fun initLocation(){
        locationHelper = LocationHelper(this) { latitude, longitude ->
            lat = latitude
            lon = longitude
        }
        locationHelper.checkAndRequestLocation()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationHelper.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun loadData() {
        val bundle = intent.extras ?: return

        gameType = bundle.getInt(MenuActivity.KEY_GAME_TYPE, MenuActivity.GAME_TYPE_BUTTONS)
        isFast = bundle.getBoolean(MenuActivity.KEY_FAST_MODE, false)
        playerName = bundle.getString(MenuActivity.KEY_PLAYER_NAME, "Player")
        gameManager.setFastMode(isFast)
    }


    private fun setUpArrays(){
        carViews = listOf(
            binding.imgCarLeftLeft, binding.imgCarLeft, binding.imgCarMiddle, binding.imgCarRight, binding.imgCarRightRight
        )
        heartViews = listOf(
            binding.hart1, binding.hart2, binding.hart3
        )
        coneViews = arrayOf(
            arrayOf(binding.imgVic1,  binding.imgVic2,  binding.imgVic3,binding.imgVic4,  binding.imgVic5),   // row 0
            arrayOf(  binding.imgVic6,binding.imgVic7,  binding.imgVic8,binding.imgVic9,binding.imgVic10),   // row 1
            arrayOf(  binding.imgVic11, binding.imgVic12,binding.imgVic13, binding.imgVic14,binding.imgVic15),   // row 2
            arrayOf(binding.imgVic16, binding.imgVic17, binding.imgVic18,binding.imgVic19,binding.imgVic20),  // row 3
            arrayOf( binding.imgVic21,binding.imgVic22,binding.imgVic23,binding.imgVic24,binding.imgVic25),  // row 4
            arrayOf(binding.imgVic26,binding.imgVic27,binding.imgVic28,binding.imgVic29,binding.imgVic30)   // row 5
        )
    }

    private fun setControlsVisibleAndEnabled(enabled: Boolean) {
        val v = if (enabled) View.VISIBLE else View.INVISIBLE
        binding.btnMoveLeft.visibility = v
        binding.btnMoveRight.visibility = v
        binding.btnMoveLeft.isEnabled = enabled
        binding.btnMoveRight.isEnabled = enabled
    }

    private fun applyControlGameType(){
        val useSensors = (gameType == GAME_TYPE_SENSORS)

        setControlsVisibleAndEnabled(!useSensors)
        if(useSensors){
            ensureTiltController()
        }
        else{
            setButtonsCarListeners()
        }
    }

    private fun ensureTiltController() {
        if (carTiltController != null) return

        carTiltController = CarTiltController(this, object : TiltCallback {
            override fun onMove(direction: Int) {
                runOnUiThread {
                    if (direction > 0) gameManager.moveCarRight()
                    else gameManager.moveCarLeft()
                    updateCarUi(gameManager.carLane)
                }
            }
        })
    }

    private fun updateTiltLifecycle(active: Boolean) {
        if (gameType != GAME_TYPE_SENSORS) return

        if (active) carTiltController?.start()
        else carTiltController?.stop()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitToMenuDialog()
                }
            }
        )
    }

    private fun makeToast(){
        Toast.makeText(this,"crashed! life left: ${gameManager.currentLives}", Toast.LENGTH_SHORT).show()
    }

    private fun showExitToMenuDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit game")
            .setMessage("Do you want to return to the menu?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()

                stopGameLoop()
                if (gameType == GAME_TYPE_SENSORS) carTiltController?.stop()

                val intent = Intent(this, MenuActivity::class.java)
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }


    private fun updateItemsUi(grid: Array<IntArray>) {
        for (r in 0 until 6) {
            for (c in 0 until 5) {
                val v = coneViews[r][c]
                when (grid[r][c]) {
                    0 -> v.visibility = View.INVISIBLE
                    1 -> { v.setImageResource(R.drawable.ic_vic); v.visibility = View.VISIBLE }
                    2 -> { v.setImageResource(R.drawable.ic_coin); v.visibility = View.VISIBLE }
                }
            }
        }
    }

    private fun updateCarUi(lane: Int) {
        carViews.forEachIndexed { index, imageView ->
            imageView.visibility = if (index == lane) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun updateHeartsUi(lives: Int) {
        heartViews.forEachIndexed { index, imageView ->
            imageView.visibility = if (index < lives) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setButtonsCarListeners() {
        binding.btnMoveLeft.setOnClickListener {
            gameManager.moveCarLeft()
            updateCarUi(gameManager.carLane)

        }
            binding.btnMoveRight.setOnClickListener {
                gameManager.moveCarRight()
                updateCarUi(gameManager.carLane)
            }
    }

    private fun updateDistanceUi(distanceKm: Float) {
        binding.lblKilometer.text = String.format(Locale.US, "%.1f km", distanceKm)
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
        stopGameLoop()

        if (gameType == GAME_TYPE_SENSORS) {
            carTiltController?.stop()
        }

        setControlsVisibleAndEnabled(false)

        val name = playerName.trim()          // <-- משתמשים בשם מה-Intent
        val km = gameManager.getDistanceKm().toDouble()

        Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show()

        if (name.isBlank()) return

        TopTenStore.addRun(
            context = this,
            playerName = name,
            km = km,
            coins = gameManager.coinsCollected,
            lat = lat,
            lon = lon
        )

        val topTenIntent = Intent(this, TopTenManager::class.java).apply {
            putExtras(Bundle().apply {
                putString(MenuActivity.KEY_PLAYER_NAME, name)
                putInt(MenuActivity.KEY_GAME_TYPE, gameType)
                putBoolean(MenuActivity.KEY_FAST_MODE, isFast)
                putDouble("EXTRA_LAST_LAT", lat)
                putDouble("EXTRA_LAST_LON", lon)
            })
        }

        startActivity(topTenIntent)
    }


    override fun onResume() {
        super.onResume()
        SoundEffectsManager.resumeAll()
        BackgroundSoundPlayer.play(this, com.example.exe1_oran_tausi.R.raw.game_background_sound)
    }

    override fun onPause() {
        super.onPause()
        BackgroundSoundPlayer.pause()
    }

    override fun onStart() {
        super.onStart()
        applyControlGameType()
        updateTiltLifecycle(true)
        if (!gameManager.isGameOver()) {
            startGameLoop()
        }
    }

    override fun onStop() {
        super.onStop()
        BackgroundSoundPlayer.pause()
        updateTiltLifecycle(false)
        stopGameLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        BackgroundSoundPlayer.stop()

    }

}