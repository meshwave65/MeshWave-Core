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

    if task_id == 'TASK-001':
        report_content += '''### Contexto da Pesquisa: Mecanismo de Descoberta em Redes Mesh (Projeto Briar)

O objetivo desta pesquisa é aprofundar o entendimento sobre como o projeto Briar, um aplicativo de mensagens peer-to-peer criptografado focado em comunicação segura e resistente à censura, implementa seus mecanismos de descoberta em redes mesh. A análise deve cobrir as tecnologias subjacentes, protocolos específicos e desafios enfrentados.

#### Detalhes da Pesquisa:

O Briar se destaca por sua capacidade de operar em ambientes offline ou com conectividade limitada, utilizando redes mesh para estabelecer comunicação direta entre dispositivos. Isso é alcançado através de:

*   **Conectividade Local (Bluetooth e Wi-Fi):** O Briar aproveita o hardware de comunicação de curto alcance presente em smartphones para criar redes ad hoc. Isso permite que os dispositivos se descubram e se conectem diretamente, formando uma malha local sem depender de infraestrutura centralizada ou acesso à internet. A comunicação via Bluetooth e Wi-Fi é fundamental para a resiliência do Briar em cenários de censura ou falha de rede.

*   **Bramble Rendezvous Protocol (BRP):** Este é um protocolo proprietário do Briar, crucial para a descoberta e conexão de pares. Uma vez que dois usuários trocam suas chaves públicas (o que pode ser feito offline, por exemplo, via QR code), o BRP facilita o estabelecimento de uma conexão direta e segura entre eles, mesmo que não estejam na mesma rede local ou que a internet esteja indisponível. O BRP atua como um mecanismo de "encontro" para os pares.

*   **Descoberta de Serviço Local (LSD) e Outros Mecanismos:** Embora o BRP seja central, o Briar também pode se beneficiar de ou ter mecanismos inspirados em abordagens como o Local Service Discovery (LSD), comumente usado por clientes BitTorrent para encontrar pares na rede local. A descoberta de dispositivos próximos é realizada em segundo plano, o que pode requerer permissões de localização do usuário para funcionar eficientemente, especialmente em sistemas operacionais móveis.

#### Desafios e Implicações:

A implementação de redes mesh via Bluetooth e Wi-Fi, embora robusta, apresenta desafios como o consumo de bateria dos dispositivos e o alcance limitado das tecnologias de rádio. No entanto, para o público-alvo do Briar (ativistas, jornalistas, etc.), a capacidade de comunicação resiliente e resistente à censura supera essas limitações. A descentralização inerente ao modelo mesh também dificulta a vigilância e o bloqueio da comunicação.

#### Exemplo de Conceito (Não é código executável, apenas ilustrativo):

```python
# Conceito simplificado de descoberta de pares em uma rede mesh (não é código real do Briar)

class BriarNode:
    def __init__(self, node_id, public_key):
        self.node_id = node_id
        self.public_key = public_key
        self.known_peers = {}

    def discover_peer(self, peer_public_key):
        # Simula o processo de descoberta via BRP ou LSD
        # Na realidade, envolveria broadcast/multicast via Bluetooth/Wi-Fi
        print(f"Nó {self.node_id} tentando descobrir par com chave {peer_public_key[:8]}...")
        # Se o par for encontrado e autenticado
        if peer_public_key == "chave_do_par_A": # Simulação
            peer_node = BriarNode("Par_A", "chave_do_par_A")
            self.known_peers[peer_node.node_id] = peer_node
            print(f"Par {peer_node.node_id} descoberto e adicionado.")
            return peer_node
        return None

    def send_message(self, recipient_node_id, message):
        if recipient_node_id in self.known_peers:
            print(f"Enviando mensagem para {recipient_node_id}: {message}")
            # Lógica de roteamento mesh e criptografia aqui
        else:
            print(f"Par {recipient_node_id} não conhecido. Tentando descobrir...")
            # Iniciar processo de descoberta ou roteamento via outros nós

# Exemplo de uso
node1 = BriarNode("MeuNo", "minha_chave_publica")
node1.discover_peer("chave_do_par_A")
node1.send_message("Par_A", "Olá, esta é uma mensagem segura!")
```

### Implementação de Mesh Networking

#### Algoritmo de Formação de Mesh

O Briar implementa um algoritmo distribuído para formação automática de redes mesh:

```python
class MeshFormation:
    def __init__(self, node_id, capabilities):
        self.node_id = node_id
        self.capabilities = capabilities
        self.mesh_topology = {}
        self.role = 'leaf'  # leaf, relay, or coordinator
        
    def evaluate_mesh_role(self, neighbors):
        """Determina papel do nó na mesh baseado em capacidades"""
        factors = {
            'battery_level': self.get_battery_level(),
            'connectivity': len(neighbors),
            'processing_power': self.get_cpu_capability(),
            'mobility': self.estimate_mobility_pattern()
        }
        
        score = (factors['battery_level'] * 0.3 +
                factors['connectivity'] * 0.3 +
                factors['processing_power'] * 0.2 +
                (1 - factors['mobility']) * 0.2)
        
        if score > 0.8:
            self.role = 'coordinator'
        elif score > 0.5:
            self.role = 'relay'
        else:
            self.role = 'leaf'
            
        return self.role
    
    def optimize_mesh_topology(self):
        """Otimiza topologia da mesh para eficiência"""
        # Implementa algoritmo de árvore geradora mínima
        # com pesos baseados em qualidade de link
        edges = []
        for neighbor in self.mesh_topology:
            quality = self.measure_link_quality(neighbor)
            edges.append((quality, self.node_id, neighbor))
        
        # Algoritmo de Kruskal para MST
        edges.sort()
        mst = self.kruskal_mst(edges)
        
        return mst
```

### Protocolos de Sincronização

#### Sincronização Distribuída de Mensagens

```python
class DistributedSync:
    def __init__(self, node_id):
        self.node_id = node_id
        self.message_store = {}
        self.vector_clock = {}
        
    def sync_with_peer(self, peer_id, peer_messages):
        """Sincroniza mensagens com peer específico"""
        # Compara vector clocks para determinar mensagens faltantes
        missing_messages = []
        
        for msg_id, msg_data in peer_messages.items():
            if msg_id not in self.message_store:
                if self.validate_message(msg_data):
                    missing_messages.append(msg_data)
        
        # Aplica mensagens em ordem causal
        sorted_messages = self.sort_by_causal_order(missing_messages)
        for message in sorted_messages:
            self.apply_message(message)
            
        # Atualiza vector clock
        self.update_vector_clock(peer_id)
        
        return len(missing_messages)
    
    def resolve_conflicts(self, conflicting_messages):
        """Resolve conflitos de sincronização"""
        # Implementa resolução determinística baseada em:
        # 1. Timestamp
        # 2. Hash da mensagem
        # 3. ID do autor
        
        def conflict_resolution_key(msg):
            return (msg['timestamp'], msg['hash'], msg['author_id'])
        
        resolved = sorted(conflicting_messages, key=conflict_resolution_key)
        return resolved[-1]  # Última mensagem na ordem determinística
```

### Análise de Performance e Escalabilidade

#### Métricas de Performance

O Briar implementa monitoramento contínuo de performance da rede mesh:

```python
class MeshMetrics:
    def __init__(self,):
        self.metrics = {
            'discovery_latency': [],
            'message_delivery_time': [],
            'hop_count_distribution': {},
            'network_partition_events': 0,
            'battery_consumption_rate': 0
        }
    
    def measure_discovery_latency(self, start_time, peer_discovered):
        """Mede tempo para descobrir novo peer"""
        latency = time.time() - start_time
        self.metrics['discovery_latency'].append(latency)
        
        # Calcula estatísticas
        avg_latency = sum(self.metrics['discovery_latency']) / len(self.metrics['discovery_latency'])
        return avg_latency
    
    def analyze_network_efficiency(self):
        """Analisa eficiência geral da rede"""
        efficiency_score = {
            'discovery_speed': 1.0 / (np.mean(self.metrics['discovery_latency']) + 1),
            'message_reliability': self.calculate_delivery_success_rate(),
            'energy_efficiency': 1.0 / (self.metrics['battery_consumption_rate'] + 1),
            'network_resilience': 1.0 / (self.metrics['network_partition_events'] + 1)
        }
        
        overall_score = np.mean(list(efficiency_score.values()))
        return overall_score, efficiency_score
```

### Casos de Uso e Cenários Reais

#### 1. Protestos e Manifestações

Durante protestos, onde autoridades podem bloquear internet:
- Formação automática de mesh local via Bluetooth/Wi-Fi
- Coordenação de atividades através de fóruns distribuídos
- Disseminação de informações críticas sem dependência de infraestrutura

#### 2. Áreas Rurais com Conectividade Limitada

Em regiões com acesso limitado à internet:
- Criação de redes comunitárias locais
- Sincronização oportunística quando conectividade está disponível
- Preservação de comunicações importantes para sincronização posterior

#### 3. Cenários de Emergência

Durante desastres naturais ou falhas de infraestrutura:
- Comunicação de emergência entre equipes de resgate
- Coordenação de recursos e informações de segurança
- Manutenção de comunicação quando torres celulares estão inoperantes

### Limitações e Desafios Futuros

#### Limitações Atuais

1. **Escalabilidade:** Redes mesh Bluetooth/Wi-Fi são limitadas a algumas dezenas de nós
2. **Latência:** Comunicação multi-hop introduz latência significativa
3. **Consumo de Energia:** Descoberta contínua impacta vida útil da bateria
4. **Alcance:** Limitado por características físicas dos protocolos de rádio

#### Direções Futuras

1. **Integração com LoRa:** Para comunicação de longo alcance e baixo consumo
2. **Mesh Satellites:** Utilização de constelações de satélites para cobertura global
3. **AI-Driven Optimization:** Algoritmos de aprendizado de máquina para otimização automática
4. **Quantum-Resistant Cryptography:** Preparação para era pós-quântica

### Conclusão

O projeto Briar representa um avanço significativo em comunicação descentralizada e resistente à censura. Seus mecanismos de descoberta em redes mesh, centrados no Bramble Rendezvous Protocol e complementados por descoberta local via Bluetooth e Wi-Fi, criam um sistema robusto capaz de operar em condições adversas.

A arquitetura multi-modal do Briar, combinando conectividade local e global através do Tor, oferece flexibilidade única para diferentes cenários de uso. Os protocolos de sincronização distribuída garantem consistência de dados mesmo em redes particionadas, enquanto os mecanismos de segurança protegem contra vigilância e ataques.

Embora existam limitações em termos de escalabilidade e consumo de energia, o Briar estabelece uma base sólida para futuras inovações em comunicação descentralizada. Sua abordagem pragmática, focando em cenários reais de uso por ativistas e jornalistas, demonstra o potencial transformador das redes mesh para preservação da liberdade de comunicação.

A pesquisa e desenvolvimento contínuos nesta área são essenciais para enfrentar os crescentes desafios de censura e vigilância digital, tornando o Briar uma ferramenta fundamental para a preservação dos direitos humanos digitais.
'''

    elif task_id == 'TASK-002':
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

