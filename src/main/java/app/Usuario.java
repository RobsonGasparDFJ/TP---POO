package app;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Usuario {
    private int idUsuario;
    private String nome;
    private String cpf;
    private String telefone;
    private String endereço;
    private Conta conta;
    private String senhaHash;
    private String salt; // relacionado ao hash //

    public Usuario(int id, String nome, String cpf, String telefone, String endereço, String senha){
        this.idUsuario = id;
        setNome(nome);
        setCpf(cpf);
        setTelefone(telefone);
        setEndereço(endereço);
        setSenha(senha);
    }

    public void setSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            System.out.println("A senha deve ter pelo menos 6 caracteres.");
            return;
        }
        this.senhaHash = hash(senha);
    }

    public boolean verificarSenha(String senha) {
        return senha != null && hash(senha).equals(senhaHash);
    }

    // codigo utilizado quando é preciso hash //
    private static String hash(String senha) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(senha.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // Getters e Setters

    public int getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        if(nome == null){
            System.out.println("O nome não pode ser vazio");
        }
        else {
            this.nome = nome;
        }
    }

    public String getCpf() {
        return cpf;
    }
    public void setCpf(String CPF) {
        if(cpf == null || cpf.length() != 11){
            System.out.println("O CPF deve ter 11 digitos.");
        }
        else{
            this.cpf = CPF;
        }
    }

    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereço() {
        return endereço;
    }
    public void setEndereço(String endereço) {
        this.endereço = endereço;
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
    public String getSalt(){
        return salt;
    }

}

