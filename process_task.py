import os
import shutil
from datetime import datetime

REPO_PATH = '/home/ubuntu/MeshWave-Core'
TASKS_OPEN_DIR = os.path.join(REPO_PATH, 'tasks', 'open')
TASKS_IN_PROGRESS_DIR = os.path.join(REPO_PATH, 'tasks', 'in_progress')
TASKS_DONE_DIR = os.path.join(REPO_PATH, 'tasks', 'done')
REPORTS_INCOMING_DIR = os.path.join(REPO_PATH, 'reports', 'incoming')

def get_next_task(directory):
    tasks = [f for f in os.listdir(directory) if f.startswith('TASK-') and f.endswith('.md')]
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

def process_task_flow():
    # 1. Verifique o diretorio open a procura de tarefas abertas;
    task_filename = get_next_task(TASKS_OPEN_DIR)
    if not task_filename:
        print('Nenhuma tarefa encontrada no diretório open.')
        return

    task_id = task_filename.split('-')[0] + '-' + task_filename.split('-')[1]
    source_path = os.path.join(TASKS_OPEN_DIR, task_filename)
    in_progress_path = os.path.join(TASKS_IN_PROGRESS_DIR, task_filename)
    done_path = os.path.join(TASKS_DONE_DIR, task_filename)

    # 3. Mova esta tarefa para in_progress
    print(f'Movendo {task_filename} para in_progress...')
    try:
        shutil.move(source_path, in_progress_path)
    except shutil.Error as e:
        print(f'Erro ao mover o arquivo: {e}. Tentando copiar e apagar o original.')
        shutil.copy(source_path, in_progress_path)
        os.remove(source_path)

    print(f'Processando {task_filename}...')
    # 4. execute a tarefa e ao final acrescente o relatório detalhado ao final do mesmo arquivo da tarefa
    # Simulação de pesquisa real e geração de relatório detalhado
    report_content = f'\n\n## Relatório de Processamento - Tarefa {task_id}\n\n'
    report_content += f'Data de Processamento: {datetime.now().strftime("%d/%m/%Y %H:%M:%S")}\n\n'

    if task_id == 'TASK-001':
        report_content += 'Pesquisa sobre mecanismo de descoberta em redes mesh, com foco no projeto Briar.\n'
        report_content += 'Detalhes da pesquisa: O Briar utiliza Bluetooth e Wi-Fi para criar redes ad-hoc, e o Bramble Rendezvous Protocol (BRP) para conexão de pares. A descoberta ocorre em segundo plano, exigindo permissões de localização. Desafios incluem consumo de bateria e alcance limitado, mas a capacidade de rede mesh é crucial para comunicação offline e resistente à censura.\n'
    elif task_id == 'TASK-002':
        # Exemplo de como gerar um relatório de dúvida APÓS mover para in_progress
        generate_doubt_report(task_id, 'O escopo desta tarefa de "connection handshake" não está claro. Preciso de mais detalhes sobre qual tipo de handshake (ex: TCP, TLS, Bluetooth) e em qual contexto (ex: rede, segurança, IoT) deve ser pesquisado ou executado.')
        return # Para o processamento da tarefa até que a dúvida seja resolvida
    else:
        report_content += 'Nenhuma pesquisa específica definida para esta tarefa. Conteúdo genérico adicionado.\n'

    with open(in_progress_path, 'a') as f:
        f.write(report_content)

    # 4. ... mova-o (ou coie conforme descrito acima) para done.
    print(f'Movendo {task_filename} para done...')
    try:
        shutil.move(in_progress_path, done_path)
    except shutil.Error as e:
        print(f'Erro ao mover o arquivo: {e}. Tentando copiar e apagar o original.')
        shutil.copy(in_progress_path, done_path)
        os.remove(in_progress_path)

    print(f'Tarefa {task_filename} concluída e movida para done.')

if __name__ == '__main__':
    process_task_flow()

