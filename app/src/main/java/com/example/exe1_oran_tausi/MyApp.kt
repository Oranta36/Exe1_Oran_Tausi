package com.example.exe1_oran_tausi

import android.app.Application
import com.example.exe1_oran_tausi.utilities.BackgroundSoundPlayer
import com.example.exe1_oran_tausi.utilities.SoundEffectsManager

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        setUpSound()
    }

    private fun setUpSound(){
        BackgroundSoundPlayer.init(this)
        SoundEffectsManager.init(this)
        SoundEffectsManager.load(this, R.raw.car_crash_sound)
        SoundEffectsManager.load(this, R.raw.coin_sound)
    }


}