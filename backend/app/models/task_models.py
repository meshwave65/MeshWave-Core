from sqlalchemy import Column, Integer, String, Text, DateTime, Enum as SQLAlchemyEnum, ForeignKey
from sqlalchemy.orm import relationship
from ..database import Base  # Importa a Base do arquivo central
import enum

# Este Enum pode ser usado para o campo status, se desejado
class TaskStatusEnum(enum.Enum):
    open = 1
    in_progress = 2
    on_hold = 3
    done = 4
    canceled = 5

class Segment(Base):
    __tablename__ = "segments"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), unique=True, nullable=False, index=True)
    description = Column(Text, nullable=True)
    
    phases = relationship("Phase", back_populates="segment")

class Phase(Base):
    __tablename__ = "phases"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    order = Column(Integer, nullable=False)
    segment_id = Column(Integer, ForeignKey("segments.id"), nullable=False)
    
    segment = relationship("Segment", back_populates="phases")
    modules = relationship("Module", back_populates="phase")

class Module(Base):
    __tablename__ = "modules"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    order = Column(Integer, nullable=False)
    phase_id = Column(Integer, ForeignKey("phases.id"), nullable=False)
    
    phase = relationship("Phase", back_populates="modules")
    tasks = relationship("Task", back_populates="module")

class Task(Base):
    __tablename__ = "tasks"
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    status = Column(SQLAlchemyEnum(TaskStatusEnum), nullable=False, default=TaskStatusEnum.open)
    module_id = Column(Integer, ForeignKey("modules.id"), nullable=False)
    
    module = relationship("Module", back_populates="tasks")

