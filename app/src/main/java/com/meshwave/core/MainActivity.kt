package com.meshwave.core

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Carrega o nosso StatusFragment assim que a activity é criada.
        if (savedInstanceState == null) {
            loadFragment(StatusFragment())
        }

        // Exemplo de como obter a versão do app sem BuildConfig
        logAppVersion()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun logAppVersion() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = "v" + pInfo.versionName
            Log.d("MainActivity", "MeshWave-Core versão: $version")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("MainActivity", "Erro ao obter a versão do app", e)
        }
    }
}

