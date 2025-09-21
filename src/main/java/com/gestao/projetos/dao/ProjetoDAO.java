package com.gestao.projetos.dao;

import database.DatabaseConnection;
import com.gestao.projetos.model.entity.Projeto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjetoDAO {
    private static final Logger logger = Logger.getLogger(ProjetoDAO.class.getName());

    // SQL simplificado para sua estrutura atual
    private static final String INSERT_SQL = "INSERT INTO projetos (nome, descricao, data_inicio, data_fim, status, id_responsavel, orcamento, prioridade) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM projetos ORDER BY created_at DESC";
    private static final String UPDATE_SQL = "UPDATE projetos SET nome = ?, descricao = ?, data_inicio = ?, data_fim = ?, status = ?, id_responsavel = ?, orcamento = ?, prioridade = ? WHERE id = ?";

    public boolean salvar(Projeto projeto) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            configurarPreparedStatement(stmt, projeto);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        projeto.setId(generatedKeys.getInt(1));
                    }
                }
                logger.log(Level.INFO, "Projeto salvo com ID: " + projeto.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao salvar projeto: " + e.getMessage(), e);
        }
        return false;
    }

    public List<Projeto> listarTodos() {
        List<Projeto> projetos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                projetos.add(mapearResultSetParaProjeto(rs));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar projetos: " + e.getMessage(), e);
        }
        return projetos;
    }

    public boolean atualizar(Projeto projeto) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            configurarPreparedStatement(stmt, projeto);
            stmt.setInt(9, projeto.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.log(Level.INFO, "Projeto atualizado: " + projeto.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar projeto: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM projetos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.log(Level.INFO, "Projeto excluído com ID: " + id);
                return true;
            } else {
                logger.log(Level.WARNING, "Nenhum projeto encontrado com ID: " + id);
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao excluir projeto com ID " + id + ": " + e.getMessage(), e);
            return false;
        }
    }

    private void configurarPreparedStatement(PreparedStatement stmt, Projeto projeto) throws SQLException {
        stmt.setString(1, projeto.getNome());
        stmt.setString(2, projeto.getDescricao());

        if (projeto.getDataInicio() != null) {
            stmt.setDate(3, java.sql.Date.valueOf(projeto.getDataInicio()));
        } else {
            stmt.setNull(3, Types.DATE);
        }

        if (projeto.getDataFim() != null) {
            stmt.setDate(4, java.sql.Date.valueOf(projeto.getDataFim()));
        } else {
            stmt.setNull(4, Types.DATE);
        }

        stmt.setString(5, projeto.getStatus());

        if (projeto.getIdResponsavel() > 0) {
            stmt.setInt(6, projeto.getIdResponsavel());
        } else {
            stmt.setNull(6, Types.INTEGER);
        }

        stmt.setDouble(7, projeto.getOrcamento());
        stmt.setString(8, projeto.getPrioridade());
    }

    private Projeto mapearResultSetParaProjeto(ResultSet rs) throws SQLException {
        Projeto projeto = new Projeto();
        projeto.setId(rs.getInt("id"));
        projeto.setNome(rs.getString("nome"));
        projeto.setDescricao(rs.getString("descricao"));

        java.sql.Date dataInicio = rs.getDate("data_inicio");
        if (dataInicio != null) {
            projeto.setDataInicio(dataInicio.toLocalDate());
        }

        java.sql.Date dataFim = rs.getDate("data_fim");
        if (dataFim != null) {
            projeto.setDataFim(dataFim.toLocalDate());
        }

        projeto.setStatus(rs.getString("status"));
        projeto.setIdResponsavel(rs.getInt("id_responsavel"));
        projeto.setOrcamento(rs.getDouble("orcamento"));
        projeto.setPrioridade(rs.getString("prioridade"));

        return projeto;
    }
}