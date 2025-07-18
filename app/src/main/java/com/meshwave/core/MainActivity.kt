// Local: app/src/main/java/com/meshwave/core/MainActivity.kt
// ... (imports e outras seções permanecem os mesmos)

class MainActivity : AppCompatActivity() {

    // ... (companion object e propriedades permanecem os mesmos)

    // ... (uiHandler permanece o mesmo)

    override fun onCreate(savedInstanceState: Bundle?) {
        // ... (código existente)
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions() // Isso já chama initializeModules, que chama o start()
    }

    override fun onPause() {
        super.onPause()
        // Parar os módulos para economizar bateria
        if (this::wifiDirectModule.isInitialized) {
            wifiDirectModule.stop()
        }
        if (this::locationModule.isInitialized) {
            locationModule.stop() // ADICIONAR ESTA LINHA
        }
    }

    // ... (checkAndRequestPermissions e onRequestPermissionsResult permanecem os mesmos)

    private fun initializeModules() {
        if (this::locationModule.isInitialized) {
            // Se os módulos já existem, apenas reinicie-os
            locationModule.start()
            wifiDirectModule.start()
            return
        }

        Log.d("MainActivity", "Inicializando os módulos...")
        addLogToFragment("[Main] Módulos sendo inicializados...")

        locationModule = LocationModule(this, uiHandler)
        identityModule = IdentityModule(this, uiHandler)
        wifiDirectModule = WiFiDirectModule(this, uiHandler, identityModule)

        // Inicia todos os módulos
        locationModule.start()
        identityModule.start()
        wifiDirectModule.start()
    }

    // ... (resto do código permanece o mesmo)
}
