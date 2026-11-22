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
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

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

    private static final String ESTILO_BASE = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: ";
    private static final String COR_GANHOU = "#398364";
    private static final String COR_PERDEU = "#B3402A";
    private static final String COR_AVISO = "#B96940";

    private final CassinoService cassinoService = new CassinoService();

    private Supplier<Conta> contaSelecionada = () -> null;
    private Runnable aoApostar = () -> { };

    @FXML private ComboBox<Jogo> comboJogo;
    @FXML private Label labelRegras;
    @FXML private Label labelRetorno;
    @FXML private VBox caixaEscolha;
    @FXML private ComboBox<String> comboEscolha;
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

        comboJogo.setConverter(new StringConverter<>() {
            @Override public String toString(Jogo jogo) { return jogo == null ? "" : jogo.getNome(); }
            @Override public Jogo fromString(String texto) { return null; } // o combo não é editável
        });
        comboEscolha.setConverter(new StringConverter<>() {
            @Override public String toString(String opcao) { return opcao == null ? "" : capitalizar(opcao); }
            @Override public String fromString(String texto) { return null; }
        });

        comboJogo.valueProperty().addListener((observavel, antigo, novo) -> mostrarJogo(novo));
        comboJogo.setItems(FXCollections.observableArrayList(cassinoService.listarJogos()));

        labelLimites.setText("Aposta de " + MOEDA.format(Jogo.APOSTA_MINIMA) + " a " + MOEDA.format(Jogo.APOSTA_MAXIMA));
        configurarTabela();

        comboJogo.getSelectionModel().selectFirst();
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
        Conta conta = contaSelecionada.get();
        if (conta == null) {
            tabelaApostas.getItems().clear();
            labelResumo.setText("Selecione uma conta");
            botaoApostar.setDisable(true);
            return;
        }
        botaoApostar.setDisable(false);

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
        try {
            Conta conta = contaSelecionada.get();
            Jogo jogo = comboJogo.getValue();
            if (conta == null) {
                throw new IllegalArgumentException("Selecione uma conta para apostar.");
            }
            if (jogo == null) {
                throw new IllegalArgumentException("Selecione um jogo.");
            }

            String escolha = jogo.getOpcoes().isEmpty() ? null : comboEscolha.getValue();
            double valor = converterValor(campoValor.getText());

            Aposta aposta = cassinoService.apostar(conta.getIdConta(), jogo.getTipo(), escolha, valor);
            mostrarResultado(aposta);
            aoApostar.run(); // o valor fica no campo para repetir a aposta
        } catch (NumberFormatException e) {
            mostrarAviso("Informe um valor de aposta válido.");
        } catch (RuntimeException e) {
            mostrarAviso(e.getMessage());
        }
    }

    private void mostrarJogo(Jogo jogo) {
        if (jogo == null) {
            return;
        }
        labelRegras.setText(jogo.getRegras());
        labelRetorno.setText("Retorno esperado: " + PERCENTUAL.format(jogo.getRetornoEsperado())
                + " de tudo que é apostado. A casa fica com o resto.");

        boolean pedeEscolha = !jogo.getOpcoes().isEmpty();
        caixaEscolha.setVisible(pedeEscolha);
        caixaEscolha.setManaged(pedeEscolha);
        comboEscolha.setItems(FXCollections.observableArrayList(jogo.getOpcoes()));
        comboEscolha.getSelectionModel().selectFirst();

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
