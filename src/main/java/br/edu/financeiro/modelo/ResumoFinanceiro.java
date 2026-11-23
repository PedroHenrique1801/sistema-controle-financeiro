package br.edu.financeiro.modelo;

import java.math.BigDecimal;

public class ResumoFinanceiro {
    private BigDecimal receitas = new BigDecimal("0.00");
    private BigDecimal despesas = new BigDecimal("0.00");

    public void adicionar(Movimentacao movimentacao) {
        if (movimentacao.getTipo() == TipoMovimentacao.RECEITA) {
            receitas = receitas.add(movimentacao.getValor());
        } else {
            despesas = despesas.add(movimentacao.getValor());
        }
    }
    public BigDecimal getReceitas() { return receitas; }
    public BigDecimal getDespesas() { return despesas; }
    public BigDecimal getSaldo() { return receitas.subtract(despesas); }
}
