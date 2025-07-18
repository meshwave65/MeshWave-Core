// Local: app/src/main/java/com/meshwave/core/IdentityModule.kt
package com.meshwave.core

import android.content.Context
import android.os.Handler
import android.util.Log

class IdentityModule(private val context: Context, private val uiHandler: Handler) {

    private val identityManager = IdentityManager(context)
    private var myNodeCPA: NodeCPA? = null

    fun start() {
        myNodeCPA = identityManager.loadNodeCPA()
        if (myNodeCPA != null) {
            Log.d("IdentityModule", "Identidade carregada: ${myNodeCPA?.username}")
            uiHandler.obtainMessage(MainActivity.LOG_UPDATE, "[Main] CPA Pronto.").sendToTarget()
            uiHandler.obtainMessage(MainActivity.IDENTITY_UPDATE, myNodeCPA).sendToTarget()
        } else {
            Log.d("IdentityModule", "Nenhuma identidade local. Aguardando geohash.")
            uiHandler.obtainMessage(MainActivity.LOG_UPDATE, "[Id] Aguardando Geohash da Área para gerar CPA...").sendToTarget()
        }
    }

    // MÉTODO CORRIGIDO E PRESENTE
    fun generateCpaIfNeeded(areaGeohash: String) {
        if (myNodeCPA == null) {
            myNodeCPA = identityManager.createNewIdentity(areaGeohash)
            Log.d("IdentityModule", "Nova identidade criada: ${myNodeCPA!!.username}")
            uiHandler.obtainMessage(MainActivity.LOG_UPDATE, "[Id] DID gerado.").sendToTarget()
            uiHandler.obtainMessage(MainActivity.LOG_UPDATE, "[Id] Username gerado: ${myNodeCPA!!.username}").sendToTarget()
            uiHandler.obtainMessage(MainActivity.LOG_UPDATE, "[Id] CPA montado com sucesso.").sendToTarget()
            uiHandler.obtainMessage(MainActivity.IDENTITY_UPDATE, myNodeCPA).sendToTarget()
        }
    }

    fun getCurrentCPA(): NodeCPA? {
        return myNodeCPA
    }

    fun updateCurrentLocation(newGeohash: String) {
        myNodeCPA?.let {
            if (it.currentClaGeohash != newGeohash) {
                myNodeCPA = it.copy(currentClaGeohash = newGeohash)
                // Opcional: notificar a UI sobre a mudança de CLA se necessário no futuro
            }
        }
    }
}
