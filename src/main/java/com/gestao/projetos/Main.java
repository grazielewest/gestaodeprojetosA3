package com.gestao.projetos;

import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.dao.UsuarioDAO;
import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // 1. Testar conexão com o banco
            if (!testarConexaoBanco()) {
                mostrarErroCritico("Falha na conexão com o banco de dados.\nA aplicação não pode ser iniciada.");
                return;
            }

            // 2. Inicializar tabelas do sistema
            if (!inicializarTabelasSistema()) {
                mostrarErroCritico("Falha na inicialização do banco de dados.\nA aplicação não pode ser iniciada.");
                return;
            }

            // 3. Carregar a interface gráfica
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/login.fxml")));
            primaryStage.setTitle("Sistema de Gestão de Projetos - A3");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();

            logger.log(Level.INFO, "✅ Aplicação iniciada com sucesso");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Erro crítico ao iniciar aplicação", e);
            mostrarErroCritico("Erro crítico ao iniciar a aplicação:\n" + e.getMessage());
        }
    }

    private boolean testarConexaoBanco() {
        boolean conexaoOk = DatabaseConnection.testarConexao();
        if (conexaoOk) {
            logger.log(Level.INFO, "✅ Conexão com banco verificada com sucesso");
        } else {
            logger.log(Level.SEVERE, "❌ Falha na conexão com o banco");
        }
        return conexaoOk;
    }

    private boolean inicializarTabelasSistema() {
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.criarTabela();

            ProjetoDAO projetoDAO = new ProjetoDAO();
            projetoDAO.criarTabela();
            // Aqui você pode adicionar a inicialização de outras tabelas no futuro
            // projetoDAO.criarTabela();
            // tarefaDAO.criarTabela();

            logger.log(Level.INFO, "✅ Tabelas do sistema inicializadas com sucesso");
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Erro ao inicializar tabelas do sistema", e);
            return false;
        }
    }

    private void mostrarErroCritico(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro Crítico");
        alert.setHeaderText("Falha na Inicialização");
        alert.setContentText(mensagem);
        alert.showAndWait();

        // Encerrar a aplicação
        System.exit(1);
    }

    public static void main(String[] args) {
        logger.log(Level.INFO, "🚀 Iniciando aplicação de gestão de projetos");

        try {
            launch(args);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Erro fatal na aplicação", e);
            System.exit(1);
        }
    }
}