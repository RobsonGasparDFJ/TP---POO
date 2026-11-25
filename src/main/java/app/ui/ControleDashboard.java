package app.ui;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import app.Conta;
import app.ContaCorrente;
import app.ContaDAO;
import app.ContaPoupanca;
import app.DatabaseSetup;
import app.MovimentacaoService;
import app.TipoConta;
import app.TipoTransacao;
import app.Transacao;
import app.TransacaoDAO;
import app.TransferenciaService;
import app.UsuarioDAO;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
<<<<<<< HEAD
import javafx.event.ActionEvent;
=======
>>>>>>> Gustavo
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/** Controla os dados reais exibidos em dashboard.fxml. */
public class ControleDashboard {

    private static final Locale BRASIL = new Locale("pt", "BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(BRASIL);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final ContaDAO contaDAO = new ContaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private final TransferenciaService transferenciaService = new TransferenciaService();
    private final MovimentacaoService movimentacaoService = new MovimentacaoService();
    private boolean transferenciaPorPix = true;
    private List<Transacao> transacoes = List.of();

    @FXML private Label labelSaudacao;
    @FXML private Label labelSaldo;
    @FXML private Label labelConta;
    @FXML private Label labelChavePix;
    @FXML private Label labelResumoTransacoes;
    @FXML private Label labelResumoExtrato;
    @FXML private Label labelEntradas;
    @FXML private Label labelSaidas;
    @FXML private ComboBox<Conta> comboContas;
    @FXML private TabPane abasConteudo;
    @FXML private TextField campoNomePerfil;
    @FXML private TextField campoTelefonePerfil;
    @FXML private TextField campoEnderecoPerfil;
    @FXML private Label labelCpfPerfil;
    @FXML private Label labelNomePerfil;
    @FXML private ImageView fotoPerfil;
    @FXML private ImageView fotoMiniatura;
    @FXML private Button botaoRemoverFoto;
    @FXML private Label labelInfoNovaConta;
    @FXML private ComboBox<TipoConta> comboTipoNovaConta;
    @FXML private TextField campoChaveNovaConta;
    @FXML private Button botaoCriarConta;
    @FXML private Button botaoDeletarConta;
    @FXML private TextField campoDestino;
    @FXML private TextField campoValor;
    @FXML private TextField campoDeposito;
    @FXML private Button botaoPix;
    @FXML private Button botaoConta;
    @FXML private Button botaoTransferir;
    @FXML private Button botaoMinimizar;
    @FXML private TableView<Transacao> tabelaTransacoes;
    @FXML private TableColumn<Transacao, String> colunaTipo;
    @FXML private TableColumn<Transacao, String> colunaDescricao;
    @FXML private TableColumn<Transacao, String> colunaData;
    @FXML private TableColumn<Transacao, String> colunaValor;
    @FXML private AreaChart<String, Number> graficoSaldo;
    @FXML private BarChart<String, Number> graficoFluxo;
    @FXML private ControleCassino cassinoController; // injetado pelo <fx:include fx:id="cassino"> do dashboard.fxml
<<<<<<< HEAD
    @FXML private Button botaoMaximizar;

=======
>>>>>>> Gustavo

    @FXML
    public void initialize() {
        configurarContas();
<<<<<<< HEAD
        JanelaUtil.sincronizarIcone(botaoMaximizar);
=======
>>>>>>> Gustavo
        configurarTabela();
        fotoMiniatura.setClip(new Circle(17.5, 17.5, 17.5));
        if (Sessao.getUsuarioLogado() == null) {
            voltarParaLogin();
            return;
        }
        labelSaudacao.setText("Olá, " + primeiroNome(Sessao.getUsuarioLogado().getNome()));
        if (cassinoController != null) {
            cassinoController.configurar(comboContas::getValue, this::atualizarDados);
        }
        carregarPerfil();
        atualizarDados();
    }

    private void configurarContas() {
        comboContas.setConverter(new StringConverter<>() {
            @Override public String toString(Conta conta) {
                if (conta == null) return "Selecione uma conta";
                String tipo = conta.getTipoConta() == TipoConta.CORRENTE ? "Corrente" : "Poupança";
                return "Conta " + conta.getIdConta() + " · " + tipo;
            }
            @Override public Conta fromString(String texto) { return null; }
        });

        comboTipoNovaConta.setConverter(new StringConverter<>() {
            @Override public String toString(TipoConta tipo) {
                if (tipo == null) return "Tipo de conta";
                return tipo == TipoConta.CORRENTE ? "Conta Corrente" : "Conta Poupança";
            }
            @Override public TipoConta fromString(String texto) { return null; }
        });
    }

    private void configurarTabela() {
        tabelaTransacoes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colunaTipo.setCellValueFactory(dado -> new ReadOnlyStringWrapper(nomeTipo(dado.getValue())));
        colunaDescricao.setCellValueFactory(dado -> new ReadOnlyStringWrapper(descricao(dado.getValue())));
        colunaData.setCellValueFactory(dado -> new ReadOnlyStringWrapper(DATA.format(dado.getValue().getDataHora())));
        colunaValor.setCellValueFactory(dado -> new ReadOnlyStringWrapper(valorFormatado(dado.getValue())));
        tabelaTransacoes.setPlaceholder(new Label("Ainda não há movimentações nesta conta."));
    }

    @FXML
    private void atualizarDados() {
        if (Sessao.getUsuarioLogado() == null) return;
        Long selecionada = comboContas.getValue() == null ? null : comboContas.getValue().getIdConta();
        try (Connection conexao = DatabaseSetup.getConnection()) {
            List<Conta> contas = contaDAO.buscarPorUsuario(conexao, Sessao.getUsuarioLogado().getIdUsuario());
            comboContas.setItems(FXCollections.observableArrayList(contas));
            Conta conta = contas.stream().filter(c -> c.getIdConta().equals(selecionada)).findFirst()
                    .orElse(contas.isEmpty() ? null : contas.get(0));
            comboContas.setValue(conta);
            carregarConta(conexao, conta);
            atualizarOpcoesNovaConta(contas);
        } catch (SQLException | RuntimeException e) {
            mostrar(Alert.AlertType.ERROR, "Não foi possível carregar o dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void trocarConta() {
        Conta conta = comboContas.getValue();
        if (conta == null) return;
        try (Connection conexao = DatabaseSetup.getConnection()) {
            carregarConta(conexao, conta);
        } catch (SQLException e) {
            mostrar(Alert.AlertType.ERROR, "Não foi possível carregar a conta: " + e.getMessage());
        }
    }

    private void carregarConta(Connection conexao, Conta conta) throws SQLException {
        if (conta == null) {
            labelSaldo.setText(MOEDA.format(0));
            labelConta.setText("Você ainda não possui conta cadastrada");
            labelChavePix.setText("");
            transacoes = List.of();
        } else {
            labelSaldo.setText(MOEDA.format(conta.getSaldo()));
            String tipo = conta.getTipoConta() == TipoConta.CORRENTE ? "CONTA CORRENTE" : "CONTA POUPANÇA";
            labelConta.setText(tipo + "  •  Nº " + conta.getIdConta());
            labelChavePix.setText("PIX: " + conta.getChave());
            transacoes = transacaoDAO.listarPorConta(conexao, conta.getIdConta());
        }
        if (botaoDeletarConta != null) {
            botaoDeletarConta.setDisable(conta == null);
        }
        tabelaTransacoes.setItems(FXCollections.observableArrayList(transacoes));
        labelResumoTransacoes.setText(transacoes.isEmpty() ? "Nenhuma movimentação recente" : transacoes.size() + " movimentação(ões) encontrada(s)");
        labelResumoExtrato.setText(transacoes.isEmpty() ? "Nenhuma transação para esta conta" : transacoes.size() + " transação(ões) na conta selecionada");
        preencherGraficos(conta);
        if (cassinoController != null) {
            cassinoController.atualizar();
        }
    }

    private void preencherGraficos(Conta conta) {
        graficoSaldo.getData().clear();
        graficoFluxo.getData().clear();
        double entradas = 0;
        double saidas = 0;
        if (conta != null) {
            for (Transacao transacao : transacoes) {
                if (ehEntrada(transacao, conta)) entradas += transacao.getValor();
                else saidas += transacao.getValor();
            }
            XYChart.Series<String, Number> saldo = new XYChart.Series<>();
            double saldoAtual = conta.getSaldo();
            List<Transacao> recentes = new ArrayList<>(transacoes.subList(0, Math.min(6, transacoes.size())));
            // Reconstrói o saldo de cada movimentação usando o saldo atual da conta.
            List<Transacao> todasReversas = new ArrayList<>(transacoes);
            java.util.Map<Long, Double> saldoApos = new java.util.HashMap<>();
            for (Transacao transacao : todasReversas) {
                saldoApos.put(transacao.getId(), saldoAtual);
                saldoAtual += ehEntrada(transacao, conta) ? -transacao.getValor() : transacao.getValor();
            }
            Collections.reverse(recentes);
            for (Transacao transacao : recentes) {
                saldo.getData().add(new XYChart.Data<>(DATA.format(transacao.getDataHora()), saldoApos.get(transacao.getId())));
            }
            if (recentes.isEmpty()) saldo.getData().add(new XYChart.Data<>("Hoje", conta.getSaldo()));
            graficoSaldo.getData().add(saldo);
        }
        XYChart.Series<String, Number> fluxo = new XYChart.Series<>();
        fluxo.getData().add(new XYChart.Data<>("Entradas", entradas));
        fluxo.getData().add(new XYChart.Data<>("Saídas", saidas));
        graficoFluxo.getData().add(fluxo);
        labelEntradas.setText("Entradas: " + MOEDA.format(entradas));
        labelSaidas.setText("Saídas: " + MOEDA.format(saidas));
    }

    @FXML private void selecionarPix() { transferenciaPorPix = true; atualizarModoTransferencia(); }
    @FXML private void selecionarConta() { transferenciaPorPix = false; atualizarModoTransferencia(); }

    private void atualizarModoTransferencia() {
        botaoPix.getStyleClass().setAll("transfer-type", transferenciaPorPix ? "transfer-type-active" : "");
        botaoConta.getStyleClass().setAll("transfer-type", transferenciaPorPix ? "" : "transfer-type-active");
        campoDestino.setPromptText(transferenciaPorPix ? "Chave Pix do destinatário" : "Número da conta de destino");
        campoDestino.clear();
    }

    @FXML
    private void transferir() {
        Conta origem = comboContas.getValue();
        try {
            if (origem == null) throw new IllegalArgumentException("Selecione uma conta de origem.");
            String destino = campoDestino.getText() == null ? "" : campoDestino.getText().trim();
            double valor = converterValor(campoValor.getText());
            if (transferenciaPorPix) {
                transferenciaService.transferirPorPix(origem.getIdConta(), destino, valor);
            } else {
                transferenciaService.transferirPorConta(origem.getIdConta(), Long.parseLong(destino), valor);
            }
            campoDestino.clear();
            campoValor.clear();
            atualizarDados();
            mostrar(Alert.AlertType.INFORMATION, "Transferência realizada com sucesso.");
        } catch (NumberFormatException e) {
            mostrar(Alert.AlertType.WARNING, transferenciaPorPix ? "Informe um valor válido." : "Informe o número da conta e um valor válidos.");
        } catch (RuntimeException e) {
            mostrar(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    private double converterValor(String texto) {
        String valor = texto == null ? "" : texto.trim().replace("R$", "").replace(" ", "");
        if (valor.contains(",")) valor = valor.replace(".", "").replace(",", ".");
        return Double.parseDouble(valor);
    }

    @FXML
    private void adicionarSaldo() {
        try {
            Conta conta = comboContas.getValue();
            double valor = converterValor(campoDeposito.getText());
            movimentacaoService.depositar(conta == null ? null : conta.getIdConta(), valor);
            campoDeposito.clear();
            atualizarDados();
            mostrar(Alert.AlertType.INFORMATION, "Saldo adicionado com sucesso.");
        } catch (NumberFormatException e) {
            mostrar(Alert.AlertType.WARNING, "Informe um valor válido para adicionar saldo.");
        } catch (RuntimeException e) {
            mostrar(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    @FXML private void copiarChavePix() {
        Conta conta = comboContas.getValue();
        if (conta == null) return;
        ClipboardContent conteudo = new ClipboardContent();
        conteudo.putString(conta.getChave());
        Clipboard.getSystemClipboard().setContent(conteudo);
        mostrar(Alert.AlertType.INFORMATION, "Chave Pix copiada.");
    }
    @FXML private void abrirTransferencia() { abasConteudo.getSelectionModel().select(1); campoDestino.requestFocus(); }
    @FXML private void mostrarInicio() { abasConteudo.getSelectionModel().select(0); }
    @FXML private void mostrarExtrato() { abasConteudo.getSelectionModel().select(2); tabelaTransacoes.requestFocus(); tabelaTransacoes.scrollTo(0); }
    @FXML private void mostrarPerfil() { carregarPerfil(); abasConteudo.getSelectionModel().select(3); }
    @FXML private void mostrarCassino() { abasConteudo.getSelectionModel().select(4); }
    @FXML private void minimizar() { ((Stage) botaoMinimizar.getScene().getWindow()).setIconified(true); }
    @FXML private void fechar() { Platform.exit(); }

    @FXML
    private void sair() {
        Sessao.sair();
        voltarParaLogin();
    }

    private void carregarPerfil() {
        if (Sessao.getUsuarioLogado() == null) return;
        var usuario = Sessao.getUsuarioLogado();
        campoNomePerfil.setText(usuario.getNome());
        campoTelefonePerfil.setText(usuario.getTelefone() == null ? "" : usuario.getTelefone());
        campoEnderecoPerfil.setText(usuario.getEndereco() == null ? "" : usuario.getEndereco());
        labelCpfPerfil.setText("CPF: " + usuario.getCpf());
        labelNomePerfil.setText(usuario.getNome());
        carregarFoto(usuario.getFotoPerfil());
    }

    @FXML
    private void escolherFoto() {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Escolher foto de perfil");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File arquivo = seletor.showOpenDialog(fotoPerfil.getScene().getWindow());
        if (arquivo == null) return;
        Sessao.getUsuarioLogado().setFotoPerfil(arquivo.toURI().toString());
        carregarFoto(Sessao.getUsuarioLogado().getFotoPerfil());
    }

    @FXML
    private void removerFoto() {
        var usuario = Sessao.getUsuarioLogado();
        if (usuario == null) return;
        try {
            usuario.setFotoPerfil(null);
            usuarioDAO.atualizarPerfil(usuario);
            carregarFoto(null);
            mostrar(Alert.AlertType.INFORMATION, "Foto de perfil removida.");
        } catch (RuntimeException e) {
            mostrar(Alert.AlertType.WARNING, "Não foi possível remover a foto: " + e.getMessage());
        }
    }

    @FXML
    private void salvarPerfil() {
        try {
            var usuario = Sessao.getUsuarioLogado();
            usuario.setNome(campoNomePerfil.getText() == null ? "" : campoNomePerfil.getText().trim());
            usuario.setTelefone(campoTelefonePerfil.getText() == null ? "" : campoTelefonePerfil.getText().trim());
            usuario.setEndereco(campoEnderecoPerfil.getText() == null ? "" : campoEnderecoPerfil.getText().trim());
            usuarioDAO.atualizarPerfil(usuario);
            labelSaudacao.setText("Olá, " + primeiroNome(usuario.getNome()));
            labelNomePerfil.setText(usuario.getNome());
            mostrar(Alert.AlertType.INFORMATION, "Perfil salvo com sucesso.");
        } catch (RuntimeException e) {
            mostrar(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    private void atualizarOpcoesNovaConta(List<Conta> contasExistentes) {
        if (comboTipoNovaConta == null) return;

        boolean temCorrente = contasExistentes.stream().anyMatch(c -> c.getTipoConta() == TipoConta.CORRENTE);
        boolean temPoupanca = contasExistentes.stream().anyMatch(c -> c.getTipoConta() == TipoConta.POUPANCA);

        List<TipoConta> disponiveis = new ArrayList<>();
        if (!temCorrente) disponiveis.add(TipoConta.CORRENTE);
        if (!temPoupanca) disponiveis.add(TipoConta.POUPANCA);

        comboTipoNovaConta.setItems(FXCollections.observableArrayList(disponiveis));

        if (disponiveis.isEmpty()) {
            comboTipoNovaConta.setDisable(true);
            campoChaveNovaConta.setDisable(true);
            botaoCriarConta.setDisable(true);
            labelInfoNovaConta.setText("Você já possui Conta Corrente e Conta Poupança ativas.");
        } else {
            comboTipoNovaConta.setDisable(false);
            campoChaveNovaConta.setDisable(false);
            botaoCriarConta.setDisable(false);
            comboTipoNovaConta.setValue(disponiveis.get(0));
            labelInfoNovaConta.setText("Você pode abrir uma "
                    + (disponiveis.get(0) == TipoConta.CORRENTE ? "Conta Corrente" : "Conta Poupança")
                    + " adicional vinculada ao seu perfil.");
        }
    }

    @FXML
    private void criarNovaConta() {
        var usuario = Sessao.getUsuarioLogado();
        if (usuario == null) return;

        TipoConta tipoSelecionado = comboTipoNovaConta.getValue();
        String chavePix = campoChaveNovaConta.getText() == null ? "" : campoChaveNovaConta.getText().trim();

        try {
            if (tipoSelecionado == null) {
                throw new IllegalArgumentException("Selecione o tipo da nova conta.");
            }
            if (chavePix.isBlank()) {
                throw new IllegalArgumentException("Informe uma chave Pix para a nova conta.");
            }

            try (Connection conexao = DatabaseSetup.getConnection()) {
                List<Conta> contasAtuais = contaDAO.buscarPorUsuario(conexao, usuario.getIdUsuario());
                boolean jaPossuiTipo = contasAtuais.stream().anyMatch(c -> c.getTipoConta() == tipoSelecionado);
                if (jaPossuiTipo) {
                    throw new IllegalArgumentException("Você já possui uma conta desse tipo.");
                }

                if (contaDAO.buscarPorChave(conexao, chavePix) != null) {
                    throw new IllegalArgumentException("Essa chave Pix já está cadastrada em outra conta.");
                }

                Conta novaConta = (tipoSelecionado == TipoConta.CORRENTE)
                        ? new ContaCorrente(usuario, chavePix)
                        : new ContaPoupanca(usuario, chavePix);

                contaDAO.inserir(conexao, novaConta);

                campoChaveNovaConta.clear();
                List<Conta> contasAtualizadas = contaDAO.buscarPorUsuario(conexao, usuario.getIdUsuario());
                comboContas.setItems(FXCollections.observableArrayList(contasAtualizadas));
                comboContas.setValue(novaConta);
                carregarConta(conexao, novaConta);
                atualizarOpcoesNovaConta(contasAtualizadas);
            }

            mostrar(Alert.AlertType.INFORMATION, "Nova conta criada e selecionada com sucesso!");
        } catch (IllegalArgumentException e) {
            mostrar(Alert.AlertType.WARNING, e.getMessage());
        } catch (SQLException e) {
            mostrar(Alert.AlertType.ERROR, "Erro ao criar nova conta no banco: " + e.getMessage());
        }
    }

    @FXML
    private void deletarContaAtual() {
        Conta contaAtual = comboContas.getValue();
        if (contaAtual == null) {
            mostrar(Alert.AlertType.WARNING, "Nenhuma conta selecionada para encerrar.");
            return;
        }

        if (Math.abs(contaAtual.getSaldo()) > 0.009) {
            mostrar(Alert.AlertType.WARNING,
                    "Não é possível encerrar uma conta com saldo diferente de R$ 0,00 (saldo atual: "
                            + MOEDA.format(contaAtual.getSaldo()) + "). Transfira o valor antes.");
            return;
        }

        String tipoNome = contaAtual.getTipoConta() == TipoConta.CORRENTE ? "Conta Corrente" : "Conta Poupança";
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Tem certeza que deseja encerrar a " + tipoNome + " Nº " + contaAtual.getIdConta()
                        + "?\nEssa ação não poderá ser desfeita.",
                ButtonType.YES, ButtonType.NO);
        confirmacao.setHeaderText("Encerrar conta");
        if (labelSaudacao != null && labelSaudacao.getScene() != null) {
            confirmacao.initOwner(labelSaudacao.getScene().getWindow());
        }

        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.YES) return;

        try (Connection conexao = DatabaseSetup.getConnection()) {
            contaDAO.deletar(conexao, contaAtual);
            comboContas.setValue(null);
            atualizarDados();
            mostrar(Alert.AlertType.INFORMATION, tipoNome + " encerrada com sucesso.");
        } catch (SQLException e) {
            mostrar(Alert.AlertType.ERROR,
                    "Não foi possível encerrar a conta (verifique se há transações vinculadas): " + e.getMessage());
        } catch (RuntimeException e) {
            mostrar(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    private void carregarFoto(String caminho) {
        boolean temFoto = caminho != null && !caminho.isBlank();
        if (botaoRemoverFoto != null) {
            botaoRemoverFoto.setDisable(!temFoto);
        }
        if (!temFoto) {
            fotoPerfil.setImage(null);
            fotoMiniatura.setImage(null);
            return;
        }
        try {
            Image imagem = new Image(caminho, 112, 112, true, true, true);
            Image foto = imagem.isError() ? null : imagem;
            fotoPerfil.setImage(foto);
            fotoMiniatura.setImage(foto);
        } catch (RuntimeException e) {
            fotoPerfil.setImage(null);
            fotoMiniatura.setImage(null);
        }
    }

    private void voltarParaLogin() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/app/ui/login.fxml"));
            if (labelSaudacao != null && labelSaudacao.getScene() != null) labelSaudacao.getScene().setRoot(login);
        } catch (IOException e) {
            mostrar(Alert.AlertType.ERROR, "Não foi possível abrir o login: " + e.getMessage());
        }
    }

    private boolean ehEntrada(Transacao transacao, Conta conta) {
        return transacao.getDestinatario() != null && transacao.getDestinatario().getIdConta().equals(conta.getIdConta());
    }
    private String nomeTipo(Transacao t) { return ehEntrada(t, comboContas.getValue()) ? "ENTRADA" : "SAÍDA"; }
    private String descricao(Transacao t) {
        if (t.getTipo() == TipoTransacao.APOSTA || t.getTipo() == TipoTransacao.PREMIO) {
            return t.getTipo().name() + " · Cassino";
        }
        Conta outra = ehEntrada(t, comboContas.getValue()) ? t.getRemetente() : t.getDestinatario();
        String nome = outra == null ? "Movimentação" : outra.getUsuario().getNome();
        return t.getTipo().name().replace('_', ' ') + " · " + nome;
    }
    private String valorFormatado(Transacao t) { return (ehEntrada(t, comboContas.getValue()) ? "+ " : "− ") + MOEDA.format(t.getValor()); }
    private String primeiroNome(String nome) { return nome == null || nome.isBlank() ? "cliente" : nome.trim().split("\\s+")[0]; }
    private void mostrar(Alert.AlertType tipo, String mensagem) {
        Alert alerta = new Alert(tipo, mensagem);
        alerta.setHeaderText(null);
        if (labelSaudacao != null && labelSaudacao.getScene() != null) alerta.initOwner(labelSaudacao.getScene().getWindow());
        alerta.showAndWait();
    }
<<<<<<< HEAD

    @FXML
    void maximizar(ActionEvent event) {
    Stage stage = (Stage) botaoMaximizar.getScene().getWindow();
    JanelaUtil.alternarMaximizada(stage);
    JanelaUtil.sincronizarIcone(botaoMaximizar);
}
=======
>>>>>>> Gustavo
}