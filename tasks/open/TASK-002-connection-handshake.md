---
task_id: "002"
title: "Análise do Processo de Estabelecimento de Conexão no Projeto Briar"
status: open
assigned_to: "Especialista-P2P"
created_at: "2025-07-25T15:00:00Z"
related_report: null
---

### Objetivo
Mapear o fluxo de eventos desde a descoberta de um par até o estabelecimento de uma conexão TCP/IP direta e funcional.

### Instruções
Assuma que um dispositivo A já descobriu a presença de um dispositivo B na rede e agora precisa se conectar a ele.
- **Tarefa Principal:** Descreva o "handshake" da conexão. Como a troca de endereço IP e porta acontece? O dispositivo que descobre inicia a conexão, ou o dispositivo que anuncia ouve por conexões?
- **Localização no Código:** Identifique as classes Java/Kotlin responsáveis por:
    1. Abrir um `ServerSocket` para aguardar conexões de entrada.
    2. Criar um `Socket` de cliente para iniciar uma conexão com um par descoberto.
- **Fluxo de Dados:** Uma vez que o `Socket` está estabelecido, qual classe gerencia o fluxo de entrada e saída (`InputStream`/`OutputStream`)?

### Resultado Esperado
Um diagrama de sequência simples (pode ser em texto) mostrando o fluxo do handshake e uma lista das classes-chave responsáveis por criar e gerenciar os sockets de conexão.

