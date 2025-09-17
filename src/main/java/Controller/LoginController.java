package Controller;

import com.gestao.projetos.database.DatabaseConnection;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.model.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        loginButton.setOnAction(event -> handleLogin());
    }

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
                // Aqui você vai abrir a próxima tela e pode passar o usuário autenticado
                System.out.println("Bem-vindo, " + usuario.getNome());
            } else {
                showAlert("Erro", "Usuário ou senha inválidos!");
                logger.log(Level.WARNING, "Tentativa de login falhou para usuário: " + username);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao autenticar usuário: " + e.getMessage(), e);
            showAlert("Erro", "Erro de conexão com o banco de dados.");
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