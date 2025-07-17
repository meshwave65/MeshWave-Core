// MainActivity.kt
// VERSÃO FINAL COM ACTIONBAR PERSONALIZADA

package com.meshwave.core

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Configura nossa ActionBar personalizada
        setupCustomActionBar()

        // 2. Carrega o StatusFragment no contêiner
        if (savedInstanceState == null) {
            val statusFragment = StatusFragment()
            loadFragment(statusFragment)
        }
    }

    private fun setupCustomActionBar() {
        // Garante que a ActionBar exista
        supportActionBar?.apply {
            // Habilita o modo de exibição personalizado
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            // Define nosso layout XML como a view personalizada
            setCustomView(R.layout.action_bar_custom)

            // Encontra o TextView da versão dentro da nossa view personalizada
            val versionTextView = customView.findViewById<TextView>(R.id.textViewVersion)

            // Tenta obter a versão do app e exibi-la
            try {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val version = "v" + pInfo.versionName
                versionTextView.text = version
                Log.d("MainActivity", "Versão do App: $version")
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
