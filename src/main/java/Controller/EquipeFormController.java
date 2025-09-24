package Controller;

import com.gestao.projetos.model.entity.Equipe;
import com.gestao.projetos.model.entity.Usuario;
import com.gestao.projetos.model.entity.Projeto;
import com.gestao.projetos.dao.EquipeDAO;
import com.gestao.projetos.dao.UsuarioDAO;
import com.gestao.projetos.dao.ProjetoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class EquipeFormController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private CheckBox chkAtiva;
    @FXML private ListView<Usuario> listViewMembros;
    @FXML private ListView<Projeto> listViewProjetos;
    @FXML private Label lblTotalMembros;
    @FXML private Label lblTotalProjetos;
    @FXML private Button btnCancelar;
    @FXML private Button btnSalvar;

    private Equipe equipeParaEdicao;
    private ObservableList<Usuario> membrosList;
    private ObservableList<Projeto> projetosList;
    private EquipeDAO equipeDAO;
    private UsuarioDAO usuarioDAO;
    private ProjetoDAO projetoDAO;

    @FXML
    private void initialize() {
        System.out.println("✅ EquipeFormController inicializando...");

        // Inicializar DAOs
        equipeDAO = new EquipeDAO();
        usuarioDAO = new UsuarioDAO();
        projetoDAO = new ProjetoDAO();

        // Inicializar as listas
        membrosList = FXCollections.observableArrayList();
        projetosList = FXCollections.observableArrayList();

        // Configurar ListViews
        if (listViewMembros != null) {
            listViewMembros.setItems(membrosList);
        }
        if (listViewProjetos != null) {
            listViewProjetos.setItems(projetosList);
        }

        // Configurar cell factories programaticamente
        configurarCellFactories();

        // Atualizar totais inicialmente
        atualizarTotais();

        System.out.println("✅ EquipeFormController inicializado com sucesso");
    }

    private void configurarCellFactories() {
        // Cell factory para membros
        if (listViewMembros != null) {
            listViewMembros.setCellFactory(new Callback<ListView<Usuario>, ListCell<Usuario>>() {
                @Override
                public ListCell<Usuario> call(ListView<Usuario> param) {
                    return new ListCell<Usuario>() {
                        @Override
                        protected void updateItem(Usuario usuario, boolean empty) {
                            super.updateItem(usuario, empty);
                            if (empty || usuario == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                String perfilDescricao = obterDescricaoPerfil(usuario.getPerfil());
                                String icone = obterIconePerfil(usuario.getPerfil());
                                setText(icone + " " + usuario.getNome() + " (" + perfilDescricao + ")");

                                // Botão para remover membro
                                Button btnRemover = new Button("✕");
                                btnRemover.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                                btnRemover.setOnAction(event -> removerMembro(usuario));

                                setGraphic(btnRemover);
                            }
                        }
                    };
                }
            });
        }

        // Cell factory para projetos
        if (listViewProjetos != null) {
            listViewProjetos.setCellFactory(new Callback<ListView<Projeto>, ListCell<Projeto>>() {
                @Override
                public ListCell<Projeto> call(ListView<Projeto> param) {
                    return new ListCell<Projeto>() {
                        @Override
                        protected void updateItem(Projeto projeto, boolean empty) {
                            super.updateItem(projeto, empty);
                            if (empty || projeto == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                setText(projeto.getNome() + " - " + (projeto.getStatus() != null ? projeto.getStatus() : "Sem status"));

                                // Botão para remover projeto
                                Button btnRemover = new Button("✕");
                                btnRemover.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                                btnRemover.setOnAction(event -> removerProjeto(projeto));

                                setGraphic(btnRemover);
                            }
                        }
                    };
                }
            });
        }
    }

    private void atualizarTotais() {
        // 🔥 CORREÇÃO: Verificar se os labels não são null antes de usar
        if (lblTotalMembros != null) {
            lblTotalMembros.setText("Total de membros: " + membrosList.size());
        }
        if (lblTotalProjetos != null) {
            lblTotalProjetos.setText("Total de projetos: " + projetosList.size());
        }
    }

    public void setEquipeParaEdicao(Equipe equipe) {
        this.equipeParaEdicao = equipe;
        if (equipe != null) {
            if (lblTitulo != null) {
                lblTitulo.setText("Editar Equipe - " + equipe.getNome());
            }
            if (txtNome != null) {
                txtNome.setText(equipe.getNome());
            }
            if (txtDescricao != null) {
                txtDescricao.setText(equipe.getDescricao() != null ? equipe.getDescricao() : "");
            }
            if (chkAtiva != null) {
                chkAtiva.setSelected(equipe.isAtiva());
            }

            // Carregar membros e projetos existentes
            carregarMembrosExistentes(equipe.getId());
            carregarProjetosExistentes(equipe.getId());

        } else {
            if (lblTitulo != null) {
                lblTitulo.setText("Nova Equipe");
            }
            // Limpar as listas para nova equipe
            membrosList.clear();
            projetosList.clear();
        }
        atualizarTotais();
    }

    private void carregarMembrosExistentes(int equipeId) {
        try {
            // Buscar a equipe completa com membros do banco
            Equipe equipeCompleta = equipeDAO.buscarPorId(equipeId);
            if (equipeCompleta != null && equipeCompleta.getMembros() != null) {
                membrosList.setAll(equipeCompleta.getMembros());
                System.out.println("✅ " + equipeCompleta.getMembros().size() + " membros carregados");
            } else {
                membrosList.clear();
                System.out.println("ℹ️ Nenhum membro encontrado para a equipe");
            }
        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar membros: " + e.getMessage());
            membrosList.clear();
        }
    }

    private void carregarProjetosExistentes(int equipeId) {
        try {
            // Buscar a equipe completa com projetos do banco
            Equipe equipeCompleta = equipeDAO.buscarPorId(equipeId);
            if (equipeCompleta != null && equipeCompleta.getProjetos() != null) {
                projetosList.setAll(equipeCompleta.getProjetos());
                System.out.println("✅ " + equipeCompleta.getProjetos().size() + " projetos carregados");
            } else {
                projetosList.clear();
                System.out.println("ℹ️ Nenhum projeto encontrado para a equipe");
            }
        } catch (Exception e) {
            System.out.println("❌ Erro ao carregar projetos: " + e.getMessage());
            projetosList.clear();
        }
    }

    @FXML
    private void handleAdicionarMembros() {
        try {
            // Carregar todos os usuários disponíveis
            List<Usuario> todosUsuarios = usuarioDAO.listarTodos();
            ObservableList<Usuario> usuariosDisponiveis = FXCollections.observableArrayList(todosUsuarios);

            // Remover usuários já adicionados
            usuariosDisponiveis.removeAll(membrosList);

            if (usuariosDisponiveis.isEmpty()) {
                mostrarAlerta("Informação", "Todos os usuários já estão nesta equipe.");
                return;
            }

            // Criar diálogo de seleção
            Dialog<Usuario> dialog = criarDialogoSelecao("Selecionar Membro", "Escolha um usuário para adicionar à equipe:", usuariosDisponiveis);

            dialog.showAndWait().ifPresent(usuarioSelecionado -> {
                if (usuarioSelecionado != null && !membrosList.contains(usuarioSelecionado)) {
                    membrosList.add(usuarioSelecionado);
                    atualizarTotais();
                }
            });

        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao carregar usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdicionarProjetos() {
        try {
            // Carregar todos os projetos disponíveis
            List<Projeto> todosProjetos = projetoDAO.listarTodos();
            ObservableList<Projeto> projetosDisponiveis = FXCollections.observableArrayList(todosProjetos);

            // Remover projetos já adicionados
            projetosDisponiveis.removeAll(projetosList);

            if (projetosDisponiveis.isEmpty()) {
                mostrarAlerta("Informação", "Todos os projetos já estão associados a esta equipe.");
                return;
            }

            // Criar diálogo de seleção
            Dialog<Projeto> dialog = criarDialogoSelecao("Selecionar Projeto", "Escolha um projeto para associar à equipe:", projetosDisponiveis);

            dialog.showAndWait().ifPresent(projetoSelecionado -> {
                if (projetoSelecionado != null && !projetosList.contains(projetoSelecionado)) {
                    projetosList.add(projetoSelecionado);
                    atualizarTotais();
                }
            });

        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao carregar projetos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> Dialog<T> criarDialogoSelecao(String titulo, String cabecalho, ObservableList<T> itens) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(cabecalho);

        // Botões
        ButtonType confirmarButtonType = new ButtonType("Selecionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmarButtonType, ButtonType.CANCEL);

        // ListView para seleção
        ListView<T> listView = new ListView<>(itens);
        listView.setPrefSize(400, 300); // Aumentei um pouco o tamanho

        // 🔥 Configurar cell factory personalizada para usuários (COM ÍCONES)
        if (itens.size() > 0 && itens.get(0) instanceof Usuario) {
            listView.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
                @Override
                public ListCell<T> call(ListView<T> param) {
                    return new ListCell<T>() {
                        @Override
                        protected void updateItem(T item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                Usuario usuario = (Usuario) item;
                                String perfilDescricao = obterDescricaoPerfil(usuario.getPerfil());
                                String icone = obterIconePerfil(usuario.getPerfil());
                                setText(icone + " " + usuario.getNome() + " - " + perfilDescricao);
                            }
                        }
                    };
                }
            });
        } else if (itens.size() > 0 && itens.get(0) instanceof Projeto) {
            // Manter a cell factory original para projetos
            listView.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
                @Override
                public ListCell<T> call(ListView<T> param) {
                    return new ListCell<T>() {
                        @Override
                        protected void updateItem(T item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                Projeto projeto = (Projeto) item;
                                setText(projeto.getNome() + " - " + projeto.getStatus());
                            }
                        }
                    };
                }
            });
        }

        dialog.getDialogPane().setContent(listView);

        // Converter resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmarButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog;
    }

    // 🔥 MÉTODO CORRIGIDO: Converter enum Perfil para descrição amigável
    private String obterDescricaoPerfil(Usuario.Perfil perfil) {
        if (perfil == null) return "Sem perfil";

        switch (perfil) {
            case ADMINISTRADOR:
                return "Administrador";
            case GERENTE:
                return "Gerente";
            case COLABORADOR:
                return "Colaborador";
            default:
                return perfil.name();
        }
    }

    // 🔥 MÉTODO PARA OBTER ÍCONES
    private String obterIconePerfil(Usuario.Perfil perfil) {
        if (perfil == null) return "👤";

        switch (perfil) {
            case ADMINISTRADOR:
                return "👑";
            case GERENTE:
                return "💼";
            case COLABORADOR:
                return "👨‍💼";
            default:
                return "👤";
        }
    }

    private void removerMembro(Usuario usuario) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Remoção");
        confirmacao.setHeaderText("Remover Membro");
        confirmacao.setContentText("Tem certeza que deseja remover " + usuario.getNome() + " da equipe?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                membrosList.remove(usuario);
                atualizarTotais();
            }
        });
    }

    private void removerProjeto(Projeto projeto) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Remoção");
        confirmacao.setHeaderText("Remover Projeto");
        confirmacao.setContentText("Tem certeza que deseja remover o projeto " + projeto.getNome() + " da equipe?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                projetosList.remove(projeto);
                atualizarTotais();
            }
        });
    }

    @FXML
    private void handleSalvar() {
        if (validarFormulario()) {
            try {
                boolean isNovaEquipe = (equipeParaEdicao == null);

                if (isNovaEquipe) {
                    equipeParaEdicao = new Equipe();
                }

                // Definir dados básicos da equipe
                equipeParaEdicao.setNome(txtNome.getText().trim());
                equipeParaEdicao.setDescricao(txtDescricao.getText().trim());
                equipeParaEdicao.setAtiva(chkAtiva.isSelected());

                // Adicionar membros e projetos à equipe
                equipeParaEdicao.getMembros().clear();
                equipeParaEdicao.getMembros().addAll(membrosList);

                equipeParaEdicao.getProjetos().clear();
                equipeParaEdicao.getProjetos().addAll(projetosList);

                // Salvar/atualizar no banco
                boolean sucesso;
                if (isNovaEquipe) {
                    sucesso = equipeDAO.salvar(equipeParaEdicao);
                } else {
                    sucesso = equipeDAO.atualizar(equipeParaEdicao);
                }

                if (sucesso) {
                    String mensagem = isNovaEquipe ? "Equipe criada com sucesso!" : "Equipe atualizada com sucesso!";
                    mostrarAlerta("Sucesso", mensagem);
                    fecharJanela();
                } else {
                    mostrarAlerta("Erro", "Erro ao salvar equipe no banco de dados.");
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
        if (txtNome.getText() == null || txtNome.getText().trim().isEmpty()) {
            mostrarAlerta("Validação", "Por favor, informe o nome da equipe.");
            txtNome.requestFocus();
            return false;
        }
        return true;
    }

    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}