package Controller;

import com.gestao.projetos.dao.ProjetoDAO;
import com.gestao.projetos.model.entity.Projeto;
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

    private ProjetoDAO projetoDAO;
    private ObservableList<Projeto> projetosList;

    @FXML
    private void initialize() {
        projetoDAO = new ProjetoDAO();
        configurarTabela();
        carregarProjetos();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        colDataInicio.setCellValueFactory(new PropertyValueFactory<>("dataInicioFormatada"));
        colDataFim.setCellValueFactory(new PropertyValueFactory<>("dataFimFormatada"));

        // Adicione esta linha para formatação personalizada
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
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("");
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

            System.out.println("✅ " + projetos.size() + " projetos carregados na tabela");

        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar projetos: " + e.getMessage());
            showAlert("Erro", "Erro ao carregar projetos: " + e.getMessage());
            e.printStackTrace();
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
            stage.showAndWait(); // Aguarda o fechamento do formulário

            // Recarrega os projetos após fechar o formulário
            carregarProjetos();

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
            stage.showAndWait();

            // Recarrega os projetos após edição
            carregarProjetos();

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

            // ✅ Use o mesmo arquivo FXML do formulário, mas no modo visualização
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/projeto-form.fxml"));
            Parent root = loader.load();

            ProjetoFormController controller = loader.getController();
            controller.setProjetoParaEdicao(projetoSelecionado);
            controller.setDashboardController(this);

            // ✅ Configure o modo de visualização (somente leitura)
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
                relatorio.append("Status: ").append(projeto.getStatus()).append("\n");
                relatorio.append("Data Início: ").append(projeto.getDataInicio()).append("\n");
                relatorio.append("Data Fim: ").append(projeto.getDataFim()).append("\n");
                relatorio.append("-----------------------------\n");
            }

            relatorio.append("\nTotal de Projetos: ").append(projetos.size());

            // Mostrar relatório em uma nova janela
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

    public void atualizarListaProjetos() {
        carregarProjetos();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
