package clinicaveterinaria.service;

import clinicaveterinaria.exception.RegistroNaoEncontradoException;
import clinicaveterinaria.exception.ValidacaoException;
import clinicaveterinaria.model.Tutor;
import clinicaveterinaria.repository.TutorRepository;
import clinicaveterinaria.util.ValidadorCpf;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF002 - Cadastro de tutores. O CPF é normalizado (somente dígitos) antes de gravar e de consultar. */
@Service
public class TutorService {

    private final TutorRepository tutorRepository;

    public TutorService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    @Transactional
    public Tutor cadastrar(String nome, String cpf, String telefone, String endereco) {
        validarCadastro(nome, cpf, telefone);
        String cpfNumerico = ValidadorCpf.somenteDigitos(cpf);
        if (tutorRepository.findByCpf(cpfNumerico).isPresent()) {
            throw new ValidacaoException("Já existe um tutor cadastrado com este CPF.");
        }
        return tutorRepository.save(new Tutor(nome.trim(), cpfNumerico, telefone, endereco));
    }

    @Transactional(readOnly = true)
    public Tutor buscarPorCpf(String cpf) {
        return tutorRepository.findByCpf(ValidadorCpf.somenteDigitos(cpf))
                .orElseThrow(() -> new RegistroNaoEncontradoException("Tutor não encontrado para o CPF informado."));
    }

    @Transactional(readOnly = true)
    public List<Tutor> buscarPorNome(String nome) {
        return tutorRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional(readOnly = true)
    public List<Tutor> listarTodos() {
        return tutorRepository.findAll();
    }

    private void validarCadastro(String nome, String cpf, String telefone) {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("O nome do tutor é obrigatório.");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new ValidacaoException("O CPF do tutor é obrigatório.");
        }
        if (!ValidadorCpf.isValido(cpf)) {
            throw new ValidacaoException("CPF inválido.");
        }
        if (telefone != null && !telefone.isBlank()) {
            int digitos = ValidadorCpf.somenteDigitos(telefone).length();
            if (digitos != 10 && digitos != 11) {
                throw new ValidacaoException("Telefone inválido. Use DDD + número.");
            }
        }
    }
}
