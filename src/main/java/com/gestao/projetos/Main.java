package com.gestao.projetos;

import com.gestao.projetos.database.DatabaseConnection;
import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Testar conexão com o banco ao iniciar a aplicação
        testarConexaoBanco();

        // Carregar a interface gráfica
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/login.fxml")));
        primaryStage.setTitle("Sistema de Gestão de Projetos - A3");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private void testarConexaoBanco() {
        try {
            Connection conn = database.DatabaseConnection.getConnection();
            System.out.println("✅ Conexão com banco estabelecida!");
            database.DatabaseConnection.closeConnection(conn);
        } catch (SQLException e) {
            System.err.println("❌ Erro na conexão: " + e.getMessage());
            // Você pode mostrar um alerta para o usuário aqui
        }
    }

    public static void main(String[] args) {
        // Iniciar a aplicação JavaFX
        launch(args);
    }
}