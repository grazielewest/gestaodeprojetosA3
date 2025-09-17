package Controller;

import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.model.entity.Projeto;
import com.gestao.projetos.model.entity.ProjetoConstantes;
import com.gestao.projetos.model.entity.Usuario;
import com.gestao.projetos.util.Validacao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class ProjetoFormController {

    @FXML
    private TextField txtNome;

    @FXML
    private TextArea txtDescricao;

    @FXML
    private DatePicker dpDataInicio;

    @FXML
    private DatePicker dpDataFim;

    @FXML
    private ComboBox<String> cbStatus;

    @FXML
    private ComboBox<Usuario> cbResponsavel;

    @FXML
    private TextField txtOrcamento;

    @FXML
    private ComboBox<String> cbPrioridade;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Label lblTitulo;

    private Projeto projeto;
    private ProjetoDAO projetoDAO;
    private UsuarioDAO usuarioDAO;
    private DashboardController dashboardController;
    private boolean modoEdicao = false;

    @FXML
    private void initialize() {
        projetoDAO = new ProjetoDAO();
        usuarioDAO = new UsuarioDAO();
        configurarComponentes();
        carregarDados();
    }

    private void configurarComponentes() {
        // Configurar ComboBox de Status
        cbStatus.getItems().addAll(ProjetoConstantes.STATUS);
        cbStatus.getSelectionModel().selectFirst();

        // Configurar ComboBox de Prioridade
        cbPrioridade.getItems().addAll(ProjetoConstantes.PRIORIDADES);
        cbPrioridade.getSelectionModel().select("Média");

        // Configurar DatePickers
        dpDataInicio.setValue(LocalDate.now());
        dpDataFim.setValue(LocalDate.now().plusMonths(1));

        // Configurar validação de orçamento
        txtOrcamento.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));

        // Adicionar listeners para validação em tempo real
        adicionarListenersValidacao();
    }

    private void adicionarListenersValidacao() {
        txtNome.textProperty().addListener((observable, oldValue, newValue) -> {
            Validacao.validarObrigatoriedade(txtNome, "Nome do projeto");
        });

        dpDataInicio.valueProperty().addListener((observable, oldValue, newValue) -> {
            validarDatas();
        });

        dpDataFim.valueProperty().addListener((observable, oldValue, newValue) -> {
            validarDatas();
        });
    }

    private void carregarDados() {
        try {
            // Carregar usuários para o ComboBox de responsáveis
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            cbResponsavel.getItems().addAll(usuarios);

            if (!usuarios.isEmpty()) {
                cbResponsavel.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao carregar usuários: " + e.getMessage());
        }
    }

    public void setProjetoParaEdicao(Projeto projeto) {
        this.projeto = projeto;
        this.modoEdicao = true;
        preencherFormulario();
        lblTitulo.setText("Editar Projeto - " + projeto.getNome());
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    private void preencherFormulario() {
        if (projeto != null) {
            txtNome.setText(projeto.getNome());
            txtDescricao.setText(projeto.getDescricao());
            dpDataInicio.setValue(projeto.getDataInicio());
            dpDataFim.setValue(projeto.getDataFim());
            cbStatus.setValue(projeto.getStatus());
            txtOrcamento.setText(String.valueOf(projeto.getOrcamento()));
            cbPrioridade.setValue(projeto.getPrioridade());

            // Selecionar o responsável correto
            if (projeto.getIdResponsavel() > 0) {
                for (Usuario usuario : cbResponsavel.getItems()) {
                    if (usuario.getId() == projeto.getIdResponsavel()) {
                        cbResponsavel.getSelectionModel().select(usuario);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    private void handleSalvar() {
        if (validarFormulario()) {
            try {
                Projeto projetoSalvar;

                if (modoEdicao) {
                    projetoSalvar = projeto;
                    atualizarProjetoFromForm(projetoSalvar);
                } else {
                    projetoSalvar = criarProjetoFromForm();
                }

                boolean sucesso;
                if (modoEdicao) {
                    sucesso = projetoDAO.atualizar(projetoSalvar);
                } else {
                    sucesso = projetoDAO.inserir(projetoSalvar) > 0;
                }

                if (sucesso) {
                    mostrarAlerta("Sucesso",
                            modoEdicao ? "Projeto atualizado com sucesso!" : "Projeto criado com sucesso!");

                    if (dashboardController != null) {
                        dashboardController.atualizarListaProjetos();
                    }

                    fecharJanela();
                } else {
                    mostrarAlerta("Erro", "Erro ao salvar projeto.");
                }

            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao salvar projeto: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelar() {
        fecharJanela();
    }

    private boolean validarFormulario() {
        boolean valido = true;

        // Validar nome
        if (!Validacao.validarObrigatoriedade(txtNome, "Nome do projeto")) {
            valido = false;
        }

        // Validar datas
        if (dpDataInicio.getValue() == null) {
            mostrarErro(dpDataInicio, "Data de início é obrigatória.");
            valido = false;
        }

        if (dpDataFim.getValue() == null) {
            mostrarErro(dpDataFim, "Data de fim é obrigatória.");
            valido = false;
        }

        if (dpDataInicio.getValue() != null && dpDataFim.getValue() != null) {
            if (dpDataFim.getValue().isBefore(dpDataInicio.getValue())) {
                mostrarAlerta("Erro", "Data de fim não pode ser anterior à data de início.");
                valido = false;
            }
        }

        // Validar responsável
        if (cbResponsavel.getSelectionModel().getSelectedItem() == null) {
            mostrarErro(cbResponsavel, "Selecione um responsável.");
            valido = false;
        }

        // Validar orçamento
        if (txtOrcamento.getText().isEmpty()) {
            mostrarErro(txtOrcamento, "Orçamento é obrigatório.");
            valido = false;
        } else {
            try {
                double orcamento = Double.parseDouble(txtOrcamento.getText());
                if (orcamento < 0) {
                    mostrarErro(txtOrcamento, "Orçamento não pode ser negativo.");
                    valido = false;
                }
            } catch (NumberFormatException e) {
                mostrarErro(txtOrcamento, "Orçamento deve ser um número válido.");
                valido = false;
            }
        }

        return valido;
    }

    private void validarDatas() {
        if (dpDataInicio.getValue() != null && dpDataFim.getValue() != null) {
            if (dpDataFim.getValue().isBefore(dpDataInicio.getValue())) {
                mostrarErro(dpDataFim, "Data de fim não pode ser anterior à data de início.");
            } else {
                removerErro(dpDataFim);
            }
        }
    }

    private Projeto criarProjetoFromForm() {
        Projeto novoProjeto = new Projeto();
        atualizarProjetoFromForm(novoProjeto);
        return novoProjeto;
    }

    private void atualizarProjetoFromForm(Projeto projeto) {
        projeto.setNome(txtNome.getText().trim());
        projeto.setDescricao(txtDescricao.getText().trim());
        projeto.setDataInicio(dpDataInicio.getValue());
        projeto.setDataFim(dpDataFim.getValue());
        projeto.setStatus(cbStatus.getValue());
        projeto.setIdResponsavel(cbResponsavel.getSelectionModel().getSelectedItem().getId());
        projeto.setOrcamento(Double.parseDouble(txtOrcamento.getText()));
        projeto.setPrioridade(cbPrioridade.getValue());
    }

    private void mostrarErro(Control campo, String mensagem) {
        campo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        if (campo.getTooltip() == null) {
            Tooltip tooltip = new Tooltip();
            campo.setTooltip(tooltip);
        }
        campo.getTooltip().setText(mensagem);
        campo.getTooltip().show(campo.getScene().getWindow());
    }

    private void removerErro(Control campo) {
        campo.setStyle("");
        if (campo.getTooltip() != null) {
            campo.getTooltip().hide();
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

    // Métodos para facilitar testes
    public Projeto getProjeto() {
        return projeto;
    }

    public boolean isModoEdicao() {
        return modoEdicao;
    }
}