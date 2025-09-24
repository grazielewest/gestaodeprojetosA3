// EquipeFormController.java
package Controller;

import com.gestao.projetos.dao.EquipeDAO;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.model.entity.Equipe;
import com.gestao.projetos.model.entity.Usuario;
import com.gestao.projetos.model.entity.Projeto;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class EquipeFormController {

    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ListView<Usuario> listViewMembros;
    @FXML private ListView<Projeto> listViewProjetos;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;
    @FXML private Label lblTitulo;
    @FXML private CheckBox chkAtiva;

    private Equipe equipe;
    private EquipeDAO equipeDAO;
    private UsuarioDAO usuarioDAO;
    private ProjetoDAO projetoDAO;
    private boolean modoEdicao = false;

    private ObservableList<Usuario> todosUsuarios;
    private ObservableList<Projeto> todosProjetos;
    private ObservableList<Usuario> membrosSelecionados;
    private ObservableList<Projeto> projetosSelecionados;

    @FXML
    private void initialize() {
        equipeDAO = new EquipeDAO();
        usuarioDAO = new UsuarioDAO();
        projetoDAO = new ProjetoDAO();

        configurarComponentes();
        carregarDados();
    }

    private void configurarComponentes() {
        // Configurar ListViews
        membrosSelecionados = FXCollections.observableArrayList();
        projetosSelecionados = FXCollections.observableArrayList();

        listViewMembros.setItems(membrosSelecionados);
        listViewProjetos.setItems(projetosSelecionados);

        // Configurar seleção múltipla
        listViewMembros.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewProjetos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Por padrão, equipe está ativa
        chkAtiva.setSelected(true);
    }

    private void carregarDados() {
        try {
            // Carregar todos os usuários
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            todosUsuarios = FXCollections.observableArrayList(usuarios);

            // Carregar todos os projetos
            List<Projeto> projetos = projetoDAO.listarTodos();
            todosProjetos = FXCollections.observableArrayList(projetos);

            System.out.println("✅ " + usuarios.size() + " usuários e " + projetos.size() + " projetos carregados");

        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar dados: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao carregar dados: " + e.getMessage());
        }
    }

    public void setEquipeParaEdicao(Equipe equipe) {
        this.equipe = equipe;
        this.modoEdicao = true;
        preencherFormulario();
        lblTitulo.setText("Editar Equipe - " + equipe.getNome());
    }

    private void preencherFormulario() {
        if (equipe != null) {
            txtNome.setText(equipe.getNome());
            txtDescricao.setText(equipe.getDescricao());
            chkAtiva.setSelected(equipe.isAtiva());

            // Preencher membros
            membrosSelecionados.clear();
            membrosSelecionados.addAll(equipe.getMembros());

            // Preencher projetos
            projetosSelecionados.clear();
            projetosSelecionados.addAll(equipe.getProjetos());
        }
    }

    @FXML
    private void handleAdicionarMembros() {
        // Dialog para selecionar usuários
        Dialog<List<Usuario>> dialog = criarDialogSelecao("Selecionar Membros", todosUsuarios, membrosSelecionados);
        dialog.showAndWait().ifPresent(selecionados -> {
            membrosSelecionados.setAll(selecionados);
        });
    }

    @FXML
    private void handleAdicionarProjetos() {
        // Dialog para selecionar projetos
        Dialog<List<Projeto>> dialog = criarDialogSelecao("Selecionar Projetos", todosProjetos, projetosSelecionados);
        dialog.showAndWait().ifPresent(selecionados -> {
            projetosSelecionados.setAll(selecionados);
        });
    }

    private <T> Dialog<List<T>> criarDialogSelecao(String titulo, ObservableList<T> todos, ObservableList<T> selecionados) {
        Dialog<List<T>> dialog = new Dialog<>();
        dialog.setTitle(titulo);

        ListView<T> listView = new ListView<>(todos);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Selecionar itens já escolhidos
        for (T item : selecionados) {
            int index = todos.indexOf(item);
            if (index >= 0) {
                listView.getSelectionModel().select(index);
            }
        }

        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return listView.getSelectionModel().getSelectedItems();
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void handleSalvar() {
        if (validarFormulario()) {
            try {
                Equipe equipeSalvar = modoEdicao ? equipe : new Equipe();
                atualizarEquipeFromForm(equipeSalvar);

                boolean sucesso;
                if (modoEdicao) {
                    sucesso = equipeDAO.atualizar(equipeSalvar);
                } else {
                    sucesso = equipeDAO.salvar(equipeSalvar);
                }

                if (sucesso) {
                    mostrarAlerta("Sucesso",
                            modoEdicao ? "Equipe atualizada com sucesso!" : "Equipe criada com sucesso!");
                    fecharJanela();
                } else {
                    mostrarAlerta("Erro", "Erro ao salvar equipe.");
                }

            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao salvar equipe: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    private boolean validarFormulario() {
        if (txtNome.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Nome da equipe é obrigatório.");
            return false;
        }

        if (membrosSelecionados.isEmpty()) {
            mostrarAlerta("Validação", "Selecione pelo menos um membro para a equipe.");
            return false;
        }

        return true;
    }

    private void atualizarEquipeFromForm(Equipe equipe) {
        equipe.setNome(txtNome.getText().trim());
        equipe.setDescricao(txtDescricao.getText().trim());
        equipe.setAtiva(chkAtiva.isSelected());

        // Atualizar membros
        equipe.getMembros().clear();
        equipe.getMembros().addAll(membrosSelecionados);

        // Atualizar projetos
        equipe.getProjetos().clear();
        equipe.getProjetos().addAll(projetosSelecionados);
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