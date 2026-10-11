package clinicaveterinaria.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Testes unitários do cálculo dos dígitos verificadores de CPF (T01-T06 da Etapa 7, migrados para JUnit 5). */
class ValidadorCpfTest {

    @Test
    void T01_deveAceitarCpfValidoSemMascara() {
        assertTrue(ValidadorCpf.isValido("52998224725"));
    }

    @Test
    void T02_deveAceitarCpfValidoComMascara() {
        assertTrue(ValidadorCpf.isValido("529.982.247-25"));
    }

    @Test
    void T03_deveRejeitarCpfComDigitoVerificadorErrado() {
        assertFalse(ValidadorCpf.isValido("52998224724"));
    }

    @Test
    void T04_deveRejeitarCpfComTodosDigitosIguais() {
        assertFalse(ValidadorCpf.isValido("11111111111"));
        assertFalse(ValidadorCpf.isValido("00000000000"));
    }

    @Test
    void T05_deveRejeitarCpfComQuantidadeErradaDeDigitos() {
        assertFalse(ValidadorCpf.isValido("123456789"));
        assertFalse(ValidadorCpf.isValido("123456789012"));
    }

    @Test
    void T06_deveRejeitarCpfNuloOuVazio() {
        assertFalse(ValidadorCpf.isValido(null));
        assertFalse(ValidadorCpf.isValido(""));
    }

    @Test
    void T28_deveRemoverMascaraDoCpf() {
        assertEquals("52998224725", ValidadorCpf.somenteDigitos("529.982.247-25"));
        assertEquals("", ValidadorCpf.somenteDigitos(null));
    }
}
