package clinicaveterinaria.util;

/**
 * Validação de CPF (migrada da Etapa 7). Na Etapa 9 foi acrescentado
 * {@link #somenteDigitos(String)}, usado para normalizar o CPF antes de gravar/consultar.
 */
public final class ValidadorCpf {

    private ValidadorCpf() {
    }

    public static boolean isValido(String cpf) {
        if (cpf == null) {
            return false;
        }
        String digitos = somenteDigitos(cpf);
        if (digitos.length() != 11 || todosDigitosIguais(digitos)) {
            return false;
        }
        int primeiro = calcularDigitoVerificador(digitos.substring(0, 9), 10);
        int segundo = calcularDigitoVerificador(digitos.substring(0, 9) + primeiro, 11);
        return digitos.equals(digitos.substring(0, 9) + primeiro + segundo);
    }

    /** Remove máscara e qualquer caractere não numérico ("529.982.247-25" vira "52998224725"). */
    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private static boolean todosDigitosIguais(String digitos) {
        for (int i = 1; i < digitos.length(); i++) {
            if (digitos.charAt(i) != digitos.charAt(0)) {
                return false;
            }
        }
        return true;
    }

    private static int calcularDigitoVerificador(String base, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (char c : base.toCharArray()) {
            soma += Character.getNumericValue(c) * peso;
            peso--;
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }
}
