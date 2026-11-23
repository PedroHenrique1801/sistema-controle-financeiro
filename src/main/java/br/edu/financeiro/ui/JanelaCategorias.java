package br.edu.financeiro.ui;

import br.edu.financeiro.modelo.Categoria;
import br.edu.financeiro.servico.ServicoFinanceiro;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class JanelaCategorias extends JDialog {
    private final ServicoFinanceiro servico;
    private final Runnable aoFechar;
    private final DefaultTableModel modelo = Estilo.modelo("Nome da categoria");
    private final JTable tabela = Estilo.tabela(modelo, "listaCategorias");
    private final JTextField nome = Estilo.nome(new JTextField(), "nomeCategoria");
    private final JButton nova = Estilo.botao("Cadastrar", "cadastrarCategoria");
    private final JButton renomear = Estilo.botao("Renomear selecionada", "renomearCategoria");
    private final JButton excluir = Estilo.botao("Excluir selecionada", "excluirCategoria");
    private final JButton atualizar = Estilo.botao("Atualizar", "atualizarCategorias");
    private final JButton fechar = Estilo.botao("Fechar", "fecharCategorias");
    private final JLabel estado = Estilo.nome(new JLabel("Carregando categorias…"), "estadoCategorias");
    private List<Categoria> categorias = new ArrayList<>();
    private boolean ocupado;
    private boolean disponivel;

    public JanelaCategorias(JFrame dono, ServicoFinanceiro servico, Runnable aoFechar) {
        super(dono, "Categorias", true);
        setName("janelaCategorias");
        this.servico = servico;
        this.aoFechar = aoFechar;
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        Estilo.margens(painel, 20);
        JPanel entrada = new JPanel(new BorderLayout(8, 8));
        entrada.add(Estilo.campo("Nome (até 60 caracteres)", nome), BorderLayout.CENTER);
        entrada.add(nova, BorderLayout.EAST);
        painel.add(entrada, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        JPanel sul = new JPanel(new BorderLayout(0, 8));
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (JButton botao : new JButton[]{renomear, excluir, atualizar, fechar}) botoes.add(botao);
        sul.add(estado, BorderLayout.NORTH);
        sul.add(botoes, BorderLayout.SOUTH);
        painel.add(sul, BorderLayout.SOUTH);
        setContentPane(painel);
        tabela.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabela.getSelectedRow() >= 0)
                nome.setText(categorias.get(tabela.getSelectedRow()).getNome());
            habilitar();
        });
        nova.addActionListener(evento -> {
            String nomeNovo = nome.getText();
            alterar(() -> servico.cadastrarCategoria(nomeNovo), "Categoria cadastrada.");
        });
        renomear.addActionListener(evento -> {
            Categoria categoria = selecionada();
            if (categoria != null) {
                String novoNome = nome.getText();
                alterar(() -> { servico.renomearCategoria(categoria.getId(), novoNome); return null; }, "Categoria renomeada.");
            }
        });
        excluir.addActionListener(evento -> {
            Categoria categoria = selecionada();
            if (categoria != null && JOptionPane.showConfirmDialog(this,
                "Excluir a categoria “" + categoria.getNome() + "”?", "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                alterar(() -> { servico.excluirCategoria(categoria.getId()); return null; }, "Categoria excluída.");
        });
        atualizar.addActionListener(evento -> carregar(""));
        fechar.addActionListener(evento -> fechar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent evento) { if (!ocupado) fechar(); }
        });
        setSize(750, 480);
        setMinimumSize(new Dimension(700, 400));
        setLocationRelativeTo(dono);
        carregar("");
    }
    private Categoria selecionada() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : categorias.get(linha);
    }
    private void alterar(Callable<?> trabalho, String mensagem) {
        TarefaTela.executar(trabalho, resultado -> {
            nome.setText("");
            carregar(mensagem);
        }, erro -> Estilo.erro(this, MensagensErro.descrever(erro, true)), this::ocupar);
    }
    private void carregar(String mensagem) {
        estado.setText(mensagem + " Consultando categorias…");
        TarefaTela.executar(servico::listarCategorias, lista -> {
            // Limpar a selecao antes de trocar a lista evita usar indices antigos.
            tabela.clearSelection();
            modelo.setRowCount(0);
            categorias = lista;
            for (Categoria categoria : lista) modelo.addRow(new Object[]{categoria.getNome()});
            disponivel = true;
            estado.setText(mensagem + " " + lista.size() + " categoria(s).");
            habilitar();
        }, erro -> {
            disponivel = false;
            estado.setText(mensagem + " Consulta não atualizada. Clique em Atualizar.");
            habilitar();
            Estilo.erro(this, mensagem + "\n" + MensagensErro.descrever(erro, false));
        }, this::ocupar);
    }
    private void ocupar(boolean valor) { ocupado = valor; habilitar(); }
    private void habilitar() {
        nome.setEnabled(!ocupado && disponivel);
        nova.setEnabled(!ocupado && disponivel);
        tabela.setEnabled(!ocupado && disponivel);
        renomear.setEnabled(!ocupado && disponivel && tabela.getSelectedRow() >= 0);
        excluir.setEnabled(renomear.isEnabled());
        atualizar.setEnabled(!ocupado);
        fechar.setEnabled(!ocupado);
    }
    private void fechar() { dispose(); aoFechar.run(); }
}
