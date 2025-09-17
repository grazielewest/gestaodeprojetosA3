package com.gestao.projetos.util;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;

public class Validacao {

    public static boolean validarObrigatoriedade(Control campo, String nomeCampo) {
        if (campo instanceof TextField) {
            TextField txt = (TextField) campo;
            if (txt.getText() == null || txt.getText().trim().isEmpty()) {
                mostrarErro(campo, nomeCampo + " é obrigatório.");
                return false;
            }
        }
        removerErro(campo);
        return true;
    }

    public static boolean validarCPF(String cpf) {
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^0-9]", "");

        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (CPF inválido)
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Calcula o primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }

        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : 11 - resto;

        // Verifica o primeiro dígito verificador
        if (Character.getNumericValue(cpf.charAt(9)) != digito1) {
            return false;
        }

        // Calcula o segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }

        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : 11 - resto;

        // Verifica o segundo dígito verificador
        if (Character.getNumericValue(cpf.charAt(10)) != digito2) {
            return false;
        }

        return true;
    }

    public static boolean validarCPF(Control campo, String nomeCampo) {
        if (campo instanceof TextField) {
            TextField txt = (TextField) campo;
            String cpf = txt.getText();

            if (cpf == null || cpf.trim().isEmpty()) {
                mostrarErro(campo, nomeCampo + " é obrigatório.");
                return false;
            }

            if (!validarCPF(cpf)) {
                mostrarErro(campo, nomeCampo + " inválido.");
                return false;
            }

            removerErro(campo);
            return true;
        }
        return false;
    }

    public static boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(regex);
    }

    public static boolean validarEmail(Control campo, String nomeCampo) {
        if (campo instanceof TextField) {
            TextField txt = (TextField) campo;
            String email = txt.getText();

            if (email == null || email.trim().isEmpty()) {
                mostrarErro(campo, nomeCampo + " é obrigatório.");
                return false;
            }

            if (!validarEmail(email)) {
                mostrarErro(campo, nomeCampo + " inválido.");
                return false;
            }

            removerErro(campo);
            return true;
        }
        return false;
    }

    private static void mostrarErro(Control campo, String mensagem) {
        campo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        // Garante que o tooltip existe
        if (campo.getTooltip() == null) {
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();
            campo.setTooltip(tooltip);
        }
        campo.getTooltip().setText(mensagem);
        campo.getTooltip().show(campo.getScene().getWindow());
    }

    private static void removerErro(Control campo) {
        campo.setStyle("");
        if (campo.getTooltip() != null) {
            campo.getTooltip().hide();
            campo.getTooltip().setText("");
        }
    }
}