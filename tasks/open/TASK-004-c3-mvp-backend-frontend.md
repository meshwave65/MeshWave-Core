#OBSOLETA
---
task_id: "004"
title: "Desenvolvimento do MVP para o Project C3 com Hierarquia Dinâmica"
status: open
assigned_to: "Engenheiro-Full-Stack"
created_at: "2025-07-26T16:00:00Z"
related_report: null
---

### Objetivo
Criar a base tecnológica para a interface web de gerenciamento "Project C3", implementando um sistema que carrega uma estrutura de projeto pré-definida do banco de dados e permite a navegação hierárquica dinâmica.

### Requisitos do Backend (Python/FastAPI + SQLAlchemy + MySQL)
1.  **Modelagem de Dados:** Implementar o esquema de banco de dados com tabelas para `Segments`, `Phases`, `Modules`, e `Tasks`, com as relações de chave estrangeira apropriadas.
2.  **Script de Carregamento (Seeding):** Criar um script `seed_database.py` que popule as tabelas `Segments`, `Phases`, e `Modules` com a estrutura completa do roadmap MeshWave. O script deve ser idempotente.
3.  **API de Hierarquia:** Implementar os endpoints para ler a estrutura do projeto:
    - `GET /api/v1/segments`
    - `GET /api/v1/segments/{segment_id}/phases`
    - `GET /api/v1/phases/{phase_id}/modules`
4.  **API de Tarefas:** Implementar os endpoints para gerenciar tarefas:
    - `GET /api/v1/tasks?module_id={module_id}`
    - `POST /api/v1/tasks`

### Requisitos do Frontend (React/Vite + React Router)
1.  **Navegação Dinâmica:** Implementar o fluxo de navegação de 4 níveis (Segmento -> Fase -> Módulo -> Dashboard). Cada nível deve buscar seus dados da API correspondente.
2.  **Componentes de Navegação:** Criar os componentes de UI para exibir as listas de Segmentos, Fases e Módulos.
3.  **Dashboard do Módulo:** Desenvolver a página final do dashboard que chama `GET /tasks` com o `module_id` do contexto e exibe as tarefas.
4.  **Breadcrumbs:** Implementar um componente de "Breadcrumbs" que mostre a localização atual do usuário na hierarquia.

### Resultado Esperado
Um repositório GitHub (ou .zip) com os diretórios `/backend` e `/frontend`. O `README.md` deve incluir instruções claras para:
1.  Configurar a conexão com o banco de dados MySQL.
2.  Executar o script `seed_database.py`.
3.  Instalar dependências e iniciar os servidores de backend e frontend.

A aplicação resultante deve, após o setup, exibir a estrutura completa do roadmap MeshWave e permitir a navegação até um dashboard de tarefas funcional.

