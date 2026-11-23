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

    private final JTextField inicio =
        Estilo.nome(new JTextField(10), "filtroInicio");

    private final JTextField fim =
        Estilo.nome(new JTextField(10), "filtroFim");

    private final JComboBox<Object> categoria =
        Estilo.nome(new JComboBox<>(), "filtroCategoria");

    private final JComboBox<Object> tipo =
        Estilo.nome(new JComboBox<>(), "filtroTipo");

    private final JButton aplicar =
        Estilo.botao("Aplicar filtros", "aplicarFiltros");

    private final JButton limpar =
        Estilo.botao("Limpar filtros", "limparFiltros");

    private final JButton nova =
        Estilo.botao("Nova movimentação", "novaMovimentacao");

    private final JButton editar =
        Estilo.botao("Editar", "editarMovimentacao");

    private final JButton excluir =
        Estilo.botao("Excluir", "excluirMovimentacao");

    private final JButton gerenciar =
        Estilo.botao("Categorias", "gerenciarCategorias");

    private final JButton atualizar =
        Estilo.botao("Atualizar", "atualizar");

    private final JLabel receitas =
        Estilo.nome(new JLabel("—"), "totalReceitas");

    private final JLabel despesas =
        Estilo.nome(new JLabel("—"), "totalDespesas");

    private final JLabel saldo =
        Estilo.nome(new JLabel("—"), "totalSaldo");

    private final JLabel estado =
        Estilo.nome(new JLabel("Conectando ao banco de dados…"), "estado");

    private final JLabel selecao =
        Estilo.nome(new JLabel("Aguardando consulta."), "selecaoAplicada");

    private final DefaultTableModel modelo =
        Estilo.modelo("Data", "Descrição", "Categoria", "Tipo", "Valor");

    private final DefaultTableModel modeloCategorias =
        Estilo.modelo("Categoria", "Receitas", "Despesas", "Saldo");

    private final JTable tabela =
        Estilo.tabela(modelo, "movimentacoes");

    private final JTable tabelaCategorias =
        Estilo.tabela(modeloCategorias, "saldosCategorias");

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

        editar.addActionListener(evento -> {
            if (selecionada() != null) {
                abrirFormulario(selecionada());
            }
        });

        excluir.addActionListener(evento -> excluir());

        gerenciar.addActionListener(evento ->
            new JanelaCategorias(
                this,
                servico,
                () -> consultar(aplicado, "Categorias atualizadas.")
            ).setVisible(true)
        );

        tabela.getSelectionModel()
            .addListSelectionListener(evento -> habilitar());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                if (!ocupado) {
                    dispose();
                }
            }
        });

        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();

        setSize(
            Math.min(1240, tela.width - 32),
            Math.min(820, tela.height - 64)
        );

        setMinimumSize(new Dimension(
            Math.min(1120, tela.width - 32),
            Math.min(700, tela.height - 64)
        ));

        setLocationRelativeTo(null);
        consultar(Filtro.todos(), "");
    }

    private void construir() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);

        JPanel cabecalho = new JPanel(new BorderLayout(14, 0));
        cabecalho.setBackground(Color.WHITE);
        Estilo.margens(cabecalho, 18);

        JLabel marca = new JLabel("CF", SwingConstants.CENTER);
        marca.setOpaque(true);
        marca.setBackground(Estilo.AZUL);
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        marca.setBorder(
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );

        JLabel titulo = new JLabel("Controle financeiro");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));

        JLabel pessoal = new JLabel("Finanças pessoais");
        pessoal.setForeground(Estilo.CINZA);

        cabecalho.add(marca, BorderLayout.WEST);
        cabecalho.add(titulo, BorderLayout.CENTER);
        cabecalho.add(pessoal, BorderLayout.EAST);
        raiz.add(cabecalho, BorderLayout.NORTH);

        JTabbedPane abas = Estilo.nome(new JTabbedPane(), "abas");

        // As paginas continuam no JTabbedPane.
        // Os botoes laterais fazem a navegacao.
        abas.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                contentBorderInsets = new Insets(0, 0, 0, 0);
            }

            @Override
            protected int calculateTabAreaHeight(
                int posicao, int linhas, int altura
            ) {
                return 0;
            }

            @Override
            protected void paintTabArea(
                Graphics g, int posicao, int selecionada
            ) { }
        });

        abas.setFocusable(false);
        abas.addTab("Movimentações", Estilo.rolagem(tabela));

        JPanel porCategoria = new JPanel(new BorderLayout(0, 12));
        porCategoria.setBackground(Color.WHITE);

        porCategoria.add(
            Estilo.rolagem(tabelaCategorias),
            BorderLayout.CENTER
        );

        JLabel nota = new JLabel(
            "Categorias sem movimentações na seleção aparecem com zero."
        );
        nota.setForeground(Estilo.CINZA);
        nota.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        porCategoria.add(nota, BorderLayout.SOUTH);
        abas.addTab("Saldo por categoria", porCategoria);

        JButton movimentos =
            Estilo.botao("Movimentações", "navegarMovimentacoes");

        JButton saldos =
            Estilo.botao("Saldo por categoria", "navegarSaldos");

        JPanel lateral = new JPanel(new BorderLayout(0, 20));
        lateral.setBackground(Color.WHITE);
        lateral.setPreferredSize(new Dimension(205, 0));
        Estilo.margens(lateral, 16);

        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 10));
        menu.setOpaque(false);

        JLabel consultas = new JLabel("CONSULTAS");
        consultas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        consultas.setForeground(Estilo.CINZA);
        menu.add(consultas);

        for (JButton botao : new JButton[]{
            movimentos, saldos, gerenciar
        }) {
            botao.setHorizontalAlignment(SwingConstants.LEFT);
            menu.add(botao);
        }

        lateral.add(menu, BorderLayout.NORTH);

        JLabel moeda = new JLabel("Valores em reais (R$)");
        moeda.setForeground(Estilo.CINZA);
        moeda.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        lateral.add(moeda, BorderLayout.SOUTH);
        raiz.add(lateral, BorderLayout.WEST);

        JPanel conteudo = new JPanel(new BorderLayout(0, 18));
        conteudo.setOpaque(false);
        Estilo.margens(conteudo, 22);

        JPanel topo = new JPanel(new BorderLayout(0, 16));
        topo.setOpaque(false);

        JPanel resumo = new JPanel(new BorderLayout(0, 14));
        resumo.setOpaque(false);

        JLabel visao = new JLabel("Visão geral");
        visao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        resumo.add(visao, BorderLayout.NORTH);

        JPanel cartoes = new JPanel(new GridLayout(1, 3, 14, 0));
        cartoes.setOpaque(false);

        cartoes.add(cartao("Saldo da seleção", saldo, Estilo.AZUL));
        cartoes.add(cartao("Receitas da seleção", receitas, Estilo.VERDE));
        cartoes.add(cartao("Despesas da seleção", despesas, Estilo.VERMELHO));

        resumo.add(cartoes, BorderLayout.CENTER);
        topo.add(resumo, BorderLayout.NORTH);

        JPanel filtros = Estilo.painel(Color.WHITE, 16);
        filtros.setLayout(new BorderLayout(12, 10));

        JPanel campos = new JPanel(new GridLayout(1, 4, 10, 0));
        campos.setOpaque(false);

        categoria.addItem("Todas as categorias");
        tipo.addItem("Receitas e despesas");
        tipo.addItem(TipoMovimentacao.RECEITA);
        tipo.addItem(TipoMovimentacao.DESPESA);

        campos.add(Estilo.campo("De (dd/mm/aaaa)", inicio));
        campos.add(Estilo.campo("Até (dd/mm/aaaa)", fim));
        campos.add(Estilo.campo("Categoria", categoria));
        campos.add(Estilo.campo("Tipo", tipo));

        filtros.add(campos, BorderLayout.NORTH);

        JPanel botoesFiltro =
            new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

        botoesFiltro.setOpaque(false);

        Estilo.destacar(aplicar, Estilo.AZUL);
        botoesFiltro.add(aplicar);
        botoesFiltro.add(limpar);

        selecao.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        selecao.setForeground(Estilo.CINZA);

        JPanel rodapeFiltro = new JPanel(new BorderLayout(8, 0));
        rodapeFiltro.setOpaque(false);
        rodapeFiltro.add(selecao, BorderLayout.CENTER);
        rodapeFiltro.add(botoesFiltro, BorderLayout.EAST);

        filtros.add(rodapeFiltro, BorderLayout.SOUTH);
        topo.add(filtros, BorderLayout.CENTER);
        conteudo.add(topo, BorderLayout.NORTH);

        JPanel centro = Estilo.painel(Color.WHITE, 16);
        centro.setLayout(new BorderLayout(0, 16));

        JPanel ferramentas = new JPanel(new BorderLayout(8, 0));
        ferramentas.setOpaque(false);

        JLabel secao = new JLabel("Movimentações");
        secao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        ferramentas.add(secao, BorderLayout.WEST);

        JPanel acoes =
            new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

        acoes.setOpaque(false);
        Estilo.destacar(nova, Estilo.AZUL);
        excluir.setForeground(Estilo.VERMELHO);

        for (JButton botao : new JButton[]{
            nova, editar, excluir, atualizar
        }) {
            acoes.add(botao);
        }

        ferramentas.add(acoes, BorderLayout.EAST);
        centro.add(ferramentas, BorderLayout.NORTH);
        centro.add(abas, BorderLayout.CENTER);

        movimentos.setSelected(true);
        movimentos.setForeground(Estilo.AZUL);

        movimentos.addActionListener(evento -> abas.setSelectedIndex(0));
        saldos.addActionListener(evento -> abas.setSelectedIndex(1));

        abas.addChangeListener(evento -> {
            boolean lista = abas.getSelectedIndex() == 0;

            movimentos.setSelected(lista);
            saldos.setSelected(!lista);

            movimentos.setForeground(
                lista ? Estilo.AZUL : Estilo.TEXTO
            );

            saldos.setForeground(
                lista ? Estilo.TEXTO : Estilo.AZUL
            );

            secao.setText(
                lista ? "Movimentações" : "Saldo por categoria"
            );

            habilitar();
        });

        tabela.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(280);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(170);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(135);

        Estilo.alinharDireita(tabela, 4);

        for (int coluna = 1; coluna < 4; coluna++) {
            Estilo.alinharDireita(tabelaCategorias, coluna);
        }

        conteudo.add(centro, BorderLayout.CENTER);

        estado.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        conteudo.add(estado, BorderLayout.SOUTH);

        raiz.add(conteudo, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel cartao(String titulo, JLabel valor, Color cor) {
        JPanel painel = Estilo.painel(cor, 20);
        painel.setLayout(new BorderLayout(0, 12));

        JLabel rotulo = new JLabel(titulo);
        rotulo.setForeground(Color.WHITE);
        painel.add(rotulo, BorderLayout.NORTH);

        valor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        valor.setForeground(Color.WHITE);
        painel.add(valor, BorderLayout.CENTER);

        return painel;
    }

    private void aplicarFiltros() {
        try {
            Long categoriaId =
                categoria.getSelectedItem() instanceof Categoria item
                    ? item.getId()
                    : null;

            TipoMovimentacao tipoEscolhido =
                tipo.getSelectedItem() instanceof TipoMovimentacao item
                    ? item
                    : null;

            consultar(
                new Filtro(
                    Formatos.lerDataOpcional(inicio.getText()),
                    Formatos.lerDataOpcional(fim.getText()),
                    categoriaId,
                    tipoEscolhido
                ),
                ""
            );
        } catch (IllegalArgumentException erro) {
            Estilo.erro(this, erro.getMessage());
        }
    }

    private void consultar(Filtro filtro, String confirmacao) {
        estado.setText(
            confirmacao + " Consultando o banco de dados…"
        );

        TarefaTela.executar(
            () -> servico.consultar(filtro),
            consulta -> {
                aplicado = filtro;
                apresentar(consulta);
                disponivel = true;

                estado.setForeground(Estilo.AZUL);
                estado.setText(
                    confirmacao
                    + " Consulta atualizada · "
                    + movimentacoes.size()
                    + " movimentação(ões)."
                    + (categorias.isEmpty()
                        ? " Cadastre uma categoria para começar."
                        : "")
                );

                habilitar();
            },
            erro -> {
                disponivel = false;
                estado.setForeground(Estilo.VERMELHO);

                estado.setText(
                    confirmacao
                    + " Consulta não atualizada. "
                    + "Os dados exibidos podem estar desatualizados. "
                    + "Clique em Atualizar."
                );

                habilitar();

                Estilo.erro(
                    this,
                    (confirmacao.isBlank() ? "" : confirmacao + "\n")
                    + MensagensErro.descrever(erro, false)
                );
            },
            this::ocupar
        );
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

            if (aplicado.getCategoriaId() != null
                && item.getId() == aplicado.getCategoriaId()) {
                categoria.setSelectedItem(item);
                nomeCategoria = item.getNome();
            }

            ResumoFinanceiro resumo =
                ServicoFinanceiro.resumirCategoria(
                    movimentacoes, item.getId()
                );

            modeloCategorias.addRow(new Object[]{
                item.getNome(),
                Formatos.moeda(resumo.getReceitas()),
                Formatos.moeda(resumo.getDespesas()),
                Formatos.moeda(resumo.getSaldo())
            });
        }

        if (aplicado.getCategoriaId() != null
            && categoria.getSelectedIndex() == 0) {
            Categoria removida = new Categoria(
                aplicado.getCategoriaId(),
                "Categoria excluída"
            );

            categoria.addItem(removida);
            categoria.setSelectedItem(removida);
            nomeCategoria = "categoria excluída";
        }

        for (Movimentacao item : movimentacoes) {
            modelo.addRow(new Object[]{
                Formatos.data(item.getData()),
                item.getDescricao(),
                item.getCategoria().getNome(),
                item.getTipo(),
                Formatos.moeda(item.getValor())
            });
        }

        ResumoFinanceiro resumo =
            ServicoFinanceiro.resumir(movimentacoes);

        receitas.setText(Formatos.moeda(resumo.getReceitas()));
        despesas.setText(Formatos.moeda(resumo.getDespesas()));
        saldo.setText(Formatos.moeda(resumo.getSaldo()));
        saldo.setForeground(Color.WHITE);

        inicio.setText(
            aplicado.getInicio() == null
                ? ""
                : Formatos.data(aplicado.getInicio())
        );

        fim.setText(
            aplicado.getFim() == null
                ? ""
                : Formatos.data(aplicado.getFim())
        );

        tipo.setSelectedItem(
            aplicado.getTipo() == null
                ? "Receitas e despesas"
                : aplicado.getTipo()
        );

        selecao.setText(
            "Seleção aplicada: "
            + (inicio.getText().isEmpty()
                ? "sem data inicial"
                : inicio.getText())
            + " até "
            + (fim.getText().isEmpty()
                ? "sem data final"
                : fim.getText())
            + " · "
            + nomeCategoria
            + " · "
            + (aplicado.getTipo() == null
                ? "receitas e despesas"
                : aplicado.getTipo().toString().toLowerCase())
        );

        selecao.setToolTipText(selecao.getText());
    }

    private Movimentacao selecionada() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : movimentacoes.get(linha);
    }

    private void abrirFormulario(Movimentacao movimentacao) {
        if (categorias.isEmpty()) {
            Estilo.erro(
                this,
                "Cadastre uma categoria antes de registrar uma movimentação."
            );
            return;
        }

        new FormularioMovimentacao(
            this,
            servico,
            categorias,
            movimentacao,
            () -> consultar(aplicado, "Movimentação salva.")
        ).setVisible(true);
    }

    private void excluir() {
        Movimentacao movimentacao = selecionada();

        if (movimentacao == null) {
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(
            this,
            "Excluir “"
                + movimentacao.getDescricao()
                + "” ("
                + Formatos.moeda(movimentacao.getValor())
                + ")?\nEssa ação não pode ser desfeita.",
            "Confirmar exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        TarefaTela.executar(
            () -> {
                servico.excluirMovimentacao(movimentacao.getId());
                return null;
            },
            resultado -> consultar(aplicado, "Movimentação excluída."),
            erro -> Estilo.erro(
                this, MensagensErro.descrever(erro, true)
            ),
            this::ocupar
        );
    }

    private void ocupar(boolean valor) {
        ocupado = valor;
        habilitar();
    }

    private void habilitar() {
        for (JComponent campo : new JComponent[]{
            inicio, fim, categoria, tipo, aplicar, limpar, atualizar
        }) {
            campo.setEnabled(!ocupado);
        }

        nova.setEnabled(!ocupado && disponivel);
        gerenciar.setEnabled(!ocupado && disponivel);
        tabela.setEnabled(!ocupado && disponivel);

        editar.setEnabled(
            !ocupado
            && disponivel
            && tabela.isShowing()
            && tabela.getSelectedRow() >= 0
        );

        excluir.setEnabled(editar.isEnabled());
    }
}