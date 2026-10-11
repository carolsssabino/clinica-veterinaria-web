package clinicaveterinaria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Animal;
import clinicaveterinaria.model.Consulta;
import clinicaveterinaria.model.Pagamento;
import clinicaveterinaria.model.Veterinario;
import clinicaveterinaria.model.enums.FormaPagamento;
import clinicaveterinaria.model.enums.StatusPagamento;
import clinicaveterinaria.repository.ConsultaRepository;
import clinicaveterinaria.repository.PagamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** RF005 - Controle de pagamentos. */
@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;
    @Mock
    private ConsultaRepository consultaRepository;

    private PagamentoService service;
    private Consulta consulta;

    @BeforeEach
    void setUp() {
        service = new PagamentoService(pagamentoRepository, consultaRepository);
        consulta = new Consulta(new Animal("Rex", "Cachorro", "Labrador", 4),
                new Veterinario("Dra. Helena Prado", "CRMV-SP 12345"), LocalDateTime.now().plusDays(1));
        consulta.setId(7L);
    }

    @Test
    void T20_naoDeveRegistrarPagamentoComValorZeroOuNegativo() {
        assertThrows(ValidacaoException.class,
                () -> service.registrar(7L, BigDecimal.ZERO, BigDecimal.ZERO, FormaPagamento.PIX, StatusPagamento.PAGO));
        assertThrows(ValidacaoException.class,
                () -> service.registrar(7L, new BigDecimal("-10"), BigDecimal.ZERO, FormaPagamento.PIX, StatusPagamento.PENDENTE));
    }

    @Test
    void T21_naoDeveAceitarStatusPagoComValorPagoMenorQueOValor() {
        // BUG-002: o status e o valor pago da tela eram ignorados pelo back-end da Etapa 7.
        assertThrows(ValidacaoException.class, () -> service.registrar(7L, new BigDecimal("100"),
                new BigDecimal("50"), FormaPagamento.DINHEIRO, StatusPagamento.PAGO));
    }

    @Test
    void T22_deveRegistrarPagamentoPagoComDataDePagamento() {
        consulta.registrarConsulta("Otite", "Antibiótico", "");
        when(consultaRepository.findById(7L)).thenReturn(Optional.of(consulta));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));
        Pagamento pagamento = service.registrar(7L, new BigDecimal("150.00"), new BigDecimal("150.00"),
                FormaPagamento.PIX, StatusPagamento.PAGO);
        assertEquals(StatusPagamento.PAGO, pagamento.getStatus());
        assertNotNull(pagamento.getDataPagamento());
        assertFalse(pagamento.verificarPendencia());
    }

    @Test
    void T23_devePermitirPagamentoPendente() {
        consulta.registrarConsulta("Otite", "Antibiótico", "");
        when(consultaRepository.findById(7L)).thenReturn(Optional.of(consulta));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));
        Pagamento pagamento = service.registrar(7L, new BigDecimal("150.00"), BigDecimal.ZERO,
                FormaPagamento.CREDITO, StatusPagamento.PENDENTE);
        assertTrue(pagamento.verificarPendencia());
        assertNull(pagamento.getDataPagamento());
    }

    @Test
    void T24_naoDeveCobrarConsultaQueNaoFoiRealizada() {
        // BUG-010: consulta ainda AGENDADA não pode receber pagamento.
        when(consultaRepository.findById(7L)).thenReturn(Optional.of(consulta));
        assertThrows(ValidacaoException.class, () -> service.registrar(7L, new BigDecimal("150"),
                BigDecimal.ZERO, FormaPagamento.PIX, StatusPagamento.PENDENTE));
    }

    @Test
    void T30_naoDeveRegistrarDoisPagamentosParaAMesmaConsulta() {
        // BUG-010: um pagamento por consulta.
        consulta.registrarConsulta("Otite", "Antibiótico", "");
        when(consultaRepository.findById(7L)).thenReturn(Optional.of(consulta));
        when(pagamentoRepository.existsByConsultaId(7L)).thenReturn(true);
        assertThrows(ValidacaoException.class, () -> service.registrar(7L, new BigDecimal("150"),
                BigDecimal.ZERO, FormaPagamento.PIX, StatusPagamento.PENDENTE));
    }
}
