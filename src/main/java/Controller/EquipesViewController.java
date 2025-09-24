package Controller;

import com.gestao.projetos.dao.EquipeDAO;
import com.gestao.projetos.model.entity.Equipe;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class EquipesViewController {

    @FXML private TableView<Equipe> tabelaEquipes;
    @FXML private Label lblTotalEquipes;
    @FXML private Label lblEquipesAtivas;
    @FXML private Label lblTotalMembros;

    private EquipeDAO equipeDAO;
    private ObservableList<Equipe> equipesList;

    @FXML
    private void initialize() {
        equipeDAO = new EquipeDAO();
        configurarTabela();
        carregarEquipes();
        atualizarEstatisticas();

        // Passar referência do controller para a tabela
        tabelaEquipes.getProperties().put("controller", this);
    }

    private void configurarTabela() {
        // Configuração básica da tabela
        tabelaEquipes.setPlaceholder(new Label("Nenhuma equipe cadastrada."));
    }

    private void carregarEquipes() {
        try {
            List<Equipe> equipes = equipeDAO.listarTodos();
            equipesList = FXCollections.observableArrayList(equipes);
            tabelaEquipes.setItems(equipesList);

            System.out.println("✅ " + equipes.size() + " equipes carregadas");

        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar equipes: " + e.getMessage());
            mostrarAlerta("Erro", "Erro ao carregar equipes: " + e.getMessage());
        }
    }

    private void atualizarEstatisticas() {
        int totalEquipes = equipesList.size();
        int equipesAtivas = (int) equipesList.stream().filter(Equipe::isAtiva).count();
        int totalMembros = equipesList.stream().mapToInt(Equipe::getQuantidadeMembros).sum();

        lblTotalEquipes.setText(String.valueOf(totalEquipes));
        lblEquipesAtivas.setText(String.valueOf(equipesAtivas));
        lblTotalMembros.setText(String.valueOf(totalMembros));
    }

    @FXML
    private void handleNovaEquipe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/equipe-form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nova Equipe");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recarregar equipes após fechar o formulário
            carregarEquipes();
            atualizarEstatisticas();

        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao abrir formulário de equipe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void editarEquipe(Equipe equipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/equipe-form.fxml"));
            Parent root = loader.load();

            EquipeFormController controller = loader.getController();
            controller.setEquipeParaEdicao(equipe);

            Stage stage = new Stage();
            stage.setTitle("Editar Equipe - " + equipe.getNome());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recarregar equipes após edição
            carregarEquipes();
            atualizarEstatisticas();

        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao editar equipe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void excluirEquipe(Equipe equipe) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir Equipe");
        confirmacao.setContentText("Tem certeza que deseja excluir a equipe: " + equipe.getNome() + "?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    boolean sucesso = equipeDAO.excluir(equipe.getId());
                    if (sucesso) {
                        mostrarAlerta("Sucesso", "Equipe excluída com sucesso!");
                        carregarEquipes();
                        atualizarEstatisticas();
                    } else {
                        mostrarAlerta("Erro", "Erro ao excluir equipe.");
                    }
                } catch (Exception e) {
                    mostrarAlerta("Erro", "Erro ao excluir equipe: " + e.getMessage());
                }
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}