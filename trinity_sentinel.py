# backend/trinity_sentinel.py

import os
import time
import json
import requests
import subprocess
from git import Repo, Actor

# --- CONFIGURAÇÃO ---
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
BACKEND_DIR = os.path.join(PROJECT_ROOT, 'backend')
FRONTEND_DIR = os.path.join(PROJECT_ROOT, 'frontend')
CONFIG_FILE_PATH = os.path.join(PROJECT_ROOT, 'config.json')

BACKEND_PORT = 8000
FRONTEND_PORT = 5173 # Porta padrão do Vite
NGROK_API_URL = "http://localhost:4040/api/tunnels"

GIT_REPO_PATH = PROJECT_ROOT
GIT_COMMIT_MESSAGE = "chore(auto ): atualiza a URL da API do ngrok"
GIT_AUTHOR_NAME = "Trinity Sentinel"
GIT_AUTHOR_EMAIL = "sentinel@meshwave.com"

# --- FUNÇÕES DE VERIFICAÇÃO E REINICIALIZAÇÃO ---

def check_service(name, port):
    """Verifica se um serviço está respondendo em uma porta local."""
    try:
        requests.get(f"http://localhost:{port}", timeout=5 )
        return True
    except requests.exceptions.ConnectionError:
        print(f"⚠️  Serviço '{name}' está INATIVO na porta {port}.")
        return False

def start_backend():
    """Inicia o servidor de backend."""
    print("🔄 Iniciando o servidor de Backend (Uvicorn)...")
    command = f"source {BACKEND_DIR}/venv/bin/activate && uvicorn app.main:app --host localhost --port {BACKEND_PORT}"
    subprocess.Popen(command, shell=True, executable='/bin/bash', cwd=BACKEND_DIR)
    time.sleep(10) # Tempo para iniciar

def start_frontend():
    """Inicia o servidor de desenvolvimento do frontend."""
    print("🔄 Iniciando o servidor de Frontend (NPM)...")
    command = f"npm run dev"
    subprocess.Popen(command, shell=True, cwd=FRONTEND_DIR)
    time.sleep(15) # Tempo para iniciar

def get_ngrok_public_url():
    """Retorna a URL pública do Ngrok, se ativa."""
    try:
        response = requests.get(NGROK_API_URL, timeout=5)
        response.raise_for_status()
        data = response.json()
        for tunnel in data.get("tunnels", []):
            if tunnel.get("proto") == "https" and tunnel.get("config", {} ).get("addr") == f"http://localhost:{BACKEND_PORT}":
                return tunnel.get("public_url" )
    except requests.exceptions.ConnectionError:
        return None
    return None

def start_ngrok():
    """Inicia um novo túnel do Ngrok."""
    print("🔄 Iniciando um novo túnel do Ngrok...")
    command = f"ngrok http {BACKEND_PORT} > /dev/null 2>&1 &"
    subprocess.Popen(command, shell=True )
    time.sleep(10)

def update_config_on_github(new_url):
    """Atualiza o config.json e faz push para o GitHub via SSH."""
    print(f"🔄 Verificando e atualizando config.json com a nova URL: {new_url}")
    try:
        with open(CONFIG_FILE_PATH, 'r+') as f:
            config_data = json.load(f)
            if config_data.get("apiUrl") == new_url:
                print("✅ URL no config.json já está atualizada.")
                return
            config_data["apiUrl"] = new_url
            f.seek(0)
            json.dump(config_data, f, indent=2)
            f.truncate()

        repo = Repo(GIT_REPO_PATH)
        if not repo.is_dirty(path=CONFIG_FILE_PATH):
            print("Git: Nenhuma mudança detectada no config.json.")
            return
            
        print("🤖 Executando git add, commit, e push via SSH...")
        repo.index.add([CONFIG_FILE_PATH])
        author = Actor(GIT_AUTHOR_NAME, GIT_AUTHOR_EMAIL)
        repo.index.commit(GIT_COMMIT_MESSAGE, author=author, committer=author)
        origin = repo.remote(name='origin')
        origin.push()
        print("✅ Push para o GitHub concluído com sucesso!")
    except Exception as e:
        print(f"❌ ERRO ao atualizar o GitHub: {e}")

# --- LOOP PRINCIPAL DO SENTINELA ---

if __name__ == "__main__":
    print("--- 🛡️  Iniciando o Sentinela da Trindade 🛡️ ---")
    
    while True:
        print(f"\n--- Verificação às {time.strftime('%Y-%m-%d %H:%M:%S')} ---")
        
        # 1. Verifica o Backend
        if not check_service("Backend", BACKEND_PORT):
            start_backend()

        # 2. Verifica o Frontend
        if not check_service("Frontend", FRONTEND_PORT):
            start_frontend()

        # 3. Verifica o Ngrok
        ngrok_url = get_ngrok_public_url()
        if not ngrok_url:
            print("⚠️  Túnel do Ngrok está INATIVO.")
            start_ngrok()
            # Dá um tempo para o novo túnel ser estabelecido antes de tentar atualizar
            time.sleep(5) 
            ngrok_url = get_ngrok_public_url()

        if ngrok_url:
            print(f"✅ Túnel do Ngrok está ATIVO: {ngrok_url}")
            update_config_on_github(ngrok_url)
        else:
            print("❌ Falha ao estabelecer o túnel do Ngrok.")

        print("--- Verificação concluída. Próxima em 60 segundos. ---")
        time.sleep(60)

