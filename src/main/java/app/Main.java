package app;

public class Main {
    public static void main(String[] args) {
        DatabaseSetup.inicializarBanco();
        new Interface().iniciar();
    }
}
