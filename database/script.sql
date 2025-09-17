-- Tabela de usuários com todas as colunas necessárias
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    cpf VARCHAR(14),
    cargo VARCHAR(50),
    perfil VARCHAR(20) DEFAULT 'COLABORADOR',
    ativo BOOLEAN DEFAULT true,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserir usuários de exemplo
INSERT INTO usuarios (username, password, nome, email, cpf, cargo, perfil) VALUES
('admin', 'admin123', 'Administrador', 'admin@email.com', '000.000.000-00', 'Administrador', 'ADMINISTRADOR'),
('user', 'user123', 'Usuário Teste', 'user@email.com', '111.111.111-11', 'Colaborador', 'COLABORADOR'),
('gerente', 'gerente123', 'Gerente Projetos', 'gerente@email.com', '222.222.222-22', 'Gerente', 'GERENTE');

-- Tabela de projetos
CREATE TABLE IF NOT EXISTS projetos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    data_inicio DATE,
    data_fim DATE,
    status VARCHAR(20) DEFAULT 'PENDENTE',
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de tarefas
CREATE TABLE IF NOT EXISTS tarefas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    projeto_id INT,
    titulo VARCHAR(100) NOT NULL,
    descricao TEXT,
    responsavel VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDENTE',
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_prevista DATE,
    data_conclusao DATE,
    FOREIGN KEY (projeto_id) REFERENCES projetos(id) ON DELETE CASCADE
);

-- Inserir alguns projetos de exemplo
INSERT INTO projetos (nome, descricao, data_inicio, data_fim, status) VALUES
('Sistema de Gestão', 'Desenvolvimento do sistema de gestão de projetos A3', '2024-01-15', '2024-06-15', 'EM_ANDAMENTO'),
('Site Corporativo', 'Desenvolvimento do novo site da empresa', '2024-02-01', '2024-04-30', 'PENDENTE');

-- Inserir tarefas de exemplo
INSERT INTO tarefas (projeto_id, titulo, descricao, responsavel, status, data_prevista) VALUES
(1, 'Criar Login', 'Implementar sistema de autenticação de usuários', 'admin', 'CONCLUIDA', '2024-01-31'),
(1, 'CRUD Usuários', 'Implementar cadastro de usuários', 'gerente', 'EM_ANDAMENTO', '2024-02-15'),
(1, 'Relatórios', 'Criar relatórios de projetos', 'user', 'PENDENTE', '2024-03-01'),
(2, 'Design Layout', 'Criar design do novo site', 'user', 'PENDENTE', '2024-02-28');