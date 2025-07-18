Relatório de Estado e Planejamento do Projeto: MeshWave-Core
Versão do Documento: 1.0
Data: 18 de julho de 2025
Autor: Manus AI (em colaboração com o Arquiteto do Projeto)
1. Sumário Executivo
Este relatório consolida o progresso significativo alcançado na definição da arquitetura fundamental do projeto MeshWave-Core. Após uma análise detalhada dos requisitos, superamos a fase inicial de depuração de conexão (EADDRINUSE) e estabelecemos uma visão arquitetural clara e robusta. O projeto evoluiu de um simples aplicativo de troca de dados P2P para a fundação de um sistema de identidade distribuída e consciência situacional, baseado nos conceitos hierárquicos de CPA (Cache Primário de Atualização) e CLA (Cache de Localização Atual).
O foco atual é a implementação da Camada 1 desta arquitetura: a comunicação e sincronização de cache totalmente automática dentro de uma única CLA.
2. Arquitetura Consolidada
A arquitetura do sistema foi definida com base nos seguintes pilares conceituais:
Identidade Soberana (CPA de Origem): Cada nó possui uma "certidão de nascimento" digital, contendo identificadores imutáveis (did, cpaGeohash de nascimento, creationTimestamp). Este registro é a âncora de identidade do nó em toda a rede.
Consciência Situacional Local (CLA): Representa a "vizinhança" geográfica (areaGeohash nível 6) onde um nó está atualmente. Cada nó mantém um cache operacional (SituationalCache) com metadados detalhados de todos os outros nós conhecidos dentro da mesma CLA. A fusão desses caches entre os nós é o principal mecanismo de disseminação de informação local.
Modelo de Operação de Rede: O comportamento padrão de um nó é atuar como Servidor (Group Owner) para garantir sua detectabilidade. Periodicamente, ele busca por outros servidores em sua vizinhança. Ao encontrar um, ele inicia um ciclo de interação: transiciona temporariamente para o modo Cliente, conecta-se, sincroniza os caches (CLAs) e, em seguida, retorna ao seu estado padrão de Servidor.
3. Estado Atual da Implementação
Com base na arquitetura definida, o trabalho de codificação está focado na construção da fundação da Camada 1.
Estruturas de Dados:
NodeProfile.kt: Implementado. Representa o "passaporte" completo de um nó, alinhado com a visão de dados imutáveis e mutáveis.
SituationalCache.kt: Implementado. Representa o objeto da CLA que é trocado entre os nós.
Módulos de Lógica:
IdentityModule.kt: Em processo de refatoração para atuar como o guardião da identidade do nó e do seu SituationalCache local.
WiFiDirectModule.kt: Em processo de refatoração para implementar o ciclo de operação de rede "Sempre Servidor", com a capacidade de transição para cliente para fins de sincronização.
Interface do Usuário (UI):
StatusFragment.kt: A UI foi simplificada, removendo controles manuais e adicionando campos para exibir o estado da rede e o conteúdo do cache local (CLA) de forma automática.
4. Próximos Passos e Plano de Ação Imediato
O foco é finalizar e estabilizar a Camada 1.
Finalizar a Refatoração do Código:
LocationModule: Ajustar para que, na primeira localização bem-sucedida, ele chame identityModule.initialize() para criar a "certidão de nascimento" do nó. Em atualizações subsequentes, ele chamará identityModule.updateCurrentLocation().
WiFiDirectModule: Concluir a implementação da máquina de estados que gerencia o ciclo "Servidor -> Busca -> Cliente -> Sincroniza -> Servidor". Garantir que a troca de objetos SituationalCache seja robusta.
MainActivity / StatusFragment: Garantir que a UI seja atualizada corretamente com os dados recebidos do SituationalCache.
Teste de Integração da Camada 1:
Realizar testes com dois ou mais dispositivos.
Critérios de Sucesso:
Os dispositivos devem se tornar Servidores (Group Owners) automaticamente.
Eles devem se descobrir mutuamente.
Um dispositivo deve iniciar o ciclo de conexão, trocando de papel para Cliente.
Os SituationalCache (CLAs) devem ser trocados e fundidos com sucesso.
A UI de ambos os dispositivos deve refletir o cache fundido, exibindo os usernames de todos os nós na CLA.
Após a sincronização, os nós devem retornar ao seu estado de Servidor.
Planejamento Futuro (Pós-Camada 1):
Camada 2 - Roteamento Inter-CLA: Implementar a lógica para um nó atualizar sua CPA de Origem quando se move para uma nova CLA.
Persistência de Dados: Salvar a identidade do nó no armazenamento local para que ela sobreviva a reinicializações do aplicativo.
Camada de Transporte Adicional: Iniciar o projeto da camada de descoberta baseada em Bluetooth Low Energy (BLE).
