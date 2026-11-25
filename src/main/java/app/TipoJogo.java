package app;

public enum TipoJogo {
    CARA_COROA("Cara ou coroa"),
    DADO("Dado"),
    CACA_NIQUEL("Caça-níquel");

    private final String nome;

    TipoJogo(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
