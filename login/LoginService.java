import LoginController.java

public class LoginService {

    private static final int MAX_TENTATIVAS_FALHAS = 3;

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final SessaoService sessaoService;

    // Construtor com injeção de dependências
    public LoginService(UsuarioRepository usuarioRepository, AuditoriaService auditoriaService, SessaoService sessaoService) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.sessaoService = sessaoService;
    }

    public AuthResult autenticar(String cpf, char[] senha, String deviceId, String ipAddress) {
        try {
            Optional<Usuario> userOpt = usuarioRepository.buscarPorCpf(cpf);

            if (userOpt.isEmpty()) {
                auditoriaService.registrarAcesso(cpf, ipAddress, false, "Usuário não encontrado");
                return new AuthResult(AuthStatus.CREDENCIAIS_INVALIDAS, null, "CPF ou senha incorretos.");
            }

            Usuario usuario = userOpt.get();

            // 1. Verifica se a conta já está bloqueada
            if (usuario.isBloqueado()) {
                auditoriaService.registrarAcesso(cpf, ipAddress, false, "Tentativa em conta bloqueada");
                return new AuthResult(AuthStatus.CONTA_BLOQUEADA, null, "Conta bloqueada. Procure sua agência.");
            }

            // 2. Valida a senha
            boolean senhaValida = SecurityUtils.verificarSenha(senha, usuario.getSalt(), usuario.getHashSenha());
            
            // LIMPEZA DA SENHA DA MEMÓRIA RAM (Crucial para bancos)
            SecurityUtils.limparMemoriaSenha(senha); 

            // 3. Tratamento de erro de senha
            if (!senhaValida) {
                registrarFalha(usuario, ipAddress);
                return new AuthResult(AuthStatus.CREDENCIAIS_INVALIDAS, null, "CPF ou senha incorretos.");
            }

            // 4. Zera falhas em caso de sucesso
            if (usuario.getTentativasFalhas() > 0) {
                usuario.setTentativasFalhas(0);
                usuarioRepository.salvar(usuario);
            }

            // 5. Validação de Dispositivo (MFA)
            if (!usuario.getDispositivosConfiaveis().contains(deviceId)) {
                auditoriaService.registrarAcesso(cpf, ipAddress, true, "Login parcial - Requer MFA");
                auditoriaService.notificarAcessoSuspeito(cpf, ipAddress);
                return new AuthResult(AuthStatus.REQUER_MFA, null, "Dispositivo não reconhecido. Validação adicional necessária.");
            }

            // 6. Sucesso Total - Criação de Sessão
            String token = sessaoService.criarSessaoSegura(usuario.getId(), deviceId, ipAddress);
            auditoriaService.registrarAcesso(cpf, ipAddress, true, "Login bem-sucedido");
            
            return new AuthResult(AuthStatus.SUCESSO, token, "Login efetuado com sucesso.");

        } finally {
            // Garante que a senha seja limpa da memória mesmo se ocorrer uma Exception inesperada
            SecurityUtils.limparMemoriaSenha(senha);
        }
    }

    private void registrarFalha(Usuario usuario, String ipAddress) {
        usuario.setTentativasFalhas(usuario.getTentativasFalhas() + 1);
        
        if (usuario.getTentativasFalhas() >= MAX_TENTATIVAS_FALHAS) {
            usuario.setBloqueado(true);
            auditoriaService.registrarAcesso(usuario.getCpf(), ipAddress, false, "Conta BLOQUEADA por excesso de tentativas");
        } else {
            auditoriaService.registrarAcesso(usuario.getCpf(), ipAddress, false, "Senha Incorreta");
        }
        
        usuarioRepository.salvar(usuario);
    }
}