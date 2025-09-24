package com.gestao.projetos.dao;

import database.DatabaseConnection;
import com.gestao.projetos.model.entity.Projeto;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjetoDAO {
    private static final Logger logger = Logger.getLogger(ProjetoDAO.class.getName());

    // SQL statements - CORRETOS para sua estrutura
    private static final String INSERT_SQL = "INSERT INTO projetos (nome, descricao, status, data_inicio, data_fim, orcamento, id_responsavel, prioridade) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM projetos ORDER BY created_at DESC";
    private static final String UPDATE_SQL = "UPDATE projetos SET nome = ?, descricao = ?, status = ?, data_inicio = ?, data_fim = ?, orcamento = ?, id_responsavel = ?, prioridade = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM projetos WHERE id = ?";

    // ✅ METODO ADICIONADO: Criar tabela de projetos
    public void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS projetos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "descricao TEXT, " +
                "data_inicio DATE, " +
                "data_fim DATE, " +
                "status TEXT, " +
                "id_responsavel INTEGER, " +
                "orcamento REAL, " +
                "prioridade TEXT, " +
                "data_criacao DATE DEFAULT CURRENT_DATE, " +
                "data_atualizacao DATE, " +
                "FOREIGN KEY (id_responsavel) REFERENCES usuarios(id)" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            logger.log(Level.INFO, "✅ Tabela de projetos criada/verificada");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Erro ao criar tabela de projetos", e);
        }
    }

    // Seus métodos existentes continuam daqui...
    public boolean salvar(Projeto projeto) {
        System.out.println("🔄 Tentando salvar projeto: " + projeto.getNome());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // Configurar parâmetros NA ORDEM CORRETA
            stmt.setString(1, projeto.getNome());
            stmt.setString(2, projeto.getDescricao());
            stmt.setString(3, projeto.getStatus());

            // Datas - converter LocalDate para java.sql.Date
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

            // Responsável - pode ser NULL
            if (projeto.getIdResponsavel() > 0) {
                stmt.setInt(7, projeto.getIdResponsavel());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setString(8, projeto.getPrioridade());

            System.out.println("📋 Executando SQL: " + stmt.toString());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        projeto.setId(generatedKeys.getInt(1));
                        System.out.println("✅ Projeto salvo com ID: " + projeto.getId());
                        return true;
                    }
                }
            }

            System.out.println("❌ Nenhuma linha afetada ao salvar projeto");
            return false;

        } catch (SQLException e) {
            System.out.println("❌ Erro SQL ao salvar projeto: " + e.getMessage());
            logger.log(Level.SEVERE, "Erro ao salvar projeto", e);
            return false;
        }
    }

    public List<Projeto> listarTodos() {
        System.out.println("🔄 Listando todos os projetos...");
        List<Projeto> projetos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                Projeto projeto = mapearResultSetParaProjeto(rs);
                projetos.add(projeto);
                System.out.println("📋 Projeto encontrado: " + projeto.getNome() + " (ID: " + projeto.getId() + ")");
            }

            System.out.println("✅ Total de projetos encontrados: " + projetos.size());

        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar projetos: " + e.getMessage());
            logger.log(Level.SEVERE, "Erro ao listar projetos", e);
        }
        return projetos;
    }

    public boolean atualizar(Projeto projeto) {
        System.out.println("🔄 Atualizando projeto ID: " + projeto.getId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            // Configurar parâmetros NA ORDEM CORRETA
            stmt.setString(1, projeto.getNome());
            stmt.setString(2, projeto.getDescricao());
            stmt.setString(3, projeto.getStatus());

            // Datas
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

            // Responsável
            if (projeto.getIdResponsavel() > 0) {
                stmt.setInt(7, projeto.getIdResponsavel());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setString(8, projeto.getPrioridade());
            stmt.setInt(9, projeto.getId());

            System.out.println("📋 Executando UPDATE: " + stmt.toString());

            int affectedRows = stmt.executeUpdate();
            boolean sucesso = affectedRows > 0;

            if (sucesso) {
                System.out.println("✅ Projeto atualizado com sucesso: " + projeto.getId());
            } else {
                System.out.println("❌ Nenhum projeto encontrado para atualizar: " + projeto.getId());
            }

            return sucesso;

        } catch (SQLException e) {
            System.out.println("❌ Erro ao atualizar projeto: " + e.getMessage());
            logger.log(Level.SEVERE, "Erro ao atualizar projeto", e);
            return false;
        }
    }

    public boolean excluir(int id) {
        System.out.println("🔄 Excluindo projeto ID: " + id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean sucesso = affectedRows > 0;

            if (sucesso) {
                System.out.println("✅ Projeto excluído com sucesso: " + id);
            } else {
                System.out.println("❌ Nenhum projeto encontrado para excluir: " + id);
            }

            return sucesso;

        } catch (SQLException e) {
            System.out.println("❌ Erro ao excluir projeto: " + e.getMessage());
            logger.log(Level.SEVERE, "Erro ao excluir projeto", e);
            return false;
        }
    }

    private Projeto mapearResultSetParaProjeto(ResultSet rs) throws SQLException {
        Projeto projeto = new Projeto();

        projeto.setId(rs.getInt("id"));
        projeto.setNome(rs.getString("nome"));
        projeto.setDescricao(rs.getString("descricao"));
        projeto.setStatus(rs.getString("status"));

        // Converter java.sql.Date para LocalDate
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

        return projeto;
    }

    // Método adicional para buscar por ID
    public Projeto buscarPorId(int id) {
        System.out.println("🔍 Buscando projeto ID: " + id);

        String sql = "SELECT * FROM projetos WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("✅ Projeto encontrado: " + id);
                    return mapearResultSetParaProjeto(rs);
                } else {
                    System.out.println("❌ Projeto não encontrado: " + id);
                    return null;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar projeto: " + e.getMessage());
            logger.log(Level.SEVERE, "Erro ao buscar projeto por ID", e);
            return null;
        }
    }
}