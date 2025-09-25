package Controller;

import com.gestao.projetos.dao.EquipeDAO;
import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.model.entity.Equipe;
import com.gestao.projetos.model.entity.Projeto;
import com.gestao.projetos.model.entity.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

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
    @FXML private ListView<Equipe> listViewEquipes;
    @FXML private Label lblTotalEquipes;

    private ObservableList<Equipe> equipesList;
    private EquipeDAO equipeDAO;
    private Projeto projeto;
    private ProjetoDAO projetoDAO;
    private UsuarioDAO usuarioDAO;
    private DashboardController dashboardController;
    private boolean modoEdicao = false;

    @FXML
    private void initialize() {
        System.out.println("🔄 Inicializando ProjetoFormController...");

        // Inicializar DAOs
        projetoDAO = new ProjetoDAO();
        usuarioDAO = new UsuarioDAO();
        equipeDAO = new EquipeDAO();
        equipesList = FXCollections.observableArrayList();

        // Configurar componentes
        configurarComponentes();
        carregarDados();

        // Configurar lista de equipes
        if (listViewEquipes != null) {
            listViewEquipes.setItems(equipesList);
            configurarCellFactoryEquipes();
        }

        atualizarTotalEquipes();

        // Debug: verificar se os componentes foram injetados
        System.out.println("btnSalvar é null? " + (btnSalvar == null));
        System.out.println("listViewEquipes é null? " + (listViewEquipes == null));
        System.out.println("lblTotalEquipes é null? " + (lblTotalEquipes == null));
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

    private void configurarCellFactoryEquipes() {
        listViewEquipes.setCellFactory(new Callback<ListView<Equipe>, ListCell<Equipe>>() {
            @Override
            public ListCell<Equipe> call(ListView<Equipe> param) {
                return new ListCell<Equipe>() {
                    @Override
                    protected void updateItem(Equipe equipe, boolean empty) {
                        super.updateItem(equipe, empty);
                        if (empty || equipe == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(equipe.getNome() + " (" + equipe.getQuantidadeMembros() + " membros)");

                            Button btnRemover = new Button("✕");
                            btnRemover.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                            btnRemover.setOnAction(event -> removerEquipe(equipe));

                            setGraphic(btnRemover);
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleAdicionarEquipes() {
        try {
            // Carregar todas as equipes disponíveis
            List<Equipe> todasEquipes = equipeDAO.listarTodos();
            ObservableList<Equipe> equipesDisponiveis = FXCollections.observableArrayList(todasEquipes);

            // Remover equipes já adicionadas
            equipesDisponiveis.removeAll(equipesList);

            if (equipesDisponiveis.isEmpty()) {
                mostrarAlerta("Informação", "Todas as equipes já estão neste projeto.");
                return;
            }

            // Criar diálogo de seleção
            Dialog<Equipe> dialog = criarDialogoSelecaoEquipes("Selecionar Equipe", "Escolha uma equipe para adicionar ao projeto:", equipesDisponiveis);

            dialog.showAndWait().ifPresent(equipeSelecionada -> {
                if (equipeSelecionada != null && !equipesList.contains(equipeSelecionada)) {
                    equipesList.add(equipeSelecionada);
                    atualizarTotalEquipes();
                }
            });

        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao carregar equipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Dialog<Equipe> criarDialogoSelecaoEquipes(String titulo, String cabecalho, ObservableList<Equipe> equipes) {
        Dialog<Equipe> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(cabecalho);

        ButtonType confirmarButtonType = new ButtonType("Selecionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmarButtonType, ButtonType.CANCEL);

        ListView<Equipe> listView = new ListView<>(equipes);
        listView.setPrefSize(400, 300);

        listView.setCellFactory(new Callback<ListView<Equipe>, ListCell<Equipe>>() {
            @Override
            public ListCell<Equipe> call(ListView<Equipe> param) {
                return new ListCell<Equipe>() {
                    @Override
                    protected void updateItem(Equipe equipe, boolean empty) {
                        super.updateItem(equipe, empty);
                        if (empty || equipe == null) {
                            setText(null);
                        } else {
                            setText(equipe.getNome() + " (" + equipe.getQuantidadeMembros() + " membros)");
                        }
                    }
                };
            }
        });

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmarButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog;
    }

    private void removerEquipe(Equipe equipe) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Remoção");
        confirmacao.setHeaderText("Remover Equipe");
        confirmacao.setContentText("Tem certeza que deseja remover " + equipe.getNome() + " do projeto?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                equipesList.remove(equipe);
                atualizarTotalEquipes();
            }
        });
    }

    private void atualizarTotalEquipes() {
        if (lblTotalEquipes != null) {
            lblTotalEquipes.setText("Total de equipes: " + equipesList.size());
        }
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

            // Limpar itens existentes
            cbResponsavel.getItems().clear();

            // ✅ CORRETO: Adicionar objetos Usuario completos
            for (Usuario usuario : usuarios) {
                cbResponsavel.getItems().add(usuario);
            }

            // ✅ Configurar para mostrar apenas o nome no ComboBox
            cbResponsavel.setCellFactory(lv -> new ListCell<Usuario>() {
                @Override
                protected void updateItem(Usuario usuario, boolean empty) {
                    super.updateItem(usuario, empty);
                    if (empty || usuario == null) {
                        setText(null);
                    } else {
                        setText(usuario.getNome());
                    }
                }
            });

            cbResponsavel.setButtonCell(new ListCell<Usuario>() {
                @Override
                protected void updateItem(Usuario usuario, boolean empty) {
                    super.updateItem(usuario, empty);
                    if (empty || usuario == null) {
                        setText(null);
                    } else {
                        setText(usuario.getNome());
                    }
                }
            });

            if (!usuarios.isEmpty()) {
                cbResponsavel.getSelectionModel().selectFirst();
            }

            System.out.println("✅ " + usuarios.size() + " usuários carregados no ComboBox");

        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar usuários: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao carregar usuários: " + e.getMessage());
        }
    }

    public void configurarModoVisualizacao() {
        System.out.println("🔒 Configurando modo visualização (sem interação)");

        // Para campos de texto: somente leitura
        txtNome.setEditable(false);
        txtDescricao.setEditable(false);
        txtOrcamento.setEditable(false);

        // Para DatePickers: desabilitar mas manter valor visível
        dpDataInicio.setDisable(true);
        dpDataInicio.setMouseTransparent(true);
        dpDataInicio.setFocusTraversable(false);

        dpDataFim.setDisable(true);
        dpDataFim.setMouseTransparent(true);
        dpDataFim.setFocusTraversable(false);

        // Para ComboBoxes: desabilitar interação mas manter aparência
        cbStatus.setDisable(true);
        cbStatus.setMouseTransparent(true);
        cbStatus.setFocusTraversable(false);
        cbStatus.setOpacity(1.0);

        cbResponsavel.setDisable(true);
        cbResponsavel.setMouseTransparent(true);
        cbResponsavel.setFocusTraversable(false);
        cbResponsavel.setOpacity(1.0);

        cbPrioridade.setDisable(true);
        cbPrioridade.setMouseTransparent(true);
        cbPrioridade.setFocusTraversable(false);
        cbPrioridade.setOpacity(1.0);

        // Botões
        btnSalvar.setVisible(false);
        btnCancelar.setText("Fechar");

        if (lblTitulo != null) {
            lblTitulo.setText("📋 " + (projeto != null ? projeto.getNome() : "Visualizar Projeto"));
        }

        System.out.println("✅ Modo visualização configurado (sem interação)");
    }

    public void setProjetoParaEdicao(Projeto projeto) {
        this.projeto = projeto;
        this.modoEdicao = true;
        preencherFormulario();
        if (lblTitulo != null) {
            lblTitulo.setText("Editar Projeto - " + projeto.getNome());
        }

        // 🔥 CARREGAR EQUIPES DO PROJETO
        if (projeto.getEquipes() != null) {
            equipesList.setAll(projeto.getEquipes());
            atualizarTotalEquipes();
        }
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

                // 🔥 ADICIONAR EQUIPES AO PROJETO
                projetoSalvar.setEquipes(new java.util.ArrayList<>(equipesList));

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
                        // Tenta chamar carregarProjetos() se existir
                        try {
                            dashboardController.getClass().getMethod("carregarProjetos").invoke(dashboardController);
                        } catch (Exception e) {
                            System.out.println("⚠️ Método carregarProjetos não encontrado, continuando...");
                        }
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

        if (txtNome.getText().trim().isEmpty()) {
            mostrarErro(txtNome, "Nome do projeto é obrigatório.");
            valido = false;
        } else {
            removerErro(txtNome);
        }

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

        if (cbResponsavel.getSelectionModel().getSelectedItem() == null) {
            mostrarErro(cbResponsavel, "Selecione um responsável.");
            valido = false;
        } else {
            removerErro(cbResponsavel);
        }

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