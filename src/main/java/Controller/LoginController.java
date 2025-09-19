package Controller;

import database.DatabaseConnection;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.model.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        // Configuração inicial se necessário
        // REMOVA a linha abaixo se estiver causando problemas
        // loginButton.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erro", "Por favor, preencha todos os campos.");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.autenticar(username, password);

            if (usuario != null) {
                showAlert("Sucesso", "Login realizado com sucesso!");
                logger.log(Level.INFO, "Usuário " + usuario.getNome() + " logou com sucesso");
                abrirDashboard();
            } else {
                showAlert("Erro", "Usuário ou senha inválidos!");
                logger.log(Level.WARNING, "Tentativa de login falhou para usuário: " + username);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao autenticar usuário: " + e.getMessage(), e);
            showAlert("Erro", "Erro de conexão com o banco de dados.");
        }
    }

    @FXML
    private void handleCriarUsuarioRapido() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erro", "Digite um usuário e senha para criar sua conta.");
            return;
        }

        if (password.length() < 3) {
            showAlert("Erro", "A senha deve ter pelo menos 3 caracteres.");
            return;
        }

        try {
            // Verificar se usuário já existe
            if (usuarioDAO.buscarPorLogin(username) != null) {
                showAlert("Erro", "Usuário já existe! Escolha outro nome.");
                return;
            }

            // Criar novo usuário
            String sql = "INSERT INTO usuarios (login, password, nome, email) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, username);
                stmt.setString(4, username + "@exemplo.com");

                int result = stmt.executeUpdate();

                if (result > 0) {
                    showAlert("Sucesso", "Conta criada com sucesso!\n\nUsuário: " + username + "\nSenha: " + password + "\n\nAgora faça login.");
                    logger.log(Level.INFO, "Novo usuário criado: " + username);

                    // Limpa os campos
                    usernameField.clear();
                    passwordField.clear();
                } else {
                    showAlert("Erro", "Não foi possível criar a conta.");
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao criar usuário", e);
            showAlert("Erro", "Erro técnico ao criar conta: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro inesperado ao criar usuário", e);
            showAlert("Erro", "Erro inesperado: " + e.getMessage());
        }
    }

    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dashboard - Sistema de Gestão de Projetos");
            stage.setScene(new Scene(root));
            stage.show();

            // Fecha a tela de login
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao abrir dashboard", e);
            showAlert("Erro", "Não foi possível abrir o sistema.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}