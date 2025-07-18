# Arquitetura de Cache: Modelo CPA e CLA (v1.0)

Este documento descreve o modelo de identidade e localização da rede MeshWave, baseado nos conceitos de Cache Primário de Atualização (CPA) и Cache de Localização Atual (CLA).

## 1. Definições Fundamentais

-   **Nó:** Um dispositivo individual na rede MeshWave.
-   **Geohash:** O sistema de grade geográfica usado para endereçamento.
    -   **Nível 10:** Usado para a localização precisa de um nó (`nodeGeohash`). Representa uma área de ~1.2m x 0.6m.
    -   **Nível 6:** Usado para definir a "vizinhança" ou a área de uma CLA/CPA (`areaGeohash`). Representa uma área de ~1.2km x 0.6km.

## 2. Cache Primário de Atualização (CPA)

-   **Função:** O "Cartório de Registro" ou a "Certidão de Nascimento" de um nó. É um registro permanente que vincula a identidade de hardware de um nó à sua "CPA de Origem".
-   **Vínculo:** Quando um nó é ativado pela primeira vez, ele é permanentemente registrado na CPA correspondente à sua localização de nascimento (`areaGeohash`).
-   **Propósito:** Manter um ponteiro para a localização atual (`currentClaGeohash`) de um nó, permitindo que ele seja encontrado em qualquer lugar da rede.

### Estrutura de Dados do CPA (`NodeCPA`)

O objeto CPA, gerado e mantido pelo `IdentityModule`, contém:

-   `did` (String): Identificador único e imutável do dispositivo (baseado no `ANDROID_ID`).
-   `username` (String): Nome de usuário aleatório (`User-XXX`) para exibição.
-   `cpaGeohash` (String): O `areaGeohash` (nível 6) de nascimento do nó. Imutável.
-   `creationTimestamp` (Long): A data/hora de "nascimento" do nó na rede. Imutável.
-   `currentClaGeohash` (String): O `areaGeohash` (nível 6) onde o nó está atualmente. Mutável.
-   `status` (String): Um código de caractere único que representa o estado do nó na rede.

## 3. Cache de Localização Atual (CLA)

-   **Função:** O "Diretório Telefônico Local". Um cache dinâmico e volátil.
-   **Vínculo:** Representa a área geográfica (`areaGeohash` de nível 6) onde um nó está fisicamente presente no momento.
-   **Propósito:** Manter a lista de todos os nós atualmente naquela área, permitindo a descoberta e a comunicação local.

## 4. Fluxo de Vida e Descoberta de um Nó

1.  **Nascimento:** Um nó é ativado. O `LocationModule` fornece o `areaGeohash` para o `IdentityModule`, que cria o objeto `NodeCPA`, definindo `cpaGeohash` e `currentClaGeohash` com o mesmo valor.
2.  **Migração:** O nó se move para uma nova área. Ele detecta a mudança no seu `areaGeohash`.
3.  **Atualização:** O nó envia uma mensagem para sua CPA de Origem, atualizando o campo `currentClaGeohash` com sua nova localização.
4.  **Descoberta Remota:** Para encontrar um nó, um solicitante primeiro consulta a CPA de Origem do nó alvo. A CPA responde com o `currentClaGeohash`, direcionando a busca para a "vizinhança" correta.

## 5. Sistema de Status do Nó

O status de um nó é representado por um único caractere alfanumérico.

-   `1`: Ativo (Padrão na ativação)
-   `0`: Desatualizado (Sem atualização de cache por um tempo)
-   `2`: Inativo (Desatualizado por muito tempo)
-   `3`: Restrito
-   `4`: Bloqueado
-   `5`: Banido
-   `x, y, w, z`: Reservado para uso interno do sistema.

A lógica de transição de status será implementada em fases futuras.

