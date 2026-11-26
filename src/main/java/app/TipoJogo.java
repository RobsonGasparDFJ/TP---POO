package app;

public enum TipoJogo {
    CARA_COROA("Cara ou coroa"),
    DADO("Dado"),
    CACA_NIQUEL("Caça-níquel"),
    ROLETA("Roleta"),
    FOGUETE("Foguete");

    private final String nome;

    TipoJogo(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
