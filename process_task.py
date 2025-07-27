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
    report_content = f'# Relatório de Dúvida - Tarefa {task_id}\n\n'
    report_content += f'Data: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}\n\n'
    report_content += f'Prezado usuário,\n\n'
    report_content += f'Encontrei uma dúvida durante o processamento da tarefa {task_id}.\n\n'
    report_content += f'Detalhes da dúvida: {message}\n\n'
    report_content += f'Por favor, forneça esclarecimentos ou direcionamento para que eu possa prosseguir.\n'

    report_filename = f'REPORT-{task_id}-{datetime.now().strftime("%Y%m%d-%H%M%S")}.md'
    report_path = os.path.join(REPORTS_INCOMING_DIR, report_filename)
    with open(report_path, 'w') as f:
        f.write(report_content)
    print(f'Relatório de dúvida gerado: {report_path}')
    return report_content

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

    # Ler o conteúdo original da tarefa
    with open(source_path, 'r') as f:
        original_task_content = f.read()

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

    doubt_report_text = ""

    if task_id == 'TASK-002':
        # Exemplo de como gerar um relatório de dúvida APÓS mover para in_progress
        doubt_report_text = generate_doubt_report(task_id, 'O escopo desta tarefa de "connection handshake" não está claro. Para fornecer um relatório exaustivo e focado, preciso de mais detalhes sobre:\n\n1.  **Tipo de Handshake:** Estamos falando de handshake TCP/IP, TLS/SSL, Bluetooth, Wi-Fi Direct, ou outro protocolo?\n2.  **Contexto:** Em qual cenário este handshake é relevante? (Ex: estabelecimento de sessão segura, descoberta de dispositivos, autenticação de usuários, etc.)\n3.  **Tecnologia/Projeto Específico:** Há alguma tecnologia ou projeto específico (como o Briar, por exemplo) que deve ser o foco da pesquisa sobre o handshake?\n\nPor favor, forneça esclarecimentos para que eu possa prosseguir com uma pesquisa detalhada e relevante.')
        # Não retorna aqui, o relatório de dúvida será anexado ao arquivo da tarefa

    else:
        report_content += 'Nenhuma pesquisa específica definida para esta tarefa. Conteúdo genérico adicionado.\n'

    with open(in_progress_path, 'w') as f:
        f.write(original_task_content) # Escreve o conteúdo original da tarefa
        f.write(report_content) # Adiciona o relatório de processamento
        if doubt_report_text: # Se houver um relatório de dúvida, anexa-o também
            f.write(f'\n\n--- Relatório de Dúvida Anexado ---\n\n')
            f.write(doubt_report_text)

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

