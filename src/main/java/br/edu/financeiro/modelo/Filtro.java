package br.edu.financeiro.modelo;

import java.time.LocalDate;

public class Filtro {
    private final LocalDate inicio;
    private final LocalDate fim;
    private final Long categoriaId;
    private final TipoMovimentacao tipo;

    public Filtro(LocalDate inicio, LocalDate fim, Long categoriaId, TipoMovimentacao tipo) {
        if (inicio != null && (inicio.getYear() < 1000 || inicio.getYear() > 9999)
                || fim != null && (fim.getYear() < 1000 || fim.getYear() > 9999))
            throw new IllegalArgumentException("Use datas entre os anos 1000 e 9999.");
        if (inicio != null && fim != null && inicio.isAfter(fim))
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        if (categoriaId != null && categoriaId <= 0)
            throw new IllegalArgumentException("Selecione uma categoria válida.");
        this.inicio = inicio;
        this.fim = fim;
        this.categoriaId = categoriaId;
        this.tipo = tipo;
    }
    public static Filtro todos() { return new Filtro(null, null, null, null); }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public Long getCategoriaId() { return categoriaId; }
    public TipoMovimentacao getTipo() { return tipo; }
}
