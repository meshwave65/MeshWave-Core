---
task_id: "001"
title: "Investigação do Mecanismo de Descoberta de Pares na LAN do Projeto Briar"
status: open
assigned_to: "Especialista-Conectividade"
created_at: "2025-07-25T15:00:00Z"
related_report: null
---

### Objetivo
Identificar e detalhar o método exato que o Briar utiliza para que dispositivos se encontrem em uma mesma rede local (Wi-Fi).

### Instruções
- **Tarefa Principal:** Determine o protocolo de descoberta utilizado. É Multicast DNS (mDNS), UDP Broadcast, ou um mecanismo customizado?
- **Localização no Código:** Identifique as classes e pacotes Java/Kotlin que implementam essa lógica. Procure por termos como `Discovery`, `Broadcast`, `Multicast`, `Socket`, `DatagramPacket` dentro de pacotes como `briar.plugins.lan`.
- **Formato da Mensagem:** Qual é o conteúdo da "mensagem de anúncio" que é enviada pela rede? Ela contém um ID do dispositivo, porta para conexão, ou outras informações?

### Resultado Esperado
Um relatório conciso explicando o mecanismo, listando as classes-chave envolvidas e descrevendo o formato da mensagem de descoberta.



## Relatório de Conclusão

Esta é uma simulação de relatório para a tarefa {task_filename}.

## Relatório de Conclusão

Esta é uma simulação de relatório para a tarefa TASK-001-discovery-mechanism.md.