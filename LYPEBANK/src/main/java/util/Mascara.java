package util;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Máscaras de digitação para campos de texto: aceita só números e insere
 * a pontuação sozinha enquanto a pessoa digita (ou cola).
 */
public final class Mascara {

    private Mascara() {
    }

    /** 000.000.000-00 */
    public static void cpf(TextField campo) {
        aplicar(campo, 11, Mascara::formatarCpf);
    }

    /** (00) 0000-0000 ou (00) 00000-0000 */
    public static void telefone(TextField campo) {
        aplicar(campo, 11, Mascara::formatarTelefone);
    }

    private static void aplicar(TextField campo, int maxDigitos, UnaryOperator<String> formatar) {
        campo.setTextFormatter(new TextFormatter<String>(mudanca -> {
            if (!mudanca.isContentChange()) {
                return mudanca; // só moveu o cursor / mudou a seleção
            }

            String digitos = mudanca.getControlNewText().replaceAll("\\D", "");
            if (digitos.length() > maxDigitos) {
                digitos = digitos.substring(0, maxDigitos);
            }

            // reescreve o campo inteiro já formatado, com o cursor no fim
            String formatado = formatar.apply(digitos);
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(formatado);
            mudanca.selectRange(formatado.length(), formatado.length());
            return mudanca;
        }));
    }

    static String formatarCpf(String d) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 3 || i == 6) {
                sb.append('.');
            } else if (i == 9) {
                sb.append('-');
            }
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    static String formatarTelefone(String d) {
        if (d.isEmpty()) {
            return "";
        }
        if (d.length() <= 2) {
            return "(" + d;
        }

        String ddd = d.substring(0, 2);
        String numero = d.substring(2);

        // celular (11 dígitos) tem 5 dígitos antes do hífen; fixo (10) tem 4
        int antesDoHifen = d.length() == 11 ? 5 : 4;
        if (numero.length() <= antesDoHifen) {
            return "(" + ddd + ") " + numero;
        }
        return "(" + ddd + ") " + numero.substring(0, antesDoHifen) + "-" + numero.substring(antesDoHifen);
    }
}
