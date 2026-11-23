package br.edu.financeiro;

import br.edu.financeiro.modelo.*;
import br.edu.financeiro.servico.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class CalculosEValidacoesTest {
    private final Categoria trabalho = new Categoria(1, "Trabalho");
    private final Categoria alimentacao = new Categoria(2, "Alimentação");
    private final Categoria transporte = new Categoria(3, "Transporte");

    private Movimentacao movimento(String valor, TipoMovimentacao tipo, Categoria categoria) {
        return new Movimentacao(0, "Teste", new BigDecimal(valor), LocalDate.of(2026, 11, 5), tipo, categoria);
    }
    private List<Movimentacao> exemplo() {
        List<Movimentacao> lista = new ArrayList<>();
        lista.add(movimento("3000.00", TipoMovimentacao.RECEITA, trabalho));
        lista.add(movimento("450.00", TipoMovimentacao.DESPESA, alimentacao));
        lista.add(movimento("50.00", TipoMovimentacao.RECEITA, alimentacao));
        lista.add(movimento("150.00", TipoMovimentacao.DESPESA, transporte));
        return lista;
    }
    @Test void calculaExemploDoProjeto() {
        ResumoFinanceiro resumo = ServicoFinanceiro.resumir(exemplo());
        assertEquals(new BigDecimal("3050.00"), resumo.getReceitas());
        assertEquals(new BigDecimal("600.00"), resumo.getDespesas());
        assertEquals(new BigDecimal("2450.00"), resumo.getSaldo());
        assertEquals(new BigDecimal("-400.00"), ServicoFinanceiro.resumirCategoria(exemplo(), 2).getSaldo());
        assertEquals(new BigDecimal("0.00"), ServicoFinanceiro.resumirCategoria(exemplo(), 99).getSaldo());
    }
    @Test void somaCentavosSemErroDePontoFlutuante() {
        List<Movimentacao> lista = List.of(movimento("0.10", TipoMovimentacao.RECEITA, trabalho),
            movimento("0.20", TipoMovimentacao.RECEITA, trabalho));
        assertEquals(new BigDecimal("0.30"), ServicoFinanceiro.resumir(lista).getSaldo());
    }
    @Test void listaVaziaTemTotaisZero() {
        ResumoFinanceiro resumo = ServicoFinanceiro.resumir(List.of());
        assertEquals(new BigDecimal("0.00"), resumo.getReceitas());
        assertEquals(new BigDecimal("0.00"), resumo.getDespesas());
        assertEquals(new BigDecimal("0.00"), resumo.getSaldo());
    }
    @ParameterizedTest @ValueSource(strings = {"0", "-1", "1.001", "10000000000.00"})
    void rejeitaValorInvalido(String valor) {
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validar(movimento(valor, TipoMovimentacao.RECEITA, trabalho)));
    }
    @Test void aceitaLimiteMonetarioEUmCentavo() {
        assertDoesNotThrow(() -> ServicoFinanceiro.validar(movimento("9999999999.99", TipoMovimentacao.RECEITA, trabalho)));
        assertDoesNotThrow(() -> ServicoFinanceiro.validar(movimento("0.01", TipoMovimentacao.DESPESA, trabalho)));
    }
    @Test void exigeCamposObrigatorios() {
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validar(null));
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validar(new Movimentacao(0, " ", BigDecimal.ONE, LocalDate.now(), TipoMovimentacao.RECEITA, trabalho)));
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validar(movimento("1", null, trabalho)));
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validar(movimento("1", TipoMovimentacao.RECEITA, null)));
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validar(new Movimentacao(0, "x".repeat(151), BigDecimal.ONE, LocalDate.now(), TipoMovimentacao.RECEITA, trabalho)));
    }
    @Test void rejeitaLancamentoFuturoMasPermiteFiltroFuturo() {
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validar(new Movimentacao(0, "Teste", BigDecimal.ONE,
            LocalDate.now().plusDays(1), TipoMovimentacao.RECEITA, trabalho)));
        assertDoesNotThrow(() -> new Filtro(null, LocalDate.now().plusDays(1), null, null));
    }
    @ParameterizedTest @ValueSource(strings = {"31/02/2026", "29/02/2025", "2026-11-05", "5/11/2026", "00/11/2026", ""})
    void rejeitaDatasInexistentesOuFormatoErrado(String valor) {
        assertThrows(IllegalArgumentException.class, () -> Formatos.lerData(valor));
    }
    @Test void leDataBissextaEDataOpcional() {
        assertEquals(LocalDate.of(2024, 2, 29), Formatos.lerData("29/02/2024"));
        assertNull(Formatos.lerDataOpcional(" "));
    }
    @Test void rejeitaIntervaloInvertido() {
        assertThrows(IllegalArgumentException.class, () -> new Filtro(LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 7), null, null));
    }
    @ParameterizedTest @ValueSource(strings = {"1.000,00", "1,000.00", "R$ 10", "1,234", "NaN", "-1", "1e2", ""})
    void rejeitaEntradaMonetariaAmbigua(String valor) {
        assertThrows(IllegalArgumentException.class, () -> Formatos.lerValor(valor));
    }
    @Test void aceitaVirgulaOuPontoSemSeparadorDeMilhar() {
        assertEquals(new BigDecimal("1250.50"), Formatos.lerValor("1250,50"));
        assertEquals(new BigDecimal("1250.50"), Formatos.lerValor("1250.50"));
    }
    @Test void validaNomeDaCategoria() {
        assertEquals("Alimentação", ServicoFinanceiro.validarNomeCategoria("  Alimentação  "));
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validarNomeCategoria(" "));
        assertThrows(IllegalArgumentException.class, () -> ServicoFinanceiro.validarNomeCategoria("x".repeat(61)));
    }
}
