package app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Interface {

    private final Scanner scanner = new Scanner(System.in);

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ContaDAO contaDAO = new ContaDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private final TransferenciaService transferenciaService = new TransferenciaService();

    // ------------------------------------------------------------------
    // MENUS
    // ------------------------------------------------------------------

    public void iniciar() {
        try {
            menuPrincipal();
        } catch (NoSuchElementException e) {
            // entrada encerrada (Ctrl+D / Ctrl+Z)
            System.out.println();
        }
        System.out.println("Encerrando o sistema. Até logo!");
    }

    private void menuPrincipal() {
        while (true) {
            System.out.println();
            System.out.println("========== SISTEMA BANCÁRIO ==========");
            System.out.println("1 - Entrar (login)");
            System.out.println("2 - Cadastrar novo usuário");
            System.out.println("0 - Sair");
            System.out.println("======================================");

            switch (lerOpcao()) {
                case 1 -> login();
                case 2 -> cadastrarUsuario();
                case 0 -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void menuUsuario(Usuario usuario) {
        while (true) {
            System.out.println();
            System.out.println("========== Olá, " + usuario.getNome() + " ==========");
            System.out.println("1 - Minhas contas / saldos");
            System.out.println("2 - Abrir nova conta");
            System.out.println("3 - Depositar");
            System.out.println("4 - Sacar");
            System.out.println("5 - Transferir (número da conta)");
            System.out.println("6 - Transferir via Pix (chave)");
            System.out.println("7 - Extrato");
            System.out.println("0 - Sair da conta (logout)");
            System.out.println("==========================================");

            int opcao = lerOpcao();
            if (opcao == 0) {
                return;
            }

            try {
                switch (opcao) {
                    case 1 -> listarContas(usuario);
                    case 2 -> abrirConta(usuario);
                    case 3 -> depositar(usuario);
                    case 4 -> sacar(usuario);
                    case 5 -> transferirPorConta(usuario);
                    case 6 -> transferirPorPix(usuario);
                    case 7 -> extrato(usuario);
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Não foi possível concluir: " + e.getMessage());
            } catch (RuntimeException | SQLException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // ACESSO / CADASTRO
    // ------------------------------------------------------------------

    private void login() {
        System.out.println("\n--- LOGIN ---");
        String cpf = somenteDigitos(lerTexto("CPF: "));
        String senha = lerSenha("Senha: ");

        try {
            if (!usuarioDAO.autenticar(cpf, senha)) {
                System.out.println("CPF ou senha inválidos.");
                return;
            }
            Usuario usuario = usuarioDAO.buscarPorCpf(cpf);
            menuUsuario(usuario);
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void cadastrarUsuario() {
        System.out.println("\n--- CADASTRO DE USUÁRIO ---");
        try {
            String nome = lerTexto("Nome: ");
            String cpf = somenteDigitos(lerTexto("CPF (11 dígitos): "));
            String telefone = lerTexto("Telefone: ");
            String endereco = lerTexto("Endereço: ");
            String senha = lerSenha("Senha (mín. 6 caracteres): ");

            if (usuarioDAO.buscarPorCpf(cpf) != null) {
                System.out.println("Já existe um usuário cadastrado com esse CPF.");
                return;
            }

            Usuario usuario = new Usuario(nome, cpf, telefone, endereco, senha);

            System.out.println("\nTodo usuário começa com uma conta.");
            TipoConta tipo = lerTipoConta();
            if (tipo == null) {
                System.out.println("Cadastro cancelado.");
                return;
            }
            String chave = lerTexto("Chave Pix desta conta (ex.: CPF, e-mail, telefone): ");
            Conta conta = new Conta(usuario, tipo, chave);

            // usuário + conta gravados juntos: ou salva os dois, ou nenhum
            try (Connection conn = DatabaseSetup.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    usuarioDAO.salvar(conn, usuario);
                    contaDAO.inserir(conn, conta);
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }

            System.out.println("Usuário cadastrado com sucesso!");
            System.out.println("Número da sua conta: " + conta.getIdConta());
        } catch (IllegalArgumentException e) {
            System.out.println("Dados inválidos: " + e.getMessage());
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("TB_Conta.chave")) {
                System.out.println("Essa chave Pix já está em uso. Tente outra.");
            } else {
                System.out.println("Erro ao cadastrar: " + e.getMessage());
            }
        } catch (RuntimeException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // OPERAÇÕES
    // ------------------------------------------------------------------

    private void listarContas(Usuario usuario) throws SQLException {
        List<Conta> contas = contasDe(usuario);

        System.out.println("\n--- MINHAS CONTAS ---");
        if (contas.isEmpty()) {
            System.out.println("Você ainda não tem contas.");
            return;
        }
        for (Conta c : contas) {
            System.out.printf("Conta %d | %s | Chave Pix: %s | Saldo: R$ %.2f%n",
                    c.getIdConta(), c.getTipoConta(), c.getChave(), c.getSaldo());
        }
    }

    private void abrirConta(Usuario usuario) throws SQLException {
        System.out.println("\n--- ABRIR NOVA CONTA ---");
        List<Conta> contas = contasDe(usuario);

        TipoConta tipo = lerTipoConta();
        if (tipo == null) {
            return;
        }
        for (Conta c : contas) {
            if (c.getTipoConta() == tipo) {
                System.out.println("Você já possui uma conta " + tipo + ".");
                return;
            }
        }

        String chave = lerTexto("Chave Pix desta conta (deve ser diferente das já cadastradas): ");
        Conta conta = new Conta(usuario, tipo, chave);

        try (Connection conn = DatabaseSetup.getConnection()) {
            contaDAO.inserir(conn, conta);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("TB_Conta.chave")) {
                System.out.println("Essa chave Pix já está em uso. Tente outra.");
                return;
            }
            throw e;
        }
        System.out.println("Conta aberta! Número da conta: " + conta.getIdConta());
    }

    private void depositar(Usuario usuario) throws SQLException {
        System.out.println("\n--- DEPÓSITO ---");
        Conta escolhida = escolherConta(usuario);
        if (escolhida == null) {
            return;
        }
        double valor = lerValor("Valor do depósito: R$ ");

        try (Connection conn = DatabaseSetup.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Conta conta = contaDAO.buscarPorId(conn, escolhida.getIdConta());
                conta.depositar(valor);
                contaDAO.atualizarSaldo(conn, conta);
                transacaoDAO.salvar(conn, new Transacao(null, conta, valor, TipoTransacao.DEPOSITO));
                conn.commit();
                System.out.printf("Depósito realizado. Novo saldo: R$ %.2f%n", conta.getSaldo());
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void sacar(Usuario usuario) throws SQLException {
        System.out.println("\n--- SAQUE ---");
        Conta escolhida = escolherConta(usuario);
        if (escolhida == null) {
            return;
        }
        double valor = lerValor("Valor do saque: R$ ");

        try (Connection conn = DatabaseSetup.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Conta conta = contaDAO.buscarPorId(conn, escolhida.getIdConta());
                conta.sacar(valor);
                contaDAO.atualizarSaldo(conn, conta);
                transacaoDAO.salvar(conn, new Transacao(conta, null, valor, TipoTransacao.SAQUE));
                conn.commit();
                System.out.printf("Saque realizado. Novo saldo: R$ %.2f%n", conta.getSaldo());
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void transferirPorConta(Usuario usuario) throws SQLException {
        System.out.println("\n--- TRANSFERÊNCIA ---");
        Conta origem = escolherConta(usuario);
        if (origem == null) {
            return;
        }
        Long idDestino = lerLong("Número da conta de destino: ");
        double valor = lerValor("Valor da transferência: R$ ");

        Transacao t = transferenciaService.transferirPorConta(origem.getIdConta(), idDestino, valor);
        System.out.printf("Transferência de R$ %.2f realizada para %s (conta %d).%n",
                t.getValor(), t.getDestinatario().getUsuario().getNome(), t.getDestinatario().getIdConta());
    }

    private void transferirPorPix(Usuario usuario) throws SQLException {
        System.out.println("\n--- PIX ---");
        Conta origem = escolherConta(usuario);
        if (origem == null) {
            return;
        }
        String chave = lerTexto("Chave Pix do destinatário: ");
        double valor = lerValor("Valor do Pix: R$ ");

        Transacao t = transferenciaService.transferirPorPix(origem.getIdConta(), chave, valor);
        System.out.printf("Pix de R$ %.2f enviado para %s.%n",
                t.getValor(), t.getDestinatario().getUsuario().getNome());
    }

    private void extrato(Usuario usuario) throws SQLException {
        System.out.println("\n--- EXTRATO ---");
        Conta escolhida = escolherConta(usuario);
        if (escolhida == null) {
            return;
        }

        try (Connection conn = DatabaseSetup.getConnection()) {
            Conta conta = contaDAO.buscarPorId(conn, escolhida.getIdConta());
            List<Transacao> transacoes = transacaoDAO.listarPorConta(conn, conta.getIdConta());

            new Extrato(conta, transacoes).exibirExtrato();
            System.out.printf("Saldo atual: R$ %.2f%n", conta.getSaldo());
        }
    }

    // ------------------------------------------------------------------
    // AUXILIARES DE NEGÓCIO
    // ------------------------------------------------------------------

    private List<Conta> contasDe(Usuario usuario) throws SQLException {
        try (Connection conn = DatabaseSetup.getConnection()) {
            return contaDAO.buscarPorUsuario(conn, usuario.getIdUsuario());
        }
    }

    /** Se o usuário tem uma só conta, usa ela; se tem mais de uma, pergunta qual. */
    private Conta escolherConta(Usuario usuario) throws SQLException {
        List<Conta> contas = contasDe(usuario);

        if (contas.isEmpty()) {
            System.out.println("Você ainda não tem contas. Abra uma no menu.");
            return null;
        }
        if (contas.size() == 1) {
            return contas.get(0);
        }

        System.out.println("Escolha a conta:");
        for (int i = 0; i < contas.size(); i++) {
            Conta c = contas.get(i);
            System.out.printf("%d - %s (conta %d) | Saldo: R$ %.2f%n",
                    i + 1, c.getTipoConta(), c.getIdConta(), c.getSaldo());
        }
        System.out.println("0 - Cancelar");

        int escolha = lerOpcao();
        if (escolha < 1 || escolha > contas.size()) {
            System.out.println("Operação cancelada.");
            return null;
        }
        return contas.get(escolha - 1);
    }

    private TipoConta lerTipoConta() {
        System.out.println("Tipo de conta:");
        System.out.println("1 - Corrente");
        System.out.println("2 - Poupança");
        System.out.println("0 - Cancelar");

        return switch (lerOpcao()) {
            case 1 -> TipoConta.CORRENTE;
            case 2 -> TipoConta.POUPANCA;
            default -> null;
        };
    }

    // ------------------------------------------------------------------
    // LEITURA DE DADOS
    // ------------------------------------------------------------------

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private String lerSenha(String mensagem) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] senha = console.readPassword(mensagem);
            if (senha == null) {
                throw new NoSuchElementException();
            }
            return new String(senha);
        }
        return lerTexto(mensagem); // IDEs normalmente não têm console: a senha aparece na tela
    }

    private int lerOpcao() {
        System.out.print("Opção: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Long lerLong(String mensagem) {
        String texto = lerTexto(mensagem);
        try {
            return Long.parseLong(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número inválido: \"" + texto + "\".");
        }
    }

    /** Aceita "100", "100.50", "100,50" e "1.000,50". */
    private double lerValor(String mensagem) {
        String texto = lerTexto(mensagem);
        if (texto.contains(",")) {
            texto = texto.replace(".", "").replace(",", ".");
        }
        try {
            double valor = Double.parseDouble(texto);
            if (!Double.isFinite(valor)) {
                throw new NumberFormatException();
            }
            return Math.round(valor * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido.");
        }
    }

    private String somenteDigitos(String texto) {
        return texto.replaceAll("\\D", "");
    }
}
