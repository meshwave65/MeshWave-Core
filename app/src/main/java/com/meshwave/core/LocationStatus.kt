package com.meshwave.core

import androidx.compose.ui.graphics.Color

/**
 * O estado de saúde de uma informação de localização, baseado na sua idade e validade.
 * Implementa a lógica de múltiplos TTLs.
 */
enum class LocationStatus {
    // Verde: Dado fresco e válido.
    UPDATED,

    // Amarelo: Dado válido, mas antigo (passou do TTL1).
    STALE,

    // Cinza: Dado muito antigo, apenas histórico (passou do TTL2).
    UNRELIABLE,

    // Vermelho: O sistema não tem uma localização válida.
    // Usado apenas no início ou em falha crônica.
    FAILED;

    /**
     * Retorna a cor correspondente a cada status para ser usada na UI.
     */
    fun toColor(): Color {
        return when (this) {
            UPDATED -> Color(0xFF4CAF50)    // Verde
            STALE -> Color(0xFFFFC107)      // Amarelo/Âmbar
            UNRELIABLE -> Color(0xFF9E9E9E) // Cinza
            FAILED -> Color(0xFFF44336)     // Vermelho
        }
    }
}
