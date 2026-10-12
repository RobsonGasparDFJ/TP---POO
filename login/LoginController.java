import LoginService.java

// Em um ambiente real com Spring, usaríamos anotações como @RestController, @PostMapping, etc.
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    public AuthResult fazerLogin(LoginRequest request) {
        // Transforma a senha String (recebida do JSON) em char[] o mais rápido possível
        char[] senhaArray = request.senha().toCharArray();
        
        AuthResult resultado = loginService.autenticar(
            request.cpf(),
            senhaArray,
            request.deviceId(),
            request.ipAddress()
        );

        return resultado;
    }

    // O Record em Java serve para declarar classes de dados imutáveis de forma concisa, eliminando a necessidade de escrever código repetitivo (boilerplate)
    public record LoginRequest(String cpf, String senha, String deviceId, String ipAddress) {}
}