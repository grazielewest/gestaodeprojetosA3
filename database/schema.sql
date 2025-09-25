-- Script de Banco de Dados - Gestão de Projetos
-- Estrutura Real Implementada
-- Versão: 1.0
-- Data: 2024-01-15

SET FOREIGN_KEY_CHECKS=0;

-- Tabela de usuários
CREATE TABLE IF NOT EXISTS usuarios (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) DEFAULT NULL,
    cargo VARCHAR(50) DEFAULT NULL,
    perfil VARCHAR(20) DEFAULT NULL,
    ativo TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY username (username),
    UNIQUE KEY email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabela de equipes
CREATE TABLE IF NOT EXISTS equipes (
    id INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_criacao TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NULL DEFAULT NULL,
    ativa TINYINT(1) DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabela de projetos
CREATE TABLE IF NOT EXISTS projetos (
    id INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    status ENUM('Planejamento', 'Em Andamento', 'Concluído', 'Cancelado') DEFAULT 'Planejamento',
    data_inicio DATE DEFAULT NULL,
    data_fim DATE DEFAULT NULL,
    orcamento DECIMAL(15,2) DEFAULT NULL,
    id_responsavel INT DEFAULT NULL,
    prioridade ENUM('Baixa', 'Média', 'Alta') DEFAULT 'Média',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY id_responsavel (id_responsavel),
    CONSTRAINT projetos_ibfk_1 FOREIGN KEY (id_responsavel) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabela de tarefas
CREATE TABLE IF NOT EXISTS tarefas (
    id INT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    id_projeto INT NOT NULL,
    id_responsavel INT NOT NULL,
    status VARCHAR(20) DEFAULT 'Pendente',
    data_inicio_prevista DATE DEFAULT NULL,
    data_fim_prevista DATE DEFAULT NULL,
    data_inicio_real DATE DEFAULT NULL,
    data_fim_real DATE DEFAULT NULL,
    prioridade VARCHAR(20) DEFAULT 'Média',
    data_criacao TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY id_projeto (id_projeto),
    KEY id_responsavel (id_responsavel),
    CONSTRAINT tarefas_ibfk_1 FOREIGN KEY (id_projeto) REFERENCES projetos (id) ON DELETE CASCADE,
    CONSTRAINT tarefas_ibfk_2 FOREIGN KEY (id_responsavel) REFERENCES usuarios (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabela de relacionamento equipe-projetos (M:N)
CREATE TABLE IF NOT EXISTS equipe_projetos (
    id INT NOT NULL AUTO_INCREMENT,
    equipe_id INT NOT NULL,
    projeto_id INT NOT NULL,
    data_vinculo TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY unique_equipe_projeto (equipe_id, projeto_id),
    KEY projeto_id (projeto_id),
    CONSTRAINT equipe_projetos_ibfk_1 FOREIGN KEY (equipe_id) REFERENCES equipes (id) ON DELETE CASCADE,
    CONSTRAINT equipe_projetos_ibfk_2 FOREIGN KEY (projeto_id) REFERENCES projetos (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabela de relacionamento equipe-usuarios (M:N)
CREATE TABLE IF NOT EXISTS equipe_usuarios (
    id INT NOT NULL AUTO_INCREMENT,
    equipe_id INT NOT NULL,
    usuario_id INT NOT NULL,
    data_vinculo TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY unique_equipe_usuario (equipe_id, usuario_id),
    KEY usuario_id (usuario_id),
    CONSTRAINT equipe_usuarios_ibfk_1 FOREIGN KEY (equipe_id) REFERENCES equipes (id) ON DELETE CASCADE,
    CONSTRAINT equipe_usuarios_ibfk_2 FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS=1;