# backend/app/main.py (v1.1)

from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from . import database, models

# Cria as tabelas no banco de dados (se não existirem)
try:
    print("Verificando e criando tabelas no banco de dados...")
    models.Base.metadata.create_all(bind=database.engine)
    print("Comando create_all executado com sucesso.")
except Exception as e:
    print(f"Ocorreu um erro ao criar as tabelas: {e}")

app = FastAPI(title="Project C3 API")

# --- Dependência para obter a sessão do DB ---
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

# --- Endpoints da API ---

@app.get("/")
def read_root():
    return {"message": "Bem-vindo à API do Project C3!"}

@app.get("/api/v1/segments")
def read_segments(db: Session = Depends(get_db)):
    """
    Endpoint para buscar todos os segmentos do banco de dados.
    """
    segments = db.query(models.Segment).order_by(models.Segment.id).all()
    return segments

# --- NOVO ENDPOINT ---
@app.get("/api/v1/segments/{segment_id}/phases")
def read_phases_for_segment(segment_id: int, db: Session = Depends(get_db)):
    """
    Endpoint para buscar todas as fases de um segmento específico.
    """
    # Primeiro, verifica se o segmento existe
    segment = db.query(models.Segment).filter(models.Segment.id == segment_id).first()
    if not segment:
        raise HTTPException(status_code=404, detail="Segmento não encontrado")
    
    # Busca as fases associadas a esse segmento
    phases = db.query(models.Phase).filter(models.Phase.segment_id == segment_id).order_by(models.Phase.order).all()
    return phases


