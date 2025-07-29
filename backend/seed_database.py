# backend/seed_database.py

from sqlalchemy.orm import sessionmaker
from app.database import engine
from app.models.task_models import Segment

# Dados iniciais do nosso roadmap
segments_data = [
    {"name": "APLICACAO", "description": "Desenvolvimento do aplicativo cliente e interface."},
    {"name": "BLOCKCHAIN", "description": "Camada de identidade e confiança do ecossistema."},
    {"name": "HARDWARE", "description": "Integração e otimização para hardware específico."},
    {"name": "INTEGRACAO", "description": "Integração com sistemas e APIs externas."},
    {"name": "OtimizacaoIA", "description": "Otimização dos modelos de IA e do Q-CyPIA."},
    {"name": "RedeMESH", "description": "Desenvolvimento do core da rede P2P e mesh."},
    {"name": "SEGURANCA", "description": "Implementação das camadas de segurança, incluindo ARC."},
    {"name": "STORE&PROCESS", "description": "Armazenamento e processamento de dados distribuídos."}
]

# Configura a sessão com o banco de dados
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
db = SessionLocal()

print("🌱 Iniciando o processo de seeding do banco de dados...")

try:
    existing_segments = db.query(Segment).all()
    existing_names = {s.name for s in existing_segments}

    for seg_data in segments_data:
        if seg_data["name"] not in existing_names:
            new_segment = Segment(name=seg_data["name"], description=seg_data["description"])
            db.add(new_segment)
            print(f"  -> Adicionando segmento: {seg_data['name']}")
        else:
            print(f"  -> Segmento '{seg_data['name']}' já existe. Pulando.")
    
    db.commit()
    print("✅ Seeding concluído com sucesso!")

except Exception as e:
    print(f"❌ Ocorreu um erro durante o seeding: {e}")
    db.rollback()
finally:
    db.close()
    print("🔌 Conexão com o banco de dados fechada.")


