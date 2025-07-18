// Caminho: app/src/main/java/com/meshwave/core/MainActivity.kt
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

class MainActivity : AppCompatActivity(), StatusFragment.ManualControlListener {

    private lateinit var locationModule: LocationModule
    private lateinit var identityModule: IdentityModule
    private lateinit var wifiDirectModule: WiFiDirectModule
    private lateinit var statusFragment: StatusFragment

    private val uiHandler = Handler(Looper.getMainLooper()) { msg ->
        if (!this::statusFragment.isInitialized || !statusFragment.isAdded) {
            return@Handler false
        }

        when (msg.what) {
            AppConstants.LOCATION_UPDATE -> {
                val locationData = msg.obj as LocationData
                statusFragment.updateLocation(locationData)
                true
            }
            AppConstants.IDENTITY_UPDATE -> {
                val cpa = msg.obj as NodeCPA
                statusFragment.updateCpaData(cpa)
                true
            }
            AppConstants.PARTNER_CPA_UPDATE -> {
                val partnerCpa = msg.obj as? NodeCPA
                statusFragment.updatePartnerCpa(partnerCpa)
                if (partnerCpa != null) {
                    addLogToFragment("[Main] Cache do parceiro sincronizado!")
                }
                true
            }
            AppConstants.LOG_UPDATE -> {
                val logMessage = msg.obj as String
                addLogToFragment(logMessage)
                true
            }
            AppConstants.WIFI_STATUS_UPDATE -> {
                val status = msg.obj as String
                statusFragment.updateWifiStatus(status)
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
        if (this::locationModule.isInitialized) {
            locationModule.stop()
        }
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        val permissionsToRequest = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), AppConstants.PERMISSIONS_REQUEST_CODE)
        } else {
            initializeModules()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeModules()
            } else {
                addLogToFragment("[Main] Permissões negadas. Funcionalidade limitada.")
            }
        }
    }

    private fun initializeModules() {
        if (this::locationModule.isInitialized) {
            locationModule.start()
            wifiDirectModule.start()
            return
        }

        addLogToFragment("[Main] Módulos sendo inicializados...")

        identityModule = IdentityModule(this, uiHandler)
        locationModule = LocationModule(this, uiHandler, identityModule)
        wifiDirectModule = WiFiDirectModule(this, uiHandler, identityModule)

        identityModule.start()
        locationModule.start()
        wifiDirectModule.start()
    }

    private fun addLogToFragment(message: String) {
        if (this::statusFragment.isInitialized && statusFragment.isAdded) {
            runOnUiThread {
                statusFragment.addLog(message)
            }
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

    // --- MÉTODOS DA INTERFACE ManualControlListener ---

    override fun onBecomeServerClicked() {
        if (this::wifiDirectModule.isInitialized) {
            wifiDirectModule.becomeServer()
        }
    }

    override fun onBecomeClientClicked() {
        if (this::wifiDirectModule.isInitialized) {
            wifiDirectModule.becomeClient()
        }
    }
}
