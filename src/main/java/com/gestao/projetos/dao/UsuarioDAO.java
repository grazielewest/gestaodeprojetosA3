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

    public Usuario autenticar(String login, String senha) {
        String sql = "SELECT * FROM usuarios WHERE login = ? AND senha = ? AND ativo = true";

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

    private Usuario criarUsuarioFromResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setEmail(rs.getString("email"));
        usuario.setCargo(rs.getString("cargo"));
        usuario.setLogin(rs.getString("login"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        usuario.setAtivo(rs.getBoolean("ativo"));
        return usuario;
    }

    // Outros métodos: salvar, atualizar, excluir, etc.
}