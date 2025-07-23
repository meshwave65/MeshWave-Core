package com.meshwave.core

import androidx.compose.ui.graphics.Color

/**
 * O estado de saúde de uma informação de localização, baseado em sua idade ou validade.
 */
enum class LocationStatus {
    UPDATED, // Verde: Dado fresco e válido.
    STALE,   // Amarelo: Dado válido, mas antigo.
    FAILED;  // Vermelho: Dado inválido, de falha ou expirado.

    /**
     * Retorna a cor correspondente a cada status para ser usada na UI.
     */
    fun toColor(): Color {
        return when (this) {
            UPDATED -> Color(0xFF4CAF50) // Verde
            STALE -> Color(0xFFFFC107)   // Amarelo
            FAILED -> Color(0xFFF44336)  // Vermelho
        }
    }
}
