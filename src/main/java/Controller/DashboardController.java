package Controller;

import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.dao.TarefaDAO;
import com.gestao.projetos.model.entity.Projeto;
import com.gestao.projetos.model.entity.Tarefa;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.TableCell;

public class DashboardController {

    @FXML
    private TableView<Projeto> tabelaProjetos;
    @FXML
    private TableColumn<Projeto, Integer> colId;
    @FXML
    private TableColumn<Projeto, String> colNome;
    @FXML
    private TableColumn<Projeto, String> colStatus;
    @FXML
    private TableColumn<Projeto, String> colDataInicio;
    @FXML
    private TableColumn<Projeto, String> colDataFim;
    @FXML
    private TableColumn<Projeto, String> colResponsavel;
    @FXML
    private TableColumn<Projeto, String> colEquipes;

    // Labels para o dashboard
    @FXML
    private Label lblProjetosAtivos;
    @FXML
    private Label lblTarefasPendentes;
    @FXML
    private Label lblProjetosAtrasados;
    @FXML
    private Label lblEquipesAtivas;
    @FXML
    private Label lblTotalProjetos;
    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblStatus;

    @FXML private TableView<Tarefa> tabelaTarefas;
    @FXML private TableColumn<Tarefa, Integer> colTarefaId;
    @FXML private TableColumn<Tarefa, String> colTarefaTitulo;
    @FXML private TableColumn<Tarefa, String> colTarefaProjeto;
    @FXML private TableColumn<Tarefa, String> colTarefaResponsavel;
    @FXML private TableColumn<Tarefa, String> colTarefaStatus;
    @FXML private TableColumn<Tarefa, String> colTarefaPrioridade;
    @FXML private TableColumn<Tarefa, String> colTarefaInicioPrevisto;
    @FXML private TableColumn<Tarefa, String> colTarefaFimPrevisto;
    @FXML private TableColumn<Tarefa, String> colTarefaAtraso;

    @FXML private ComboBox<Projeto> cbFiltroProjetos;
    @FXML private ComboBox<String> cbFiltroStatus;
    @FXML private Label lblTotalTarefas;
    @FXML private Label lblTarefasAtrasadas;

    private ProjetoDAO projetoDAO;
    private ObservableList<Projeto> projetosList;

    private TarefaDAO tarefaDAO;
    private ObservableList<Tarefa> tarefasList;

    @FXML
    private void initialize() { // ✅ CORREÇÃO: Adicionei a chave de abertura aqui
        try {
            projetoDAO = new ProjetoDAO();
            tarefaDAO = new TarefaDAO();

            configurarTabelaProjetos();
            configurarTabelaTarefas();
            carregarProjetos();
            carregarTarefas();
            atualizarDashboard();
            configurarLabels();
            System.out.println("✅ DashboardController inicializado com sucesso!");
        } catch (Exception e) {
            System.out.println("❌ Erro na inicialização: " + e.getMessage());
            e.printStackTrace();
        }
    } // ✅ CORREÇÃO: Chave de fechamento do método initialize()

    // 🔥 NOVO MÉTODO: Configurar tabela de tarefas
    private void configurarTabelaTarefas() {
        try {
            System.out.println("🔄 Configurando tabela de tarefas...");

            colTarefaId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colTarefaTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
            colTarefaProjeto.setCellValueFactory(cellData -> {
                Tarefa tarefa = cellData.getValue();
                return new SimpleStringProperty(tarefa.getNomeProjeto());
            });
            colTarefaResponsavel.setCellValueFactory(cellData -> {
                Tarefa tarefa = cellData.getValue();
                return new SimpleStringProperty(tarefa.getNomeResponsavel());
            });
            colTarefaStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
            colTarefaPrioridade.setCellValueFactory(new PropertyValueFactory<>("prioridade"));
            colTarefaInicioPrevisto.setCellValueFactory(new PropertyValueFactory<>("dataInicioPrevistaFormatada"));
            colTarefaFimPrevisto.setCellValueFactory(new PropertyValueFactory<>("dataFimPrevistaFormatada"));

            colTarefaAtraso.setCellValueFactory(cellData -> {
                Tarefa tarefa = cellData.getValue();
                String situacao = tarefa.estaAtrasada() ? "🚨 Atrasada" : "✅ No prazo";
                return new SimpleStringProperty(situacao);
            });

            // Formatação de cores para status
            colTarefaStatus.setCellFactory(column -> new TableCell<Tarefa, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.replace("⏳ ", "").replace("🚀 ", "").replace("✅ ", "").replace("❌ ", ""));
                        Tarefa tarefa = getTableView().getItems().get(getIndex());
                        if (tarefa.estaAtrasada()) {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else if ("Concluída".equals(tarefa.getStatus())) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else if ("Em Execução".equals(tarefa.getStatus())) {
                            setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #6c757d;");
                        }
                    }
                }
            });

            colTarefaAtraso.setCellFactory(column -> new TableCell<Tarefa, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.contains("Atrasada")) {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        }
                    }
                }
            });

            tabelaTarefas.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            System.out.println("✅ Tabela de tarefas configurada com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro ao configurar tabela de tarefas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🔥 NOVO MÉTODO: Carregar tarefas
    private void carregarTarefas() {
        try {
            System.out.println("🔄 Carregando tarefas para a tabela...");
            List<Tarefa> tarefas = tarefaDAO.listarTodos();
            tarefasList = FXCollections.observableArrayList(tarefas);
            tabelaTarefas.setItems(tarefasList);

            if (lblTotalTarefas != null) {
                lblTotalTarefas.setText("Total: " + tarefas.size() + " tarefas");
            }

            System.out.println("✅ " + tarefas.size() + " tarefas carregadas na tabela");

        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar tarefas: " + e.getMessage());
            showAlert("Erro", "Erro ao carregar tarefas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🔥 NOVOS MÉTODOS PARA TAREFAS - REMOVIDOS OS DUPLICADOS

    @FXML
    private void handleEditarTarefa() {
        Tarefa tarefaSelecionada = tabelaTarefas.getSelectionModel().getSelectedItem();

        if (tarefaSelecionada == null) {
            showAlert("Aviso", "Selecione uma tarefa para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/tarefa-form.fxml"));
            Parent root = loader.load();

            TarefaFormController controller = loader.getController();
            controller.setTarefaParaEdicao(tarefaSelecionada);
            controller.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Editar Tarefa - " + tarefaSelecionada.getTitulo());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaTarefas.getScene().getWindow());
            stage.showAndWait();

            // Recarrega as tarefas após edição
            carregarTarefas();
            atualizarDashboard();

        } catch (Exception e) {
            showAlert("Erro", "Erro ao abrir formulário de edição: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExcluirTarefa() {
        Tarefa tarefaSelecionada = tabelaTarefas.getSelectionModel().getSelectedItem();

        if (tarefaSelecionada == null) {
            showAlert("Aviso", "Selecione uma tarefa para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir Tarefa");
        confirmacao.setContentText("Tem certeza que deseja excluir a tarefa: " + tarefaSelecionada.getTitulo() + "?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                boolean sucesso = tarefaDAO.excluir(tarefaSelecionada.getId());
                if (sucesso) {
                    showAlert("Sucesso", "Tarefa excluída com sucesso!");
                    carregarTarefas();
                    atualizarDashboard();
                } else {
                    showAlert("Erro", "Não foi possível excluir a tarefa.");
                }
            } catch (Exception e) {
                showAlert("Erro", "Erro ao excluir tarefa: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleConcluirTarefa() {
        Tarefa tarefaSelecionada = tabelaTarefas.getSelectionModel().getSelectedItem();

        if (tarefaSelecionada == null) {
            showAlert("Aviso", "Selecione uma tarefa para concluir.");
            return;
        }

        if ("Concluída".equals(tarefaSelecionada.getStatus())) {
            showAlert("Info", "Esta tarefa já está concluída.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Conclusão");
        confirmacao.setHeaderText("Concluir Tarefa");
        confirmacao.setContentText("Deseja marcar a tarefa '" + tarefaSelecionada.getTitulo() + "' como concluída?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                tarefaSelecionada.setStatus("Concluída");
                tarefaSelecionada.setDataFimReal(LocalDate.now());

                boolean sucesso = tarefaDAO.salvar(tarefaSelecionada);
                if (sucesso) {
                    showAlert("Sucesso", "Tarefa concluída com sucesso!");
                    carregarTarefas();
                    atualizarDashboard();
                } else {
                    showAlert("Erro", "Não foi possível concluir a tarefa.");
                }
            } catch (Exception e) {
                showAlert("Erro", "Erro ao concluir tarefa: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVerTarefasProjeto() {
        Projeto projetoSelecionado = tabelaProjetos.getSelectionModel().getSelectedItem();

        if (projetoSelecionado == null) {
            showAlert("Aviso", "Selecione um projeto para ver suas tarefas.");
            return;
        }

        try {
            List<Tarefa> tarefasDoProjeto = tarefaDAO.listarPorProjeto(projetoSelecionado.getId());

            StringBuilder mensagem = new StringBuilder();
            mensagem.append("Tarefas do Projeto: ").append(projetoSelecionado.getNome()).append("\n\n");

            for (Tarefa tarefa : tarefasDoProjeto) {
                mensagem.append("• ").append(tarefa.getTitulo())
                        .append(" (").append(tarefa.getStatus()).append(")\n");
            }

            if (tarefasDoProjeto.isEmpty()) {
                mensagem.append("Nenhuma tarefa encontrada para este projeto.");
            }

            showAlert("Tarefas do Projeto", mensagem.toString());

        } catch (Exception e) {
            showAlert("Erro", "Erro ao carregar tarefas do projeto: " + e.getMessage());
        }
    }

    @FXML
    private void handleRelatorioTarefas() {
        try {
            List<Tarefa> tarefas = tarefaDAO.listarTodos();

            StringBuilder relatorio = new StringBuilder();
            relatorio.append("RELATÓRIO DE TAREFAS\n");
            relatorio.append("====================\n\n");

            for (Tarefa tarefa : tarefas) {
                relatorio.append("ID: ").append(tarefa.getId()).append("\n");
                relatorio.append("Título: ").append(tarefa.getTitulo()).append("\n");
                relatorio.append("Projeto: ").append(tarefa.getNomeProjeto()).append("\n");
                relatorio.append("Responsável: ").append(tarefa.getNomeResponsavel()).append("\n");
                relatorio.append("Status: ").append(tarefa.getStatus()).append("\n");
                relatorio.append("Prioridade: ").append(tarefa.getPrioridade()).append("\n");
                relatorio.append("Início Previsto: ").append(tarefa.getDataInicioPrevistaFormatada()).append("\n");
                relatorio.append("Fim Previsto: ").append(tarefa.getDataFimPrevistaFormatada()).append("\n");
                relatorio.append("Situação: ").append(tarefa.estaAtrasada() ? "Atrasada" : "No prazo").append("\n");
                relatorio.append("-----------------------------\n");
            }

            relatorio.append("\nTotal de Tarefas: ").append(tarefas.size());

            TextArea textArea = new TextArea(relatorio.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            Stage stage = new Stage();
            stage.setTitle("Relatório de Tarefas");
            stage.setScene(new Scene(scrollPane, 700, 500));
            stage.show();

        } catch (Exception e) {
            showAlert("Erro", "Erro ao gerar relatório de tarefas: " + e.getMessage());
        }
    }

    private void configurarTabelaProjetos() {
        // Configuração das colunas básicas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        colDataInicio.setCellValueFactory(new PropertyValueFactory<>("dataInicioFormatada"));
        colDataFim.setCellValueFactory(new PropertyValueFactory<>("dataFimFormatada"));

        // 🔥 CORREÇÃO: Use os métodos corretos da sua classe Projeto
        colResponsavel.setCellValueFactory(cellData -> {
            Projeto projeto = cellData.getValue();
            return new SimpleStringProperty(projeto.getNomeResponsavel());
        });

        colEquipes.setCellValueFactory(cellData -> {
            Projeto projeto = cellData.getValue();
            return new SimpleStringProperty(projeto.getNomesEquipes());
        });

        // Formatação personalizada para status
        colStatus.setCellFactory(column -> new TableCell<Projeto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Projeto projeto = getTableView().getItems().get(getIndex());
                    if (projeto.estaAtrasado()) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if ("Concluído".equals(projeto.getStatus())) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if ("Em Andamento".equals(projeto.getStatus())) {
                        setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6c757d;");
                    }
                }
            }
        });

        // Permitir seleção de apenas uma linha
        tabelaProjetos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void carregarProjetos() {
        try {
            System.out.println("🔄 Carregando projetos para a tabela...");
            List<Projeto> projetos = projetoDAO.listarTodos();
            projetosList = FXCollections.observableArrayList(projetos);
            tabelaProjetos.setItems(projetosList);

            // Atualiza o label de total de projetos
            if (lblTotalProjetos != null) {
                lblTotalProjetos.setText("Total: " + projetos.size() + " projetos");
            }

            System.out.println("✅ " + projetos.size() + " projetos carregados na tabela");

        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar projetos: " + e.getMessage());
            showAlert("Erro", "Erro ao carregar projetos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarDashboard() {
        try {
            List<Projeto> projetos = projetoDAO.listarTodos();
            List<Tarefa> tarefas = tarefaDAO.listarTodos();

            // Calcular métricas
            long projetosAtivos = projetos.stream()
                    .filter(p -> "Em Andamento".equals(p.getStatus()))
                    .count();

            long projetosAtrasados = projetos.stream()
                    .filter(Projeto::estaAtrasado)
                    .count();

            // Calcular métricas de tarefas
            long tarefasPendentes = tarefas.stream()
                    .filter(t -> "Pendente".equals(t.getStatus()))
                    .count();

            long tarefasAtrasadas = tarefas.stream()
                    .filter(Tarefa::estaAtrasada)
                    .count();

            // ✅ CORREÇÃO: Removi a variável duplicada e usei as existentes
            int equipesAtivas = calcularEquipesAtivas(projetos);

            // Atualizar os labels do dashboard
            if (lblProjetosAtivos != null) {
                lblProjetosAtivos.setText(String.valueOf(projetosAtivos));
            }
            if (lblProjetosAtrasados != null) {
                lblProjetosAtrasados.setText(String.valueOf(projetosAtrasados));
            }
            if (lblTarefasPendentes != null) {
                lblTarefasPendentes.setText(String.valueOf(tarefasPendentes));
            }
            if (lblTarefasAtrasadas != null) {
                lblTarefasAtrasadas.setText(String.valueOf(tarefasAtrasadas));
            }
            if (lblEquipesAtivas != null) {
                lblEquipesAtivas.setText(String.valueOf(equipesAtivas));
            }

        } catch (Exception e) {
            System.out.println("❌ Erro ao atualizar dashboard: " + e.getMessage());
        }
    }

    private int calcularTarefasPendentes(List<Projeto> projetos) {
        // Placeholder - implemente sua lógica real aqui
        return projetos.stream()
                .mapToInt(p -> 5) // Exemplo: cada projeto tem 5 tarefas pendentes
                .sum();
    }

    private int calcularEquipesAtivas(List<Projeto> projetos) {
        // Placeholder - implemente sua lógica real aqui
        return (int) projetos.stream()
                .filter(p -> p.getEquipes() != null && !p.getEquipes().isEmpty())
                .count();
    }

    private void configurarLabels() {
        if (lblUsuario != null) {
            lblUsuario.setText("Usuário: Admin"); // Você pode tornar isso dinâmico
        }
        if (lblStatus != null) {
            lblStatus.setText("✅ Conectado como Admin | Sistema: Gestão de Projetos v1.0 | © 2024");
        }
    }

    @FXML
    private void handleSair() {
        System.exit(0);
    }

    @FXML
    private void handleGerenciarProjetos() {
        // Já estamos na aba de projetos, apenas recarrega os dados
        carregarProjetos();
        atualizarDashboard();
        showAlert("Info", "Projetos recarregados com sucesso!");
    }

    @FXML
    private void handleNovoProjeto() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/projeto-form.fxml"));
            Parent root = loader.load();

            ProjetoFormController controller = loader.getController();
            controller.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Novo Projeto");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaProjetos.getScene().getWindow());
            stage.showAndWait();

            // Recarrega os projetos após fechar o formulário
            carregarProjetos();
            atualizarDashboard();

        } catch (Exception e) {
            showAlert("Erro", "Não foi possível abrir o formulário de projeto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditarProjeto() {
        Projeto projetoSelecionado = tabelaProjetos.getSelectionModel().getSelectedItem();

        if (projetoSelecionado == null) {
            showAlert("Aviso", "Selecione um projeto para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/projeto-form.fxml"));
            Parent root = loader.load();

            ProjetoFormController controller = loader.getController();
            controller.setProjetoParaEdicao(projetoSelecionado);
            controller.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Editar Projeto - " + projetoSelecionado.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaProjetos.getScene().getWindow());
            stage.showAndWait();

            // Recarrega os projetos após edição
            carregarProjetos();
            atualizarDashboard();

        } catch (Exception e) {
            showAlert("Erro", "Erro ao abrir formulário de edição: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExcluirProjeto() {
        Projeto projetoSelecionado = tabelaProjetos.getSelectionModel().getSelectedItem();

        if (projetoSelecionado == null) {
            showAlert("Aviso", "Selecione um projeto para excluir.");
            return;
        }

        // Confirmação de exclusão
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir Projeto");
        confirmacao.setContentText("Tem certeza que deseja excluir o projeto: " + projetoSelecionado.getNome() + "?");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                boolean sucesso = projetoDAO.excluir(projetoSelecionado.getId());
                if (sucesso) {
                    showAlert("Sucesso", "Projeto excluído com sucesso!");
                    carregarProjetos(); // Recarrega a lista
                    atualizarDashboard();
                } else {
                    showAlert("Erro", "Não foi possível excluir o projeto.");
                }
            } catch (Exception e) {
                showAlert("Erro", "Erro ao excluir projeto: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVisualizarProjeto() {
        Projeto projetoSelecionado = tabelaProjetos.getSelectionModel().getSelectedItem();

        if (projetoSelecionado == null) {
            showAlert("Aviso", "Selecione um projeto para visualizar.");
            return;
        }

        try {
            System.out.println("📋 Tentando visualizar projeto: " + projetoSelecionado.getNome());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/projeto-form.fxml"));
            Parent root = loader.load();

            ProjetoFormController controller = loader.getController();
            controller.setProjetoParaEdicao(projetoSelecionado);
            controller.setDashboardController(this);
            controller.configurarModoVisualizacao();

            Stage stage = new Stage();
            stage.setTitle("Visualizar Projeto - " + projetoSelecionado.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaProjetos.getScene().getWindow());
            stage.show();

            System.out.println("✅ Visualização do projeto aberta com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro ao visualizar projeto: " + e.getMessage());
            showAlert("Erro", "Erro ao visualizar projeto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRelatorioProjetos() {
        try {
            List<Projeto> projetos = projetoDAO.listarTodos();

            StringBuilder relatorio = new StringBuilder();
            relatorio.append("RELATÓRIO DE PROJETOS\n");
            relatorio.append("=====================\n\n");

            for (Projeto projeto : projetos) {
                relatorio.append("ID: ").append(projeto.getId()).append("\n");
                relatorio.append("Nome: ").append(projeto.getNome()).append("\n");
                relatorio.append("Status: ").append(projeto.getStatusDisplay()).append("\n");

                // CORREÇÃO
                relatorio.append("Responsável: ").append(projeto.getNomeResponsavel()).append("\n");

                relatorio.append("Data Início: ").append(projeto.getDataInicioFormatada()).append("\n");
                relatorio.append("Data Fim: ").append(projeto.getDataFimFormatada()).append("\n");

                // CORREÇÃO
                relatorio.append("Equipes: ").append(projeto.getNomesEquipes()).append("\n");
                relatorio.append("Quantidade de Equipes: ").append(projeto.getQuantidadeEquipes()).append("\n");
                relatorio.append("-----------------------------\n");
            }

            relatorio.append("\nTotal de Projetos: ").append(projetos.size());

            TextArea textArea = new TextArea(relatorio.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            Stage stage = new Stage();
            stage.setTitle("Relatório de Projetos");
            stage.setScene(new Scene(scrollPane, 600, 400));
            stage.show();

        } catch (Exception e) {
            showAlert("Erro", "Erro ao gerar relatório: " + e.getMessage());
        }
    }

    @FXML
    private void handleGerenciarUsuarios() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/usuario-form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gerenciar Usuários");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaProjetos.getScene().getWindow());
            stage.show();

        } catch (Exception e) {
            showAlert("Erro", "Não foi possível abrir o formulário de usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGerenciarEquipes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/equipes-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gerenciamento de Equipes");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaProjetos.getScene().getWindow());
            stage.show();

        } catch (Exception e) {
            showAlert("Erro", "Não foi possível abrir o gerenciamento de equipes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ CORREÇÃO: Mantive apenas uma versão de cada método
    @FXML
    private void handleGerenciarTarefas() {
        // Já estamos na aba de tarefas, apenas recarrega os dados
        carregarTarefas();
        atualizarDashboard();
        showAlert("Info", "Tarefas recarregadas com sucesso!");
    }

    @FXML
    private void handleNovaTarefa() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/tarefa-form.fxml"));
            Parent root = loader.load();

            TarefaFormController controller = loader.getController();
            controller.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Nova Tarefa");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabelaTarefas.getScene().getWindow());
            stage.showAndWait();

            // Recarrega as tarefas após fechar o formulário
            carregarTarefas();
            atualizarDashboard();

        } catch (Exception e) {
            showAlert("Erro", "Não foi possível abrir o formulário de tarefa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void atualizarListaProjetos() {
        carregarProjetos();
        atualizarDashboard();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}