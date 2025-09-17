package com.gestao.projetos.dao;

import com.gestao.projetos.database.DatabaseConnection;
import com.gestao.projetos.model.entity.Usuario;
import com.gestao.projetos.model.entity.Usuario.Perfil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioDAO {
    private static final Logger logger = Logger.getLogger(UsuarioDAO.class.getName());

    // CREATE - Salvar novo usuário
    public boolean salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, cpf, email, cargo, login, senha, perfil, ativo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getCargo());
            stmt.setString(5, usuario.getLogin());
            stmt.setString(6, usuario.getSenha());
            stmt.setString(7, usuario.getPerfil().name());
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

    // READ - Buscar por ID
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

    // READ - Buscar por login
    public Usuario buscarPorLogin(String login) {
        String sql = "SELECT * FROM usuarios WHERE login = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarUsuarioFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao buscar usuário por login: " + login, e);
        }
        return null;
    }

    // READ - Listar todos (já existente)
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

    // READ - Listar por perfil
    public List<Usuario> listarPorPerfil(Perfil perfil) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE perfil = ? ORDER BY nome";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, perfil.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(criarUsuarioFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao listar usuários por perfil: " + perfil, e);
        }
        return usuarios;
    }

    // UPDATE - Atualizar usuário
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, cpf = ?, email = ?, cargo = ?, login = ?, perfil = ?, ativo = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getCargo());
            stmt.setString(5, usuario.getLogin());
            stmt.setString(6, usuario.getPerfil().name());
            stmt.setBoolean(7, usuario.isAtivo());
            stmt.setInt(8, usuario.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.log(Level.INFO, "Usuário atualizado: " + usuario.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar usuário: " + usuario.getId(), e);
        }
        return false;
    }

    // UPDATE - Atualizar senha
    public boolean atualizarSenha(int usuarioId, String novaSenha) {
        String sql = "UPDATE usuarios SET senha = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaSenha);
            stmt.setInt(2, usuarioId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao atualizar senha do usuário: " + usuarioId, e);
        }
        return false;
    }

    // DELETE - Excluir usuário (lógico - desativa)
    public boolean excluir(int id) {
        String sql = "UPDATE usuarios SET ativo = false WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.log(Level.INFO, "Usuário excluído (desativado): " + id);
                return true;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao excluir usuário: " + id, e);
        }
        return false;
    }

    // DELETE - Excluir permanentemente
    public boolean excluirPermanentemente(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao excluir permanentemente usuário: " + id, e);
        }
        return false;
    }

    // Verificar se login já existe
    public boolean loginExiste(String login) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE login = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao verificar existência do login: " + login, e);
        }
        return false;
    }

    // Autenticar usuário (já existente)
    public Usuario autenticar(String login, String senha) {
        String sql = "SELECT * FROM usuarios WHERE login = ? AND password = ? AND ativo = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, senha);

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

    // Metodo auxiliar para criar objeto Usuario do ResultSet
    private Usuario criarUsuarioFromResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setEmail(rs.getString("email"));
        usuario.setCargo(rs.getString("cargo"));
        usuario.setLogin(rs.getString("login"));
        usuario.setSenha(rs.getString("password")); // Note: mudou para "password"
        usuario.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        usuario.setAtivo(rs.getBoolean("ativo"));
        return usuario;
    }
}