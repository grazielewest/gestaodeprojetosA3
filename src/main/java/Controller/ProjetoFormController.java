package Controller;

import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.model.entity.Projeto;
import com.gestao.projetos.model.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class ProjetoFormController {

    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private DatePicker dpDataInicio;
    @FXML private DatePicker dpDataFim;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<Usuario> cbResponsavel;
    @FXML private TextField txtOrcamento;
    @FXML private ComboBox<String> cbPrioridade;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;
    @FXML private Label lblTitulo;

    private Projeto projeto;
    private ProjetoDAO projetoDAO;
    private UsuarioDAO usuarioDAO;
    private DashboardController dashboardController;
    private boolean modoEdicao = false;

    @FXML
    private void initialize() {
        System.out.println("🔄 Inicializando ProjetoFormController...");

        // Debug: verificar se os componentes foram injetados
        System.out.println("btnSalvar é null? " + (btnSalvar == null));
        System.out.println("btnCancelar é null? " + (btnCancelar == null));
        System.out.println("txtNome é null? " + (txtNome == null));
        System.out.println("cbStatus é null? " + (cbStatus == null));

        projetoDAO = new ProjetoDAO();
        usuarioDAO = new UsuarioDAO();
        configurarComponentes();
        carregarDados();
    }

    private void configurarComponentes() {
        // Configurar ComboBox de Status
        cbStatus.getItems().addAll("Planejamento", "Em Andamento", "Concluído", "Cancelado");
        cbStatus.getSelectionModel().selectFirst();

        // Configurar ComboBox de Prioridade
        cbPrioridade.getItems().addAll("Baixa", "Média", "Alta");
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

            // Já é LocalDate - não precisa converter!
            dpDataInicio.setValue(projeto.getDataInicio());
            dpDataFim.setValue(projeto.getDataFim());

            cbStatus.setValue(projeto.getStatus());

            if (projeto.getOrcamento() > 0) {
                txtOrcamento.setText(String.valueOf(projeto.getOrcamento()));
            }

            cbPrioridade.setValue(projeto.getPrioridade());

            // Selecionar o responsável
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
                Projeto projetoSalvar = modoEdicao ? projeto : new Projeto();
                atualizarProjetoFromForm(projetoSalvar);

                boolean sucesso;
                if (modoEdicao) {
                    sucesso = projetoDAO.atualizar(projetoSalvar);
                } else {
                    sucesso = projetoDAO.salvar(projetoSalvar);
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
                e.printStackTrace();
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
        if (txtNome.getText().trim().isEmpty()) {
            mostrarErro(txtNome, "Nome do projeto é obrigatório.");
            valido = false;
        } else {
            removerErro(txtNome);
        }

        // Validar datas
        if (dpDataInicio.getValue() == null) {
            mostrarErro(dpDataInicio, "Data de início é obrigatória.");
            valido = false;
        } else {
            removerErro(dpDataInicio);
        }

        if (dpDataFim.getValue() == null) {
            mostrarErro(dpDataFim, "Data de fim é obrigatória.");
            valido = false;
        } else {
            removerErro(dpDataFim);
        }

        if (dpDataInicio.getValue() != null && dpDataFim.getValue() != null) {
            if (dpDataFim.getValue().isBefore(dpDataInicio.getValue())) {
                mostrarErro(dpDataFim, "Data de fim não pode ser anterior à data de início.");
                valido = false;
            } else {
                removerErro(dpDataFim);
            }
        }

        // Validar responsável
        if (cbResponsavel.getSelectionModel().getSelectedItem() == null) {
            mostrarErro(cbResponsavel, "Selecione um responsável.");
            valido = false;
        } else {
            removerErro(cbResponsavel);
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
                } else {
                    removerErro(txtOrcamento);
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

    private void atualizarProjetoFromForm(Projeto projeto) {
        projeto.setNome(txtNome.getText().trim());
        projeto.setDescricao(txtDescricao.getText().trim());

        // Já é LocalDate - não precisa converter!
        projeto.setDataInicio(dpDataInicio.getValue());
        projeto.setDataFim(dpDataFim.getValue());

        projeto.setStatus(cbStatus.getValue());

        Usuario responsavel = cbResponsavel.getSelectionModel().getSelectedItem();
        if (responsavel != null) {
            projeto.setIdResponsavel(responsavel.getId());
        }

        if (!txtOrcamento.getText().isEmpty()) {
            projeto.setOrcamento(Double.parseDouble(txtOrcamento.getText()));
        }

        projeto.setPrioridade(cbPrioridade.getValue());

        // Atualizar data de atualização
        projeto.setDataAtualizacao(LocalDate.now());
    }

    private void mostrarErro(Control campo, String mensagem) {
        campo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        if (campo.getTooltip() == null) {
            Tooltip tooltip = new Tooltip(mensagem);
            campo.setTooltip(tooltip);
        } else {
            campo.getTooltip().setText(mensagem);
        }
    }

    private void removerErro(Control campo) {
        campo.setStyle("");
        if (campo.getTooltip() != null) {
            campo.getTooltip().setText("");
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