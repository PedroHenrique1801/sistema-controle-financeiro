package br.edu.financeiro;

import br.edu.financeiro.modelo.*;
import br.edu.financeiro.persistencia.ConexaoBanco;
import br.edu.financeiro.servico.ServicoFinanceiro;
import br.edu.financeiro.ui.*;
import java.awt.*;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Aciona os componentes reais do Swing e confere os resultados no MySQL. */
@Tag("interface")
class InterfaceTest {
    private JFrame janela;
    private final List<String> evidencias = new ArrayList<>();
    private final Path pasta = Path.of("target/evidencias");

    @Test void fluxoCompletoNaInterfaceComBancoReal() throws Exception {
        assertFalse(GraphicsEnvironment.isHeadless(), "O teste de interface precisa de uma sessão gráfica ou Xvfb.");
        ConexaoBanco original = BancoTeste.abrirConfiguracao();
        ConexaoInterrompivel banco = new ConexaoInterrompivel(original);
        ServicoFinanceiro servico = new ServicoFinanceiro(banco);
        assertTrue(servico.listarCategorias().isEmpty(), "Use um banco _teste vazio e exclusivo para o teste de interface.");
        Files.createDirectories(pasta);
        List<Long> categoriasCriadas = new ArrayList<>();
        try {
            abrir(servico);
            atualizada();
            clicar("gerenciarCategorias");
            esperar(() -> componente("cadastrarCategoria") != null && componente("cadastrarCategoria").isEnabled());
            for (String nome : new String[]{"Trabalho", "Alimentação", "Transporte", "Lazer"}) {
                texto("nomeCategoria", nome);
                clicar("cadastrarCategoria");
                esperar(() -> rotulo("estadoCategorias").contains("Categoria cadastrada.") && componente("cadastrarCategoria").isEnabled());
            }
            categoriasCriadas.addAll(servico.listarCategorias().stream().map(Categoria::getId).toList());
            marcarLinha("listaCategorias", "Lazer");
            texto("nomeCategoria", "Outros");
            clicar("renomearCategoria");
            esperar(() -> rotulo("estadoCategorias").contains("Categoria renomeada.") && componente("renomearCategoria") != null && componente("atualizarCategorias").isEnabled());
            marcarLinha("listaCategorias", "Outros");
            clicar("excluirCategoria");
            confirmar("Sim");
            esperar(() -> rotulo("estadoCategorias").contains("Categoria excluída.") && componente("atualizarCategorias").isEnabled());
            clicar("fecharCategorias");
            atualizada();
            registrar("Categorias: cadastro, renomeação e exclusão de categoria vazia pela interface.");

            lancar("Salário", "3000,00", "05/11/2026", "Receita", "Trabalho");
            lancar("Mercado", "450,00", "06/11/2026", "Despesa", "Alimentação");
            lancar("Reembolso", "50,00", "07/11/2026", "Receita", "Alimentação");
            lancar("Ônibus", "150,00", "08/11/2026", "Despesa", "Transporte");
            assertEquals(4, ler(() -> ((JTable) componente("movimentacoes")).getRowCount()));
            assertTrue(ler(() -> rotulo("totalReceitas")).contains("3.050,00"));
            assertTrue(ler(() -> rotulo("totalDespesas")).contains("600,00"));
            conferirSaldo("2.450,00");
            capturar("01-movimentacoes.png");
            ler(() -> { ((JTabbedPane) componente("abas")).setSelectedIndex(1); return null; });
            assertEquals("-400,00", numeroExibido(ler(() -> valorLinha("saldosCategorias", "Alimentação", 3))));
            capturar("02-saldo-por-categoria.png");
            ler(() -> { ((JTabbedPane) componente("abas")).setSelectedIndex(0); return null; });
            registrar("Exemplo completo: receitas 3050.00, despesas 600.00, saldo 2450.00; Alimentação -400.00.");

            clicar("novaMovimentacao");
            esperar(() -> componente("descricao") != null);
            texto("descricao", "Não deve salvar"); texto("valor", "0"); texto("data", "05/11/2026");
            clicar("salvar"); esperarMensagem("maior que zero"); confirmar("OK");
            assertEquals("Não deve salvar", ler(() -> ((JTextField) componente("descricao")).getText()));
            clicar("cancelar");
            assertEquals(4, servico.consultar(Filtro.todos()).getMovimentacoes().size());
            registrar("Valor zero rejeitado; formulário preservado e nenhum registro acrescentado.");

            selecionar("filtroCategoria", "Alimentação"); clicar("aplicarFiltros"); atualizada(); conferirSaldo("-400,00");
            assertEquals(2, ler(() -> ((JTable) componente("movimentacoes")).getRowCount()));
            selecionar("filtroTipo", "Receita"); clicar("aplicarFiltros"); atualizada(); conferirSaldo("50,00");
            assertEquals(1, ler(() -> ((JTable) componente("movimentacoes")).getRowCount()));
            clicar("limparFiltros"); atualizada();
            texto("filtroInicio", "07/11/2026"); texto("filtroFim", "08/11/2026");
            clicar("aplicarFiltros"); atualizada(); conferirSaldo("-100,00");
            assertEquals(2, ler(() -> ((JTable) componente("movimentacoes")).getRowCount()));
            registrar("Filtros: categoria -400.00; categoria + receita 50.00; 07/11 a 08/11 inclusivos -100.00.");
            texto("filtroInicio", "09/11/2026"); clicar("aplicarFiltros"); esperarMensagem("posterior"); confirmar("OK");
            conferirSaldo("-100,00");
            clicar("limparFiltros"); atualizada();

            clicar("gerenciarCategorias");
            esperar(() -> componente("cadastrarCategoria") != null && componente("cadastrarCategoria").isEnabled());
            marcarLinha("listaCategorias", "Alimentação"); clicar("excluirCategoria"); confirmar("Sim");
            esperarMensagem("possui movimentações"); confirmar("OK");
            texto("nomeCategoria", "  alimentação  "); clicar("cadastrarCategoria");
            esperarMensagem("Já existe"); confirmar("OK"); clicar("fecharCategorias"); atualizada();
            registrar("Categoria em uso e categoria duplicada bloqueadas com aviso visível.");

            marcarLinha("movimentacoes", "Mercado"); clicar("editarMovimentacao");
            esperar(() -> componente("valor") != null); texto("valor", "400,00");
            capturarDialogo("03-edicao.png");
            clicar("salvar"); atualizada(); conferirSaldo("2.500,00");
            marcarLinha("movimentacoes", "Mercado"); clicar("excluirMovimentacao"); confirmar("Não");
            conferirSaldo("2.500,00"); assertEquals(4, servico.consultar(Filtro.todos()).getMovimentacoes().size());
            marcarLinha("movimentacoes", "Mercado"); clicar("excluirMovimentacao"); confirmar("Sim");
            atualizada(); conferirSaldo("2.900,00");
            registrar("Edição 450.00 -> 400.00: saldo 2500.00; cancelar exclusão preservou 4 registros; confirmar deixou saldo 2900.00.");

            List<Long> ids = servico.consultar(Filtro.todos()).getMovimentacoes().stream().map(Movimentacao::getId).toList();
            ler(() -> { janela.dispose(); return null; });
            abrir(new ServicoFinanceiro(banco)); atualizada(); conferirSaldo("2.900,00");
            assertEquals(ids, servico.consultar(Filtro.todos()).getMovimentacoes().stream().map(Movimentacao::getId).toList());
            registrar("Janela fechada e reaberta: os 3 registros restantes mantiveram os mesmos IDs e valores no MySQL.");
            banco.indisponivel = true;
            clicar("atualizar"); esperarMensagem("não foram atualizados"); confirmar("OK");
            conferirSaldo("2.900,00");
            assertFalse(ler(() -> componente("novaMovimentacao").isEnabled()));
            assertTrue(ler(() -> rotulo("estado")).contains("desatualizados"));
            capturar("04-falha-de-consulta.png");
            banco.indisponivel = false;
            clicar("atualizar"); atualizada(); conferirSaldo("2.900,00");
            registrar("Falha de consulta simulada: valores anteriores preservados, aviso de desatualização e gravação bloqueada; atualização recuperou a tela.");
            texto("filtroInicio", "09/11/2026"); clicar("aplicarFiltros"); atualizada(); conferirSaldo("0,00");
            assertEquals(0, ler(() -> ((JTable) componente("movimentacoes")).getRowCount()));
            registrar("Consulta válida sem resultados: zero movimentações e saldo 0.00.");
            try (Connection conexao = original.abrir(); Statement comando = conexao.createStatement();
                 ResultSet resultado = comando.executeQuery("SELECT COUNT(*), SUM(CASE WHEN tipo='RECEITA' THEN valor ELSE -valor END) FROM movimentacao")) {
                assertTrue(resultado.next());
                assertEquals(3, resultado.getInt(1));
                assertEquals("2900.00", resultado.getBigDecimal(2).toPlainString());
                registrar("Conferência SQL independente: COUNT(*)=3; saldo armazenado nos lançamentos=2900.00.");
            }
            Files.write(pasta.resolve("interface.txt"), evidencias);
        } finally {
            banco.indisponivel = false;
            ler(() -> { for (Window janelaAberta : Window.getWindows()) janelaAberta.dispose(); return null; });
            // O banco era vazio. Removemos apenas as categorias criadas pelo teste e seus lancamentos.
            if (categoriasCriadas.isEmpty()) categoriasCriadas.addAll(servico.listarCategorias().stream().map(Categoria::getId).toList());
            for (long id : categoriasCriadas) {
                try (Connection conexao = original.abrir(); PreparedStatement comando = conexao.prepareStatement("DELETE FROM movimentacao WHERE id_categoria=?")) {
                    comando.setLong(1, id); comando.executeUpdate();
                }
                try (Connection conexao = original.abrir(); PreparedStatement comando = conexao.prepareStatement("DELETE FROM categoria WHERE id_categoria=?")) {
                    comando.setLong(1, id); comando.executeUpdate();
                }
            }
        }
    }
    private void abrir(ServicoFinanceiro servico) throws Exception {
        ler(() -> { Estilo.configurar(); janela = new JanelaPrincipal(servico); janela.setVisible(true); return null; });
    }
    private void lancar(String descricao, String valor, String data, String tipo, String categoria) throws Exception {
        clicar("novaMovimentacao");
        esperar(() -> componente("descricao") != null);
        texto("descricao", descricao);
        texto("valor", valor);
        texto("data", data);
        selecionar("tipo", tipo);
        selecionar("categoria", categoria);
        clicar("salvar");
        atualizada();
    }
    private void registrar(String texto) { evidencias.add("PASSOU: " + texto); }
    private String numeroExibido(String texto) { return texto.replace("R$", "").replace("\u00a0", "").replace(" ", ""); }
    private void conferirSaldo(String valor) throws Exception { assertEquals(valor, numeroExibido(ler(() -> rotulo("totalSaldo")))); }
    private void atualizada() throws Exception {
        esperar(() -> componente("descricao") == null && componente("nomeCategoria") == null
            && componente("estado") != null && rotulo("estado").contains("Consulta atualizada") && componente("atualizar").isEnabled());
    }
    private void clicar(String nome) throws Exception {
        JButton botao = ler(() -> (JButton) componente(nome));
        assertNotNull(botao, nome);
        assertTrue(ler(botao::isEnabled), nome + " precisa estar habilitado");
        SwingUtilities.invokeLater(botao::doClick);
    }
    private void texto(String nome, String valor) throws Exception {
        ler(() -> { ((JTextField) componente(nome)).setText(valor); return null; });
    }
    private void selecionar(String nome, String valor) throws Exception {
        ler(() -> {
            JComboBox<?> combo = (JComboBox<?>) componente(nome);
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).toString().equals(valor)) { combo.setSelectedIndex(i); return null; }
            }
            throw new AssertionError("Opção não encontrada: " + valor);
        });
    }
    private void marcarLinha(String nome, String texto) throws Exception {
        ler(() -> {
            JTable tabela = (JTable) componente(nome);
            for (int linha = 0; linha < tabela.getRowCount(); linha++) {
                for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
                    if (texto.equals(tabela.getValueAt(linha, coluna).toString())) {
                        tabela.setRowSelectionInterval(linha, linha); return null;
                    }
                }
            }
            throw new AssertionError("Linha não encontrada: " + texto);
        });
    }
    private String valorLinha(String nome, String texto, int coluna) {
        JTable tabela = (JTable) componente(nome);
        for (int linha = 0; linha < tabela.getRowCount(); linha++) {
            if (texto.equals(tabela.getValueAt(linha, 0))) return tabela.getValueAt(linha, coluna).toString();
        }
        throw new AssertionError(texto);
    }
    private void confirmar(String texto) throws Exception {
        esperar(() -> botaoTexto(texto) != null);
        JButton botao = ler(() -> botaoTexto(texto));
        SwingUtilities.invokeLater(botao::doClick);
        esperar(() -> !botao.isShowing());
    }
    private JButton botaoTexto(String texto) {
        for (Window janelaAberta : Window.getWindows()) {
            if (janelaAberta.isShowing() && janelaAberta instanceof JDialog) {
                for (Component componente : descendentes(janelaAberta)) {
                    if (componente instanceof JButton botao && texto.equals(botao.getText())) return botao;
                }
            }
        }
        return null;
    }
    private void esperarMensagem(String trecho) throws Exception {
        esperar(() -> {
            for (Window janelaAberta : Window.getWindows()) {
                if (janelaAberta.isShowing() && janelaAberta instanceof JDialog) {
                    for (Component componente : descendentes(janelaAberta))
                        if (componente instanceof JLabel rotulo && rotulo.getText() != null && rotulo.getText().contains(trecho)) return true;
                }
            }
            return false;
        });
    }
    private JComponent componente(String nome) {
        for (Window janelaAberta : Window.getWindows()) {
            if (janelaAberta.isShowing()) {
                for (Component componente : descendentes(janelaAberta))
                    if (nome.equals(componente.getName())) return (JComponent) componente;
            }
        }
        return null;
    }
    private String rotulo(String nome) { return ((JLabel) componente(nome)).getText(); }
    private List<Component> descendentes(Container raiz) {
        List<Component> lista = new ArrayList<>();
        for (Component componente : raiz.getComponents()) {
            lista.add(componente);
            if (componente instanceof Container filho) lista.addAll(descendentes(filho));
        }
        return lista;
    }
    private void capturar(String arquivo) throws Exception { capturarJanela(janela, arquivo); }
    private void capturarDialogo(String arquivo) throws Exception {
        Window dialogo = ler(() -> {
            for (Window item : Window.getWindows()) if (item.isShowing() && "formularioMovimentacao".equals(item.getName())) return item;
            throw new AssertionError("Formulário não encontrado");
        });
        capturarJanela(dialogo, arquivo);
    }
    private void capturarJanela(Window alvo, String arquivo) throws Exception {
        ler(() -> { alvo.repaint(); return null; });
        Robot robo = new Robot();
        robo.waitForIdle();
        Rectangle area = ler(() -> new Rectangle(alvo.getLocationOnScreen(), alvo.getSize()));
        ImageIO.write(robo.createScreenCapture(area), "png", pasta.resolve(arquivo).toFile());
    }
    private void esperar(Callable<Boolean> condicao) throws Exception {
        long limite = System.nanoTime() + 15_000_000_000L;
        while (System.nanoTime() < limite) {
            if (ler(condicao)) return;
            Thread.sleep(50);
        }
        fail("A interface não chegou ao estado esperado em 15 segundos.");
    }
    private <T> T ler(Callable<T> acao) throws Exception {
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Throwable> erro = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try { resultado.set(acao.call()); } catch (Throwable falha) { erro.set(falha); }
        });
        if (erro.get() != null) throw new AssertionError(erro.get());
        return resultado.get();
    }
    private static class ConexaoInterrompivel extends ConexaoBanco {
        private final ConexaoBanco original;
        volatile boolean indisponivel;
        ConexaoInterrompivel(ConexaoBanco original) { super("", "", ""); this.original = original; }
        @Override public Connection abrir() throws SQLException {
            if (indisponivel) throw new SQLException("Indisponibilidade simulada no teste de interface", "08001");
            return original.abrir();
        }
    }
}
