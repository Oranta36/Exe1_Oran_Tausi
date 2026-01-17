package com.example.exe1_oran_tausi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.exe1_oran_tausi.R
import com.example.exe1_oran_tausi.callback.ScoreClickedCallBack
import com.example.exe1_oran_tausi.data.ScoreEntry
import com.example.exe1_oran_tausi.data.TopTenStore
import com.example.exe1_oran_tausi.databinding.ActivityTopTenManagerBinding
import com.example.exe1_oran_tausi.fragment.FragmentMap
import com.example.exe1_oran_tausi.fragment.ListFragment

class TopTenManager : AppCompatActivity(), ScoreClickedCallBack {

    private lateinit var binding: ActivityTopTenManagerBinding

    private val listFragment = ListFragment()
    private val fragmentMap = FragmentMap()

    private var focusLat: Double = Double.NaN
    private var focusLon: Double = Double.NaN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()

        readExtras(intent.extras)

        val top10 = loadTopTenGlobal()

        attachFragments()
        bindList(top10)
        focusMap(top10)
    }

    override fun onScoreClicked(lat: Double, lon: Double) {
        fragmentMap.moveToLocation(lat, lon)
    }

    private fun initUi() {
        enableEdgeToEdge()
        binding = ActivityTopTenManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { navigateToMenu() }
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        finish()
    }

    private fun readExtras(extras: Bundle?) {
        focusLat = extras?.getDouble(EXTRA_LAST_LAT, Double.NaN) ?: Double.NaN
        focusLon = extras?.getDouble(EXTRA_LAST_LON, Double.NaN) ?: Double.NaN
    }

    private fun loadTopTenGlobal(): List<ScoreEntry> =
        TopTenStore.loadTop10Global(this)

    private fun attachFragments() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.layLst, listFragment)
            .replace(R.id.layMap, fragmentMap)
            .commit()
    }

    private fun bindList(top10: List<ScoreEntry>) {
        listFragment.setListener(this)
        listFragment.submitList(top10)
    }

    private fun focusMap(top10: List<ScoreEntry>) {
        binding.root.post {
            when {
                !focusLat.isNaN() && !focusLon.isNaN() ->
                    fragmentMap.moveToLocation(focusLat, focusLon)
                top10.isNotEmpty() ->
                    fragmentMap.moveToLocation(top10.first().lat, top10.first().lon)
            }
        }
    }

    private companion object {
        const val EXTRA_LAST_LAT = "EXTRA_LAST_LAT"
        const val EXTRA_LAST_LON = "EXTRA_LAST_LON"
    }
}
