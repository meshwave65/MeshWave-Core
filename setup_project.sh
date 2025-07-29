#!/bin/bash

# ==============================================================================
# Script de Setup v2 para o Project C3 (Backend e Frontend)
# ==============================================================================
#
# USO:
# 1. Certifique-se de que este script está na raiz do projeto.
# 2. Dê permissão de execução: chmod +x setup_project.sh
# 3. Execute o script: ./setup_project.sh
#
# ==============================================================================

# --- Cores para o output ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}--- Iniciando a Configuração Completa do Project C3 ---${NC}"

# --- PARTE 1: CONFIGURAÇÃO DO BACKEND ---
echo -e "\n${YELLOW}=== CONFIGURANDO O BACKEND ===${NC}"

# 1.1. Criar Estrutura de Diretórios
echo -e "\n${BLUE}[BACKEND 1/5] Criando estrutura de diretórios...${NC}"
mkdir -p backend/app/models backend/app/routers backend/app/schemas backend/app/services backend/tests docs
touch backend/app/__init__.py backend/app/models/__init__.py backend/app/routers/__init__.py backend/app/schemas/__init__.py backend/app/services/__init__.py
echo -e "${GREEN}Estrutura do backend criada.${NC}"

# 1.2. Configurar Ambiente Virtual Python
echo -e "\n${BLUE}[BACKEND 2/5] Configurando ambiente virtual Python...${NC}"
if [ ! -d "backend/venv" ]; then
    python3 -m venv backend/venv
    echo -e "${GREEN}Ambiente virtual 'backend/venv' criado.${NC}"
fi
source backend/venv/bin/activate
echo -e "${GREEN}Ambiente virtual do backend ativado.${NC}"

# 1.3. Instalar Dependências Python
echo -e "\n${BLUE}[BACKEND 3/5] Instalando dependências Python...${NC}"
pip install fastapi "uvicorn[standard]" sqlalchemy pymysql python-dotenv cryptography > /dev/null
echo -e "${GREEN}Dependências do backend instaladas.${NC}"

# 1.4. Criar Arquivo de Configuração .env
echo -e "\n${BLUE}[BACKEND 4/5] Configurando o banco de dados...${NC}"
if [ -f "backend/.env" ]; then
    echo -e "${YELLOW}Arquivo backend/.env já existe. Pulando.${NC}"
else
    read -p "Digite o usuário do seu banco de dados MySQL (padrão: root): " DB_USER
    DB_USER=${DB_USER:-root}
    read -s -p "Digite a senha para o usuário '$DB_USER': " DB_PASSWORD
    echo ""
    cat > backend/.env << EOL
# Variáveis de Ambiente para o Project C3
DB_HOST=localhost
DB_PORT=3306
DB_USER=${DB_USER}
DB_PASSWORD='${DB_PASSWORD}'
DB_NAME=project_c3_db
EOL
    echo -e "${GREEN}Arquivo de configuração backend/.env criado.${NC}"
fi

# 1.5. Gerar e Executar Script de Criação do Banco de Dados
echo -e "\n${BLUE}[BACKEND 5/5] Criando o banco de dados 'project_c3_db'...${NC}"
python3 -c "
import os, pymysql, sys
from dotenv import load_dotenv
load_dotenv('backend/.env')
try:
    connection = pymysql.connect(host=os.getenv('DB_HOST'), port=int(os.getenv('DB_PORT')), user=os.getenv('DB_USER'), password=os.getenv('DB_PASSWORD'))
    print(f'✅ Conectado ao servidor MySQL.')
    with connection.cursor() as cursor:
        db_name = os.getenv('DB_NAME')
        cursor.execute(f'CREATE DATABASE IF NOT EXISTS \`{db_name}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
        print(f'✅ Banco de dados \'{db_name}\' verificado/criado.')
    connection.close()
except Exception as e:
    print(f'❌ ERRO: {e}', file=sys.stderr)
    sys.exit(1)
"
deactivate
echo -e "${GREEN}Ambiente virtual do backend desativado.${NC}"

# --- PARTE 2: CONFIGURAÇÃO DO FRONTEND ---
echo -e "\n${YELLOW}=== CONFIGURANDO O FRONTEND ===${NC}"

# 2.1. Instalar NVM (Node Version Manager)
echo -e "\n${BLUE}[FRONTEND 1/3] Verificando/Instalando NVM (Node Version Manager)...${NC}"
export NVM_DIR="$HOME/.nvm"
if [ -s "$NVM_DIR/nvm.sh" ]; then
    echo -e "${YELLOW}NVM já está instalado. Pulando.${NC}"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" # Carrega o NVM
else
    echo "Instalando NVM..."
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
    echo -e "${GREEN}NVM instalado. Por favor, feche e reabra seu terminal após o script terminar e execute-o novamente se o passo do Node falhar.${NC}"
fi

# 2.2. Instalar Node.js e Criar Projeto Vite
echo -e "\n${BLUE}[FRONTEND 2/3] Instalando Node.js e criando projeto React...${NC}"
nvm install --lts # Instala a versão Long-Term Support mais recente
nvm use --lts

mkdir -p frontend
cd frontend
if [ -f "package.json" ]; then
    echo -e "${YELLOW}Projeto frontend (package.json ) já existe. Pulando criação.${NC}"
else
    # Cria o projeto React com Vite de forma não-interativa
    npm create vite@latest . -- --template react > /dev/null 2>&1
    echo -e "${GREEN}Projeto React criado com Vite.${NC}"
fi

# 2.3. Instalar Dependências do Frontend
echo -e "\n${BLUE}[FRONTEND 3/3] Instalando dependências do frontend...${NC}"
npm install > /dev/null
echo -e "${GREEN}Dependências do frontend instaladas.${NC}"
cd ..

# --- Conclusão ---
echo -e "\n\n${GREEN}====================================================="
echo -e "🎉 Configuração Completa Concluída com Sucesso! 🎉"
echo -e "=====================================================${NC}"
echo -e "\n${YELLOW}Próximos Passos:${NC}"
echo -e "1. ${BLUE}(Terminal 1 - Backend)${NC} cd backend && source venv/bin/activate && uvicorn main:app --reload"
echo -e "2. ${BLUE}(Terminal 2 - Frontend)${NC} cd frontend && npm run dev"
echo -e "3. ${BLUE}(Terminal 3 - ngrok)${NC} ngrok http 8000"
echo -e "\nSeu backend estará em ${BLUE}http://localhost:8000${NC} e seu frontend em ${BLUE}http://localhost:5173${NC} (ou outra porta indicada )."

