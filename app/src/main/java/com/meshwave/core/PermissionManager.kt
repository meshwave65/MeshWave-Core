package com.meshwave.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Classe dedicada para gerenciar a lógica de permissões.
 * Agora ela verifica o status antes de pedir.
 */
class PermissionManager(private val activity: ComponentActivity) {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var onResultCallback: ((Boolean) -> Unit)? = null

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    init {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            onResultCallback?.invoke(allGranted)
        }
    }

    /**
     * Verifica se a permissão já foi concedida.
     */
    fun hasLocationPermission(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Pede a permissão ao usuário.
     */
    fun requestLocationPermission(onResult: (Boolean) -> Unit) {
        this.onResultCallback = onResult
        permissionLauncher.launch(requiredPermissions)
    }
}
