package clinicaveterinaria.service;

import clinicaveterinaria.exception.RegistroNaoEncontradoException;
import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Consulta;
import clinicaveterinaria.model.Pagamento;
import clinicaveterinaria.model.enums.FormaPagamento;
import clinicaveterinaria.model.enums.StatusConsulta;
import clinicaveterinaria.model.enums.StatusPagamento;
import clinicaveterinaria.repository.ConsultaRepository;
import clinicaveterinaria.repository.PagamentoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF005 - Controle de pagamentos e pendências financeiras. */
@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final ConsultaRepository consultaRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository, ConsultaRepository consultaRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.consultaRepository = consultaRepository;
    }

    @Transactional
    public Pagamento registrar(Long consultaId, BigDecimal valor, BigDecimal valorPago,
            FormaPagamento formaPagamento, StatusPagamento status) {
        if (consultaId == null) {
            throw new ValidacaoException("Selecione a consulta.");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new ValidacaoException("O valor do pagamento deve ser maior que zero.");
        }
        if (formaPagamento == null) {
            throw new ValidacaoException("Selecione a forma de pagamento.");
        }
        if (status != StatusPagamento.PAGO && status != StatusPagamento.PENDENTE) {
            throw new ValidacaoException("Informe o status do pagamento (PAGO ou PENDENTE).");
        }
        BigDecimal pago = (valorPago == null) ? BigDecimal.ZERO : valorPago;
        if (pago.signum() < 0) {
            throw new ValidacaoException("O valor pago não pode ser negativo.");
        }
        if (status == StatusPagamento.PAGO && pago.compareTo(valor) < 0) {
            throw new ValidacaoException("Status PAGO exige valor pago igual ou maior que o valor.");
        }
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Consulta não encontrada."));
        if (consulta.getStatus() != StatusConsulta.REALIZADA) {
            throw new ValidacaoException("Só é possível cobrar consultas já realizadas.");
        }
        if (pagamentoRepository.existsByConsultaId(consultaId)) {
            throw new ValidacaoException("Esta consulta já possui pagamento registrado.");
        }
        Pagamento pagamento = new Pagamento(consulta, valor, formaPagamento);
        pagamento.setValorPago(pago);
        if (status == StatusPagamento.PAGO) {
            pagamento.registrarPagamento();
        }
        return pagamentoRepository.save(pagamento);
    }

    @Transactional(readOnly = true)
    public List<Pagamento> listarPendentes() {
        return pagamentoRepository.findByStatus(StatusPagamento.PENDENTE);
    }
}
