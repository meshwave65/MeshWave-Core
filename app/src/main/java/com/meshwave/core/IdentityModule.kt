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
            logToUi("[Id] Identidade carregada: ${myNodeCPA?.username}")
        } else {
            logToUi("[Id] Nenhuma identidade local. Aguardando geohash.")
            myNodeCPA = identityManager.createNewIdentity(LocationModule.GEOHASH_FAIL_AREA)
            logToUi("[Id] Identidade provisória criada: ${myNodeCPA!!.username}")
        }
        uiHandler.obtainMessage(AppConstants.IDENTITY_UPDATE, myNodeCPA).sendToTarget()
    }

    fun updateCpaIfNeeded(areaGeohash: String) {
        myNodeCPA?.let {
            if (it.cpaGeohash == LocationModule.GEOHASH_FAIL_AREA) {
                logToUi("[Id] CPA de origem atualizado para: $areaGeohash")
                myNodeCPA = it.copy(cpaGeohash = areaGeohash)
                identityManager.saveNodeCPA(myNodeCPA!!)
                uiHandler.obtainMessage(AppConstants.IDENTITY_UPDATE, myNodeCPA).sendToTarget()
            }
        }
    }

    fun getCurrentCPA(): NodeCPA? {
        return myNodeCPA
    }

    fun updateCurrentLocation(newGeohash: String) {
        myNodeCPA?.let {
            if (it.currentClaGeohash != newGeohash) {
                myNodeCPA = it.copy(currentClaGeohash = newGeohash)
                uiHandler.obtainMessage(AppConstants.IDENTITY_UPDATE, myNodeCPA).sendToTarget()
            }
        }
    }

    private fun logToUi(logMessage: String) {
        Log.d("IdentityModule", logMessage)
        uiHandler.obtainMessage(AppConstants.LOG_UPDATE, logMessage).sendToTarget()
    }
}
