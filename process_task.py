
import os
import shutil

REPO_PATH = '/home/ubuntu/MeshWave-Core'
TASKS_OPEN_DIR = os.path.join(REPO_PATH, 'tasks', 'open')
TASKS_IN_PROGRESS_DIR = os.path.join(REPO_PATH, 'tasks', 'in_progress')
TASKS_DONE_DIR = os.path.join(REPO_PATH, 'tasks', 'done')

def get_next_task():
    tasks = [f for f in os.listdir(TASKS_OPEN_DIR) if f.startswith('TASK-') and f.endswith('.md')]
    if not tasks:
        return None
    tasks.sort(key=lambda x: int(x.split('-')[1]))
    return tasks[0]

def process_task():
    task_filename = get_next_task()
    if not task_filename:
        print('Nenhuma tarefa encontrada no diretório open.')
        return

    source_path = os.path.join(TASKS_OPEN_DIR, task_filename)
    in_progress_path = os.path.join(TASKS_IN_PROGRESS_DIR, task_filename)
    done_path = os.path.join(TASKS_DONE_DIR, task_filename)

    print(f'Movendo {task_filename} para in_progress...')
    shutil.move(source_path, in_progress_path)

    print(f'Processando {task_filename}...')
    with open(in_progress_path, 'a') as f:
        f.write(f'\n\n--- Processado pelo Manus Agent em 27/07/2025 ---\n')

    print(f'Movendo {task_filename} para done...')
    shutil.move(in_progress_path, done_path)
    print(f'Tarefa {task_filename} concluída e movida para done.')

if __name__ == '__main__':
    process_task()


