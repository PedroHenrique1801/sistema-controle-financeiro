package br.edu.financeiro.modelo;

public class Categoria {
    private final long id;
    private final String nome;

    public Categoria(long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public long getId() { return id; }
    public String getNome() { return nome; }
    @Override public String toString() { return nome; }
}
