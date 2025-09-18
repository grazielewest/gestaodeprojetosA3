package com.gestao.projetos.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) {
        testarConexao();
    }

    public static void testarConexao() {
        // Altere estas configurações conforme seu banco de dados
        String url = "jdbc:mysql://localhost:3306/gestao_projetos";
        String usuario = "seu_usuario";
        String senha = "sua_senha";

        try {
            // Carregar o driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Tentar estabelecer a conexão
            Connection conexao = DriverManager.getConnection(url, usuario, senha);

            if (conexao != null) {
                System.out.println("✅ Conexão com o banco de dados estabelecida com sucesso!");
                conexao.close();
            } else {
                System.out.println("❌ Falha ao estabelecer conexão com o banco de dados.");
            }

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver JDBC não encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Erro ao conectar com o banco de dados: " + e.getMessage());
        }
    }
}