package br.edu.financeiro;

import br.edu.financeiro.modelo.*;
import br.edu.financeiro.persistencia.ConexaoBanco;
import br.edu.financeiro.servico.ServicoFinanceiro;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integracao")
class IntegracaoMySqlTest {
    private ConexaoBanco banco;
    private ServicoFinanceiro servico;
    private final List<Long> categoriasCriadas = new ArrayList<>();
    private String prefixo;
    private Categoria trabalho, alimentacao, transporte;

    @BeforeEach void preparar() throws Exception {
        banco = BancoTeste.abrirConfiguracao();
        servico = new ServicoFinanceiro(banco);
        prefixo = "T" + UUID.randomUUID().toString().substring(0, 8) + " ";
        trabalho = categoria("Trabalho");
        alimentacao = categoria("Alimentação");
        transporte = categoria("Transporte");
    }
    private Categoria categoria(String nome) throws Exception {
        long id = servico.cadastrarCategoria(prefixo + nome);
        categoriasCriadas.add(id);
        return new Categoria(id, prefixo + nome);
    }
    private long inserir(String descricao, String valor, int dia, TipoMovimentacao tipo, Categoria categoria) throws Exception {
        return servico.salvar(new Movimentacao(0, descricao, new BigDecimal(valor), LocalDate.of(2026, 11, dia), tipo, categoria));
    }
    private void exemplo() throws Exception {
        inserir("Salário", "3000.00", 5, TipoMovimentacao.RECEITA, trabalho);
        inserir("Mercado", "450.00", 6, TipoMovimentacao.DESPESA, alimentacao);
        inserir("Reembolso", "50.00", 7, TipoMovimentacao.RECEITA, alimentacao);
        inserir("Ônibus", "150.00", 8, TipoMovimentacao.DESPESA, transporte);
    }
    private List<Movimentacao> consultarCategoria(Categoria categoria) throws Exception {
        return servico.consultar(new Filtro(null, null, categoria.getId(), null)).getMovimentacoes();
    }
    @AfterEach void limparSomenteRegistrosCriadosNesteTeste() throws Exception {
        if (banco == null) return;
        try (Connection conexao = banco.abrir()) {
            for (long id : categoriasCriadas) {
                try (PreparedStatement comando = conexao.prepareStatement("DELETE FROM movimentacao WHERE id_categoria = ?")) {
                    comando.setLong(1, id); comando.executeUpdate();
                }
                try (PreparedStatement comando = conexao.prepareStatement("DELETE FROM categoria WHERE id_categoria = ?")) {
                    comando.setLong(1, id); comando.executeUpdate();
                }
            }
        }
    }
    @Test void gravaConsultaEditaEExcluiMantendoIdentificador() throws Exception {
        long id = inserir("Mercado", "450.00", 6, TipoMovimentacao.DESPESA, alimentacao);
        Movimentacao lida = consultarCategoria(alimentacao).getFirst();
        assertEquals(id, lida.getId());
        assertEquals(new BigDecimal("450.00"), lida.getValor());
        assertEquals(id, servico.salvar(new Movimentacao(id, "Mercado revisado", new BigDecimal("400.00"),
            lida.getData(), TipoMovimentacao.DESPESA, alimentacao)));
        assertEquals(new BigDecimal("400.00"), consultarCategoria(alimentacao).getFirst().getValor());
        servico.excluirMovimentacao(id);
        assertTrue(consultarCategoria(alimentacao).isEmpty());
    }
    @Test void reabreConexaoERecuperaRegistroPersistido() throws Exception {
        long id = inserir("Persistência", "0.30", 5, TipoMovimentacao.RECEITA, trabalho);
        ServicoFinanceiro reaberto = new ServicoFinanceiro(BancoTeste.abrirConfiguracao());
        Movimentacao lida = reaberto.consultar(new Filtro(null, null, trabalho.getId(), null)).getMovimentacoes().getFirst();
        assertEquals(id, lida.getId());
        assertEquals(new BigDecimal("0.30"), lida.getValor());
    }
    @Test void filtraCategoriaTipoEDatasInclusivas() throws Exception {
        exemplo();
        assertEquals(new BigDecimal("-400.00"), ServicoFinanceiro.resumir(consultarCategoria(alimentacao)).getSaldo());
        List<Movimentacao> lista = servico.consultar(new Filtro(LocalDate.of(2026, 11, 7), LocalDate.of(2026, 11, 7),
            alimentacao.getId(), TipoMovimentacao.RECEITA)).getMovimentacoes();
        assertEquals(1, lista.size());
        assertEquals("Reembolso", lista.getFirst().getDescricao());
        assertTrue(servico.consultar(new Filtro(LocalDate.of(2026, 11, 8), null, alimentacao.getId(), null)).getMovimentacoes().isEmpty());
    }
    @Test void ordenaDataDecrescenteEDesempataPorId() throws Exception {
        inserir("Anterior", "1.00", 5, TipoMovimentacao.RECEITA, trabalho);
        inserir("Primeiro do dia", "1.00", 8, TipoMovimentacao.RECEITA, trabalho);
        long ultimo = inserir("Último do dia", "1.00", 8, TipoMovimentacao.RECEITA, trabalho);
        List<Movimentacao> lista = consultarCategoria(trabalho);
        assertEquals(ultimo, lista.getFirst().getId());
        assertEquals("Anterior", lista.getLast().getDescricao());
    }
    @Test void bloqueiaDuplicataSemDiferenciarEspacosExternosOuMaiusculas() {
        SQLException erro = assertThrows(SQLException.class, () -> servico.cadastrarCategoria("  " + trabalho.getNome().toUpperCase() + "  "));
        assertEquals(1062, erro.getErrorCode());
    }
    @Test void renomeiaCategoriaSemPerderVinculo() throws Exception {
        long id = inserir("Compra", "20.00", 5, TipoMovimentacao.DESPESA, alimentacao);
        servico.renomearCategoria(alimentacao.getId(), prefixo + "Comida");
        Movimentacao lida = consultarCategoria(alimentacao).getFirst();
        assertEquals(id, lida.getId());
        assertEquals(alimentacao.getId(), lida.getCategoria().getId());
        assertEquals(prefixo + "Comida", lida.getCategoria().getNome());
    }
    @Test void impedeExcluirCategoriaEmUsoEPermiteCategoriaVazia() throws Exception {
        inserir("Compra", "20.00", 5, TipoMovimentacao.DESPESA, alimentacao);
        SQLException erro = assertThrows(SQLException.class, () -> servico.excluirCategoria(alimentacao.getId()));
        assertEquals(1451, erro.getErrorCode());
        servico.excluirCategoria(transporte.getId());
        assertFalse(servico.listarCategorias().stream().anyMatch(item -> item.getId() == transporte.getId()));
    }
    @Test void trataAspasComoDadosEImpedeCategoriaInexistente() throws Exception {
        String texto = "Compra d'água; DROP TABLE categoria; --";
        inserir(texto, "3.00", 5, TipoMovimentacao.DESPESA, alimentacao);
        assertEquals(texto, consultarCategoria(alimentacao).getFirst().getDescricao());
        SQLException erro = assertThrows(SQLException.class, () -> inserir("Inválido", "1.00", 5, TipoMovimentacao.RECEITA,
            new Categoria(Long.MAX_VALUE, "Inexistente")));
        assertEquals(1452, erro.getErrorCode());
    }
    @Test void bancoImpedeValorNegativoMesmoForaDaInterface() throws Exception {
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(
            "INSERT INTO movimentacao (descricao,valor,data,tipo,id_categoria) VALUES ('Inválido',-1,'2026-11-05','DESPESA',?)")) {
            comando.setLong(1, trabalho.getId());
            assertThrows(SQLException.class, comando::executeUpdate);
        }
    }
    @Test void erroDeConexaoNaoViraConsultaVazia() {
        ServicoFinanceiro offline = new ServicoFinanceiro(new ConexaoBanco(
            "jdbc:mysql://127.0.0.1:1/indisponivel_teste?connectTimeout=500&socketTimeout=500", "teste", "teste"));
        assertThrows(SQLException.class, () -> offline.consultar(Filtro.todos()));
    }
}
