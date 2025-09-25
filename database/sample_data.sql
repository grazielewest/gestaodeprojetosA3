-- Dados de Exemplo para o Sistema ProjectFlow

-- Inserir usuários
INSERT INTO usuarios (username, password, nome, email, cpf, cargo, perfil) VALUES
('admin', '$2a$10$rL6.8sQxZJkqB1VY5WQ3O.9zQaM7nB2cC3dE4fG5hV6jK7lM8nP9', 'Administrador do Sistema', 'admin@projectflow.com', '123.456.789-00', 'Administrador', 'ADMIN'),
('maria.silva', '$2a$10$rL6.8sQxZJkqB1VY5WQ3O.9zQaM7nB2cC3dE4fG5hV6jK7lM8nP9', 'Maria Silva', 'maria.silva@empresa.com', '111.222.333-44', 'Gerente de Projetos', 'GERENTE'),
('joao.santos', '$2a$10$rL6.8sQxZJkqB1VY5WQ3O.9zQaM7nB2cC3dE4fG5hV6jK7lM8nP9', 'João Santos', 'joao.santos@empresa.com', '222.333.444-55', 'Desenvolvedor Sênior', 'COLABORADOR'),
('ana.oliveira', '$2a$10$rL6.8sQxZJkqB1VY5WQ3O.9zQaM7nB2cC3dE4fG5hV6jK7lM8nP9', 'Ana Oliveira', 'ana.oliveira@empresa.com', '333.444.555-66', 'Designer UX/UI', 'COLABORADOR'),
('carlos.lima', '$2a$10$rL6.8sQxZJkqB1VY5WQ3O.9zQaM7nB2cC3dE4fG5hV6jK7lM8nP9', 'Carlos Lima', 'carlos.lima@empresa.com', '444.555.666-77', 'Analista de QA', 'COLABORADOR');

-- Inserir equipes
INSERT INTO equipes (nome, descricao, data_atualizacao) VALUES
('Equipe de Desenvolvimento Backend', 'Responsável pelo desenvolvimento da camada backend do sistema', CURRENT_TIMESTAMP),
('Equipe de Desenvolvimento Frontend', 'Responsável pelo desenvolvimento da interface do usuário', CURRENT_TIMESTAMP),
('Equipe de Design e UX', 'Responsável pela experiência do usuário e interface visual', CURRENT_TIMESTAMP),
('Equipe de Qualidade', 'Responsável por testes e garantia de qualidade', CURRENT_TIMESTAMP);

-- Inserir projetos
INSERT INTO projetos (nome, descricao, status, data_inicio, data_fim, id_responsavel, prioridade) VALUES
('Sistema de Gestão ProjectFlow', 'Desenvolvimento do sistema integrado de gestão de projetos', 'Em Andamento', '2024-01-15', '2024-06-30', 2, 'Alta'),
('Portal Corporativo', 'Novo site institucional da empresa', 'Planejamento', '2024-03-01', '2024-05-31', 2, 'Média'),
('Aplicativo Mobile Vendas', 'App para força de vendas em campo', 'Planejamento', '2024-04-01', '2024-08-31', 3, 'Alta'),
('Sistema de BI', 'Plataforma de Business Intelligence', 'Planejamento', '2024-05-01', '2024-09-30', 2, 'Média');

-- Inserir tarefas
INSERT INTO tarefas (titulo, descricao, id_projeto, id_responsavel, status, prioridade, data_inicio_prevista, data_fim_prevista) VALUES
('Modelagem do Banco de Dados', 'Criar modelo entidade-relacionamento e script SQL', 1, 2, 'Concluída', 'Alta', '2024-01-15', '2024-01-25'),
('Implementar Sistema de Autenticação', 'Desenvolver login, registro e controle de acesso', 1, 3, 'Em Execução', 'Alta', '2024-01-26', '2024-02-15'),
('Criar Interface do Dashboard', 'Desenvolver dashboard principal com métricas', 1, 4, 'Pendente', 'Média', '2024-02-16', '2024-03-10'),
('Definir Escopo do Portal', 'Reunir requisitos e definir escopo do projeto', 2, 2, 'Pendente', 'Média', '2024-03-01', '2024-03-15'),
('Prototipar Telas do App', 'Criar protótipos das principais telas do aplicativo', 3, 4, 'Pendente', 'Alta', '2024-04-01', '2024-04-15'),
('Configurar Ambiente de Testes', 'Preparar ambiente para testes automatizados', 1, 5, 'Pendente', 'Média', '2024-02-01', '2024-02-20');

-- Inserir associações equipe-projetos
INSERT INTO equipe_projetos (equipe_id, projeto_id) VALUES
(1, 1), -- Backend no ProjectFlow
(2, 1), -- Frontend no ProjectFlow
(3, 1), -- Design no ProjectFlow
(4, 1), -- QA no ProjectFlow
(2, 2), -- Frontend no Portal
(3, 2), -- Design no Portal
(1, 3), -- Backend no App Mobile
(3, 3), -- Design no App Mobile
(4, 3), -- QA no App Mobile
(1, 4), -- Backend no BI
(4, 4); -- QA no BI

-- Inserir associações equipe-usuarios
INSERT INTO equipe_usuarios (equipe_id, usuario_id) VALUES
(1, 3), -- João na equipe Backend
(2, 3), -- João na equipe Frontend
(2, 4), -- Ana na equipe Frontend
(3, 4), -- Ana na equipe Design
(4, 5), -- Carlos na equipe QA
(1, 2), -- Maria na equipe Backend (como gerente)
(2, 2); -- Maria na equipe Frontend (como gerente)