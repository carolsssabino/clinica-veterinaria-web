/* Validações e máscaras reutilizáveis (espelham as regras do back-end da Etapa 7) */
const Validacoes = {
  soDigitos: (v) => String(v || '').replace(/\D/g, ''),
  cpfValido(v) {
    const c = this.soDigitos(v);
    if (c.length !== 11 || /^(\d)\1{10}$/.test(c)) return false;
    for (const t of [9, 10]) {
      let s = 0;
      for (let i = 0; i < t; i++) s += Number(c[i]) * (t + 1 - i);
      if (((s * 10) % 11) % 10 !== Number(c[t])) return false;
    }
    return true;
  },
  telefoneValido(v) { const n = this.soDigitos(v).length; return n === 10 || n === 11; },
  dataFutura(iso) {
    if (!iso) return false;
    const [a, m, d] = iso.split('-').map(Number);
    const hoje = new Date(); hoje.setHours(0, 0, 0, 0);
    return new Date(a, m - 1, d) >= hoje;
  },
  numero(v) { return parseFloat(String(v).replace(/\./g, '').replace(',', '.')); },
  mascaraCpf(v) {
    return this.soDigitos(v).slice(0, 11).replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  },
  mascaraTelefone(v) {
    const d = this.soDigitos(v).slice(0, 11);
    if (d.length <= 2) return d ? '(' + d : '';
    if (d.length <= 6) return d.replace(/(\d{2})(\d+)/, '($1) $2');
    if (d.length <= 10) return d.replace(/(\d{2})(\d{4})(\d+)/, '($1) $2-$3');
    return d.replace(/(\d{2})(\d{5})(\d+)/, '($1) $2-$3');
  },
  regra(nome, el) {
    const v = el.value.trim();
    switch (nome) {
      case 'obrigatorio': return v ? '' : 'Campo obrigatório.';
      case 'selecao': return v ? '' : 'Selecione uma opção.';
      case 'cpf': return this.cpfValido(v) ? '' : 'CPF inválido.';
      case 'telefone': return this.telefoneValido(v) ? '' : 'Telefone inválido. Use DDD + número.';
      case 'inteiro': return /^\d+$/.test(v) ? '' : 'Informe um número inteiro maior ou igual a 0.';
      case 'data-futura': return this.dataFutura(v) ? '' : 'Escolha uma data de hoje em diante.';
      case 'valor': return this.numero(v) > 0 ? '' : 'Informe um valor maior que zero.';
      case 'valor-pago': return this.numero(v) >= 0 ? '' : 'Informe o valor pago.';
    }
    return '';
  },
};
