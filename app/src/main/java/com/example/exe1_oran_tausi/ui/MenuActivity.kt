package com.example.exe1_oran_tausi.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.exe1_oran_tausi.R
import com.example.exe1_oran_tausi.databinding.ActivityMenuBinding
import com.example.exe1_oran_tausi.utilities.BackgroundSoundPlayer

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding

    companion object {
        const val KEY_PLAYER_NAME = "playerName"

        const val KEY_GAME_TYPE = "gameType"

        const val GAME_TYPE_SENSORS = 0

        const val GAME_TYPE_BUTTONS = 1
        const val KEY_FAST_MODE = "fastMode"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        menuButtons()

    }

    private fun menuButtons(){
        binding.btnArrow.setOnClickListener {
            startGame(GAME_TYPE_BUTTONS)
        }
        binding.btnSensors.setOnClickListener {
            startGame(GAME_TYPE_SENSORS)
        }
        binding.btnTopTen.setOnClickListener {
            startActivity(Intent(this, TopTenManager::class.java))
        }

    }
    private fun startGame(gameType: Int) {
        val bundle = buildPlayerBundleOrNull(gameType) ?: return
        startActivity(Intent(this, MainActivity::class.java).putExtras(bundle))
    }

    private fun buildPlayerBundleOrNull(
        gameType: Int = GAME_TYPE_BUTTONS
    ): Bundle? {
        val playerName = binding.edtPlayerName.text.toString().trim()
        if (playerName.isEmpty()) {
            binding.edtPlayerName.error = "Please enter your name"
            binding.edtPlayerName.requestFocus()
            return null
        }
        val isFast = binding.switchSpeed.isChecked
        return Bundle().apply {
            putString(KEY_PLAYER_NAME, playerName)
            putInt(KEY_GAME_TYPE, gameType)
            putBoolean(KEY_FAST_MODE, isFast)
        }
    }

    override fun onResume() {
        super.onResume()
        BackgroundSoundPlayer.play(this,R.raw.menu_background_sound)
    }

}