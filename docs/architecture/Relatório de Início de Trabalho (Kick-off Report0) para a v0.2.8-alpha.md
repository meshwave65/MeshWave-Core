Relatório de Início de Trabalho (Kick-off Report) para a v0.2.8-alpha
Este é um relatório formal que você pode usar para documentar o início do sprint de desenvolvimento para esta nova versão.
Projeto: MeshWave-Core
Versão Alvo: v0.2.8-alpha
Data de Início: 18 de julho de 2025
1. Visão Geral e Objetivo
Esta nova fase de desenvolvimento, designada como v0.2.8-alpha, marca a transição do projeto de um protótipo de prova de conceito para a primeira implementação da arquitetura de rede autônoma. O objetivo principal é eliminar toda a necessidade de intervenção manual do usuário para descoberta, conexão e sincronização de dados, implementando a lógica de Consciência Situacional Local (CLA).
2. Estado de Partida (Baseline v0.2.7-alpha)
A versão anterior serviu como base de testes e validou a camada de transporte P2P. Suas características principais são:
Controle de conexão manual.
Troca de dados de nó único.
Arquitetura monolítica centrada na MainActivity.
Presença de bugs de gerenciamento de socket (EADDRINUSE).
3. Escopo e Tarefas para a v0.2.8-alpha
O trabalho será dividido nas seguintes frentes de implementação, que devem ser concluídas para que a versão seja considerada finalizada:
Refatoração da Arquitetura de Dados:
Tarefa: Descontinuar a classe NodeCPA legada.
Tarefa: Criar e implementar as novas classes NodeProfile.kt (passaporte do nó) e SituationalCache.kt (representação da CLA).
Implementação da Máquina de Estados de Rede:
Tarefa: Refatorar o WiFiDirectModule.kt para operar com uma máquina de estados autônoma.
Tarefa: Implementar o ciclo "Sempre Servidor -> Busca -> Transição para Cliente -> Sincroniza -> Retorno para Servidor".
Tarefa: Garantir o gerenciamento robusto do ServerSocket para corrigir definitivamente o erro EADDRINUSE.
Implementação da Lógica de Fusão de Cache:
Tarefa: Refatorar o IdentityModule.kt para gerenciar o SituationalCache.
Tarefa: Implementar o método mergeSituationalCache() que compara e agrega informações de nós recebidas de um par.
Desacoplamento da Lógica de Negócio:
Tarefa: Criar a classe CoreManager.kt para orquestrar os diferentes módulos (Identity, Location, WiFi).
Tarefa: Simplificar a MainActivity, que passará a apenas gerenciar a UI e delegar todas as ações de negócio para o CoreManager.
4. Critérios de Conclusão ("Definition of Done")
A versão v0.2.8-alpha será considerada concluída e pronta para a próxima fase quando:
Dois dispositivos, com o aplicativo em execução, se descobrirem e se conectarem automaticamente.
A troca do SituationalCache (CLA) ocorrer com sucesso.
Ambos os dispositivos exibirem em sua UI a lista de nós combinada, provando que a fusão de cache foi bem-sucedida.
O ciclo de conexão se encerrar de forma limpa, com ambos os nós retornando ao seu estado operacional padrão, prontos para futuras interações.
