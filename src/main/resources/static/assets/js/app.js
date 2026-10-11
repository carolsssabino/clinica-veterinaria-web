/* Lógica de tela do front-end (Etapa 9).
   As páginas da Etapa 8 agora conversam com a API REST do back-end Spring (/api/**) em vez de usar
   localStorage. As validações de tela (validacoes.js) continuam; o servidor repete as regras e devolve
   {"erro": "mensagem"} quando algo é recusado. */
(function () {
  const V = Validacoes;
  const $ = (id) => document.getElementById(id);
  const esc = (s) => String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const dataBR = (iso) => iso.split('-').reverse().join('/');
  const dataHoraBR = (dt) => dataBR(dt.slice(0, 10)) + ' ' + dt.slice(11, 16);
  const moeda = (n) => 'R$ ' + Number(n).toFixed(2).replace('.', ',');
  const hojeISO = () => { const d = new Date(); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); };

  /* ---- acesso à API (sessão por cookie; 401 volta para o login) ---- */
  async function api(metodo, url, corpo) {
    let resp;
    try {
      resp = await fetch(url, {
        method: metodo,
        headers: corpo ? { 'Content-Type': 'application/json' } : {},
        body: corpo ? JSON.stringify(corpo) : undefined,
        credentials: 'same-origin',
      });
    } catch (e) { throw new Error('Não foi possível conectar ao servidor.'); }
    let dados = null;
    try { dados = await resp.json(); } catch (e) { /* resposta sem corpo (204) */ }
    if (resp.status === 401 && document.body.dataset.pagina !== 'login') {
      location.href = 'login.html';
      throw new Error('Sessão expirada. Faça login novamente.');
    }
    if (!resp.ok) throw new Error((dados && dados.erro) || 'Erro no servidor (' + resp.status + ').');
    return dados;
  }

  function aviso(msg, ok) {
    const a = $('aviso'); if (!a) return;
    a.textContent = msg; a.className = 'aviso ' + (ok ? 'ok' : 'falha');
  }

  /* ---- validação genérica via atributo data-validar="regra1 regra2" ---- */
  function validarCampo(el) {
    const campo = el.closest('.campo'); if (!campo) return true;
    let msg = '';
    for (const r of (el.dataset.validar || '').split(' ').filter(Boolean)) {
      if (r === 'cpf' && !el.value.trim()) continue;
      msg = V.regra(r, el); if (msg) break;
    }
    campo.classList.toggle('invalido', !!msg);
    const e = campo.querySelector('.erro'); if (e) e.textContent = msg;
    return !msg;
  }
  function validarForm(form) {
    let ok = true;
    form.querySelectorAll('[data-validar]').forEach((el) => { if (!validarCampo(el)) ok = false; });
    return ok;
  }
  function ligarForm(form) {
    form.noValidate = true;
    form.querySelectorAll('[data-mascara]').forEach((el) => el.addEventListener('input', () => {
      el.value = el.dataset.mascara === 'cpf' ? V.mascaraCpf(el.value) : V.mascaraTelefone(el.value);
    }));
    form.querySelectorAll('[data-validar]').forEach((el) => el.addEventListener('blur', () => validarCampo(el)));
  }
  const preencher = (sel, itens, rotulo = 'Selecione') => {
    sel.innerHTML = '<option value="">' + esc(rotulo) + '</option>' + itens.map((i) => `<option value="${esc(i.v)}">${esc(i.t)}</option>`).join('');
  };

  /* ---- busca de tutor por CPF (botão BUSCAR) usada em 3 telas ---- */
  function ligarBuscaTutor(aoEncontrar) {
    $('btn-buscar').addEventListener('click', async () => {
      const cpf = $('cpf');
      if (!validarCampo(cpf)) return;
      try {
        const t = await api('GET', '/api/tutores/buscar?cpf=' + encodeURIComponent(V.soDigitos(cpf.value)));
        aviso('Tutor encontrado: ' + t.nome, true);
        await aoEncontrar(t);
      } catch (e) { aviso(e.message, false); }
    });
  }

  const paginas = {
    login() {
      const f = $('form-login'); ligarForm(f);
      f.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validarForm(f)) return;
        try {
          await api('POST', '/api/auth/login', { login: f.elements.usuario.value.trim(), senha: f.elements.senha.value });
          location.href = 'index.html';
        } catch (err) { aviso(err.message, false); }
      });
    },
    inicio(usuario) {
      // O menu mostra só o que o perfil pode usar (o servidor também confere: HTTP 403).
      document.querySelectorAll('[data-perfis]').forEach((a) => {
        if (usuario.tipo !== 'GERENTE' && !a.dataset.perfis.split(' ').includes(usuario.tipo)) a.style.display = 'none';
      });
      $('sair').addEventListener('click', async (e) => {
        e.preventDefault();
        try { await api('POST', '/api/auth/logout'); } finally { location.href = 'login.html'; }
      });
    },
    tutor() {
      const f = $('form-tutor'); ligarForm(f);
      f.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validarForm(f)) { aviso('Corrija os campos destacados.', false); return; }
        try {
          await api('POST', '/api/tutores', { nome: f.elements.nome.value.trim(), cpf: f.elements.cpf.value, telefone: f.elements.telefone.value, endereco: f.elements.endereco.value.trim() });
          f.reset(); aviso('Tutor salvo com sucesso!', true);
        } catch (err) { aviso(err.message, false); }
      });
    },
    animal() {
      const f = $('form-animal'); ligarForm(f);
      f.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validarForm(f)) { aviso('Corrija os campos destacados.', false); return; }
        try {
          const a = await api('POST', '/api/animais', { nome: f.elements.nome.value.trim(), tipo: f.elements.tipo.value.trim(), raca: f.elements.raca.value.trim(), idade: Number(f.elements.idade.value), cpfTutor: f.elements.cpf.value });
          f.reset(); aviso('Animal de ' + a.tutorNome + ' salvo com sucesso!', true);
        } catch (err) { aviso(err.message, false); }
      });
    },
    async agendamento() {
      const f = $('form-agendamento'); ligarForm(f);
      f.elements.data.min = hojeISO();
      try {
        const vets = await api('GET', '/api/veterinarios');
        preencher(f.elements.veterinario, vets.map((v) => ({ v: v.id, t: v.nome + ' (' + v.crmv + ')' })));
      } catch (err) { aviso(err.message, false); }
      ligarBuscaTutor(async (t) => {
        const animais = await api('GET', '/api/animais?cpf=' + encodeURIComponent(t.cpf));
        preencher(f.elements.animal, animais.map((a) => ({ v: a.id, t: a.nome + ' (' + a.tipo + ')' })), animais.length ? 'Selecione' : 'Tutor sem animais');
      });
      f.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validarForm(f)) { aviso('Corrija os campos destacados.', false); return; }
        const data = f.elements.data.value, hora = f.elements.hora.value;
        if (new Date(data + 'T' + hora + ':00') <= new Date()) { aviso('A data/hora da consulta deve ser futura.', false); return; }
        try {
          await api('POST', '/api/consultas', { animalId: Number(f.elements.animal.value), veterinarioId: Number(f.elements.veterinario.value), dataHora: data + 'T' + hora + ':00' });
          f.reset(); preencher(f.elements.animal, [], 'Busque o CPF do tutor');
          aviso('Consulta agendada para ' + dataBR(data) + ' às ' + hora + '.', true);
        } catch (err) { aviso(err.message, false); }
      });
    },
    registro() {
      const f = $('form-consulta'); ligarForm(f);
      ligarBuscaTutor(async (t) => {
        const cs = await api('GET', '/api/consultas?status=AGENDADA&cpf=' + encodeURIComponent(t.cpf));
        preencher(f.elements.animal, cs.map((c) => ({ v: c.id, t: c.animalNome + ' — ' + dataHoraBR(c.dataHora) })), cs.length ? 'Selecione' : 'Nenhuma consulta agendada');
      });
      f.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validarForm(f)) { aviso('Corrija os campos destacados.', false); return; }
        try {
          await api('PUT', '/api/consultas/' + f.elements.animal.value + '/registro', { diagnostico: f.elements.diagnostico.value.trim(), tratamento: f.elements.tratamento.value.trim(), observacoes: f.elements.observacoes.value.trim() });
          f.reset(); preencher(f.elements.animal, [], 'Busque o CPF do tutor');
          aviso('Consulta registrada como REALIZADA.', true);
        } catch (err) { aviso(err.message, false); }
      });
    },
    pagamentos() {
      const f = $('form-pagamento'); ligarForm(f);
      let cpfAtual = '';
      ligarBuscaTutor(async (t) => {
        cpfAtual = t.cpf;
        const animais = await api('GET', '/api/animais?cpf=' + encodeURIComponent(t.cpf));
        preencher(f.elements.animal, animais.map((a) => ({ v: a.id, t: a.nome })), animais.length ? 'Selecione' : 'Tutor sem animais');
        preencher(f.elements.consulta, [], 'Selecione o animal');
      });
      f.elements.animal.addEventListener('change', async () => {
        if (!f.elements.animal.value) { preencher(f.elements.consulta, [], 'Selecione o animal'); return; }
        try {
          const cs = await api('GET', '/api/consultas?status=REALIZADA&cpf=' + encodeURIComponent(cpfAtual) + '&animalId=' + encodeURIComponent(f.elements.animal.value));
          preencher(f.elements.consulta, cs.map((c) => ({ v: c.id, t: dataHoraBR(c.dataHora) })), cs.length ? 'Selecione' : 'Sem consulta realizada');
        } catch (err) { aviso(err.message, false); }
      });
      f.addEventListener('submit', async (e) => {
        e.preventDefault();
        let ok = validarForm(f);
        const forma = f.querySelector('input[name=forma]:checked');
        $('erro-forma').textContent = forma ? '' : 'Escolha a forma de pagamento.'; if (!forma) ok = false;
        const valor = V.numero(f.elements.valor.value), pago = V.numero(f.elements.valorPago.value);
        if (ok && f.elements.status.value === 'PAGO' && !(pago >= valor)) {
          const c = f.elements.valorPago.closest('.campo'); c.classList.add('invalido');
          c.querySelector('.erro').textContent = 'Status PAGO exige valor pago igual ou maior que o valor.'; ok = false;
        }
        if (!ok) { aviso('Corrija os campos destacados.', false); return; }
        try {
          await api('POST', '/api/pagamentos', { consultaId: Number(f.elements.consulta.value), valor, valorPago: isNaN(pago) ? 0 : pago, formaPagamento: forma.value, status: f.elements.status.value });
          f.reset(); aviso('Pagamento de ' + moeda(valor) + ' (' + forma.value + ') salvo.', true);
        } catch (err) { aviso(err.message, false); }
      });
    },
    busca() {
      const f = $('form-busca'), saida = $('resultados');
      let timer = null, seq = 0;
      async function buscar() {
        const q = f.elements.termo.value.trim(), minha = ++seq;
        if (!q) { saida.innerHTML = '<h2>Resultados:</h2><p>Digite um nome ou CPF.</p>'; return; }
        let html;
        try {
          const itens = await api('GET', '/api/busca?termo=' + encodeURIComponent(q));
          html = !itens.length ? '<p>Nada encontrado.</p>' :
            itens.map((i) => `<div class="item">Tutor: ${esc(i.tutorNome)}<br>Animal: ${i.animalNome ? esc(i.animalNome) : '—'}<br>Telefone: ${esc(i.telefone || '—')}</div>`).join('');
        } catch (err) { html = '<p>' + esc(err.message) + '</p>'; }
        if (minha === seq) saida.innerHTML = '<h2>Resultados:</h2>' + html; // ignora respostas antigas
      }
      f.addEventListener('submit', (e) => { e.preventDefault(); buscar(); });
      f.elements.termo.addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(buscar, 250); });
      buscar();
    },
  };

  document.addEventListener('DOMContentLoaded', async () => {
    const p = document.body.dataset.pagina;
    if (!paginas[p]) return;
    if (p === 'login') { paginas.login(); return; }
    let usuario;
    try { usuario = await api('GET', '/api/auth/me'); } catch (e) { return; } // 401 já redireciona ao login
    paginas[p](usuario);
  });
})();
