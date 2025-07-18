// Local do arquivo: app/src/main/java/com/meshwave/core/MainActivity.kt
package com.meshwave.core

// --- IMPORTAÇÕES NECESSÁRIAS ---
// Adicionadas para resolver as referências que estavam faltando.
import com.meshwave.core.LocationModule
import com.meshwave.core.IdentityModule
import com.meshwave.core.WiFiDirectModule
import com.meshwave.core.LocationData
import com.meshwave.core.NodeCPA
// ---------------------------------

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

    // Constantes para o Handler da UI
    companion object {
        const val IDENTITY_UPDATE = 1
        const val LOCATION_UPDATE = 2
        const val LOG_UPDATE = 3
        const val WIFI_STATUS_UPDATE = 10
    }

    // Constante para o pedido de permissões
    private val PERMISSIONS_REQUEST_CODE = 101

    // Módulos do nosso sistema
    private lateinit var locationModule: LocationModule
    private lateinit var identityModule: IdentityModule
    private lateinit var wifiDirectModule: WiFiDirectModule

    // Usando lateinit para garantir que o fragmento não seja nulo após o onCreate
    private lateinit var statusFragment: StatusFragment

    // Variáveis para guardar os dados recebidos
    private var lastLocationData: LocationData? = null

    // Handler para comunicação entre Módulos e a UI Thread
    private val uiHandler = Handler(Looper.getMainLooper()) { msg ->
        // Garante que o fragmento esteja pronto antes de processar mensagens
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
                identityModule.generateCpa(locationData.areaGeohash)
                true
            }
            IDENTITY_UPDATE -> {
                val cpa = msg.obj as NodeCPA
                statusFragment.updateCpaData(cpa)
                addLogToFragment("[Main] CPA Recebido.")
                true
            }
            LOG_UPDATE -> {
                val logMessage = msg.obj as String
                addLogToFragment(logMessage)
                true
            }
            WIFI_STATUS_UPDATE -> {
                val status = msg.obj as String
                statusFragment.updateWifiStatus(status)
                // Não precisa logar aqui, pois o próprio módulo já loga e a UI é atualizada
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
            // Recupera a instância do fragmento após recriação da activity
            statusFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as StatusFragment
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    override fun onPause() {
        super.onPause()
        // Para o módulo Wi-Fi para economizar bateria quando o app está em segundo plano
        if (this::wifiDirectModule.isInitialized) {
            wifiDirectModule.stop()
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
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        } else {
            Log.d("MainActivity", "Todas as permissões já foram concedidas.")
            initializeModules()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("MainActivity", "Todas as permissões foram concedidas pelo usuário.")
                initializeModules()
            } else {
                Log.w("MainActivity", "Algumas permissões foram negadas.")
                addLogToFragment("[Main] Permissões de rede/localização negadas.")
            }
        }
    }

    private fun initializeModules() {
        // Evita reinicializar os módulos se eles já existem (ex: ao voltar para o app)
        if (this::locationModule.isInitialized) {
            // Se os módulos existem, apenas reinicia o Wi-Fi que foi parado no onPause
            if (this::wifiDirectModule.isInitialized) {
                wifiDirectModule.start()
            }
            return
        }

        Log.d("MainActivity", "Inicializando os módulos...")
        addLogToFragment("[Main] Módulos sendo inicializados...")

        // Cria as instâncias dos módulos
        locationModule = LocationModule(this, uiHandler)
        identityModule = IdentityModule(this, uiHandler)
        wifiDirectModule = WiFiDirectModule(this, uiHandler)

        // Inicia os módulos
        locationModule.start()
        identityModule.start()
        wifiDirectModule.start()
    }

    private fun addLogToFragment(message: String) {
        if (this::statusFragment.isInitialized && statusFragment.isAdded) {
            statusFragment.addLog(message)
        } else {
            Log.w("MainActivity", "StatusFragment não está pronto para receber log: $message")
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
                versionTextView?.text = "v_error"
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
