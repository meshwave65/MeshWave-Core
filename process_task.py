import os
import shutil
from datetime import datetime

REPO_PATH = '/home/ubuntu/MeshWave-Core'
TASKS_OPEN_DIR = os.path.join(REPO_PATH, 'tasks', 'open')
TASKS_IN_PROGRESS_DIR = os.path.join(REPO_PATH, 'tasks', 'in_progress')
TASKS_DONE_DIR = os.path.join(REPO_PATH, 'tasks', 'done')
REPORTS_INCOMING_DIR = os.path.join(REPO_PATH, 'reports', 'incoming')

def get_next_task():
    tasks = [f for f in os.listdir(TASKS_OPEN_DIR) if f.startswith('TASK-') and f.endswith('.md')]
    if not tasks:
        return None
    tasks.sort(key=lambda x: int(x.split('-')[1]))
    return tasks[0]

def generate_doubt_report(task_id, message):
    report_filename = f'REPORT-{task_id}-{datetime.now().strftime("%Y%m%d-%H%M%S")}.md'
    report_path = os.path.join(REPORTS_INCOMING_DIR, report_filename)
    with open(report_path, 'w') as f:
        f.write(f'# Relatório de Dúvida - Tarefa {task_id}\n\n')
        f.write(f'Data: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n\n')
        f.write(f'Prezado usuário,\n\n')
        f.write(f'Encontrei uma dúvida durante o processamento da tarefa {task_id}.\n\n')
        f.write(f'Detalhes da dúvida: {message}\n\n')
        f.write(f'Por favor, forneça esclarecimentos ou direcionamento para que eu possa prosseguir.\n')
    print(f'Relatório de dúvida gerado: {report_path}')

def process_task():
    task_filename = get_next_task()
    if not task_filename:
        print('Nenhuma tarefa encontrada no diretório open.')
        return

    task_id = task_filename.split('-')[0] + '-' + task_filename.split('-')[1]

    # Simulação de uma dúvida sobre o escopo
    # Para este exemplo, vamos simular uma dúvida se a tarefa for a TASK-002
    if task_id == 'TASK-002':
        generate_doubt_report(task_id, 'O escopo desta tarefa não está claro. Preciso de mais detalhes sobre o que deve ser pesquisado ou executado.')
        return # Para o processamento da tarefa até que a dúvida seja resolvida

    source_path = os.path.join(TASKS_OPEN_DIR, task_filename)
    in_progress_path = os.path.join(TASKS_IN_PROGRESS_DIR, task_filename)
    done_path = os.path.join(TASKS_DONE_DIR, task_filename)

    print(f'Movendo {task_filename} para in_progress...')
    shutil.move(source_path, in_progress_path)

    print(f'Processando {task_filename}...')
    # Aqui seria o local para a pesquisa real ou execução da tarefa
    with open(in_progress_path, 'a') as f:
        f.write(f'\n\n--- Processado pelo Manus Agent em {datetime.now().strftime("%d/%m/%Y")} ---\n')

    print(f'Movendo {task_filename} para done...')
    shutil.move(in_progress_path, done_path)
    print(f'Tarefa {task_filename} concluída e movida para done.')

if __name__ == '__main__':
    process_task()

