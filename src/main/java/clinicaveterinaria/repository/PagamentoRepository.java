package clinicaveterinaria.repository;

import clinicaveterinaria.model.Pagamento;
import clinicaveterinaria.model.enums.StatusPagamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    boolean existsByConsultaId(Long consultaId);

    List<Pagamento> findByStatus(StatusPagamento status);
}
