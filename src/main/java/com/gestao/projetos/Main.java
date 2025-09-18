package com.gestao.projetos;

import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Testar conexão com o banco ao iniciar a aplicação
        testarConexaoBanco();

        // Carregar a interface gráfica
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/login.fxml")));
        primaryStage.setTitle("Sistema de Gestão de Projetos - A3");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        logger.log(Level.INFO, "✅ Aplicação iniciada com sucesso");
    }

    private void testarConexaoBanco() {
        if (DatabaseConnection.testarConexao()) {
            logger.log(Level.INFO, "✅ Conexão com banco verificada com sucesso");
        } else {
            logger.log(Level.SEVERE, "❌ Falha crítica: Não foi possível conectar ao banco");
            // Aqui você pode mostrar um alerta para o usuário
        }
    }

    public static void main(String[] args) {
        logger.log(Level.INFO, "🚀 Iniciando aplicação de gestão de projetos");
        launch(args);
    }
}