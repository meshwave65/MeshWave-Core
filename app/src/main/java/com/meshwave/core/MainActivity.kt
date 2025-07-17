// MainActivity.kt
// VERSÃO COM ORQUESTRAÇÃO CORRIGIDA

package com.meshwave.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var permissionsRequested = false

    private lateinit var identityModule: IdentityModule
    private lateinit var locationModule: LocationModule
    private lateinit var statusFragment: StatusFragment

    private val uiHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (!::statusFragment.isInitialized || !statusFragment.isAdded) return

            when (msg.what) {
                LocationModule.GEOHASH_UPDATE -> {
                    val locationData = msg.obj as LocationData
                    statusFragment.updateGeohash(locationData.nodeGeohash) // Atualiza a localização precisa
                    statusFragment.addLog("[Main] Geohash Nó(9) recebido: ${locationData.nodeGeohash}")
                    statusFragment.addLog("[Main] Geohash Área(6) recebido: ${locationData.areaGeohash}")

                    // ✅ CORREÇÃO CRÍTICA: Conectando os módulos
                    // Passa o Geohash da área para o IdentityModule iniciar a geração do CPA.
                    if (::identityModule.isInitialized) {
                        identityModule.receiveAreaGeohash(locationData.areaGeohash)
                    }
                }
                IdentityModule.CPA_UPDATE -> {
                    val cpa = msg.obj as NodeCPA
                    statusFragment.addLog("[Main] CPA Recebido.")
                    // Atualiza a UI com os dados do CPA
                    statusFragment.updateCpaOrigin(cpa.cpaGeohash) // Antigo DID, agora CPA de Origem
                    statusFragment.updateUsername(cpa.username)
                    statusFragment.updateLocalCache(cpa.toString()) // Exibe o CPA completo no campo de cache
                }
                LocationModule.LOG_UPDATE -> {
                    if (msg.obj is String) {
                        val logMessage = msg.obj as String
                        statusFragment.addLog(logMessage)
                    }
                }
            }
        }
    }

    // ... (Restante do código da MainActivity permanece o mesmo)
    // ... (onCreate, onResume, checkAndRequestPermissions, etc.)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupCustomActionBar()
        if (savedInstanceState == null) {
            statusFragment = StatusFragment()
            loadFragment(statusFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!permissionsRequested) {
            checkAndRequestPermissions()
        }
    }

    private fun checkAndRequestPermissions() {
        permissionsRequested = true
        val requiredPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionsToRequest = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        } else {
            Log.d("MainActivity", "Todas as permissões já foram concedidas.")
            initializeModules()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            initializeModules()
        }
    }

    private fun initializeModules() {
        if (::locationModule.isInitialized) return
        Log.d("MainActivity", "Inicializando os módulos...")
        locationModule = LocationModule(this, uiHandler)
        locationModule.start()
        identityModule = IdentityModule(this, uiHandler)
        identityModule.start()
    }

    private fun setupCustomActionBar() {
        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.action_bar_custom)
            val versionTextView = customView.findViewById<TextView>(R.id.textViewVersion)
            try {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val version = "v" + pInfo.versionName
                versionTextView.text = version
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("MainActivity", "Erro ao obter a versão do app", e)
                versionTextView.text = "v_error"
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
