
### **Relatório Mestre de Design e Implementação: Project C3 v2.0 (Final)**

**ID do Documento:** `P3-MASTER-PLAN-V2.0`
**Data da Última Revisão:** 2025-07-27
**Status:** **CONGELADO. Blueprint Oficial para Implementação do MVP.**

#### **1. Sumário Executivo: A Visão Estratégica**

Este documento detalha o plano de implementação para o **"Project C3" (Centro de Comando e Controle de Projetos)**, a fundação sobre a qual todo o ecossistema MeshWave será construído. O objetivo é criar uma "fábrica de software inteligente" para orquestrar o desenvolvimento dos pilares do ecossistema: **MeshBlockchain**, **Q-CyPIA** e o **MeshWave App**.

A prioridade estratégica é a construção desta fábrica, que nos permitirá gerenciar o desenvolvimento de forma escalável, controlada e eficiente.

#### **2. Arquitetura do Sistema "Project C3"**

O Project C3 é uma aplicação web com uma arquitetura desacoplada, projetada para flexibilidade e robustez.

*   **Backend:**
    *   **Tecnologia:** Python com **FastAPI**.
    *   **Hospedagem:** Inicialmente, rodando em um **ambiente Linux local** para agilidade no desenvolvimento.
    *   **Exposição:** A API será exposta à internet via **ngrok**.

*   **Frontend:**
    *   **Tecnologia:** **React (com Vite)** ou um framework JavaScript moderno equivalente.
    *   **Hospedagem:** Os arquivos estáticos compilados serão implantados na **Locaweb** para disponibilidade 24/7 e um domínio profissional.

*   **Ponte de Configuração (Ponteiro Simbólico):**
    *   Um arquivo `config.json` será hospedado no **repositório GitHub `MeshWave-Core`** e servido via `raw.githubusercontent.com`. O frontend buscará este arquivo em sua inicialização para descobrir a URL atual da API do ngrok, permitindo atualizações dinâmicas com um simples `git push`.

*   **Banco de Dados:**
    *   **Tecnologia:** **MySQL**, gerenciado pelo ORM **SQLAlchemy**.
    *   **Princípio Temporal:** Todas as operações e armazenamento de data/hora no backend e no banco de dados utilizarão exclusivamente o padrão **Timestamp Unix UTC**. A conversão para o fuso horário local do usuário é uma responsabilidade exclusiva da camada de apresentação (UI).

#### **3. Gerenciamento de Tarefas e Fluxo de Trabalho**

Abandonamos a estrutura de múltiplos diretórios em favor de um sistema mais poderoso baseado em metadados.

*   **Diretório Único:** Todas as tarefas residirão em um único diretório: `/tasks`.
*   **Gerenciamento por Estado:** O estado de uma tarefa é definido por um campo `status` numérico dentro do próprio arquivo da tarefa. Isso elimina a necessidade de mover arquivos, simplificando as operações do Git.
*   **Mapeamento de Status e Cores (UI):**
    *   `status: 1` (Open) -> **Verde**
    *   `status: 2` (In Progress) -> **Azul**
    *   `status: 3` (On Hold) -> **Amarelo**
    *   `status: 4` (Done) -> **Cinza/Preto**
    *   `status: 5` (Warning/Atrasada) -> **Vermelho** (calculado dinamicamente comparando `due_date` com o tempo atual).
*   **Propriedade do Estado:** O agente responsável pela tarefa é o único que pode alterar seu status de/para `in_progress` e `on_hold`, garantindo que o estado do sistema reflita a realidade do trabalho.
*   **Tratamento de Dúvidas:** Um processo formal de "Solicitação de Esclarecimento" foi definido, utilizando o status `on_hold` e arquivos `QUERY-[ID].md` para gerenciar dúvidas sem interromper o fluxo principal.

#### **4. Design da Interface e Experiência do Usuário (UX)**

A UI do Project C3 será projetada para ser intuitiva e contextual, através de uma **Navegação Hierárquica Dinâmica**.

*   **Descoberta Dinâmica:** A estrutura de navegação (Segmentos, Fases, Módulos) não será "hard-coded". A UI a construirá dinamicamente, fazendo chamadas à API para buscar a hierarquia diretamente do banco de dados.
*   **Fluxo de Navegação:** O usuário navegará por 4 níveis (Segmento -> Fase -> Módulo -> Dashboard) para chegar a um painel de controle focado.
*   **Dashboard Contextual:** O dashboard final exibirá uma lista de tarefas filtradas pelo contexto selecionado, com cores indicando o status de cada uma e opções de filtragem e ordenação.
*   **Gerenciamento Futuro:** O design prevê uma "Página de Manutenção" futura, que permitirá a administradores gerenciar a estrutura do roadmap (adicionar/editar módulos) através da própria UI.

#### **5. Plano de Implementação Imediata: O MVP do "Project C3"**

O escopo do MVP está congelado para garantir o foco na entrega. A implementação seguirá a ordem de serviço final.

**Tarefa a ser Executada:** `TASK-005-c3-mvp-final-engineering-plan.md`

**Requisitos Essenciais do MVP:**

1.  **Backend:**
    *   Modelar as tabelas `Segments`, `Phases`, `Modules`, `Tasks` no SQLAlchemy, incluindo campos de timestamp UTC.
    *   Criar um script `seed_database.py` para carregar o roadmap inicial.
    *   Implementar os endpoints **`GET`** para a API de hierarquia e para as tarefas (`/api/v1/tasks?module_id={id}`).

2.  **Frontend:**
    *   Implementar a navegação hierárquica dinâmica que consome a API.
    *   A aplicação **deve** buscar a URL da API do `config.json` no GitHub.
    *   O dashboard deve exibir a lista de tarefas com seus respectivos status (cores).
    *   Implementar estados básicos de UI (`loading`, `error`).

3.  **Documentação:**
    *   Criar um `README.md` detalhado para setup e execução.
    *   Criar um `PROJECT_BLUEPRINT.md` na raiz do projeto, contendo este relatório consolidado como a "pedra basilar" da arquitetura.

---
Este documento representa o ponto final do nosso processo de design. Ele é o nosso contrato e o nosso guia. O próximo passo é a execução. Estamos prontos para construir.
