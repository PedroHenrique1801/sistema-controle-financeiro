package br.edu.financeiro.ui;

import br.edu.financeiro.modelo.*;
import br.edu.financeiro.servico.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class JanelaPrincipal extends JFrame {
    private final ServicoFinanceiro servico;
    private final JTextField inicio = Estilo.nome(new JTextField(10), "filtroInicio");
    private final JTextField fim = Estilo.nome(new JTextField(10), "filtroFim");
    private final JComboBox<Object> categoria = Estilo.nome(new JComboBox<>(), "filtroCategoria");
    private final JComboBox<Object> tipo = Estilo.nome(new JComboBox<>(), "filtroTipo");
    private final JButton aplicar = Estilo.botao("Aplicar filtros", "aplicarFiltros");
    private final JButton limpar = Estilo.botao("Limpar filtros", "limparFiltros");
    private final JButton nova = Estilo.botao("Nova movimentação", "novaMovimentacao");
    private final JButton editar = Estilo.botao("Editar", "editarMovimentacao");
    private final JButton excluir = Estilo.botao("Excluir", "excluirMovimentacao");
    private final JButton gerenciar = Estilo.botao("Categorias", "gerenciarCategorias");
    private final JButton atualizar = Estilo.botao("Atualizar", "atualizar");
    private final JLabel receitas = Estilo.nome(new JLabel("—"), "totalReceitas");
    private final JLabel despesas = Estilo.nome(new JLabel("—"), "totalDespesas");
    private final JLabel saldo = Estilo.nome(new JLabel("—"), "totalSaldo");
    private final JLabel estado = Estilo.nome(new JLabel("Conectando ao banco de dados…"), "estado");
    private final JLabel selecao = Estilo.nome(new JLabel("Aguardando consulta."), "selecaoAplicada");
    private final DefaultTableModel modelo = Estilo.modelo("Data", "Descrição", "Categoria", "Tipo", "Valor");
    private final DefaultTableModel modeloCategorias = Estilo.modelo("Categoria", "Receitas", "Despesas", "Saldo");
    private final JTable tabela = Estilo.tabela(modelo, "movimentacoes");
    private final JTable tabelaCategorias = Estilo.tabela(modeloCategorias, "saldosCategorias");
    private List<Categoria> categorias = new ArrayList<>();
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private Filtro aplicado = Filtro.todos();
    private boolean ocupado;
    private boolean disponivel;

    public JanelaPrincipal(ServicoFinanceiro servico) {
        super("Controle financeiro pessoal");
        setName("janelaPrincipal");
        this.servico = servico;
        construir();
        aplicar.addActionListener(evento -> aplicarFiltros());
        limpar.addActionListener(evento -> consultar(Filtro.todos(), ""));
        atualizar.addActionListener(evento -> consultar(aplicado, ""));
        nova.addActionListener(evento -> abrirFormulario(null));
        editar.addActionListener(evento -> { if (selecionada() != null) abrirFormulario(selecionada()); });
        excluir.addActionListener(evento -> excluir());
        gerenciar.addActionListener(evento -> new JanelaCategorias(this, servico,
            () -> consultar(aplicado, "Categorias atualizadas.")).setVisible(true));
        tabela.getSelectionModel().addListSelectionListener(evento -> habilitar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent evento) { if (!ocupado) dispose(); }
        });
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1120, tela.width - 32), Math.min(780, tela.height - 64));
        setMinimumSize(new Dimension(Math.min(950, tela.width - 32), Math.min(640, tela.height - 64)));
        setLocationRelativeTo(null);
        consultar(Filtro.todos(), "");
    }
    private void construir() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        JPanel cabecalho = new JPanel(new BorderLayout(0, 7));
        cabecalho.setBackground(Estilo.AZUL);
        Estilo.margens(cabecalho, 24);
        JLabel titulo = new JLabel("Controle financeiro pessoal");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Suas receitas, despesas e resultados em um só lugar.");
        subtitulo.setForeground(new Color(215, 226, 241));
        cabecalho.add(titulo, BorderLayout.NORTH);
        cabecalho.add(subtitulo, BorderLayout.SOUTH);
        raiz.add(cabecalho, BorderLayout.NORTH);

        JPanel conteudo = new JPanel(new BorderLayout(0, 16));
        conteudo.setOpaque(false);
        Estilo.margens(conteudo, 22);
        JPanel topo = new JPanel(new BorderLayout(0, 16));
        topo.setOpaque(false);
        JPanel filtros = new JPanel(new BorderLayout(12, 0));
        filtros.setOpaque(false);
        JPanel campos = new JPanel(new GridLayout(1, 4, 12, 0));
        campos.setOpaque(false);
        categoria.addItem("Todas as categorias");
        tipo.addItem("Receitas e despesas");
        tipo.addItem(TipoMovimentacao.RECEITA);
        tipo.addItem(TipoMovimentacao.DESPESA);
        campos.add(Estilo.campo("De (dd/mm/aaaa)", inicio));
        campos.add(Estilo.campo("Até (dd/mm/aaaa)", fim));
        campos.add(Estilo.campo("Categoria", categoria));
        campos.add(Estilo.campo("Tipo", tipo));
        filtros.add(campos, BorderLayout.CENTER);
        JPanel botoesFiltro = new JPanel(new GridLayout(2, 1, 0, 3));
        botoesFiltro.add(aplicar);
        botoesFiltro.add(limpar);
        filtros.add(botoesFiltro, BorderLayout.EAST);
        topo.add(filtros, BorderLayout.NORTH);
        JPanel cartoes = new JPanel(new GridLayout(1, 3, 14, 0));
        cartoes.setOpaque(false);
        cartoes.add(cartao("Receitas da seleção", receitas, Estilo.VERDE));
        cartoes.add(cartao("Despesas da seleção", despesas, Estilo.VERMELHO));
        cartoes.add(cartao("Saldo da seleção", saldo, Estilo.AZUL));
        topo.add(cartoes, BorderLayout.CENTER);
        selecao.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        topo.add(selecao, BorderLayout.SOUTH);
        conteudo.add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        acoes.setOpaque(false);
        for (JButton botao : new JButton[]{nova, editar, excluir, gerenciar, atualizar}) acoes.add(botao);
        centro.add(acoes, BorderLayout.NORTH);
        JTabbedPane abas = Estilo.nome(new JTabbedPane(), "abas");
        abas.addTab("Movimentações", new JScrollPane(tabela));
        JPanel porCategoria = new JPanel(new BorderLayout(0, 8));
        porCategoria.add(new JScrollPane(tabelaCategorias), BorderLayout.CENTER);
        porCategoria.add(new JLabel("Mesmos filtros da consulta. Categorias sem movimentações na seleção aparecem com zero."), BorderLayout.SOUTH);
        abas.addTab("Saldo por categoria", porCategoria);
        abas.addChangeListener(evento -> habilitar());
        centro.add(abas, BorderLayout.CENTER);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(320);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(180);
        Estilo.alinharDireita(tabela, 4);
        for (int coluna = 1; coluna < 4; coluna++) Estilo.alinharDireita(tabelaCategorias, coluna);
        conteudo.add(centro, BorderLayout.CENTER);
        estado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        conteudo.add(estado, BorderLayout.SOUTH);
        raiz.add(conteudo, BorderLayout.CENTER);
        setContentPane(raiz);
    }
    private JPanel cartao(String titulo, JLabel valor, Color cor) {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(Color.WHITE);
        Estilo.margens(painel, 18);
        painel.add(new JLabel(titulo), BorderLayout.NORTH);
        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        valor.setForeground(cor);
        painel.add(valor, BorderLayout.CENTER);
        return painel;
    }
    private void aplicarFiltros() {
        try {
            Long categoriaId = categoria.getSelectedItem() instanceof Categoria item ? item.getId() : null;
            TipoMovimentacao tipoEscolhido = tipo.getSelectedItem() instanceof TipoMovimentacao item ? item : null;
            consultar(new Filtro(Formatos.lerDataOpcional(inicio.getText()), Formatos.lerDataOpcional(fim.getText()), categoriaId, tipoEscolhido), "");
        } catch (IllegalArgumentException erro) { Estilo.erro(this, erro.getMessage()); }
    }
    private void consultar(Filtro filtro, String confirmacao) {
        estado.setText(confirmacao + " Consultando o banco de dados…");
        TarefaTela.executar(() -> servico.consultar(filtro), consulta -> {
            aplicado = filtro;
            apresentar(consulta);
            disponivel = true;
            estado.setForeground(Estilo.AZUL);
            estado.setText(confirmacao + " Consulta atualizada · " + movimentacoes.size() + " movimentação(ões)."
                + (categorias.isEmpty() ? " Cadastre uma categoria para começar." : ""));
            habilitar();
        }, erro -> {
            disponivel = false;
            estado.setForeground(Estilo.VERMELHO);
            estado.setText(confirmacao + " Consulta não atualizada. Os dados exibidos podem estar desatualizados. Clique em Atualizar.");
            habilitar();
            Estilo.erro(this, (confirmacao.isBlank() ? "" : confirmacao + "\n") + MensagensErro.descrever(erro, false));
        }, this::ocupar);
    }
    private void apresentar(ConsultaFinanceira consulta) {
        tabela.clearSelection();
        modelo.setRowCount(0);
        modeloCategorias.setRowCount(0);
        categorias = consulta.getCategorias();
        movimentacoes = consulta.getMovimentacoes();
        categoria.removeAllItems();
        categoria.addItem("Todas as categorias");
        String nomeCategoria = "todas as categorias";
        for (Categoria item : categorias) {
            categoria.addItem(item);
            if (aplicado.getCategoriaId() != null && item.getId() == aplicado.getCategoriaId()) {
                categoria.setSelectedItem(item);
                nomeCategoria = item.getNome();
            }
            ResumoFinanceiro resumo = ServicoFinanceiro.resumirCategoria(movimentacoes, item.getId());
            modeloCategorias.addRow(new Object[]{item.getNome(), Formatos.moeda(resumo.getReceitas()),
                Formatos.moeda(resumo.getDespesas()), Formatos.moeda(resumo.getSaldo())});
        }
        if (aplicado.getCategoriaId() != null && categoria.getSelectedIndex() == 0) {
            // Uma categoria excluida pode estar no filtro anterior: explicitar a selecao sem resultados.
            Categoria removida = new Categoria(aplicado.getCategoriaId(), "Categoria excluída");
            categoria.addItem(removida);
            categoria.setSelectedItem(removida);
            nomeCategoria = "categoria excluída";
        }
        for (Movimentacao item : movimentacoes) modelo.addRow(new Object[]{Formatos.data(item.getData()),
            item.getDescricao(), item.getCategoria().getNome(), item.getTipo(), Formatos.moeda(item.getValor())});
        ResumoFinanceiro resumo = ServicoFinanceiro.resumir(movimentacoes);
        receitas.setText(Formatos.moeda(resumo.getReceitas()));
        despesas.setText(Formatos.moeda(resumo.getDespesas()));
        saldo.setText(Formatos.moeda(resumo.getSaldo()));
        saldo.setForeground(resumo.getSaldo().signum() < 0 ? Estilo.VERMELHO : Estilo.AZUL);
        inicio.setText(aplicado.getInicio() == null ? "" : Formatos.data(aplicado.getInicio()));
        fim.setText(aplicado.getFim() == null ? "" : Formatos.data(aplicado.getFim()));
        tipo.setSelectedItem(aplicado.getTipo() == null ? "Receitas e despesas" : aplicado.getTipo());
        selecao.setText("Seleção aplicada: " + (inicio.getText().isEmpty() ? "sem data inicial" : inicio.getText())
            + " até " + (fim.getText().isEmpty() ? "sem data final" : fim.getText()) + " · " + nomeCategoria
            + " · " + (aplicado.getTipo() == null ? "receitas e despesas" : aplicado.getTipo().toString().toLowerCase()));
    }
    private Movimentacao selecionada() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : movimentacoes.get(linha);
    }
    private void abrirFormulario(Movimentacao movimentacao) {
        if (categorias.isEmpty()) { Estilo.erro(this, "Cadastre uma categoria antes de registrar uma movimentação."); return; }
        new FormularioMovimentacao(this, servico, categorias, movimentacao,
            () -> consultar(aplicado, "Movimentação salva.")).setVisible(true);
    }
    private void excluir() {
        Movimentacao movimentacao = selecionada();
        if (movimentacao == null) return;
        int resposta = JOptionPane.showConfirmDialog(this,
            "Excluir “" + movimentacao.getDescricao() + "” (" + Formatos.moeda(movimentacao.getValor()) + ")?\nEssa ação não pode ser desfeita.",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) return;
        TarefaTela.executar(() -> { servico.excluirMovimentacao(movimentacao.getId()); return null; },
            resultado -> consultar(aplicado, "Movimentação excluída."),
            erro -> Estilo.erro(this, MensagensErro.descrever(erro, true)), this::ocupar);
    }
    private void ocupar(boolean valor) { ocupado = valor; habilitar(); }
    private void habilitar() {
        for (JComponent campo : new JComponent[]{inicio, fim, categoria, tipo, aplicar, limpar, atualizar}) campo.setEnabled(!ocupado);
        nova.setEnabled(!ocupado && disponivel);
        gerenciar.setEnabled(!ocupado && disponivel);
        tabela.setEnabled(!ocupado && disponivel);
        editar.setEnabled(!ocupado && disponivel && tabela.isShowing() && tabela.getSelectedRow() >= 0);
        excluir.setEnabled(editar.isEnabled());
    }
}
