package com.gestao.projetos.dao;

import database.DatabaseConnection;
import com.gestao.projetos.model.entity.Projeto;
import com.gestao.projetos.model.entity.Equipe;
import com.gestao.projetos.model.entity.Usuario;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjetoDAO {
    private static final Logger logger = Logger.getLogger(ProjetoDAO.class.getName());

    // Adicione esta variável para controlar a recursão
    private static final ThreadLocal<Set<Integer>> projetosEmCarregamento =
            ThreadLocal.withInitial(HashSet::new);

    // SQL statements
    private static final String INSERT_SQL = "INSERT INTO projetos (nome, descricao, status, data_inicio, data_fim, orcamento, id_responsavel, prioridade) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL =
            "SELECT p.*, u.nome as nome_responsavel, " +
                    "GROUP_CONCAT(DISTINCT e.nome SEPARATOR ', ') as nomes_equipes " +
                    "FROM projetos p " +
                    "LEFT JOIN usuarios u ON p.id_responsavel = u.id " +
                    "LEFT JOIN equipe_projetos ep ON p.id = ep.projeto_id " +
                    "LEFT JOIN equipes e ON ep.equipe_id = e.id " +
                    "GROUP BY p.id, p.nome, p.descricao, p.status, p.data_inicio, p.data_fim, " +
                    "p.orcamento, p.id_responsavel, p.prioridade, u.nome " +
                    "ORDER BY p.id DESC";
    private static final String UPDATE_SQL = "UPDATE projetos SET nome = ?, descricao = ?, status = ?, data_inicio = ?, data_fim = ?, orcamento = ?, id_responsavel = ?, prioridade = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM projetos WHERE id = ?";

    // 🔥 NOVOS SQLs para gerenciar equipes
    private static final String INSERT_EQUIPE_PROJETO = "INSERT INTO equipe_projetos (equipe_id, projeto_id) VALUES (?, ?)";
    private static final String DELETE_EQUIPES_PROJETO = "DELETE FROM equipe_projetos WHERE projeto_id = ?";
    private static final String SELECT_EQUIPES_PROJETO = "SELECT e.* FROM equipes e INNER JOIN equipe_projetos ep ON e.id = ep.equipe_id WHERE ep.projeto_id = ?";

    public void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS projetos (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "nome VARCHAR(255) NOT NULL, " +
                "descricao TEXT, " +
                "data_inicio DATE, " +
                "data_fim DATE, " +
                "status VARCHAR(50), " +
                "id_responsavel INT, " +
                "orcamento DECIMAL(10,2), " +
                "prioridade VARCHAR(50), " +
                "data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "data_atualizacao TIMESTAMP NULL, " +
                "FOREIGN KEY (id_responsavel) REFERENCES usuarios(id) ON DELETE SET NULL" +
                ")";

        // 🔥 Tabela de relacionamento (já deve existir da EquipeDAO)
        String sqlEquipeProjetos = "CREATE TABLE IF NOT EXISTS equipe_projetos (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "equipe_id INT NOT NULL, " +
                "projeto_id INT NOT NULL, " +
                "data_vinculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (equipe_id) REFERENCES equipes(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (projeto_id) REFERENCES projetos(id) ON DELETE CASCADE, " +
                "UNIQUE KEY unique_equipe_projeto (equipe_id, projeto_id)" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            stmt.execute(sqlEquipeProjetos);
            logger.log(Level.INFO, "✅ Tabelas de projetos criadas/verificadas");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao criar tabelas de projetos", e);
        }
    }

    public boolean salvar(Projeto projeto) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transação

            // Salvar projeto básico
            boolean projetoSalvo = salvarProjetoBasico(projeto, conn);
            if (!projetoSalvo) {
                conn.rollback();
                return false;
            }

            // Salvar equipes do projeto
            salvarEquipesProjeto(projeto, conn);

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Erro ao fazer rollback", ex);
            }
            logger.log(Level.SEVERE, "❌ Erro ao salvar projeto", e);
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erro ao restaurar auto-commit", e);
            }
        }
    }

    private boolean salvarProjetoBasico(Projeto projeto, Connection conn) throws SQLException {
        String sql = projeto.getId() == 0 ? INSERT_SQL : UPDATE_SQL;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, projeto.getNome());
            stmt.setString(2, projeto.getDescricao());
            stmt.setString(3, projeto.getStatus());

            if (projeto.getDataInicio() != null) {
                stmt.setDate(4, java.sql.Date.valueOf(projeto.getDataInicio()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            if (projeto.getDataFim() != null) {
                stmt.setDate(5, java.sql.Date.valueOf(projeto.getDataFim()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setDouble(6, projeto.getOrcamento());

            if (projeto.getIdResponsavel() > 0) {
                stmt.setInt(7, projeto.getIdResponsavel());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setString(8, projeto.getPrioridade());

            if (projeto.getId() > 0) {
                stmt.setInt(9, projeto.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0 && projeto.getId() == 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        projeto.setId(generatedKeys.getInt(1));
                    }
                }
            }

            return affectedRows > 0;
        }
    }

    private void salvarEquipesProjeto(Projeto projeto, Connection conn) throws SQLException {
        // Remover equipes atuais (apenas na atualização)
        if (projeto.getId() > 0) {
            try (PreparedStatement stmt = conn.prepareStatement(DELETE_EQUIPES_PROJETO)) {
                stmt.setInt(1, projeto.getId());
                stmt.executeUpdate();
            }
        }

        // Adicionar novas equipes
        if (projeto.getEquipes() != null && !projeto.getEquipes().isEmpty()) {
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_EQUIPE_PROJETO)) {
                for (Equipe equipe : projeto.getEquipes()) {
                    stmt.setInt(1, equipe.getId());
                    stmt.setInt(2, projeto.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
    }

    public List<Projeto> listarTodos() {
        List<Projeto> projetos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Projeto projeto = mapearResultSetParaProjeto(rs);
                carregarEquipesProjeto(projeto, conn);
                projetos.add(projeto);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao listar projetos", e);
        }
        return projetos;
    }

    public Projeto buscarPorId(int id) {
        // Verificar se já estamos carregando este projeto (evitar recursão)
        Set<Integer> emCarregamento = projetosEmCarregamento.get();
        if (emCarregamento.contains(id)) {
            logger.log(Level.WARNING, "⚠️ Tentativa de carregamento recursivo do projeto ID: " + id);
            return criarProjetoBasico(id); // Retorna apenas dados básicos
        }

        try {
            emCarregamento.add(id); // Marcar como em carregamento

            String sql = "SELECT p.*, u.nome as nome_responsavel FROM projetos p LEFT JOIN usuarios u ON p.id_responsavel = u.id WHERE p.id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Projeto projeto = mapearResultSetParaProjeto(rs);
                    carregarEquipesProjeto(projeto, conn);
                    return projeto;
                }

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "❌ Erro ao buscar projeto por ID", e);
            }
            return null;

        } finally {
            emCarregamento.remove(id); // Remover após carregamento
        }
    }

    // Método para carregar apenas dados básicos (sem recursão)
    private Projeto criarProjetoBasico(int id) {
        String sql = "SELECT p.*, u.nome as nome_responsavel FROM projetos p LEFT JOIN usuarios u ON p.id_responsavel = u.id WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Projeto projeto = mapearResultSetParaProjeto(rs);
                // Não carrega equipes para evitar recursão
                projeto.setEquipes(new ArrayList<>());
                return projeto;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao criar projeto básico", e);
        }
        return null;
    }

    // 🔥 METODO MODIFICADO: Agora evita recursão
    private void carregarEquipesProjeto(Projeto projeto, Connection conn) throws SQLException {
        List<Equipe> equipes = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_EQUIPES_PROJETO)) {
            stmt.setInt(1, projeto.getId());
            ResultSet rs = stmt.executeQuery();

            // 🔥 MODIFICAÇÃO AQUI: Usar método que não carrega projetos da equipe
            EquipeDAO equipeDAO = new EquipeDAO();
            while (rs.next()) {
                // 🔥 Usar novo método que não carrega projetos (evita recursão)
                Equipe equipe = equipeDAO.buscarBasicoPorId(rs.getInt("id"));
                if (equipe != null) {
                    equipes.add(equipe);
                }
            }
        }
        projeto.setEquipes(equipes);
    }

    // 🔥 NOVO MÉTODO: Carregar equipes sem projetos (para uso do EquipeDAO)
    public List<Equipe> carregarEquipesBasico(int projetoId) throws SQLException {
        List<Equipe> equipes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_EQUIPES_PROJETO)) {

            stmt.setInt(1, projetoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Equipe equipe = new Equipe();
                equipe.setId(rs.getInt("id"));
                equipe.setNome(rs.getString("nome"));
                equipe.setDescricao(rs.getString("descricao"));
                // 🔥 Não carrega projetos para evitar recursão
                equipe.setProjetos(new ArrayList<>());
                equipes.add(equipe);
            }
        }
        return equipes;
    }

    private Projeto mapearResultSetParaProjeto(ResultSet rs) throws SQLException {
        Projeto projeto = new Projeto();

        projeto.setId(rs.getInt("id"));
        projeto.setNome(rs.getString("nome"));
        projeto.setDescricao(rs.getString("descricao"));
        projeto.setStatus(rs.getString("status"));

        java.sql.Date dataInicio = rs.getDate("data_inicio");
        if (dataInicio != null) {
            projeto.setDataInicio(dataInicio.toLocalDate());
        }

        java.sql.Date dataFim = rs.getDate("data_fim");
        if (dataFim != null) {
            projeto.setDataFim(dataFim.toLocalDate());
        }

        projeto.setOrcamento(rs.getDouble("orcamento"));
        projeto.setIdResponsavel(rs.getInt("id_responsavel"));
        projeto.setPrioridade(rs.getString("prioridade"));

        //  Carregar nome do responsável se disponível
        try {
            projeto.setNomeResponsavel(rs.getString("nome_responsavel"));
        } catch (SQLException e) {
            // Coluna pode não existir em todas as queries
        }

        //  Carregar nomes das equipes
        try {
            String nomesEquipes = rs.getString("nomes_equipes");
            projeto.setNomesEquipes(nomesEquipes != null ? nomesEquipes : "Sem equipes");
        } catch (SQLException e) {
            // Coluna pode não existir se o GROUP_CONCAT falhar
            projeto.setNomesEquipes("Sem equipes");
        }

        //  IMPORTANTE: Inicializar a lista vazia para evitar null
        projeto.setEquipes(new ArrayList<>());

        return projeto;
    }

    //  NOVO MÉTODO: Buscar projetos por equipe
    public List<Projeto> listarPorEquipe(int equipeId) {
        List<Projeto> projetos = new ArrayList<>();
        String sql = "SELECT p.* FROM projetos p INNER JOIN equipe_projetos ep ON p.id = ep.projeto_id WHERE ep.equipe_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, equipeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Projeto projeto = mapearResultSetParaProjeto(rs);
                projetos.add(projeto);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao listar projetos por equipe", e);
        }
        return projetos;
    }

    // Mantenha os outros métodos existentes (atualizar, excluir, etc.)
    public boolean atualizar(Projeto projeto) {
        return salvar(projeto); // Já usa transação
    }

    public boolean excluir(int id) {
        // A exclusão em cascata da tabela equipe_projetos será feita automaticamente
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao excluir projeto", e);
            return false;
        }
    }
}