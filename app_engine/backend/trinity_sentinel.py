# app_engine/backend/trinity_sentinel.py (v1.2 - Caminhos Corrigidos)

import os
import time
import json
import requests
import subprocess
from git import Repo, Actor, GitCommandError

# --- CONFIGURAÇÃO COM NOVOS CAMINHOS ---
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
APP_ENGINE_DIR = os.path.join(PROJECT_ROOT, 'app_engine')
BACKEND_DIR = os.path.join(APP_ENGINE_DIR, 'backend')
FRONTEND_DIR = os.path.join(APP_ENGINE_DIR, 'frontend')
CONFIG_FILE_PATH = os.path.join(PROJECT_ROOT, 'clients', 'meshwave', 'project_c3', 'config.json')
BACKEND_PORT = 8000
FRONTEND_PORT = 5173
NGROK_API_URL = "http://localhost:4040/api/tunnels"
GIT_REPO_PATH = PROJECT_ROOT
GIT_COMMIT_MESSAGE = "chore(auto ): atualiza a URL da API do ngrok"
GIT_AUTHOR_NAME = "Trinity Sentinel"
GIT_AUTHOR_EMAIL = "sentinel@meshwave.com"

# --- FUNÇÕES DE VERIFICAÇÃO E REINICIALIZAÇÃO ---
def check_service(name, port):
    try:
        requests.get(f"http://localhost:{port}", timeout=5 )
        return True
    except requests.exceptions.ConnectionError:
        print(f"⚠️  Serviço '{name}' está INATIVO na porta {port}.")
        return False

def start_backend():
    print("🔄 Iniciando o servidor de Backend (Uvicorn)...")
    command = f"source {BACKEND_DIR}/venv/bin/activate && uvicorn app.main:app --host localhost --port {BACKEND_PORT}"
    subprocess.Popen(command, shell=True, executable='/bin/bash', cwd=BACKEND_DIR)
    time.sleep(10)

def start_frontend():
    print("🔄 Iniciando o servidor de Frontend (NPM)...")
    command = "npm run dev"
    subprocess.Popen(command, shell=True, cwd=FRONTEND_DIR)
    time.sleep(15)

def get_ngrok_public_url():
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
    print("🔄 Iniciando um novo túnel do Ngrok...")
    command = f"ngrok http {BACKEND_PORT} > /dev/null 2>&1 &"
    subprocess.Popen(command, shell=True )
    time.sleep(10)

def update_config_on_github(new_url):
    print(f"🔄 Verificando e atualizando config.json com a nova URL: {new_url}")
    try:
        repo = Repo(GIT_REPO_PATH)
        origin = repo.remote(name='origin')
        print("🤖 Sincronizando com o GitHub (git pull)...")
        origin.pull()
        with open(CONFIG_FILE_PATH, 'r+') as f:
            config_data = json.load(f)
            if config_data.get("apiUrl") == new_url:
                print("✅ URL no config.json já está atualizada.")
                return
            config_data["apiUrl"] = new_url
            f.seek(0)
            json.dump(config_data, f, indent=2)
            f.truncate()
        if repo.is_dirty(path=CONFIG_FILE_PATH):
            print("🤖 Executando git add, commit, e push via SSH...")
            repo.index.add([CONFIG_FILE_PATH])
            author = Actor(GIT_AUTHOR_NAME, GIT_AUTHOR_EMAIL)
            repo.index.commit(GIT_COMMIT_MESSAGE, author=author, committer=author)
            push_info = origin.push()
            if push_info[0].flags & push_info[0].ERROR:
                print(f"❌ ERRO durante o git push: {push_info[0].summary}")
            else:
                print("✅ Push para o GitHub concluído com sucesso!")
    except Exception as e:
        print(f"❌ ERRO inesperado ao atualizar o GitHub: {e}")

# --- LOOP PRINCIPAL DO SENTINELA ---
if __name__ == "__main__":
    print("--- 🛡️  Iniciando o Sentinela da Trindade (v1.2 - SaaS Arch) 🛡️ ---")
    while True:
        print(f"\n--- Verificação às {time.strftime('%Y-%m-%d %H:%M:%S')} ---")
        if not check_service("Backend", BACKEND_PORT):
            start_backend()
        if not check_service("Frontend", FRONTEND_PORT):
            start_frontend()
        ngrok_url = get_ngrok_public_url()
        if not ngrok_url:
            print("⚠️  Túnel do Ngrok está INATIVO.")
            start_ngrok()
            time.sleep(5) 
            ngrok_url = get_ngrok_public_url()
        if ngrok_url:
            print(f"✅ Túnel do Ngrok está ATIVO: {ngrok_url}")
            update_config_on_github(ngrok_url)
        else:
            print("❌ Falha ao estabelecer o túnel do Ngrok.")
        print("--- Verificação concluída. Próxima em 60 segundos. ---")
        time.sleep(60)

