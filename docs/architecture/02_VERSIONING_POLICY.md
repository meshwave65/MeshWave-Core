# Política de Versionamento do MeshWave-Core (v1.0)

Este documento define a estratégia de versionamento para o projeto `MeshWave-Core`, garantindo um histórico claro, consistente e compatível com as práticas da indústria e as exigências de lojas de aplicativos.

## 1. Estrutura de Versão

O projeto utiliza duas versões paralelas, gerenciadas pelo `gradle.properties` e aplicadas pelo `app/build.gradle.kts`:

### 1.1. `versionName` (A Versão para Humanos)

-   **Formato:** Segue o padrão de **Versionamento Semântico (SemVer)**: `MAJOR.MINOR.PATCH-SUFFIX`.
    -   `v0.1.5-alpha`
-   **Propósito:** Comunicar o estágio e a magnitude das mudanças para os desenvolvedores e testadores.
-   **Regras de Incremento:**
    -   **`MAJOR` (0.x.x):** Incrementado para mudanças arquiteturais massivas que quebram a compatibilidade (ex: mudança de Fase no roadmap). A versão `1.0.0` marcará o primeiro lançamento público estável.
    -   **`MINOR` (x.1.x):** Incrementado para a introdução de novas funcionalidades ou "Fases de Reanimação" significativas (ex: passar da Fase de Identidade para a Fase de Rede). Reseta o `PATCH` para 0.
    -   **`PATCH` (x.x.5):** Incrementado para cada novo APK gerado que contém correções de bugs ou pequenas melhorias em uma funcionalidade existente.
    -   **`SUFFIX` (alpha, beta, rc):** Indica o estágio de desenvolvimento da versão.

### 1.2. `versionCode` (A Versão para a Máquina)

-   **Formato:** Um único número inteiro (`Integer`).
    -   `8`
-   **Propósito:** Usado internamente pelo Android e pela Google Play Store para determinar se uma versão é mais nova que outra.
-   **Regra de Incremento:**
    -   O `versionCode` é um **contador que nunca volta para trás**.
    -   Ele **DEVE** ser incrementado em, no mínimo, `+1` para **CADA** novo APK que é gerado para instalação, seja para teste ou para lançamento.

## 2. Exemplo de Fluxo de Trabalho

1.  **Início do Trabalho na v0.2.0:**
    -   `versionName` muda para `0.2.0-alpha`.
    -   `versionCode` é incrementado para `9`.
2.  **Correção de um bug na v0.2.0:**
    -   `versionName` permanece `0.2.0-alpha`.
    -   `versionCode` é incrementado para `10`.
3.  **Adição de uma pequena melhoria na v0.2.0:**
    -   `versionName` muda para `0.2.1-alpha`.
    -   `versionCode` é incrementado para `11`.

Esta política garante clareza no desenvolvimento e conformidade com os requisitos de distribuição.

