package service;

import model.Conta;
import model.TipoConta;
import model.TipoTransacao;
import model.Transacao;
import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardServiceTest {

    private AnaliseFinanceiraService servico;
    private Conta conta1;
    private Conta conta2;
    private Conta conta3;

    @BeforeEach
    void prepararContas() {
        servico = new AnaliseFinanceiraService();
        Usuario usuario = new Usuario("Usuario Teste", "12345678901",
                "000000000", "Endereco Teste", "senha123");
        conta1 = new Conta(1L, usuario, TipoConta.CORRENTE, 1590.0, "conta1");
        conta2 = new Conta(2L, usuario, TipoConta.CORRENTE, 500.0, "conta2");
        conta3 = new Conta(3L, usuario, TipoConta.POUPANCA, 100.0, "conta3");
    }

    @Test
    void contaSemMovimentacoesRetornaZeros() {
        AnaliseFinanceiraService.Resumo resumo = servico.resumir(1L, List.of());

        assertEquals(0.0, resumo.entradas());
        assertEquals(0.0, resumo.saidas());
        assertEquals(0.0, resumo.getSaldoLiquido());
    }

    @Test
    void dashboardSomaTodasAsMovimentacoesDaConta() {
        AnaliseFinanceiraService.Resumo resumo = servico.resumir(1L, massaCompleta());

        assertEquals(1330.0, resumo.entradas(), 0.001);
        assertEquals(330.0, resumo.saidas(), 0.001);
        assertEquals(1000.0, resumo.getSaldoLiquido(), 0.001);
    }

    @Test
    void premioEEntradaEApostaESaida() {
        List<Transacao> transacoes = massaCompleta();

        Transacao premio = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.PREMIO).findFirst().orElseThrow();
        Transacao aposta = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.APOSTA).findFirst().orElseThrow();

        assertTrue(servico.ehEntrada(premio, 1L));
        assertFalse(servico.ehEntrada(aposta, 1L));
    }

    @Test
    void resumoFicaRestritoAContaSelecionada() {
        List<Transacao> transacoes = new ArrayList<>(massaCompleta());
        transacoes.add(new Transacao(null, conta2, conta3, 999.0,
                LocalDateTime.parse("2026-10-18T12:00:00"), TipoTransacao.TRANSFERENCIA));

        AnaliseFinanceiraService.Resumo resumo = servico.resumir(1L, transacoes);

        assertEquals(1330.0, resumo.entradas(), 0.001);
        assertEquals(330.0, resumo.saidas(), 0.001);
    }

    @Test
    void limitarRecentesNuncaExcedeSeis() {
        List<Transacao> recentes = servico.limitarRecentes(massaCompleta(), 6);

        assertEquals(6, recentes.size());
    }

    @Test
    void contaInvalidaGeraExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.resumir(null, List.of()));
    }

    private List<Transacao> massaCompleta() {
        List<Transacao> transacoes = new ArrayList<>();
        transacoes.add(new Transacao(null, null, conta1, 1000.0,
                LocalDateTime.parse("2026-10-02T09:00:00"), TipoTransacao.DEPOSITO));
        transacoes.add(new Transacao(null, conta1, conta2, 120.0,
                LocalDateTime.parse("2026-10-10T11:30:00"), TipoTransacao.PIX));
        transacoes.add(new Transacao(null, conta2, conta1, 250.0,
                LocalDateTime.parse("2026-10-15T16:45:00"), TipoTransacao.PIX));
        transacoes.add(new Transacao(null, conta1, null, 30.0,
                LocalDateTime.parse("2026-10-20T19:00:00"), TipoTransacao.APOSTA));
        transacoes.add(new Transacao(null, null, conta1, 10.0,
                LocalDateTime.parse("2026-10-25T20:15:00"), TipoTransacao.PREMIO));
        transacoes.add(new Transacao(null, conta1, conta2, 40.0,
                LocalDateTime.parse("2026-10-28T13:20:00"), TipoTransacao.TRANSFERENCIA));
        transacoes.add(new Transacao(null, conta1, null, 50.0,
                LocalDateTime.parse("2026-10-31T10:00:00"), TipoTransacao.SAQUE));
        transacoes.add(new Transacao(null, conta1, null, 90.0,
                LocalDateTime.parse("2026-09-30T14:00:00"), TipoTransacao.SAQUE));
        transacoes.add(new Transacao(null, conta2, conta1, 70.0,
                LocalDateTime.parse("2026-11-01T08:30:00"), TipoTransacao.PIX));
        return transacoes;
    }
}
