package br.edu.financeiro.servico;

import br.edu.financeiro.modelo.*;
import br.edu.financeiro.persistencia.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ServicoFinanceiro {
    private final CategoriaDAO categorias;
    private final MovimentacaoDAO movimentacoes;

    public ServicoFinanceiro(ConexaoBanco banco) {
        categorias = new CategoriaDAO(banco);
        movimentacoes = new MovimentacaoDAO(banco);
    }
    public static void validar(Movimentacao movimentacao) {
        if (movimentacao == null) throw new IllegalArgumentException("Informe a movimentação.");
        if (movimentacao.getId() < 0) throw new IllegalArgumentException("Identificador inválido.");
        String descricao = movimentacao.getDescricao();
        if (descricao == null || descricao.isBlank() || descricao.strip().length() > 150)
            throw new IllegalArgumentException("A descrição deve ter entre 1 e 150 caracteres.");
        BigDecimal valor = movimentacao.getValor();
        if (valor == null || valor.signum() <= 0 || valor.compareTo(new BigDecimal("9999999999.99")) > 0)
            throw new IllegalArgumentException("O valor deve ser maior que zero e no máximo 9.999.999.999,99.");
        try { valor.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException erro) { throw new IllegalArgumentException("O valor deve ter no máximo duas casas decimais."); }
        LocalDate data = movimentacao.getData();
        if (data == null || data.getYear() < 1000 || data.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Informe uma data entre 01/01/1000 e hoje. Cadastre apenas movimentações realizadas.");
        if (movimentacao.getTipo() == null) throw new IllegalArgumentException("Selecione receita ou despesa.");
        if (movimentacao.getCategoria() == null || movimentacao.getCategoria().getId() <= 0)
            throw new IllegalArgumentException("Selecione uma categoria.");
    }
    public long salvar(Movimentacao movimentacao) throws SQLException {
        validar(movimentacao);
        Movimentacao normalizada = new Movimentacao(movimentacao.getId(), movimentacao.getDescricao().strip(),
            movimentacao.getValor().setScale(2, RoundingMode.UNNECESSARY), movimentacao.getData(),
            movimentacao.getTipo(), movimentacao.getCategoria());
        if (normalizada.getId() == 0) return movimentacoes.inserir(normalizada);
        movimentacoes.atualizar(normalizada);
        return normalizada.getId();
    }
    public void excluirMovimentacao(long id) throws SQLException {
        validarId(id);
        movimentacoes.excluir(id);
    }
    public ConsultaFinanceira consultar(Filtro filtro) throws SQLException {
        if (filtro == null) throw new IllegalArgumentException("Informe os filtros da consulta.");
        // Nenhum resultado parcial e entregue se uma das consultas falhar.
        List<Categoria> listaCategorias = categorias.listar();
        List<Movimentacao> listaMovimentacoes = movimentacoes.listar(filtro);
        return new ConsultaFinanceira(listaCategorias, listaMovimentacoes);
    }
    public List<Categoria> listarCategorias() throws SQLException { return categorias.listar(); }
    public long cadastrarCategoria(String nome) throws SQLException {
        return categorias.inserir(validarNomeCategoria(nome));
    }
    public void renomearCategoria(long id, String nome) throws SQLException {
        validarId(id);
        categorias.renomear(id, validarNomeCategoria(nome));
    }
    public void excluirCategoria(long id) throws SQLException {
        validarId(id);
        categorias.excluir(id);
    }
    public static String validarNomeCategoria(String nome) {
        if (nome == null || nome.isBlank() || nome.strip().length() > 60)
            throw new IllegalArgumentException("O nome da categoria deve ter entre 1 e 60 caracteres.");
        return nome.strip();
    }
    private static void validarId(long id) {
        if (id <= 0) throw new IllegalArgumentException("Selecione um registro válido.");
    }
    public static ResumoFinanceiro resumir(List<Movimentacao> lista) {
        ResumoFinanceiro resumo = new ResumoFinanceiro();
        for (Movimentacao movimentacao : lista) resumo.adicionar(movimentacao);
        return resumo;
    }
    public static ResumoFinanceiro resumirCategoria(List<Movimentacao> lista, long categoriaId) {
        ResumoFinanceiro resumo = new ResumoFinanceiro();
        for (Movimentacao movimentacao : lista) {
            if (movimentacao.getCategoria().getId() == categoriaId) resumo.adicionar(movimentacao);
        }
        return resumo;
    }
}
