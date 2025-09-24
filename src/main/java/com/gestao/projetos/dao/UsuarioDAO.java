package com.gestao.projetos.dao;

import database.DatabaseConnection;
import com.gestao.projetos.model.entity.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioDAO {
    private static final Logger logger = Logger.getLogger(UsuarioDAO.class.getName());

    // Método para criar tabela se não existir
    public void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "nome TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "cpf TEXT UNIQUE, " +
                "cargo TEXT, " +
                "perfil TEXT NOT NULL DEFAULT 'COLABORADOR', " +
                "ativo BOOLEAN DEFAULT 1, " +
                "data_criacao DATE DEFAULT CURRENT_DATE, " +
                "data_atualizacao DATE" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            logger.log(Level.INFO, "Tabela de usuários criada/verificada");

            // Criar usuário admin padrão se não existir
            criarUsuarioAdminPadrao();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao criar tabela de usuários", e);
        }
    }

    private void criarUsuarioAdminPadrao() {
        if (!usernameExiste("admin")) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail("admin@sistema.com");
            admin.setLogin("admin");
            admin.setSenha("admin123");
            admin.setPerfil(Usuario.Perfil.ADMINISTRADOR);
            admin.setCargo("Administrador do Sistema");
            salvar(admin);
            logger.log(Level.INFO, "Usuário admin criado: admin/admin123");
        }
    }

    // Seu método salvar existente (mantenha como está)
    public boolean salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password, nome, email, cpf, cargo, perfil, ativo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getLogin());
            stmt.setString(2, usuario.getSenha());
            stmt.setString(3, usuario.getNome());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getCpf());
            stmt.setString(6, usuario.getCargo());
            stmt.setString(7, usuario.getPerfil() != null ? usuario.getPerfil().name() : Usuario.Perfil.COLABORADOR.name());
            stmt.setBoolean(8, usuario.isAtivo());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getInt(1));
                    }
                }
                logger.log(Level.INFO, "Usuário salvo com ID: " + usuario.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao salvar usuário", e);
        }
        return false;
    }

    // Método para atualizar usuário (NOVO)
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET username = ?, password = ?, nome = ?, email = ?, cpf = ?, cargo = ?, perfil = ?, ativo = ?, data_atualizacao = CURRENT_DATE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getLogin());
            stmt.setString(2, usuario.getSenha());
            stmt.setString(3, usuario.getNome());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getCpf());
            stmt.setString(6, usuario.getCargo());
            stmt.setString(7, usuario.getPerfil().name());
            stmt.setBoolean(8, usuario.isAtivo());
            stmt.setInt(9, usuario.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.log(Level.INFO, "Usuário atualizado com ID: " + usuario.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar usuário", e);
        }
        return false;
    }

    // Método para buscar por ID (NOVO)
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarUsuarioFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar usuário por ID: " + id, e);
        }
        return null;
    }

    // Método para excluir (desativar) usuário (NOVO)
    public boolean excluir(int id) {
        String sql = "UPDATE usuarios SET ativo = 0, data_atualizacao = CURRENT_DATE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao excluir usuário", e);
        }
        return false;
    }

    // Seus métodos existentes (mantenha todos)
    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarUsuarioFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar usuário por username: " + username, e);
        }
        return null;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nome";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(criarUsuarioFromResultSet(rs));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar usuários", e);
        }
        return usuarios;
    }

    public Usuario autenticar(String username, String password) {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ? AND ativo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarUsuarioFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao autenticar usuário", e);
        }
        return null;
    }

    public boolean usernameExiste(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao verificar existência do username: " + username, e);
        }
        return false;
    }

    private Usuario criarUsuarioFromResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setLogin(rs.getString("username"));
        usuario.setSenha(rs.getString("password"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setCargo(rs.getString("cargo"));

        // Tratamento seguro para perfil
        String perfilStr = rs.getString("perfil");
        if (perfilStr != null) {
            try {
                usuario.setPerfil(Usuario.Perfil.valueOf(perfilStr));
            } catch (IllegalArgumentException e) {
                usuario.setPerfil(Usuario.Perfil.COLABORADOR); // Valor padrão
            }
        }

        usuario.setAtivo(rs.getBoolean("ativo"));
        return usuario;
    }
}