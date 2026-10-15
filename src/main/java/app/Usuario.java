package app;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Usuario {
    
    private Long idUsuario;
    private String nome;
    private String cpf;
    private String telefone;
    private String endereco;
    private Conta conta;
    private String senhaHash;
    private String salt;

    public Usuario(String nome, String cpf, String telefone, String endereco, String senha) {
        this.salt = gerarSalt();
        setNome(nome);
        setCpf(cpf);
        setTelefone(telefone);
        setEndereco(endereco);
        setSenha(senha);
    }

    // Construtor utilitário para recuperar do banco de dados
    public Usuario(Long idUsuario, String nome, String cpf, String telefone, String endereco, String senhaHash, String salt) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.senhaHash = senhaHash;
        this.salt = salt;
    }

    private static String gerarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public void setSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
        this.senhaHash = hash(senha, this.salt);
    }

    public boolean verificarSenha(String senha) {
        return senha != null && hash(senha, this.salt).equals(this.senhaHash);
    }

    private static String hash(String senha, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashBytes = digest.digest(senha.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);
            
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de criptografia indisponível", e);
        }
    }

    public Long getIdUsuario() {
        return this.idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            throw new IllegalArgumentException("O CPF deve ter exatamente 11 dígitos.");
        }
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getSalt() {
        return salt;
    }
}