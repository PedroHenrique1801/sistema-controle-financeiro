package br.edu.financeiro.modelo;

public enum TipoMovimentacao {
    RECEITA("Receita"), DESPESA("Despesa");

    private final String rotulo;
    TipoMovimentacao(String rotulo) { this.rotulo = rotulo; }
    @Override public String toString() { return rotulo; }
}
