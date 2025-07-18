// Local: app/src/main/java/com/meshwave/core/MainActivity.kt
package com.meshwave.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {

    companion object {
        const val IDENTITY_UPDATE = 1
        const val LOCATION_UPDATE = 2
        const val LOG_UPDATE = 3
        const val WIFI_STATUS_UPDATE = 10
        const val PARTNER_CPA_UPDATE = 11 // Novo
    }

    private val PERMISSIONS_REQUEST_CODE = 101

    private lateinit var locationModule: LocationModule
    private lateinit var identityModule: IdentityModule
    private lateinit var wifiDirectModule: WiFiDirectModule
    private lateinit var statusFragment: StatusFragment
    private var lastLocationData: LocationData? = null

    private val uiHandler = Handler(Looper.getMainLooper()) { msg ->
        if (!this::statusFragment.isInitialized || !statusFragment.isAdded) {
            return@Handler false
        }
        when (msg.what) {
            LOCATION_UPDATE -> {
                val locationData = msg.obj as LocationData
                lastLocationData = locationData
                statusFragment.updateNodeGeohash(locationData.nodeGeohash)
                addLogToFragment("[Main] Geohash Nó(9) recebido: ${locationData.nodeGeohash}")
                addLogToFragment("[Main] Geohash Área(6) recebido: ${locationData.areaGeohash}")
                identityModule.generateCpaIfNeeded(locationData.areaGeohash)
                true
            }
            IDENTITY_UPDATE -> {
                val cpa = msg.obj as NodeCPA
                statusFragment.updateCpaData(cpa)
                addLogToFragment("[Main] CPA Pronto.")
                true
            }
            LOG_UPDATE -> {
                addLogToFragment(msg.obj as String)
                true
            }
            WIFI_STATUS_UPDATE -> {
                statusFragment.updateWifiStatus(msg.obj as String)
                true
            }
            PARTNER_CPA_UPDATE -> {
                val partnerCpa = msg.obj as NodeCPA
                statusFragment.updatePartnerCpa(partnerCpa)
                addLogToFragment("[Main] CPA do Parceiro recebido!")
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupCustomActionBar()

        if (savedInstanceState == null) {
            statusFragment = StatusFragment()
            loadFragment(statusFragment)
        } else {
            statusFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as StatusFragment
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    override fun onPause() {
        super.onPause()
        if (this::wifiDirectModule.isInitialized) {
            wifiDirectModule.stop()
        }
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        val permissionsToRequest = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        } else {
            initializeModules()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeModules()
            } else {
                addLogToFragment("[Main] Permissões negadas.")
            }
        }
    }

    private fun initializeModules() {
        if (this::locationModule.isInitialized) {
            if (this::wifiDirectModule.isInitialized) wifiDirectModule.start()
            return
        }
        addLogToFragment("[Main] Módulos sendo inicializados...")
        locationModule = LocationModule(this, uiHandler)
        identityModule = IdentityModule(this, uiHandler)
        wifiDirectModule = WiFiDirectModule(this, uiHandler, identityModule)
        locationModule.start()
        identityModule.start()
        wifiDirectModule.start()
    }

    private fun addLogToFragment(message: String) {
        if (this::statusFragment.isInitialized && statusFragment.isAdded) {
            statusFragment.addLog(message)
        } else {
            Log.w("MainActivity", "StatusFragment não pronto para log: $message")
        }
    }

    private fun setupCustomActionBar() {
        supportActionBar?.apply {
            setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM)
            setCustomView(R.layout.action_bar_custom)
            val versionTextView = customView?.findViewById<TextView>(R.id.textViewVersion)
            try {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                versionTextView?.text = "v${pInfo.versionName}"
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("MainActivity", "Erro ao obter a versão do app", e)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
