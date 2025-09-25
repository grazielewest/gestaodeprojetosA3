package com.gestao.projetos.dao;

import com.gestao.projetos.model.entity.Tarefa;
import java.sql.Connection;
import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TarefaDAO {

    private Connection conexao;

    public TarefaDAO() {
        try {
            this.conexao = DatabaseConnection.getConnection();
            System.out.println("✅ TarefaDAO inicializado com sucesso!");
        } catch (Exception e) {
            System.out.println("❌ Erro ao inicializar TarefaDAO: " + e.getMessage());
            throw new RuntimeException("Não foi possível inicializar TarefaDAO", e);
        }
    }

    public boolean salvar(Tarefa tarefa) {
        String sql = tarefa.getId() == 0 ?
                "INSERT INTO tarefas (titulo, descricao, id_projeto, id_responsavel, status, data_inicio_prevista, data_fim_prevista, data_inicio_real, data_fim_real, prioridade) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" :
                "UPDATE tarefas SET titulo = ?, descricao = ?, id_projeto = ?, id_responsavel = ?, status = ?, data_inicio_prevista = ?, data_fim_prevista = ?, data_inicio_real = ?, data_fim_real = ?, prioridade = ?, data_atualizacao = CURRENT_TIMESTAMP WHERE id = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tarefa.getTitulo());
            stmt.setString(2, tarefa.getDescricao());
            stmt.setInt(3, tarefa.getIdProjeto());
            stmt.setInt(4, tarefa.getIdResponsavel());
            stmt.setString(5, tarefa.getStatus());
            stmt.setDate(6, tarefa.getDataInicioPrevista() != null ? Date.valueOf(tarefa.getDataInicioPrevista()) : null);
            stmt.setDate(7, tarefa.getDataFimPrevista() != null ? Date.valueOf(tarefa.getDataFimPrevista()) : null);
            stmt.setDate(8, tarefa.getDataInicioReal() != null ? Date.valueOf(tarefa.getDataInicioReal()) : null);
            stmt.setDate(9, tarefa.getDataFimReal() != null ? Date.valueOf(tarefa.getDataFimReal()) : null);
            stmt.setString(10, tarefa.getPrioridade());

            if (tarefa.getId() != 0) {
                stmt.setInt(11, tarefa.getId());
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0 && tarefa.getId() == 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        tarefa.setId(rs.getInt(1));
                    }
                }
            }
            System.out.println("✅ Tarefa salva com sucesso! ID: " + tarefa.getId());
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erro ao salvar tarefa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM tarefas WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean sucesso = stmt.executeUpdate() > 0;
            if (sucesso) {
                System.out.println("✅ Tarefa excluída com sucesso! ID: " + id);
            }
            return sucesso;
        } catch (SQLException e) {
            System.out.println("❌ Erro ao excluir tarefa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Tarefa> listarTodos() {
        List<Tarefa> tarefas = new ArrayList<>();
        String sql = "SELECT t.*, p.nome as nome_projeto, u.nome as nome_responsavel " +
                "FROM tarefas t " +
                "LEFT JOIN projetos p ON t.id_projeto = p.id " +
                "LEFT JOIN usuarios u ON t.id_responsavel = u.id " +
                "ORDER BY t.data_criacao DESC";

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Tarefa tarefa = mapearTarefa(rs);
                tarefas.add(tarefa);
            }
            System.out.println("✅ " + tarefas.size() + " tarefas carregadas");

        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar tarefas: " + e.getMessage());
            e.printStackTrace();
        }
        return tarefas;
    }

    public List<Tarefa> listarPorProjeto(int idProjeto) {
        List<Tarefa> tarefas = new ArrayList<>();
        String sql = "SELECT t.*, p.nome as nome_projeto, u.nome as nome_responsavel " +
                "FROM tarefas t " +
                "LEFT JOIN projetos p ON t.id_projeto = p.id " +
                "LEFT JOIN usuarios u ON t.id_responsavel = u.id " +
                "WHERE t.id_projeto = ? " +
                "ORDER BY t.data_criacao DESC";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idProjeto);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Tarefa tarefa = mapearTarefa(rs);
                    tarefas.add(tarefa);
                }
            }

            System.out.println("✅ " + tarefas.size() + " tarefas carregadas para o projeto " + idProjeto);

        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar tarefas por projeto: " + e.getMessage());
            e.printStackTrace();
        }
        return tarefas;
    }

    public Tarefa buscarPorId(int id) {
        String sql = "SELECT t.*, p.nome as nome_projeto, u.nome as nome_responsavel " +
                "FROM tarefas t " +
                "LEFT JOIN projetos p ON t.id_projeto = p.id " +
                "LEFT JOIN usuarios u ON t.id_responsavel = u.id " +
                "WHERE t.id = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearTarefa(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar tarefa por ID: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("⚠️ Tarefa não encontrada: ID " + id);
        return null;
    }

    private Tarefa mapearTarefa(ResultSet rs) throws SQLException {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(rs.getInt("id"));
        tarefa.setTitulo(rs.getString("titulo"));
        tarefa.setDescricao(rs.getString("descricao"));
        tarefa.setIdProjeto(rs.getInt("id_projeto"));
        tarefa.setNomeProjeto(rs.getString("nome_projeto"));
        tarefa.setIdResponsavel(rs.getInt("id_responsavel"));
        tarefa.setNomeResponsavel(rs.getString("nome_responsavel"));
        tarefa.setStatus(rs.getString("status"));
        tarefa.setPrioridade(rs.getString("prioridade"));

        // Datas
        Date data = rs.getDate("data_inicio_prevista");
        if (data != null) tarefa.setDataInicioPrevista(data.toLocalDate());

        data = rs.getDate("data_fim_prevista");
        if (data != null) tarefa.setDataFimPrevista(data.toLocalDate());

        data = rs.getDate("data_inicio_real");
        if (data != null) tarefa.setDataInicioReal(data.toLocalDate());

        data = rs.getDate("data_fim_real");
        if (data != null) tarefa.setDataFimReal(data.toLocalDate());

        data = rs.getDate("data_criacao");
        if (data != null) tarefa.setDataCriacao(data.toLocalDate());

        data = rs.getDate("data_atualizacao");
        if (data != null) tarefa.setDataAtualizacao(data.toLocalDate());

        return tarefa;
    }
    // Método para verificar se a conexão está funcionando
    public boolean testarConexao() {
        try {
            return conexao != null && !conexao.isClosed();
        } catch (SQLException e) {
            System.out.println("❌ Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }
}
