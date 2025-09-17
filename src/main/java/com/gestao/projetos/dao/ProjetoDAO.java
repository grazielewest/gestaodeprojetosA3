package com.gestao.projetos.dao;

import com.gestao.projetos.database.DatabaseConnection;
import com.gestao.projetos.model.entity.Projeto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjetoDAO {

    private static final Logger logger = Logger.getLogger(ProjetoDAO.class.getName());

    // SQL statements
    private static final String INSERT_SQL = "INSERT INTO projetos (nome, descricao, data_inicio, data_fim, status, id_responsavel, orcamento, prioridade, data_criacao, data_atualizacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT p.*, u.nome as nome_responsavel FROM projetos p LEFT JOIN usuarios u ON p.id_responsavel = u.id ORDER BY p.data_criacao DESC";
    private static final String SELECT_BY_ID_SQL = "SELECT p.*, u.nome as nome_responsavel FROM projetos p LEFT JOIN usuarios u ON p.id_responsavel = u.id WHERE p.id = ?";
    private static final String UPDATE_SQL = "UPDATE projetos SET nome = ?, descricao = ?, data_inicio = ?, data_fim = ?, status = ?, id_responsavel = ?, orcamento = ?, prioridade = ?, data_atualizacao = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM projetos WHERE id = ?";
    private static final String SELECT_BY_STATUS_SQL = "SELECT p.*, u.nome as nome_responsavel FROM projetos p LEFT JOIN usuarios u ON p.id_responsavel = u.id WHERE p.status = ? ORDER BY p.data_criacao DESC";
    private static final String SELECT_BY_RESPONSAVEL_SQL = "SELECT p.*, u.nome as nome_responsavel FROM projetos p LEFT JOIN usuarios u ON p.id_responsavel = u.id WHERE p.id_responsavel = ? ORDER BY p.data_criacao DESC";
    private static final String COUNT_BY_STATUS_SQL = "SELECT status, COUNT(*) as quantidade FROM projetos GROUP BY status";

    public int inserir(Projeto projeto) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            configurarPreparedStatement(stmt, projeto);
            stmt.setDate(9, Date.valueOf(projeto.getDataCriacao()));
            stmt.setDate(10, Date.valueOf(projeto.getDataAtualizacao()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idGerado = generatedKeys.getInt(1);
                        logger.log(Level.INFO, "Projeto inserido com ID: " + idGerado);
                        return idGerado;
                    }
                }
            }

            return -1;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inserir projeto: " + e.getMessage(), e);
            return -1;
        }
    }

    public List<Projeto> listarTodos() {
        List<Projeto> projetos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                projetos.add(mapearResultSetParaProjeto(rs));
            }

            logger.log(Level.INFO, "Listados " + projetos.size() + " projetos");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar projetos: " + e.getMessage(), e);
        }

        return projetos;
    }

    public Projeto buscarPorId(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetParaProjeto(rs);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar projeto por ID: " + e.getMessage(), e);
        }

        return null;
    }

    public boolean atualizar(Projeto projeto) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            configurarPreparedStatement(stmt, projeto);
            stmt.setDate(9, Date.valueOf(projeto.getDataAtualizacao()));
            stmt.setInt(10, projeto.getId());

            int affectedRows = stmt.executeUpdate();
            boolean sucesso = affectedRows > 0;

            if (sucesso) {
                logger.log(Level.INFO, "Projeto atualizado com ID: " + projeto.getId());
            }

            return sucesso;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar projeto: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean excluir(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean sucesso = affectedRows > 0;

            if (sucesso) {
                logger.log(Level.INFO, "Projeto excluído com ID: " + id);
            }

            return sucesso;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao excluir projeto: " + e.getMessage(), e);
            return false;
        }
    }

    public List<Projeto> listarPorStatus(String status) {
        List<Projeto> projetos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_STATUS_SQL)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projetos.add(mapearResultSetParaProjeto(rs));
                }
            }

            logger.log(Level.INFO, "Listados " + projetos.size() + " projetos com status: " + status);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar projetos por status: " + e.getMessage(), e);
        }

        return projetos;
    }

    public List<Projeto> listarPorResponsavel(int idResponsavel) {
        List<Projeto> projetos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_RESPONSAVEL_SQL)) {

            stmt.setInt(1, idResponsavel);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projetos.add(mapearResultSetParaProjeto(rs));
                }
            }

            logger.log(Level.INFO, "Listados " + projetos.size() + " projetos do responsável ID: " + idResponsavel);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar projetos por responsável: " + e.getMessage(), e);
        }

        return projetos;
    }

    public int contarTotal() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM projetos")) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao contar projetos: " + e.getMessage(), e);
        }

        return 0;
    }

    public List<Object[]> contarPorStatus() {
        List<Object[]> resultados = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(COUNT_BY_STATUS_SQL)) {

            while (rs.next()) {
                Object[] resultado = new Object[2];
                resultado[0] = rs.getString("status");
                resultado[1] = rs.getInt("quantidade");
                resultados.add(resultado);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao contar projetos por status: " + e.getMessage(), e);
        }

        return resultados;
    }

    public List<Projeto> buscarPorNome(String nome) {
        List<Projeto> projetos = new ArrayList<>();
        String sql = "SELECT p.*, u.nome as nome_responsavel FROM projetos p LEFT JOIN usuarios u ON p.id_responsavel = u.id WHERE p.nome LIKE ? ORDER BY p.data_criacao DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projetos.add(mapearResultSetParaProjeto(rs));
                }
            }

            logger.log(Level.INFO, "Encontrados " + projetos.size() + " projetos com nome: " + nome);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar projetos por nome: " + e.getMessage(), e);
        }

        return projetos;
    }

    public List<Projeto> listarProjetosAtrasados() {
        List<Projeto> projetos = new ArrayList<>();
        String sql = "SELECT p.*, u.nome as nome_responsavel FROM projetos p LEFT JOIN usuarios u ON p.id_responsavel = u.id WHERE p.data_fim < CURDATE() AND p.status NOT IN ('Concluído', 'Cancelado') ORDER BY p.data_fim ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                projetos.add(mapearResultSetParaProjeto(rs));
            }

            logger.log(Level.INFO, "Encontrados " + projetos.size() + " projetos atrasados");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar projetos atrasados: " + e.getMessage(), e);
        }

        return projetos;
    }

    private void configurarPreparedStatement(PreparedStatement stmt, Projeto projeto) throws SQLException {
        stmt.setString(1, projeto.getNome());
        stmt.setString(2, projeto.getDescricao());
        stmt.setDate(3, Date.valueOf(projeto.getDataInicio()));
        stmt.setDate(4, Date.valueOf(projeto.getDataFim()));
        stmt.setString(5, projeto.getStatus());
        stmt.setInt(6, projeto.getIdResponsavel());
        stmt.setDouble(7, projeto.getOrcamento());
        stmt.setString(8, projeto.getPrioridade());
    }

    private Projeto mapearResultSetParaProjeto(ResultSet rs) throws SQLException {
        Projeto projeto = new Projeto();

        projeto.setId(rs.getInt("id"));
        projeto.setNome(rs.getString("nome"));
        projeto.setDescricao(rs.getString("descricao"));

        // Tratamento de datas nulas
        Date dataInicio = rs.getDate("data_inicio");
        if (dataInicio != null) {
            projeto.setDataInicio(dataInicio.toLocalDate());
        }

        Date dataFim = rs.getDate("data_fim");
        if (dataFim != null) {
            projeto.setDataFim(dataFim.toLocalDate());
        }

        projeto.setStatus(rs.getString("status"));
        projeto.setIdResponsavel(rs.getInt("id_responsavel"));
        projeto.setOrcamento(rs.getDouble("orcamento"));
        projeto.setPrioridade(rs.getString("prioridade"));

        // Dados de auditoria
        Date dataCriacao = rs.getDate("data_criacao");
        if (dataCriacao != null) {
            projeto.setDataCriacao(dataCriacao.toLocalDate());
        }

        Date dataAtualizacao = rs.getDate("data_atualizacao");
        if (dataAtualizacao != null) {
            projeto.setDataAtualizacao(dataAtualizacao.toLocalDate());
        }

        // Nome do responsável (se disponível)
        try {
            projeto.setNomeResponsavel(rs.getString("nome_responsavel"));
        } catch (SQLException e) {
            // Coluna pode não existir em algumas queries
        }

        return projeto;
    }

    // Método para verificar se já existe projeto com o mesmo nome
    public boolean existeProjetoComNome(String nome) {
        String sql = "SELECT COUNT(*) as total FROM projetos WHERE nome = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao verificar existência de projeto: " + e.getMessage(), e);
        }

        return false;
    }

    // Método para obter estatísticas gerais
    public Object[] obterEstatisticas() {
        Object[] estatisticas = new Object[4];

        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "COUNT(CASE WHEN data_fim < CURDATE() AND status NOT IN ('Concluído', 'Cancelado') THEN 1 END) as atrasados, " +
                "COUNT(CASE WHEN status = 'Concluído' THEN 1 END) as concluidos, " +
                "AVG(orcamento) as media_orcamento " +
                "FROM projetos";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                estatisticas[0] = rs.getInt("total");
                estatisticas[1] = rs.getInt("atrasados");
                estatisticas[2] = rs.getInt("concluidos");
                estatisticas[3] = rs.getDouble("media_orcamento");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao obter estatísticas: " + e.getMessage(), e);
        }

        return estatisticas;
    }
}