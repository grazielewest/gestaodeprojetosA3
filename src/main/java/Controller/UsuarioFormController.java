// UsuarioFormController.java
package Controller;

import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.model.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UsuarioFormController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCargo;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    @FXML private ComboBox<Usuario.Perfil> cbPerfil;
    @FXML private CheckBox chkAtivo;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;
    @FXML private Label lblTitulo;

    private Usuario usuario;
    private UsuarioDAO usuarioDAO;
    private boolean modoEdicao = false;

    @FXML
    private void initialize() {
        usuarioDAO = new UsuarioDAO();
        configurarComponentes();
    }

    private void configurarComponentes() {
        // Configurar combobox de perfis
        cbPerfil.getItems().addAll(Usuario.Perfil.values());
        cbPerfil.getSelectionModel().select(Usuario.Perfil.COLABORADOR);

        // Configurar máscara para CPF
        txtCpf.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9.-]*")) {
                return change;
            }
            return null;
        }));

        // Por padrão, usuário novo está ativo
        chkAtivo.setSelected(true);
    }

    public void setUsuarioParaEdicao(Usuario usuario) {
        this.usuario = usuario;
        this.modoEdicao = true;
        preencherFormulario();
        lblTitulo.setText("Editar Usuário - " + usuario.getNome());
    }

    private void preencherFormulario() {
        if (usuario != null) {
            txtNome.setText(usuario.getNome());
            txtCpf.setText(usuario.getCpf());
            txtEmail.setText(usuario.getEmail());
            txtCargo.setText(usuario.getCargo());
            txtLogin.setText(usuario.getLogin());
            cbPerfil.setValue(usuario.getPerfil());
            chkAtivo.setSelected(usuario.isAtivo());

            // Em modo edição, deixar senha em branco para preencher apenas se quiser alterar
            txtSenha.setPromptText("Deixe em branco para manter senha atual");
        }
    }

    @FXML
    private void handleSalvar() {
        if (validarFormulario()) {
            try {
                Usuario usuarioSalvar = modoEdicao ? usuario : new Usuario();
                atualizarUsuarioFromForm(usuarioSalvar);

                boolean sucesso;
                if (modoEdicao) {
                    sucesso = usuarioDAO.atualizar(usuarioSalvar);
                } else {
                    sucesso = usuarioDAO.salvar(usuarioSalvar);
                }

                if (sucesso) {
                    mostrarAlerta("Sucesso",
                            modoEdicao ? "Usuário atualizado com sucesso!" : "Usuário criado com sucesso!");
                    fecharJanela();
                } else {
                    mostrarAlerta("Erro", "Erro ao salvar usuário.");
                }

            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao salvar usuário: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    private boolean validarFormulario() {
        StringBuilder erros = new StringBuilder();

        if (txtNome.getText().trim().isEmpty()) {
            erros.append("• Nome é obrigatório\n");
        }

        if (txtEmail.getText().trim().isEmpty()) {
            erros.append("• Email é obrigatório\n");
        } else if (!txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            erros.append("• Email inválido\n");
        }

        if (txtLogin.getText().trim().isEmpty()) {
            erros.append("• Login é obrigatório\n");
        }

        if (!modoEdicao && txtSenha.getText().isEmpty()) {
            erros.append("• Senha é obrigatória para novo usuário\n");
        }

        if (cbPerfil.getValue() == null) {
            erros.append("• Perfil é obrigatório\n");
        }

        if (erros.length() > 0) {
            mostrarAlerta("Validação", "Corrija os seguintes erros:\n\n" + erros.toString());
            return false;
        }

        return true;
    }

    private void atualizarUsuarioFromForm(Usuario usuario) {
        usuario.setNome(txtNome.getText().trim());
        usuario.setCpf(txtCpf.getText().trim());
        usuario.setEmail(txtEmail.getText().trim());
        usuario.setCargo(txtCargo.getText().trim());
        usuario.setLogin(txtLogin.getText().trim());
        usuario.setPerfil(cbPerfil.getValue());
        usuario.setAtivo(chkAtivo.isSelected());

        // Só atualiza senha se foi preenchida
        if (!txtSenha.getText().isEmpty()) {
            usuario.setSenha(txtSenha.getText().trim());
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnSalvar.getScene().getWindow();
        stage.close();
    }
}