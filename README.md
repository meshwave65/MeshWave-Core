MeshWave-Core v0.1.0-alpha

1. Visão do Projeto
MeshWave-Core é a fundação de um sistema de comunicação móvel, descentralizado, resiliente e autônomo. O objetivo é criar uma rede ad-hoc (malha ou mesh) que opera independentemente de infraestrutura de rede tradicional (Wi-Fi, 4G/5G, satélite), permitindo a troca de informações em cenários onde a conectividade é limitada, intermitente ou inexistente.
Este projeto representa um recomeço, uma reconstrução sobre um terreno sólido, priorizando a estabilidade do núcleo, a arquitetura modular e um processo de desenvolvimento pragmático e transparente.
2. A Saga: Lições Aprendidas do Protótipo
O MeshWave-Core nasce das cinzas de seu predecessor, o meshwave_android_prototype. O protótipo, embora tenha validado conceitos iniciais, nos ensinou lições valiosas através de uma série de desafios e falhas de build, que culminaram na necessidade de um recomeço.
O Ciclo da "Fé Cega": Tentativas reativas de correção, sem um diagnóstico profundo, nos levaram a um ciclo de "rodadas cegas", onde cada nova versão era uma aposta, não um passo de engenharia.
A Instabilidade do Ambiente: Problemas com o ambiente de build (versões de Gradle, JDK, plugins) não foram tratados como prioridade, contaminando o processo de desenvolvimento e mascarando os verdadeiros bugs do código.
A Falta de Telemetria: A ausência de uma UI de diagnóstico robusta nos forçou a trabalhar "às cegas", sem visibilidade sobre o estado interno dos módulos, tornando a depuração um exercício de adivinhação.
Este novo projeto é a materialização dessas lições.
3. Diretrizes e Filosofia de Desenvolvimento
Este projeto é governado por um conjunto estrito de princípios. Cada linha de código e cada decisão de arquitetura deve aderir a esta filosofia.
Diretriz #1: Nenhum Esqueleto no Armário
Definição: Não toleramos dívida técnica. Avisos (warnings) do compilador ou do linter são tratados com a mesma seriedade de erros (errors). Código obsoleto (deprecated) é refatorado para sua alternativa moderna. Não há "remendos" ou soluções temporárias.
Aplicação: Um build só é considerado bem-sucedido se estiver 100% livre de erros e avisos.
Diretriz #2: O Core Primeiro (De Dentro para Fora)
Definição: Inspirado na filosofia do Linux, nosso foco absoluto é na estabilidade, segurança e funcionalidade do núcleo do sistema. A interface do usuário (UI) é uma consequência da funcionalidade, não o contrário.
Aplicação: Priorizamos a lógica de negócio, a robustez dos módulos e a resiliência da rede. Uma UI "bonita" sobre um núcleo instável é inaceitável.
Diretriz #3: Reanimação Progressiva e Controlada
Definição: O desenvolvimento avança em passos pequenos, metódicos e testáveis. Cada nova funcionalidade é desenvolvida em sua própria branch no Git.
Aplicação:
Criar uma branch para a nova feature (ex: feature/location-module).
Implementar a funcionalidade.
Garantir que o build esteja limpo (sem erros ou avisos).
Testar a funcionalidade de forma isolada.
Fazer o "merge" para a branch principal (main) através de um Pull Request. A branch main deve sempre representar a última versão estável do projeto.
Diretriz #4: Módulos Especializados e Resilientes
Definição: O sistema é composto por módulos altamente encapsulados, cada um com uma única responsabilidade. Os módulos são projetados para operar de forma independente e assíncrona.
Aplicação: Um módulo que depende de uma informação de outro (ex: IdentityModule precisa do Geohash) não deve travar se a informação não estiver disponível. Ele deve usar um valor de falha padrão (um "Default Fail") e continuar operando, registrando a falha para diagnóstico.
4. Glossário e Conceitos
WFD (Wi-Fi Direct): A tecnologia base para nossa comunicação P2P em média distância.
BT (Bluetooth): Tecnologia para comunicação em curta distância e, potencialmente, para descoberta de serviços e troca de dados de baixo volume.
GO (Group Owner): Em uma rede WFD, o nó que atua como o "servidor" ou ponto de acesso do grupo.
CPA (Cache Primário de Ativação): A identidade fundamental de um nó, contendo seu DID, Username e Geohash. Usado como o "cartão de visita" na rede.
CLA (Célula Local de Ativação): Um grupo de nós que compartilham o mesmo Geohash, formando uma área de comunicação local.
Default Fail: Um valor de retorno padrão e informativo que um módulo fornece quando não consegue obter a informação real (ex: g9fail para uma falha de geolocalização). Garante que o sistema não trave.
Macaco Velho: (Gíria) Refere-se à experiência adquirida através de erros e depuração difícil. Um "macaco velho" não coloca a mão na mesma cumbuca duas vezes; ele aprende com os erros e adota processos mais robustos. Nossa decisão de recomeçar é uma ação de "macaco velho".






Documento Mestre de Arquitetura e Conceitos do Projeto MeshWave
Versão do Documento: 1.0
Data da Consolidação: 19 de julho de 2025
Status: Diretriz Estratégica Ativa
Prefácio
Este documento consolida uma série de relatórios conceituais que definem a arquitetura fundamental e a visão de longo prazo para o projeto MeshWave. Ele serve como a fonte canônica de verdade para as decisões de design e implementação, garantindo que o desenvolvimento permaneça alinhado com os princípios de resiliência, segurança, escalabilidade e inteligência que definem o projeto.
Capítulo 1: Arquitetura de Transporte e Comunicação
1.1. O Paradigma Briar de Encapsulamento de Transporte
ID do Conceito: ARCH-TRANSPORT-20250715-01
Status: Diretriz Estratégica Ativa
Sumário Executivo: Para evitar a instabilidade (ANRs e Crashes) observada em protótipos anteriores, o projeto adota uma arquitetura de comunicação baseada no encapsulamento rigoroso. Toda a lógica de um protocolo de transporte específico (ex: Bluetooth, Wi-Fi Direct) será contida em seu próprio módulo de software isolado (Handler/Service). A MainActivity (ou camada de UI) é relegada ao papel de uma Orquestradora de Alto Nível, que interage com esses módulos de forma assíncrona, mantendo a thread de UI protegida e o sistema resiliente.
Arquitetura:
Módulos Especialistas: Classes dedicadas (ex: BluetoothChatService, WiFiDirectHandler) gerenciam o ciclo de vida completo de seu respectivo canal de comunicação, executando operações de rede em threads de segundo plano.
A Orquestradora (UI): A camada de UI gerencia o ciclo de vida dos módulos de transporte e implementa a política de comunicação da rede com base no status recebido de cada módulo.
Benefícios: Aumento drástico da estabilidade, escalabilidade para novos transportes (ex: Satelital) e testabilidade de cada módulo de forma isolada.
1.2. Orquestração de Transporte Multimodal (O Paradigma do "Macaco Velho")
ID do Conceito: NET-20250712-01
Status: Diretriz Estratégica Ativa
Sumário Executivo: A MeshWave operará como uma rede de transporte multimodal e oportunista. A arquitetura transcende a dependência de um único canal e, em vez disso, orquestra todas as tecnologias de rádio disponíveis (Wi-Fi, Bluetooth, 4G/5G) para garantir a rota de menor custo e máxima resiliência para cada tarefa.
Arquitetura:
Múltiplas Interfaces ("Galhos"): Cada nó trata suas interfaces de rádio como "galhos" disponíveis.
Camada de Decisão Inteligente: Uma camada de roteamento, alimentada por IA, avalia constantemente o "custo" de cada galho (latência, banda, energia, custo de dados).
Roteamento Oportunista: Se uma conexão de alta velocidade (Wi-Fi) cai, a comunicação é instantânea e transparentemente transferida para um canal alternativo já estabelecido (Bluetooth), sem interrupção do serviço.
Contextualização no Roadmap:
Segmento: Otimização de IA, Rede MESH
Fases: Implementado através dos módulos Roteamento Preditivo, Energia Adaptativa e Interface SDN.
Capítulo 2: Arquitetura de Identidade e Segurança
2.1. A Resiliência da Identidade (O Paradigma do "Barco de Teseu")
ID do Conceito: ID-20250712-01
Status: Diretriz Estratégica Ativa
Sumário Executivo: A identidade de um nó na rede MeshWave é definida por um "DNA de Hardware" composto por múltiplos identificadores. O sistema utiliza Inferência Bayesiana para validar a identidade de forma probabilística, permitindo que um nó persista com sua identidade e reputação mesmo após modificações de hardware (as "amputações").
Arquitetura:
DNA de Hardware: Na primeira ativação, o sistema coleta um "genoma" de identificadores de hardware estáveis (ANDROID_ID, MAC Address, etc.). Um hash seguro deste genoma cria o Fingerprint Mestre.
Validação Probabilística: A validação de identidade não é uma simples comparação de hash, mas um cálculo da probabilidade de que ainda se trata do mesmo dispositivo, mesmo que um ou mais "genes" do DNA tenham mudado.
Aprendizado e Antifragilidade: O sistema aprende com as mudanças. Se um identificador (ex: MAC Address) se mostra instável, seu peso no cálculo de confiança futuro é dinamicamente reduzido.
2.2. Evolução Planejada da Identidade
Fase 1 (Protótipo Atual - v0.3.x): DID Baseado em IMEI/ANDROID_ID: Garante uma identidade imutável e persistente para a fase de testes, usando um identificador de dispositivo prontamente disponível.
Fase 2 (Produção Inicial - v0.5.x): "DNA do Equipamento": O DID será o hash criptográfico (SHA-256) de um conjunto de fatores (IMEI, Build.SERIAL, Build.MODEL, creationTimestamp, initialGeohash) para criar uma identidade pseudo-anônima e resistente a falsificações.
Fase 3 (Segurança Avançada - v0.6.x): Autenticação Recessiva Contextual (ARC): A rede passivamente verifica se o comportamento de um nó (padrões de conexão, localização) é consistente com seu histórico. Desvios anômalos podem acionar desafios de verificação ou rebaixar o nível de confiança do nó.
Contextualização no Roadmap:
Segmento: Segurança
Fases: Implementado através dos módulos SSI e DIDs, Sistema de Reputação e Segurança Quântica.
Capítulo 3: Arquitetura de Cache e Consciência Situacional
3.1. Estrutura de Cache Hierárquico
Status: Em Evolução
Sumário Executivo: A consciência situacional da rede é construída sobre uma estrutura de cache hierárquica, onde cada nível serve a um propósito específico, balanceando a frequência de atualização com o custo de comunicação.
Níveis de Cache:
Nível 1 (Futuro) - Cache de Proximidade (CP): Responde "Quem está ao meu alcance agora?". Preenchido por beacons de baixa energia (Bluetooth LE), serve como um gatilho de baixo custo para iniciar conexões mais caras.
Nível 2 (Foco Atual) - Cache de Roteamento (CR / CPA): Responde "Onde estão os nós e qual o melhor caminho?". Contém DID, currentGeohash, lastUpdateTimestamp. Sincronizado via conexão direta (BT/Wi-Fi). É a base para o roteamento preditivo.
Nível 3 (Futuro) - Cache Local de Atributos (CLA): Responde "Quais são as capacidades detalhadas de cada nó?". Contém a identidade completa, modelo de hardware, nível de bateria, funções na rede. Sincronizado com baixa frequência.
Nível 4 (Futuro) - Caches Especializados: Caches pequenos e de alta frequência para dados críticos e voláteis, como o BatteryStateCache, para decisões táticas rápidas.
3.2. Balanceamento de Carga e Divisão de Célula Adaptativa
ID do Conceito: SCALE-20250712-01
Status: Diretriz Estratégica Ativa
Sumário Executivo: Para resolver o problema de escalabilidade em áreas de alta densidade, a rede implementa um protocolo de divisão de célula. Uma rede sobrecarregada pode se "fatorar" dinamicamente em múltiplos subgrupos interconectados.
Arquitetura:
Limite de Carga: Cada grupo P2P (SESSID) tem um limite de clientes.
Protocolo de Divisão: Ao atingir o limite, o Nó Líder elege o melhor candidato entre seus clientes para se tornar o líder de uma nova célula, comanda a criação de um novo grupo e ordena a migração de uma parte dos clientes.
Interconexão (Bridging): Os dois líderes atuam como Nós Ponte, garantindo a comunicação entre as células.
Resiliência ("Rei Morto, Rei Posto"): Cada célula mantém uma hierarquia de sucessão para garantir a transição suave caso um líder fique offline.
Contextualização no Roadmap:
Segmento: Rede MESH, Otimização de IA
Fases: Implementado através de um módulo de Balanceamento de Carga Dinâmico, utilizando o Aprendizado Federado e o Sistema de Reputação.
