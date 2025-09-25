package com.gestao.projetos.dao;

import com.gestao.projetos.model.entity.Projeto;
import database.DatabaseConnection;
import com.gestao.projetos.model.entity.Equipe;
import com.gestao.projetos.model.entity.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EquipeDAO {
    private static final Logger logger = Logger.getLogger(EquipeDAO.class.getName());

    // Controle de recursão
    private static final ThreadLocal<Set<Integer>> equipesEmCarregamento =
            ThreadLocal.withInitial(HashSet::new);

    // SQL statements
    private static final String SELECT_BY_ID = "SELECT e.* FROM equipes e WHERE e.id = ?";
    private static final String SELECT_PROJETOS_EQUIPE = "SELECT p.* FROM projetos p INNER JOIN equipe_projetos ep ON p.id = ep.projeto_id WHERE ep.equipe_id = ?";

    public void criarTabela() {
        // Tabela principal de equipes
        String sqlEquipes = "CREATE TABLE IF NOT EXISTS equipes (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "nome VARCHAR(255) NOT NULL, " +
                "descricao TEXT, " +
                "data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "data_atualizacao TIMESTAMP NULL, " +
                "ativa BOOLEAN DEFAULT TRUE" +
                ")";

        // Tabela de relacionamento equipe_usuarios (muitos para muitos)
        String sqlEquipeUsuarios = "CREATE TABLE IF NOT EXISTS equipe_usuarios (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "equipe_id INT NOT NULL, " +
                "usuario_id INT NOT NULL, " +
                "data_vinculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (equipe_id) REFERENCES equipes(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE, " +
                "UNIQUE KEY unique_equipe_usuario (equipe_id, usuario_id)" +
                ")";

        // Tabela de relacionamento equipe_projetos (muitos para muitos)
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

            stmt.execute(sqlEquipes);
            stmt.execute(sqlEquipeUsuarios);
            stmt.execute(sqlEquipeProjetos);

            logger.log(Level.INFO, "✅ Tabelas de equipes criadas/verificadas");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao criar tabelas de equipes", e);
        }
    }

    public boolean salvar(Equipe equipe) {
        String sql = "INSERT INTO equipes (nome, descricao, data_criacao) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, equipe.getNome());
            stmt.setString(2, equipe.getDescricao());
            stmt.setDate(3, Date.valueOf(equipe.getDataCriacao()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        equipe.setId(generatedKeys.getInt(1));
                        logger.log(Level.INFO, "✅ Equipe salva com ID: " + equipe.getId());
                        return true;
                    }
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao salvar equipe", e);
        }
        return false;
    }

    public boolean atualizar(Equipe equipe) {
        String sql = "UPDATE equipes SET nome = ?, descricao = ?, data_atualizacao = CURRENT_TIMESTAMP, ativa = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, equipe.getNome());
            stmt.setString(2, equipe.getDescricao());
            stmt.setBoolean(3, equipe.isAtiva());
            stmt.setInt(4, equipe.getId());

            boolean sucesso = stmt.executeUpdate() > 0;
            if (sucesso) {
                // Atualizar membros e projetos
                atualizarMembros(equipe, conn);
                atualizarProjetos(equipe, conn);
            }

            return sucesso;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao atualizar equipe", e);
        }
        return false;
    }

    public List<Equipe> listarTodos() {
        List<Equipe> equipes = new ArrayList<>();
        String sql = "SELECT * FROM equipes ORDER BY nome";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Equipe equipe = mapearResultSetParaEquipe(rs);
                carregarMembros(equipe, conn);
                carregarProjetos(equipe, conn);
                equipes.add(equipe);
            }

            logger.log(Level.INFO, "✅ " + equipes.size() + " equipes carregadas");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao listar equipes", e);
        }
        return equipes;
    }

    // 🔥 MÉTODO PRINCIPAL MODIFICADO
    public Equipe buscarPorId(int id) {
        Set<Integer> emCarregamento = equipesEmCarregamento.get();
        if (emCarregamento.contains(id)) {
            logger.log(Level.WARNING, "⚠️ Tentativa de carregamento recursivo da equipe ID: " + id);
            return buscarBasicoPorId(id);
        }

        try {
            emCarregamento.add(id);

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Equipe equipe = mapearResultSetParaEquipe(rs);
                    carregarProjetos(equipe, conn);
                    carregarMembros(equipe, conn);
                    return equipe;
                }

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "❌ Erro ao buscar equipe por ID", e);
            }
            return null;

        } finally {
            emCarregamento.remove(id);
        }
    }

    // 🔥 NOVO MÉTODO: Buscar apenas dados básicos (sem projetos)
    public Equipe buscarBasicoPorId(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Equipe equipe = mapearResultSetParaEquipe(rs);
                // 🔥 Não carrega projetos para evitar recursão
                equipe.setProjetos(new ArrayList<>());
                return equipe;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao buscar equipe básica por ID", e);
        }
        return null;
    }

    public boolean excluir(int id) {
        String sql = "UPDATE equipes SET ativa = FALSE, data_atualizacao = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao excluir equipe", e);
        }
        return false;
    }

    // Métodos para gerenciar membros
    public void adicionarMembro(int equipeId, int usuarioId) {
        String sql = "INSERT INTO equipe_usuarios (equipe_id, usuario_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, equipeId);
            stmt.setInt(2, usuarioId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao adicionar membro à equipe", e);
        }
    }

    public void removerMembro(int equipeId, int usuarioId) {
        String sql = "DELETE FROM equipe_usuarios WHERE equipe_id = ? AND usuario_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, equipeId);
            stmt.setInt(2, usuarioId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao remover membro da equipe", e);
        }
    }

    // Métodos privados auxiliares
    private Equipe mapearResultSetParaEquipe(ResultSet rs) throws SQLException {
        Equipe equipe = new Equipe();
        equipe.setId(rs.getInt("id"));
        equipe.setNome(rs.getString("nome"));
        equipe.setDescricao(rs.getString("descricao"));
        equipe.setDataCriacao(rs.getDate("data_criacao").toLocalDate());

        Timestamp dataAtualizacao = rs.getTimestamp("data_atualizacao");
        if (dataAtualizacao != null) {
            equipe.setDataAtualizacao(dataAtualizacao.toLocalDateTime().toLocalDate());
        }

        equipe.setAtiva(rs.getBoolean("ativa"));
        return equipe;
    }

    private void carregarMembros(Equipe equipe, Connection conn) throws SQLException {
        String sql = "SELECT u.* FROM usuarios u " +
                "INNER JOIN equipe_usuarios eu ON u.id = eu.usuario_id " +
                "WHERE eu.equipe_id = ? AND u.ativo = TRUE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipe.getId());
            ResultSet rs = stmt.executeQuery();

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            while (rs.next()) {
                equipe.adicionarMembro(usuarioDAO.buscarPorId(rs.getInt("id")));
            }
        }
    }

    private void carregarProjetos(Equipe equipe, Connection conn) throws SQLException {
        List<Projeto> projetos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_PROJETOS_EQUIPE)) {
            stmt.setInt(1, equipe.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Projeto projeto = ProjetoMapper.mapearBasico(rs);
                projetos.add(projeto);
            }
        }
        equipe.setProjetos(projetos);
    }


    private void atualizarMembros(Equipe equipe, Connection conn) throws SQLException {
        // Remover todos os membros atuais
        String deleteSql = "DELETE FROM equipe_usuarios WHERE equipe_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, equipe.getId());
            stmt.executeUpdate();
        }

        // Adicionar os novos membros
        String insertSql = "INSERT INTO equipe_usuarios (equipe_id, usuario_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            for (Usuario membro : equipe.getMembros()) {
                stmt.setInt(1, equipe.getId());
                stmt.setInt(2, membro.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void atualizarProjetos(Equipe equipe, Connection conn) throws SQLException {
        // Remover todos os projetos atuais
        String deleteSql = "DELETE FROM equipe_projetos WHERE equipe_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, equipe.getId());
            stmt.executeUpdate();
        }

        // Adicionar os novos projetos
        String insertSql = "INSERT INTO equipe_projetos (equipe_id, projeto_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            for (Projeto projeto : equipe.getProjetos()) {
                stmt.setInt(1, equipe.getId());
                stmt.setInt(2, projeto.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}