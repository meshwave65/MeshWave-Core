# backend/trinity_stop.py (v1.2 - com lógica de "Pai Primeiro")

import psutil
import os
import signal
import time

print("--- 🛑 Iniciando o Encerramento de TODOS os Serviços do Project C3 🛑 ---")

def find_and_kill(target_name, target_string_in_cmd):
    """Função auxiliar para encontrar e matar um processo específico."""
    killed_pid = None
    for proc in psutil.process_iter(['pid', 'name', 'cmdline']):
        try:
            cmdline = ' '.join(proc.info['cmdline'])
            if target_string_in_cmd in cmdline and proc.pid != os.getpid():
                pid = proc.info['pid']
                print(f"  -> Encontrado: {target_name} com PID: {pid}")
                try:
                    os.kill(pid, signal.SIGTERM)
                    print(f"  ✅ Sinal de encerramento enviado para o PID {pid}.")
                    killed_pid = pid
                except Exception as e:
                    print(f"  ❌ Falha ao encerrar o PID {pid}: {e}")
                # Retorna após encontrar e tentar matar o primeiro, para evitar complexidade
                return killed_pid
        except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
            continue
    return killed_pid

# --- PASSADA 1: MATAR O SENTINELA PRIMEIRO ---
print("\n[PASSO 1/2] Procurando e encerrando o processo do Sentinela...")
sentinel_pid = find_and_kill("Sentinela", "trinity_sentinel.py")
if sentinel_pid:
    print(f"Aguardando 2 segundos para garantir o encerramento do Sentinela...")
    time.sleep(2)
else:
    print("Nenhum processo do Sentinela encontrado.")

# --- PASSADA 2: LIMPAR OS PROCESSOS FILHOS RESTANTES ---
print("\n[PASSO 2/2] Procurando e encerrando os serviços restantes...")

services_to_kill = {
    "Uvicorn": "uvicorn app.main:app",
    "Vite/NPM": "vite",
    "Ngrok": "ngrok http"
}

any_killed_in_pass2 = False
for name, cmd_str in services_to_kill.items( ):
    if find_and_kill(name, cmd_str):
        any_killed_in_pass2 = True

if not sentinel_pid and not any_killed_in_pass2:
    print("\nNenhum serviço ativo do Project C3 foi encontrado.")
else:
    print("\n✅ Processo de encerramento concluído.")


