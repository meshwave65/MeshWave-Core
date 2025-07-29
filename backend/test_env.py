import os
from dotenv import load_dotenv

print("--- Iniciando Teste de Diagnóstico ---")

# Caminho para o arquivo .env
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
print(f"Procurando pelo arquivo .env em: {dotenv_path}")

# Carrega o arquivo .env
if os.path.exists(dotenv_path):
    load_dotenv(dotenv_path=dotenv_path)
    print("✅ Arquivo .env encontrado e carregado.")
else:
    print("❌ ERRO: Arquivo .env NÃO encontrado no caminho esperado.")

# Pega as variáveis de ambiente
db_user = os.getenv("DB_USER")
db_password = os.getenv("DB_PASSWORD")
db_host = os.getenv("DB_HOST")
db_port = os.getenv("DB_PORT")
db_name = os.getenv("DB_NAME")

# Imprime os valores que foram lidos
print("\n--- Valores das Variáveis de Ambiente ---")
print(f"DB_USER:     '{db_user}'")
print(f"DB_PASSWORD: '{db_password}'")
print(f"DB_HOST:     '{db_host}'")
print(f"DB_PORT:     '{db_port}'")
print(f"DB_NAME:     '{db_name}'")
print("-----------------------------------------")

# Monta e imprime a string de conexão final
if all([db_user, db_password, db_host, db_port, db_name]):
    connection_string = f"mysql+pymysql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"
    print("\n--- String de Conexão Gerada ---")
    print(connection_string)
    print("----------------------------------")
else:
    print("\n❌ ERRO: Uma ou mais variáveis de ambiente estão faltando.")

