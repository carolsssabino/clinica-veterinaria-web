package clinicaveterinaria.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração dos endpoints (Controller + Service + JPA + banco H2 de teste), previstos na seção 6
 * do plano de testes da Etapa 7. Cada teste é revertido ao final (@Transactional), então um não interfere no outro.
 * Usuários da carga inicial: atendente, gerente e veterinario (senha 1234). Tutor de exemplo: João Silva, com o animal Rex.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegracaoTest {

    private static final String CPF_JOAO = "529.982.247-25";

    @Autowired
    private MockMvc mvc;

    // ------------------------------------------------------------------ utilitários
    private MockHttpSession login(String usuario) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"" + usuario + "\",\"senha\":\"1234\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private ResultActions postJson(MockHttpSession sessao, String url, String json) throws Exception {
        return mvc.perform(post(url).session(sessao).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions putJson(MockHttpSession sessao, String url, String json) throws Exception {
        return mvc.perform(put(url).session(sessao).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String texto(ResultActions resultado) throws Exception {
        return resultado.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private long idDe(String json, String caminho) {
        return ((Number) JsonPath.read(json, caminho)).longValue();
    }

    private long animalDoJoao(MockHttpSession sessao) throws Exception {
        return idDe(texto(mvc.perform(get("/api/animais").param("cpf", CPF_JOAO).session(sessao))), "$[0].id");
    }

    private long primeiroVeterinario(MockHttpSession sessao) throws Exception {
        return idDe(texto(mvc.perform(get("/api/veterinarios").session(sessao))), "$[0].id");
    }

    private String horario(int diasAFrente, int hora) {
        return LocalDateTime.now().plusDays(diasAFrente).withHour(hora).withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private long agendar(MockHttpSession atendente, int diasAFrente) throws Exception {
        String json = "{\"animalId\":" + animalDoJoao(atendente) + ",\"veterinarioId\":" + primeiroVeterinario(atendente)
                + ",\"dataHora\":\"" + horario(diasAFrente, 9) + "\"}";
        return idDe(texto(postJson(atendente, "/api/consultas", json).andExpect(status().isCreated())), "$.id");
    }

    // ------------------------------------------------------------------ testes
    @Test
    void IT01_loginValidoCriaSessaoComOPerfilDoUsuario() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"atendente\",\"senha\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("ATENDENTE"));
    }

    @Test
    void IT02_loginComSenhaIncorretaRetorna401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"atendente\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Login ou senha incorretos."));
    }

    @Test
    void IT03_acessoSemLoginRetorna401() throws Exception {
        mvc.perform(get("/api/tutores/buscar").param("cpf", CPF_JOAO)).andExpect(status().isUnauthorized());
    }

    @Test
    void IT04_cadastraTutorERejeitaCpfDuplicadoMesmoComMascaraDiferente() throws Exception {
        MockHttpSession atendente = login("atendente");
        String json = "{\"nome\":\"Maria Souza\",\"cpf\":\"390.533.447-05\",\"telefone\":\"(13) 99876-5432\",\"endereco\":\"Rua A, 10\"}";
        postJson(atendente, "/api/tutores", json)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("39053344705"));
        postJson(atendente, "/api/tutores", json.replace("390.533.447-05", "39053344705"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Já existe um tutor cadastrado com este CPF."));
    }

    @Test
    void IT05_cadastraAnimalVinculadoAoTutorERejeitaNomeEmBranco() throws Exception {
        MockHttpSession atendente = login("atendente");
        postJson(atendente, "/api/animais", "{\"nome\":\"Mia\",\"tipo\":\"Gato\",\"raca\":\"SRD\",\"idade\":2,\"cpfTutor\":\"" + CPF_JOAO + "\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tutorNome").value("João Silva"));
        postJson(atendente, "/api/animais", "{\"nome\":\"  \",\"tipo\":\"Gato\",\"raca\":\"SRD\",\"idade\":2,\"cpfTutor\":\"" + CPF_JOAO + "\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void IT06_agendaConsultaFuturaERejeitaDataPassadaEHorarioOcupado() throws Exception {
        MockHttpSession atendente = login("atendente");
        String base = "{\"animalId\":" + animalDoJoao(atendente) + ",\"veterinarioId\":" + primeiroVeterinario(atendente) + ",\"dataHora\":\"";
        String futura = base + horario(10, 9) + "\"}";
        postJson(atendente, "/api/consultas", futura)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"));
        postJson(atendente, "/api/consultas", futura)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Esse veterinário já tem consulta nesse horário."));
        postJson(atendente, "/api/consultas", base + horario(-1, 9) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A data/hora da consulta deve ser futura."));
    }

    @Test
    void IT07_somenteVeterinarioRegistraAtendimentoEConsultaViraRealizada() throws Exception {
        MockHttpSession atendente = login("atendente");
        MockHttpSession veterinario = login("veterinario");
        long consulta = agendar(atendente, 11);
        String registro = "{\"diagnostico\":\"Otite\",\"tratamento\":\"Antibiótico\",\"observacoes\":\"Retorno em 7 dias\"}";
        putJson(atendente, "/api/consultas/" + consulta + "/registro", registro).andExpect(status().isForbidden());
        putJson(veterinario, "/api/consultas/" + consulta + "/registro", registro)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REALIZADA"));
    }

    @Test
    void IT08_registraPagamentoValidoERejeitaValorZerado() throws Exception {
        MockHttpSession atendente = login("atendente");
        MockHttpSession veterinario = login("veterinario");
        long consulta = agendar(atendente, 12);
        putJson(veterinario, "/api/consultas/" + consulta + "/registro", "{\"diagnostico\":\"Vacinação\"}").andExpect(status().isOk());
        postJson(atendente, "/api/pagamentos", "{\"consultaId\":" + consulta
                + ",\"valor\":150.00,\"valorPago\":150.00,\"formaPagamento\":\"PIX\",\"status\":\"PAGO\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAGO"));
        postJson(atendente, "/api/pagamentos", "{\"consultaId\":" + consulta
                + ",\"valor\":0,\"valorPago\":0,\"formaPagamento\":\"PIX\",\"status\":\"PAGO\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O valor do pagamento deve ser maior que zero."));
    }

    @Test
    void IT09_buscaPorNomeParcialDoTutorPorCpfEPorNomeDoAnimal() throws Exception {
        MockHttpSession atendente = login("atendente");
        for (String termo : new String[] {"silva", "529.982", "rex"}) {
            mvc.perform(get("/api/busca").param("termo", termo).session(atendente))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].tutorNome").value("João Silva"))
                    .andExpect(jsonPath("$[0].animalNome").value("Rex"));
        }
    }

    @Test
    void IT10_veterinarioNaoPodeCadastrarTutor() throws Exception {
        MockHttpSession veterinario = login("veterinario");
        postJson(veterinario, "/api/tutores", "{\"nome\":\"Teste\",\"cpf\":\"390.533.447-05\"}")
                .andExpect(status().isForbidden());
    }
}
