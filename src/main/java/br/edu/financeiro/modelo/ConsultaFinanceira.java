package br.edu.financeiro.modelo;

import java.util.List;

/** Um resultado completo; a tela so e atualizada quando as duas consultas terminam. */
public class ConsultaFinanceira {
    private final List<Categoria> categorias;
    private final List<Movimentacao> movimentacoes;

    public ConsultaFinanceira(List<Categoria> categorias, List<Movimentacao> movimentacoes) {
        this.categorias = List.copyOf(categorias);
        this.movimentacoes = List.copyOf(movimentacoes);
    }
    public List<Categoria> getCategorias() { return categorias; }
    public List<Movimentacao> getMovimentacoes() { return movimentacoes; }
}
