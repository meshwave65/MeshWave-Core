# MeshWave - Project C3 Blueprint (v2.0 - Turn-Based)

**ID do Documento:** `P3-MASTER-BLUEPRINT-V2.0`
**Data da Última Revisão:** 2025-07-29
**Status:** **ATIVO - Documento de Arquitetura e Engenharia de Referência**

---

## 1. Sumário Executivo

Este documento é a "pedra basilar" que detalha a arquitetura, o design e o plano de implementação para o **"Project C3" (Centro de Comando e Controle de Projetos)**, nosso servidor MCP (Model Context Protocol). O Project C3 é a fundação de software sobre a qual todo o ecossistema MeshWave será desenvolvido e gerenciado.

## 2. Arquitetura do Sistema

O Project C3 é uma aplicação web completa com uma arquitetura desacoplada e escalável.

- **Backend:** Python com **FastAPI**, rodando localmente em Linux e exposto via **ngrok**.
- **Frontend:** **React (com Vite)**, com arquivos estáticos hospedados na **Locaweb**.
- **Ponte de Configuração:** Um arquivo `config.json` hospedado no próprio site (`meshwave.com.br/mcp/...`) servirá como um "ponteiro simbólico" para a URL da API.
- **Banco de Dados:** **MySQL**, com todas as operações de data/hora utilizando **Timestamp Unix UTC**.

## 3. Gerenciamento de Tarefas e Fluxo de Trabalho (Modelo "Turn-Based")

O sistema utiliza um modelo de "documento vivo" com um sistema de turnos explícito para garantir clareza e responsabilidade.

- **Diretório Único:** Todas as tarefas residirão em `/tasks`.
- **Estrutura do Arquivo:** Um arquivo Markdown que acumula blocos de conteúdo cronologicamente (Dúvida, Esclarecimento, Relatório), cada um com seu próprio timestamp no cabeçalho do bloco.
- **Metadados Essenciais (Cabeçalho YAML):**
    - `task_id`, `title`, `assigned_to`.
    - `status_agente`: O estado do trabalho (1-Open, 2-In_Progress, 3-On_Hold, 4-Done, 5-Canceled).
    - `turn_holder`: **O metadado de controle de fluxo.**
        - `0`: Ação requerida do **Agente/Consultor**.
        - `1`: Ação requerida do **Gestor/Orquestrador**.

## 4. O Agente Sentinela: Controle de Tempo e SLA

Um serviço independente ("Sentinela") gerenciará o ciclo de vida temporal das tarefas.

- **Base de Dados do Sentinela:** Manterá uma base de dados separada com `task_id`, `timestamp_creation`, e `timestamp_deadline`.
- **Visualização por Cor (Determinada pelo Sentinela):**
    - `0` (Aberta) -> 🔵 Azul
    - `1` (Em Andamento) -> 🟢 Verde
    - `2` (Atenção - Atrasada) -> 🟡 Amarelo
    - `3` (Urgente - Atraso Crítico) -> 🔴 Vermelho
    - `4` (Concluída no Prazo) -> ⚫️ Cinza
    - `5` (Concluída com Atraso) -> ⚫️ Preto

## 5. Plano de Implementação do MVP

A implementação seguirá este plano de engenharia detalhado.

- **Estrutura do Repositório:** Adotar a estrutura SaaS com `app_engine/` e `clients/`.
- **Backend:**
    - Modelar as tabelas no SQLAlchemy, incluindo o campo `turn_holder` na tabela de tarefas.
    - Criar um script `seed_database.py`.
    - Implementar os endpoints da API para ler a hierarquia e gerenciar tarefas, permitindo a filtragem por `status_agente` e `turn_holder`.
- **Frontend:**
    - Implementar a navegação hierárquica dinâmica.
    - A aplicação deve buscar a URL da API do `config.json` no domínio `meshwave.com.br`.
    - O dashboard deve exibir as tarefas com as cores definidas pelo `status_sla` do Sentinela.
- **Documentação:** Manter um `README.md` detalhado e este `PROJECT_BLUEPRINT.md` atualizados.

---

