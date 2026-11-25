package app.ui;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import app.Aposta;
import app.CassinoService;
import app.Conta;
import app.Jogo;
import app.ResumoApostas;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Aba do cassino (cassino.fxml). O dashboard diz qual conta está selecionada e é avisado
 * depois de cada aposta, para atualizar saldo, gráficos e extrato.
 */
public class ControleCassino {

    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(BRASIL);
    private static final NumberFormat PERCENTUAL = NumberFormat.getPercentInstance(BRASIL);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final int LIMITE_HISTORICO = 30;

    private static final String ESTILO_BASE = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: ";
    private static final String COR_GANHOU = "#6FD0A4";
    private static final String COR_PERDEU = "#F08A72";
    private static final String COR_AVISO = "#E8B84A";

    private final CassinoService cassinoService = new CassinoService();

    private Supplier<Conta> contaSelecionada = () -> null;
    private Runnable aoApostar = () -> { };

    private final ToggleGroup grupoJogos = new ToggleGroup();
    private final ToggleGroup grupoEscolhas = new ToggleGroup();
    private boolean animando = false;

    @FXML private VBox painelControles;
    @FXML private FlowPane caixaJogos;
    @FXML private Label labelRegras;
    @FXML private Label labelRetorno;
    @FXML private VBox caixaEscolha;
    @FXML private FlowPane caixaOpcoes;
    @FXML private PalcoCassino palco;
    @FXML private Label labelSaldoCassino;
    @FXML private TextField campoValor;
    @FXML private Label labelLimites;
    @FXML private Button botaoApostar;
    @FXML private Label labelResultado;
    @FXML private Label labelResumo;
    @FXML private TableView<Aposta> tabelaApostas;
    @FXML private TableColumn<Aposta, String> colunaJogo;
    @FXML private TableColumn<Aposta, String> colunaValor;
    @FXML private TableColumn<Aposta, String> colunaSorteio;
    @FXML private TableColumn<Aposta, String> colunaResultado;
    @FXML private TableColumn<Aposta, String> colunaData;

    @FXML
    public void initialize() {
        PERCENTUAL.setMaximumFractionDigits(1);

        manterUmSelecionado(grupoJogos);
        manterUmSelecionado(grupoEscolhas);
        for (Jogo jogo : cassinoService.listarJogos()) {
            caixaJogos.getChildren().add(criarOpcao(jogo.getNome(), jogo, grupoJogos));
        }
        grupoJogos.selectedToggleProperty().addListener((observavel, antigo, novo) -> {
            if (novo != null) mostrarJogo((Jogo) novo.getUserData());
        });

        labelLimites.setText("Aposta de " + MOEDA.format(Jogo.APOSTA_MINIMA) + " a " + MOEDA.format(Jogo.APOSTA_MAXIMA));
        configurarTabela();

        grupoJogos.selectToggle(grupoJogos.getToggles().get(0));
    }

    /**
     * Liga a aba ao dashboard.
     *
     * @param contaSelecionada devolve a conta escolhida no seletor do dashboard
     * @param aoApostar        executado depois de cada aposta (o dashboard recarrega os dados)
     */
    public void configurar(Supplier<Conta> contaSelecionada, Runnable aoApostar) {
        this.contaSelecionada = contaSelecionada;
        this.aoApostar = aoApostar;
        atualizar();
    }

    /** Recarrega o histórico e o resumo da conta selecionada. */
    public void atualizar() {
        if (animando) {
            return; // não revela o resultado no histórico antes do fim da animação
        }
        Conta conta = contaSelecionada.get();
        if (conta == null) {
            tabelaApostas.getItems().clear();
            labelResumo.setText("Selecione uma conta");
            labelSaldoCassino.setText("");
            botaoApostar.setDisable(true);
            return;
        }
        botaoApostar.setDisable(false);
        mostrarSaldo(conta.getSaldo());

        try {
            List<Aposta> apostas = cassinoService.listarRecentes(conta.getIdConta(), LIMITE_HISTORICO);
            tabelaApostas.setItems(FXCollections.observableArrayList(apostas));

            ResumoApostas resumo = cassinoService.resumir(conta.getIdConta());
            if (resumo.quantidade() == 0) {
                labelResumo.setText("Nenhuma aposta nesta conta");
            } else {
                labelResumo.setText(resumo.quantidade() + " aposta(s) · resultado total: "
                        + formatarResultado(resumo.saldoLiquido()));
            }
        } catch (RuntimeException e) {
            labelResumo.setText("Não foi possível carregar as apostas");
        }
    }

    @FXML
    private void apostar() {
        if (animando) {
            return; // Enter no campo de valor durante uma rodada
        }
        try {
            Conta conta = contaSelecionada.get();
            Jogo jogo = jogoSelecionado();
            if (conta == null) {
                throw new IllegalArgumentException("Selecione uma conta para apostar.");
            }

            String escolha = jogo.getOpcoes().isEmpty()
                    ? null : (String) grupoEscolhas.getSelectedToggle().getUserData();
            double valor = converterValor(campoValor.getText());

            // o saldo é gravado aqui; a animação só revela o resultado
            Aposta aposta = cassinoService.apostar(conta.getIdConta(), jogo.getTipo(), escolha, valor);

            definirAnimando(true);
            mostrarSaldo(conta.getSaldo() - aposta.getValor());
            definirMensagem("Boa sorte...", COR_AVISO);
            palco.animar(aposta, () -> {
                definirAnimando(false);
                mostrarResultado(aposta);
                // fora da animação, porque o dashboard pode abrir um alerta se a recarga falhar
                Platform.runLater(aoApostar); // o valor fica no campo para repetir a aposta
            });
        } catch (NumberFormatException e) {
            mostrarAviso("Informe um valor de aposta válido.");
        } catch (RuntimeException e) {
            mostrarAviso(e.getMessage());
        }
    }

    /** Botões de valor rápido: o userData é um número, "METADE" ou "MAXIMO". */
    @FXML
    private void valorRapido(ActionEvent evento) {
        Conta conta = contaSelecionada.get();
        if (conta == null) {
            return;
        }
        String tipo = (String) ((Button) evento.getSource()).getUserData();
        double valor = switch (tipo) {
            case "METADE" -> Math.min(conta.getSaldo() / 2, Jogo.APOSTA_MAXIMA);
            case "MAXIMO" -> Math.min(conta.getSaldo(), Jogo.APOSTA_MAXIMA);
            default -> Double.parseDouble(tipo);
        };
        // arredonda para baixo nos centavos, para nunca passar do saldo
        valor = Math.floor(valor * 100 + 1e-6) / 100;

        if (valor < Jogo.APOSTA_MINIMA || valor > conta.getSaldo()) {
            mostrarAviso("Saldo insuficiente para essa aposta.");
            return;
        }
        campoValor.setText(String.format(BRASIL, "%.2f", valor));
        labelResultado.setText("");
    }

    private Jogo jogoSelecionado() {
        return (Jogo) grupoJogos.getSelectedToggle().getUserData();
    }

    private ToggleButton criarOpcao(String texto, Object valor, ToggleGroup grupo) {
        ToggleButton botao = new ToggleButton(texto);
        botao.setUserData(valor);
        botao.setToggleGroup(grupo);
        botao.getStyleClass().add("opcao-cassino");
        return botao;
    }

    /** Impede que um clique no botão já marcado deixe o grupo sem seleção. */
    private void manterUmSelecionado(ToggleGroup grupo) {
        grupo.selectedToggleProperty().addListener((observavel, antigo, novo) -> {
            if (novo == null && antigo != null && grupo.getToggles().contains(antigo)) {
                grupo.selectToggle(antigo);
            }
        });
    }

    private void definirAnimando(boolean valor) {
        animando = valor;
        painelControles.setDisable(valor);
    }

    private void mostrarSaldo(double saldo) {
        labelSaldoCassino.setText("Saldo: " + MOEDA.format(saldo));
    }

    private void mostrarJogo(Jogo jogo) {
        labelRegras.setText(jogo.getRegras());
        labelRetorno.setText("Retorno esperado: " + PERCENTUAL.format(jogo.getRetornoEsperado())
                + " de tudo que é apostado. A casa fica com o resto.");

        boolean pedeEscolha = !jogo.getOpcoes().isEmpty();
        caixaEscolha.setVisible(pedeEscolha);
        caixaEscolha.setManaged(pedeEscolha);

        grupoEscolhas.getToggles().clear();
        caixaOpcoes.getChildren().clear();
        for (String opcao : jogo.getOpcoes()) {
            caixaOpcoes.getChildren().add(criarOpcao(capitalizar(opcao), opcao, grupoEscolhas));
        }
        if (pedeEscolha) {
            grupoEscolhas.selectToggle(grupoEscolhas.getToggles().get(0));
        }

        palco.preparar(jogo.getTipo());
        labelResultado.setText("");
    }

    private void mostrarResultado(Aposta aposta) {
        if (aposta.isGanhou()) {
            definirMensagem("Você ganhou " + MOEDA.format(aposta.getPremio()) + "!  " + aposta.getDescricao(), COR_GANHOU);
        } else {
            definirMensagem("Você perdeu " + MOEDA.format(aposta.getValor()) + ".  " + aposta.getDescricao(), COR_PERDEU);
        }
    }

    private void mostrarAviso(String mensagem) {
        definirMensagem(mensagem, COR_AVISO);
    }

    private void definirMensagem(String texto, String cor) {
        labelResultado.setText(texto);
        labelResultado.setStyle(ESTILO_BASE + cor + ";");
    }

    private void configurarTabela() {
        tabelaApostas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaApostas.setPlaceholder(new Label("Ainda não há apostas nesta conta."));

        colunaJogo.setCellValueFactory(dado -> new ReadOnlyStringWrapper(dado.getValue().getJogo().getNome()));
        colunaValor.setCellValueFactory(dado -> new ReadOnlyStringWrapper(MOEDA.format(dado.getValue().getValor())));
        colunaSorteio.setCellValueFactory(dado -> new ReadOnlyStringWrapper(descricaoDaRodada(dado.getValue())));
        colunaResultado.setCellValueFactory(dado -> new ReadOnlyStringWrapper(formatarResultado(dado.getValue().getLiquido())));
        colunaData.setCellValueFactory(dado -> new ReadOnlyStringWrapper(DATA.format(dado.getValue().getDataHora())));

        // verde para lucro, laranja para prejuízo (mesmas classes do fluxo financeiro do dashboard)
        colunaResultado.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vazio) {
                super.updateItem(item, vazio);
                getStyleClass().removeAll("flow-income", "flow-expense");
                if (vazio || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add(item.startsWith("+") ? "flow-income" : "flow-expense");
            }
        });
    }

    private String descricaoDaRodada(Aposta aposta) {
        if (aposta.getEscolha() == null) {
            return aposta.getDescricao();
        }
        return "Você: " + capitalizar(aposta.getEscolha()) + " · " + aposta.getDescricao();
    }

    private String formatarResultado(double valor) {
        return (valor >= 0 ? "+ " : "− ") + MOEDA.format(Math.abs(valor));
    }

    private String capitalizar(String texto) {
        if (texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(BRASIL) + texto.substring(1).toLowerCase(BRASIL);
    }

    /** Aceita "10", "10.50", "10,50" e "1.000,50". */
    private double converterValor(String texto) {
        String valor = texto == null ? "" : texto.trim().replace("R$", "").replace(" ", "");
        if (valor.contains(",")) {
            valor = valor.replace(".", "").replace(",", ".");
        }
        return Double.parseDouble(valor);
    }
}
