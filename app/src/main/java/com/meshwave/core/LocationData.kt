package com.meshwave.core

/**
 * Uma classe de dados para encapsular os resultados da geolocalização.
 *
 * @param nodeGeohash O Geohash de alta precisão (nível 9) para a localização exata do nó.
 * @param areaGeohash O Geohash de média precisão (nível 6) para identificar a CLA/CPA.
 */
data class LocationData(val nodeGeohash: String, val areaGeohash: String)
