package Controller;

import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.dao.TarefaDAO;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.model.entity.Projeto;
import com.gestao.projetos.model.entity.Tarefa;
import com.gestao.projetos.model.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.List;

public class TarefaFormController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<Projeto> cbProjeto;
    @FXML private ComboBox<Usuario> cbResponsavel;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<String> cbPrioridade;
    @FXML private DatePicker dpDataInicioPrevista;
    @FXML private DatePicker dpDataFimPrevista;
    @FXML private DatePicker dpDataInicioReal;
    @FXML private DatePicker dpDataFimReal;
    @FXML private Button btnSalvar;

    private TarefaDAO tarefaDAO;
    private ProjetoDAO projetoDAO;
    private UsuarioDAO usuarioDAO;
    private Tarefa tarefaParaEdicao;
    private DashboardController dashboardController;

    @FXML
    private void initialize() {
        tarefaDAO = new TarefaDAO();
        projetoDAO = new ProjetoDAO();
        usuarioDAO = new UsuarioDAO();

        carregarComboboxes();
        configurarValidacoes();
    }

    private void carregarComboboxes() {
        // Carregar projetos
        List<Projeto> projetos = projetoDAO.listarTodos();
        cbProjeto.getItems().addAll(projetos);

        // Carregar usuários (responsáveis)
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        cbResponsavel.getItems().addAll(usuarios);

        // Carregar status
        cbStatus.getItems().addAll("Pendente", "Em Execução", "Concluída", "Cancelada");

        // Carregar prioridades
        cbPrioridade.getItems().addAll("Baixa", "Média", "Alta", "Urgente");

        // Selecionar valores padrão
        cbStatus.setValue("Pendente");
        cbPrioridade.setValue("Média");
    }

    private void configurarValidacoes() {
        // Validação de datas
        dpDataFimPrevista.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (dpDataInicioPrevista.getValue() != null && date != null) {
                    setDisable(date.isBefore(dpDataInicioPrevista.getValue()));
                }
            }
        });
    }

    public void setTarefaParaEdicao(Tarefa tarefa) {
        this.tarefaParaEdicao = tarefa;
        preencherFormulario();
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    private void preencherFormulario() {
        if (tarefaParaEdicao != null) {
            lblTitulo.setText("Editar Tarefa");
            txtTitulo.setText(tarefaParaEdicao.getTitulo());
            txtDescricao.setText(tarefaParaEdicao.getDescricao());
            cbStatus.setValue(tarefaParaEdicao.getStatus());
            cbPrioridade.setValue(tarefaParaEdicao.getPrioridade());

            // Selecionar projeto
            for (Projeto projeto : cbProjeto.getItems()) {
                if (projeto.getId() == tarefaParaEdicao.getIdProjeto()) {
                    cbProjeto.setValue(projeto);
                    break;
                }
            }

            // Selecionar responsável
            for (Usuario usuario : cbResponsavel.getItems()) {
                if (usuario.getId() == tarefaParaEdicao.getIdResponsavel()) {
                    cbResponsavel.setValue(usuario);
                    break;
                }
            }

            // Preencher datas
            if (tarefaParaEdicao.getDataInicioPrevista() != null) {
                dpDataInicioPrevista.setValue(tarefaParaEdicao.getDataInicioPrevista());
            }
            if (tarefaParaEdicao.getDataFimPrevista() != null) {
                dpDataFimPrevista.setValue(tarefaParaEdicao.getDataFimPrevista());
            }
            if (tarefaParaEdicao.getDataInicioReal() != null) {
                dpDataInicioReal.setValue(tarefaParaEdicao.getDataInicioReal());
            }
            if (tarefaParaEdicao.getDataFimReal() != null) {
                dpDataFimReal.setValue(tarefaParaEdicao.getDataFimReal());
            }
        }
    }

    @FXML
    private void handleSalvar() {
        if (!validarFormulario()) {
            return;
        }

        try {
            Tarefa tarefa = (tarefaParaEdicao != null) ? tarefaParaEdicao : new Tarefa();

            tarefa.setTitulo(txtTitulo.getText().trim());
            tarefa.setDescricao(txtDescricao.getText().trim());
            tarefa.setIdProjeto(cbProjeto.getValue().getId());
            tarefa.setIdResponsavel(cbResponsavel.getValue().getId());
            tarefa.setStatus(cbStatus.getValue());
            tarefa.setPrioridade(cbPrioridade.getValue());
            tarefa.setDataInicioPrevista(dpDataInicioPrevista.getValue());
            tarefa.setDataFimPrevista(dpDataFimPrevista.getValue());
            tarefa.setDataInicioReal(dpDataInicioReal.getValue());
            tarefa.setDataFimReal(dpDataFimReal.getValue());

            boolean sucesso = tarefaDAO.salvar(tarefa);

            if (sucesso) {
                showAlert("Sucesso",
                        tarefaParaEdicao != null ? "Tarefa atualizada com sucesso!" : "Tarefa criada com sucesso!");

                if (dashboardController != null) {
                    dashboardController.atualizarListaProjetos();
                }

                fecharJanela();
            } else {
                showAlert("Erro", "Erro ao salvar tarefa.");
            }

        } catch (Exception e) {
            showAlert("Erro", "Erro ao salvar tarefa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validarFormulario() {
        if (txtTitulo.getText() == null || txtTitulo.getText().trim().isEmpty()) {
            showAlert("Validação", "O título é obrigatório.");
            txtTitulo.requestFocus();
            return false;
        }

        if (cbProjeto.getValue() == null) {
            showAlert("Validação", "Selecione um projeto.");
            cbProjeto.requestFocus();
            return false;
        }

        if (cbResponsavel.getValue() == null) {
            showAlert("Validação", "Selecione um responsável.");
            cbResponsavel.requestFocus();
            return false;
        }

        if (cbStatus.getValue() == null) {
            showAlert("Validação", "Selecione um status.");
            cbStatus.requestFocus();
            return false;
        }

        if (dpDataInicioPrevista.getValue() != null && dpDataFimPrevista.getValue() != null) {
            if (dpDataFimPrevista.getValue().isBefore(dpDataInicioPrevista.getValue())) {
                showAlert("Validação", "A data fim prevista não pode ser anterior à data início.");
                dpDataFimPrevista.requestFocus();
                return false;
            }
        }

        return true;
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnSalvar.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}