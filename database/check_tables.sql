-- Script para verificar se as tabelas estão corretamente implementadas

SELECT
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'seu_banco_de_dados'
ORDER BY TABLE_NAME;

-- Verificar colunas de cada tabela
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_KEY
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'seu_banco_de_dados'
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- Verificar chaves estrangeiras
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'seu_banco_de_dados'
AND REFERENCED_TABLE_NAME IS NOT NULL;