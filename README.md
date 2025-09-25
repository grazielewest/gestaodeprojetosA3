# Sistema de Gestão de Projetos e Equipes
Gestão de Projetos - Projeto A3

## Descrição
Sistema desenvolvido em Java para gestão de projetos, equipes e tarefas, com interface JavaFX e banco de dados MySQL.

## Informações Acadêmicas
- **Universidade**: [Universidade Anhembi Morumbi]
- **Curso**: [Análise e Desenvolvimento de Sistemas]
- **Disciplina**: [Programação de Soluções Computacionais]
- **Professor**: 
- **Aluno**: [Tamires Graziele de Souza]
- **Ano/Semestre**: [2025/01]

## Tecnologias Utilizadas
- Java 11+
- JavaFX
- MySQL
- MVC Pattern
- JDBC

## Funcionalidades
- [x] Cadastro de usuários com perfis (Admin, Gerente, Colaborador)
- [x] Gestão de projetos e equipes
- [x] Alocação de tarefas
- [x] Relatórios e dashboards
- [x] Autenticação e controle de acesso
- [x] Relacionamentos muitos-para-muitos entre equipes e projetos

## Estrutura do Banco de Dados

### Diagrama Entidade-Relacionamento

erDiagram
    USUARIOS {
        int id PK
        varchar username UK
        varchar password
        varchar nome
        varchar email UK
        varchar cpf
        varchar cargo
        varchar perfil
        boolean ativo
        timestamp created_at
    }
    
    EQUIPES {
        int id PK
        varchar nome
        text descricao
        timestamp data_criacao
        timestamp data_atualizacao
        boolean ativa
    }
    
    PROJETOS {
        int id PK
        varchar nome
        text descricao
        enum status
        date data_inicio
        date data_fim
        decimal orcamento
        int id_responsavel FK
        enum prioridade
        timestamp created_at
        timestamp updated_at
    }
    
    TAREFAS {
        int id PK
        varchar titulo
        text descricao
        int id_projeto FK
        int id_responsavel FK
        varchar status
        date data_inicio_prevista
        date data_fim_prevista
        date data_inicio_real
        date data_fim_real
        varchar prioridade
        timestamp data_criacao
        timestamp data_atualizacao
    }
    
    EQUIPE_PROJETOS {
        int id PK
        int equipe_id FK
        int projeto_id FK
        timestamp data_vinculo
    }
    
    EQUIPE_USUARIOS {
        int id PK
        int equipe_id FK
        int usuario_id FK
        timestamp data_vinculo
    }
    
    USUARIOS ||--o{ PROJETOS : responsavel
    USUARIOS ||--o{ TAREFAS : responsavel
    PROJETOS ||--o{ TAREFAS : contem
    EQUIPES }o--o{ PROJETOS : equipe_projetos
    EQUIPES }o--o{ USUARIOS : equipe_usuarios
    
## Modelagem Implementada

- 6 Tabelas: 4 entidades principais + 2 tabelas de relacionamento
- Normalização: 3ª Forma Normal (3FN)
- Relacionamentos: M:N implementados com tabelas associativas
- Integridade Referencial: Chaves estrangeiras com CASCADE/RESTRICT
     

## Scripts de Banco de Dados

### Estrutura das Tabelas

O sistema utiliza 6 tabelas inter-relacionadas:

1. usuarios: Gestão de usuários e perfis de acesso
2. projetos: Cadastro e acompanhamento de projetos
3. tarefas: Atividades associadas aos projetos
4. equipes: Grupos de trabalho
5. equipe_projetos: Relacionamento muitos-para-muitos (equipes × projetos)
6. equipe_usuarios: Relacionamento muitos-para-muitos (equipes × usuários)

## Como Configurar o Banco de Dados

1. Criar o Banco de Dados
   
CREATE DATABASE gestao_projetos;
USE gestao_projetos; 

2. Executar os Scripts

-Estrutura das tabelas
mysql -u root -p gestao_projetos < database/schema.sql

-Dados de exemplo (opcional)
mysql -u root -p gestao_projetos < database/sample_data.sql

3. Configurar a Conexão
 
- Edite o arquivo src/main/resources/config.properties:
   db.url=jdbc:mysql://localhost:3306/gestao_projetos
   db.username=seu_usuario
   db.password=sua_senha

## Pré-requisitos
1. Java 11 ou superior
2. MySQL 8.0 ou superior
3. Maven 3.6+

## Funcionalidades Implementadas

### Módulo de Usuários
- Cadastro com validação de CPF/Email único
- Perfis de acesso (Admin, Gerente, Colaborador)
- Controle de ativação/desativação

### Módulo de Projetos
- CRUD completo de projetos
- Controle de status e prioridades
- Associação com equipes responsáveis

### Módulo de Tarefas
- Criação e atribuição de tarefas
- Controle de prazos e status
- Dashboard de acompanhamento

### Módulo de Equipes
- Gestão de equipes e membros
- Alocação em múltiplos projetos
- Relacionamentos flexíveis

## Estrutura Técnica

### Padrões de Projeto Implementados
- DAO Pattern: Isolamento da camada de dados
- MVC: Separação de concerns na interface
- Singleton: Gerenciamento de conexões DB


## Licença
Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## Disclaimer
Este software foi desenvolvido para fins acadêmicos. Não há garantia de funcionamento em ambientes de produção.

## Como Executar
1. Clone o repositório:
```bash
git clone https://github.com/grazielewest/gestaodeprojetosA3/





