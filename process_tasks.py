import os
import shutil
import subprocess

OPEN_DIR = '/home/ubuntu/MeshWave-Core/tasks/open'
IN_PROGRESS_DIR = '/home/ubuntu/MeshWave-Core/tasks/in_progress'
DONE_DIR = '/home/ubuntu/MeshWave-Core/tasks/done'

PAT = "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"
REPO_DIR = "/home/ubuntu/MeshWave-Core"

def get_next_task():
    tasks = [f for f in os.listdir(OPEN_DIR) if f.startswith("TASK-")]
    if not tasks:
        return None
    tasks.sort(key=lambda x: int(x.split("-")[1]))
    return tasks[0]

def move_task(task_filename, source_dir, destination_dir):
    shutil.move(os.path.join(source_dir, task_filename), os.path.join(destination_dir, task_filename))

def process_task(task_filename):
    print(f"Processando tarefa: {task_filename}")
    task_path = os.path.join(IN_PROGRESS_DIR, task_filename)
    
    # Aqui você adicionaria a lógica para executar a pesquisa/tarefa
    # Por enquanto, vamos apenas simular a adição de um relatório
    report_content = f"\n\n## Relatório de Conclusão\n\nEsta é uma simulação de relatório para a tarefa {task_filename}."
    
    with open(task_path, "a") as f:
        f.write(report_content)
    print(f"Relatório adicionado à tarefa: {task_filename}")

def git_commit_and_push(message):
    os.chdir(REPO_DIR)
    subprocess.run(["git", "add", "."], check=True)
    subprocess.run(["git", "commit", "-m", message], check=True)
    subprocess.run(["git", "push", f"https://{PAT}@github.com/meshwave65/MeshWave-Core.git"], check=True)
    os.chdir("/home/ubuntu") # Voltar para o diretório inicial

def main():
    while True:
        next_task = get_next_task()
        if not next_task:
            print("Nenhuma tarefa encontrada no diretório 'open'. Encerrando.")
            break
        
        print(f"Movendo {next_task} para 'in_progress'...")
        move_task(next_task, OPEN_DIR, IN_PROGRESS_DIR)
        
        process_task(next_task)
        
        print(f"Movendo {next_task} para 'done'...")
        move_task(next_task, IN_PROGRESS_DIR, DONE_DIR)
        
        git_commit_and_push(f"Processado {next_task}")
        print(f"Alterações para {next_task} enviadas para o GitHub.")

if __name__ == "__main__":
    main()

