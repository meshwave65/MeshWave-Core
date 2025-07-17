// MainActivity.kt
// VERSÃO COM INTEGRAÇÃO DO LOCATIONMODULE

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

    // --- INÍCIO DAS NOVAS ADIÇÕES ---

    // Código de requisição para o pedido de permissões. Pode ser qualquer número.
    private val PERMISSIONS_REQUEST_CODE = 100

    // Declaração da nossa instância do LocationModule.
    // 'lateinit' significa que prometemos inicializá-la mais tarde.
    private lateinit var locationModule: LocationModule

    // Declaração do nosso StatusFragment para que possamos nos comunicar com ele.
    private lateinit var statusFragment: StatusFragment

    // Handler: O "carteiro" que entrega mensagens dos módulos para a UI.
    // Ele garante que qualquer atualização na tela aconteça na thread principal.
    private val uiHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // Verifica se o fragmento ainda está na tela antes de tentar atualizá-lo.
            if (!::statusFragment.isInitialized || !statusFragment.isAdded) return

            when (msg.what) {
                // Se a mensagem for do tipo GEOHASH_UPDATE...
                LocationModule.GEOHASH_UPDATE -> {
                    val geohash = msg.obj as String
                    statusFragment.updateGeohash(geohash)
                    statusFragment.addLog("[Main] Geohash recebido: $geohash")
                }
                // Se a mensagem for do tipo LOG_UPDATE...
                LocationModule.LOG_UPDATE -> {
                    val logMessage = msg.obj as String
                    statusFragment.addLog(logMessage)
                }
            }
        }
    }

    // --- FIM DAS NOVAS ADIÇÕES ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupCustomActionBar()

        if (savedInstanceState == null) {
            // Criamos a instância do fragmento e a guardamos na nossa variável.
            statusFragment = StatusFragment()
            loadFragment(statusFragment)
        }

        // Em vez de iniciar os módulos diretamente, primeiro pedimos as permissões.
        checkAndRequestPermissions()
    }

    // --- INÍCIO DOS NOVOS MÉTODOS ---

    private fun checkAndRequestPermissions() {
        // Lista de permissões que nosso app precisa. Por enquanto, apenas localização.
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Verifica quais permissões da lista nós ainda não temos.
        val permissionsToRequest = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Se faltar alguma permissão, pedimos ao usuário.
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        } else {
            // Se já temos todas as permissões, podemos iniciar os módulos imediatamente.
            Log.d("MainActivity", "Todas as permissões já foram concedidas.")
            initializeModules()
        }
    }

    // Este método é chamado pelo Android após o usuário responder ao pedido de permissão.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Verifica se a primeira (e única) permissão que pedimos foi concedida.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Permissão de localização concedida pelo usuário.")
                initializeModules()
            } else {
                Log.w("MainActivity", "Permissão de localização negada pelo usuário.")
                // Se a permissão for negada, o próprio LocationModule enviará o 'g9fail_permission'.
                initializeModules()
            }
        }
    }

    // O método que realmente cria e inicia nossos módulos.
    private fun initializeModules() {
        // A verificação '::locationModule.isInitialized' previne que o módulo seja criado mais de uma vez.
        if (::locationModule.isInitialized) return

        Log.d("MainActivity", "Inicializando os módulos...")
        // Criamos a instância do LocationModule, passando o contexto e o nosso "carteiro" (handler).
        locationModule = LocationModule(this, uiHandler)
        // Damos a ordem para o módulo começar a trabalhar.
        locationModule.start()
    }

    // --- FIM DOS NOVOS MÉTODOS ---

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
