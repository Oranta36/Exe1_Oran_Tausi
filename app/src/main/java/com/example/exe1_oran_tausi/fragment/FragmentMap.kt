package com.example.exe1_oran_tausi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.exe1_oran_tausi.R
import com.example.exe1_oran_tausi.databinding.ActivityFragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class FragmentMap : Fragment(), OnMapReadyCallback {
    private val TEL_AVIV = LatLng(32.0853, 34.7818)
    private var didInitialFocus = false

    private var _binding: ActivityFragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var pendingLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityFragmentMapBinding.inflate(inflater, container, false)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        enableMyLocationIfPermitted()

        pendingLatLng?.let { target ->
            moveToLocation(target.latitude, target.longitude)
            pendingLatLng = null
            didInitialFocus = true
        }

        if (!didInitialFocus) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(TEL_AVIV, 13f))
            didInitialFocus = true
        }
    }


    fun moveToLocation(lat: Double, lon: Double) {

        binding.txtLocation.text = String.format(Locale.US, "lat=%.5f  lon=%.5f", lat, lon)

        val target = LatLng(lat, lon)
        val map = googleMap
        if (map == null) {
            pendingLatLng = target
            return
        }

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15f))
    }

    private fun enableMyLocationIfPermitted() {
        val ctx = context ?: return
        val map = googleMap ?: return

        val fineGranted =
            androidx.core.content.ContextCompat.checkSelfPermission(
                ctx,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
            try {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } catch (_: SecurityException) { }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        googleMap = null
    }
}
