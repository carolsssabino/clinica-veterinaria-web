# ClinicaVeterinaria-Web — Projeto Integrador, Etapa 9

Aluna: Carolina Sabino de Araujo

Versão **web** do sistema de Clínica Veterinária: back-end **Spring Boot (REST) + JPA/Hibernate + MySQL**,
com o front-end da Etapa 8 (HTML/CSS/JS) servido pela própria aplicação e integrado à API.

## Como o projeto reúne as etapas anteriores
| Etapa | O que foi aproveitado |
|---|---|
| 6 | Modelo (`Tutor`, `Animal`, `Veterinario`, `Consulta`, `Pagamento`, `Usuario`, enums), camada `service` e exceções de negócio. Os DAOs JDBC viraram repositórios Spring Data JPA. |
| 7 | `ValidadorCpf` e os testes T01–T10 do plano de testes (migrados para JUnit 5), mais os testes previstos para a versão web (integração dos endpoints, sessão, banco de teste separado). |
| 8 | Páginas HTML/CSS/JS em `src/main/resources/static`. O `app.js` deixou de usar `localStorage` e passou a chamar a API (`/api/**`). |

## Tecnologias
Java 17 ou superior · Spring Boot 3.5.0 · Spring Data JPA · MySQL 8 (H2 para demonstração e testes) · BCrypt ·
JUnit 5 · Mockito · MockMvc · Maven · NetBeans · GitHub Actions.
Funciona com JDK 17 a 25 (o `pom.xml` já libera o Byte Buddy para JDKs novos, como o 25 que vem no NetBeans 28).
Se o MySQL tiver senha no usuário root, crie a variável de ambiente `DB_SENHA` (e `DB_USUARIO`, se não for root).

## Como executar
1. **NetBeans**: *File > Open Project* e escolha a pasta `ClinicaVeterinaria-Web` (o Maven baixa as dependências).
2. **Com MySQL** (como nas etapas 6 e 7): deixe o MySQL ligado em `localhost:3306`. O banco `clinicaveterinaria_web`
   e as tabelas são criados automaticamente. Usuário/senha: variáveis de ambiente `DB_USUARIO` (padrão `root`) e
   `DB_SENHA` (padrão vazio) — não grave senha no repositório. Depois: *Run* (F6).
3. **Sem MySQL** (demonstração rápida): botão direito no projeto > *Custom* > *run (perfil H2, sem MySQL)*, ou no terminal
   `mvn spring-boot:run -Dspring-boot.run.profiles=h2`. Os dados ficam em `./data` (fora do Git).
4. Abra <http://localhost:8080>. Usuários de demonstração (senha `1234`): `atendente`, `gerente`, `veterinario`.
   Já existem o tutor João Silva (CPF 529.982.247-25) e o animal Rex.

## Testes
`mvn test` (NetBeans: botão direito no projeto > *Test*). Usam banco **H2 em memória**, separado do banco de produção.
- **30 testes unitários** (`T01`–`T30`): `ValidadorCpfTest`, `TutorServiceTest`, `AnimalServiceTest`, `ConsultaServiceTest`, `PagamentoServiceTest`, `AutenticacaoServiceTest`.
- **10 testes de integração** (`IT01`–`IT10`): `ApiIntegracaoTest` (MockMvc + JPA + H2, com sessão e perfis).
- O GitHub Actions (`.github/workflows/ci.yml`) roda `mvn test` a cada push.

## Estrutura
```
src/main/java/clinicaveterinaria/
  ClinicaVeterinariaApplication.java   ponto de entrada
  model/ (+ enums/)                    entidades JPA (Etapa 6)
  repository/                          Spring Data JPA (substitui os DAOs JDBC)
  service/                             regras de negócio
  controller/                          API REST + tratamento de erros (JSON {"erro": "..."})
  dto/                                 objetos de entrada/saída da API
  security/                            sessão do usuário, interceptor 401 e conferência de perfil (403)
  config/                              BCrypt, interceptor e carga inicial de dados
  util/ValidadorCpf.java               validação de CPF (Etapa 7)
src/main/resources/static/             páginas da Etapa 8 integradas à API
src/test/java/                         testes unitários e de integração
```

## Rastreabilidade: requisitos -> tela -> API -> perfil -> testes
| Requisito | Tela (Etapa 8) | Endpoint | Perfil | Testes |
|---|---|---|---|---|
| RF001 Cadastro de animais | `animal.html` | `POST /api/animais`, `GET /api/animais?cpf=` | Atendente (e Gerente) | T12–T14, IT05 |
| RF002 Cadastro de tutores | `tutor.html` | `POST /api/tutores`, `GET /api/tutores/buscar?cpf=` | Atendente (e Gerente) | T01–T11, T28, IT04, IT10 |
| RF003 Agendamento de consultas | `agendamento.html` | `POST /api/consultas`, `GET /api/veterinarios` | Atendente (e Gerente) | T15–T17, IT06 |
| RF004 Registro de consultas | `consulta.html` | `PUT /api/consultas/{id}/registro` | Veterinário (e Gerente) | T18, T19, T29, IT07 |
| RF005 Controle de pagamentos | `pagamentos.html` | `POST /api/pagamentos`, `GET /api/pagamentos/pendentes` | Atendente (e Gerente) | T20–T24, T30, IT08 |
| RF006 Sistema de busca | `busca.html` | `GET /api/busca?termo=` | Atendente e Veterinário | IT09 |
| RNF003 Segurança e controle de acesso | `login.html` | `POST /api/auth/login`, `/logout`, `GET /api/auth/me` | todos | T25–T27, IT01–IT03, IT07, IT10 |

Regras de perfil (documento de requisitos, seção 3): o **Gerente** acessa tudo; o **Atendente** cadastra tutores/animais,
agenda consultas e controla pagamentos; o **Veterinário** registra consultas. Fora do perfil, a API responde **403**;
sem login, **401**. O menu da tela inicial mostra apenas o que o perfil pode usar.

## Defeitos encontrados na integração e corrigidos
Registrados como BUG-001 a BUG-010 no documento de evidências de testes.
CPF duplicado com máscara diferente · pagamento sempre gravado como PAGO · conflito de horário só no JavaScript ·
"hoje" com horário passado aceito pela tela · data mínima calculada em UTC · senha em texto puro (agora BCrypt) ·
tipo do animal não exigido no servidor · XSS na busca (agora com escape) · perfis de acesso não aplicados ·
consulta já realizada podia ser registrada de novo e pagamento duplicado/sem consulta realizada.

## Limitações conhecidas
- Edição/cancelamento de consultas e atualização de dados de animais (RF001/RF003), cadastro de usuários e relatórios do Gerente
  não tinham tela na Etapa 8 e não foram incluídos. (`Consulta.cancelar()` existe no modelo.)
- Sem HTTPS e sem token CSRF (a API só aceita JSON e o cookie de sessão usa `SameSite=Lax`); senhas de demonstração `1234`.
- Remova/altere os usuários de demonstração (`config/DadosIniciais.java`) antes de qualquer uso real.

## Versionamento
Repositório novo, separado do repositório das etapas 6 e 7. Commits no padrão `tipo: descrição`
(`chore`, `feat`, `fix`, `test`, `docs`), referenciando as issues do GitHub (`Closes #n`). Detalhes no documento de evidências de versionamento.
