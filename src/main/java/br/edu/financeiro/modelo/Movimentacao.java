package br.edu.financeiro.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Movimentacao {
    private final long id;
    private final String descricao;
    private final BigDecimal valor;
    private final LocalDate data;
    private final TipoMovimentacao tipo;
    private final Categoria categoria;

    public Movimentacao(long id, String descricao, BigDecimal valor, LocalDate data,
                        TipoMovimentacao tipo, Categoria categoria) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.categoria = categoria;
    }
    public long getId() { return id; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getData() { return data; }
    public TipoMovimentacao getTipo() { return tipo; }
    public Categoria getCategoria() { return categoria; }
}
