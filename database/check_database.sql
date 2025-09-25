-- Verificação Completa da Estrutura do Banco ProjectFlow

-- 1. RESUMO DAS TABELAS
SELECT
    TABLE_NAME,
    TABLE_ROWS as 'Registros',
    ENGINE as 'Motor',
    TABLE_COLLATION as 'Collation'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME;

-- 2. ESTRUTURA DAS TABELAS PRINCIPAIS
SELECT
    TABLE_NAME as 'Tabela',
    COLUMN_NAME as 'Coluna',
    DATA_TYPE as 'Tipo',
    IS_NULLABLE as 'Pode Null?',
    COLUMN_KEY as 'Chave'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- 3. RELACIONAMENTOS E CHAVES ESTRANGEIRAS
SELECT
    TABLE_NAME as 'Tabela',
    COLUMN_NAME as 'Coluna',
    CONSTRAINT_NAME as 'Constraint',
    REFERENCED_TABLE_NAME as 'Tabela Referenciada',
    REFERENCED_COLUMN_NAME as 'Coluna Referenciada'
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- 4. DADOS DE EXEMPLO - CONTAGEM
SELECT
    'usuarios' AS tabela,
    COUNT(*) AS total_registros,
    (SELECT GROUP_CONCAT(username) FROM usuarios LIMIT 3) AS exemplos
FROM usuarios

UNION ALL SELECT 'projetos', COUNT(*), (SELECT GROUP_CONCAT(nome) FROM projetos LIMIT 3) FROM projetos
UNION ALL SELECT 'tarefas', COUNT(*), (SELECT GROUP_CONCAT(titulo) FROM tarefas LIMIT 3) FROM tarefas
UNION ALL SELECT 'equipes', COUNT(*), (SELECT GROUP_CONCAT(nome) FROM equipes LIMIT 3) FROM equipes
UNION ALL SELECT 'equipe_projetos', COUNT(*), CONCAT('Associações: ', COUNT(*)) FROM equipe_projetos
UNION ALL SELECT 'equipe_usuarios', COUNT(*), CONCAT('Associações: ', COUNT(*)) FROM equipe_usuarios;

-- 5. VERIFICAÇÃO DE INTEGRIDADE DOS RELACIONAMENTOS
SELECT
    'Projetos sem responsável' as verificacao,
    COUNT(*) as problemas
FROM projetos
WHERE id_responsavel IS NULL

UNION ALL
SELECT
    'Tarefas com responsável inválido',
    COUNT(*)
FROM tarefas t
LEFT JOIN usuarios u ON t.id_responsavel = u.id
WHERE u.id IS NULL

UNION ALL
SELECT
    'Tarefas com projeto inválido',
    COUNT(*)
FROM tarefas t
LEFT JOIN projetos p ON t.id_projeto = p.id
WHERE p.id IS NULL;