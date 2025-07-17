# Como Contribuir para o MeshWave-Core

Este documento descreve o processo e o fluxo de trabalho que todos os desenvolvedores devem seguir ao contribuir para o projeto MeshWave-Core. A adesão a estas diretrizes é obrigatória e garante a qualidade, estabilidade e rastreabilidade do nosso trabalho.

Nossa filosofia é governada pelo princípio de **"Nenhum Esqueleto no Armário"**. Isso se aplica tanto ao código quanto ao processo.

## O Ciclo de Desenvolvimento de Versão

Cada nova versão (ex: de `v0.1.1` para `v0.1.2`) deve seguir este roteiro meticuloso e metódico.

### **Passo 1: Definição e Desenvolvimento**

1.  **Criar uma Branch:** Todo o trabalho deve ser feito em uma *feature branch* separada, criada a partir da `main`. O nome da branch deve ser descritivo.
    ```bash
    # Exemplo para implementar o módulo de identidade
    git checkout -b feature/identity-module
    ```
2.  **Desenvolver a Funcionalidade:** Implemente o código necessário para atingir o objetivo da versão. Siga as diretrizes de arquitetura (módulos encapsulados, resiliência por design, etc.) definidas no `README.md`.

### **Passo 2: Verificação de Qualidade (Pré-Compilação)**

Antes mesmo de gerar um APK para teste, a qualidade do código deve ser verificada.

1.  **Análise Estática (Lint):** Execute o inspetor de qualidade do Lint para encontrar problemas potenciais.
    ```bash
    ./gradlew lintDebug
    ```
2.  **Tratamento de Issues:** Analise o relatório gerado em `app/build/reports/lint-results-debug.html`. **Todos os erros (`errors`) e avisos (`warnings`) devem ser corrigidos.** Código obsoleto (`deprecated`) deve ser refatorado. O build do Lint deve passar sem falhas.

### **Passo 3: Teste em Emulador (Opcional, mas Recomendado)**

Para uma verificação rápida antes de mover para hardware real, pode-se usar o emulador.

1.  **Executar no Emulador:** Inicie o aplicativo no emulador do Android Studio.
2.  **Analisar o Logcat:** Observe o Logcat em busca de erros óbvios em tempo de execução (crashes, exceções) que possam ser corrigidos antes do teste em dispositivo real.

### **Passo 4: Geração e Teste do APK em Dispositivo Real**

Este é o teste de validação final.

1.  **Gerar o APK:** Compile o APK de debug.
    ```bash
    ./gradlew assembleDebug
    ```
2.  **Instalar:** Instale o arquivo `app-debug.apk` no(s) dispositivo(s) de teste.
3.  **Executar Testes:** Realize os testes funcionais para validar a nova funcionalidade e garantir que nenhuma funcionalidade antiga foi quebrada (teste de regressão).

### **Passo 5: Relatório de Teste**

Todo teste em dispositivo real deve ser documentado.

1.  **Formato do Relatório:** O relatório deve ser claro e conciso. O título deve seguir o padrão:
    `Teste APK v[versão]-[sufixo]` (ex: `Teste APK v0.1.2-alpha`).
2.  **Conteúdo:**
    *   Liste os pontos de sucesso (ex: "1. Módulo de Identidade gera DID com sucesso.").
    *   Liste quaisquer falhas ou comportamentos inesperados.
    *   **Forneça imagens (screenshots)** da tela para ilustrar tanto os sucessos quanto os problemas. As imagens são fundamentais para um diagnóstico rápido.

### **Passo 6: Finalização e Commit**

Após a validação bem-sucedida, o ciclo é fechado.

1.  **Incrementar a Versão:** Abra o arquivo `gradle.properties` e atualize a versão para o próximo incremento.
    *   `APP_VERSION_PATCH` ou `APP_VERSION_MINOR` é incrementado.
    *   `APP_VERSION_BUILD` é **sempre** incrementado.
2.  **Fazer o Commit:** Faça o commit do trabalho na sua feature branch com uma mensagem clara e descritiva.
    ```bash
    git add .
    git commit -m "feat(escopo): Descreve a funcionalidade (vX.Y.Z-alpha)"
    ```
3.  **Enviar e Integrar:** Envie a branch para o GitHub (`git push`) e abra um Pull Request para fazer o merge para a `main`.

Este processo garante que cada versão adicionada à `main` seja funcional, testada, documentada e de alta qualidade.
