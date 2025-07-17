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
