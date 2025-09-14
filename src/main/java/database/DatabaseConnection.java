package com.gestao.projetos.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String URL = "jdbc:mysql://localhost:3306/gestao_projetos";
    private static final String USER = "seu_usuario";
    private static final String PASSWORD = "sua_senha";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.log(Level.INFO, "✅ Conexão com banco estabelecida com sucesso");
            return connection;
        } catch (ClassNotFoundException e) {
            String errorMsg = "❌ Driver JDBC não encontrado: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new SQLException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "❌ Erro ao conectar com o banco: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw e;
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.log(Level.INFO, "✅ Conexão com banco fechada com sucesso");
            } catch (SQLException e) {
                String errorMsg = "❌ Erro ao fechar conexão: " + e.getMessage();
                logger.log(Level.WARNING, errorMsg, e);
            }
        }
    }

    // Método adicional para testar conexão
    public static boolean testarConexao() {
        try (Connection conn = getConnection()) {
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Falha no teste de conexão", e);
            return false;
        }
    }
}