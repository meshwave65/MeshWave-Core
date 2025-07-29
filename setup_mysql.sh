#!/bin/bash

# Script para configurar o banco de dados MySQL para o Project C3
# Requer MySQL 8.0.42 instalado e acesso ao usuário root

# Configurações
DB_NAME="project_c3"
DB_USER="c3_user"
DB_PASSWORD="c3_password" # Substitua por uma senha segura
ROOT_USER="root"
ROOT_PASSWORD="sua-senha-root" # Substitua pela senha do root

# Verifica se o MySQL está instalado
if ! command -v mysql &> /dev/null; then
    echo "Erro: MySQL não está instalado. Instale com 'sudo apt install mysql-server'."
    exit 1
fi

# Cria o banco de dados e o usuário
mysql -u "$ROOT_USER" -p"$ROOT_PASSWORD" -e "
CREATE DATABASE IF NOT EXISTS $DB_NAME;
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED WITH mysql_native_password BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
"

if [ $? -eq 0 ]; then
    echo "Banco de dados '$DB_NAME' e usuário '$DB_USER' configurados com sucesso."
else
    echo "Erro ao configurar o banco de dados. Verifique as credenciais e permissões."
    exit 1
fi

# Testa a conexão com o novo usuário
mysql -u "$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "Conexão com o usuário '$DB_USER' testada com sucesso."
else
    echo "Erro: Falha ao conectar com o usuário '$DB_USER'. Verifique a senha ou permissões."
    exit 1
fi
