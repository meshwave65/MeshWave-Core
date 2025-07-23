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
