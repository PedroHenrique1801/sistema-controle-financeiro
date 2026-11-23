package br.edu.financeiro.ui;

import br.edu.financeiro.modelo.*;
import br.edu.financeiro.servico.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;

public class FormularioMovimentacao extends JDialog {
    private final JTextField descricao = Estilo.nome(new JTextField(), "descricao");
    private final JTextField valor = Estilo.nome(new JTextField(), "valor");
    private final JTextField data = Estilo.nome(new JTextField(), "data");
    private final JComboBox<TipoMovimentacao> tipo = Estilo.nome(new JComboBox<>(TipoMovimentacao.values()), "tipo");
    private final JComboBox<Categoria> categoria = Estilo.nome(new JComboBox<>(), "categoria");
    private final JButton salvar = Estilo.botao("Salvar movimentação", "salvar");
    private final JButton cancelar = Estilo.botao("Cancelar", "cancelar");
    private final JLabel aviso = new JLabel("Informe apenas movimentações já realizadas.");
    private final ServicoFinanceiro servico;
    private final Runnable aoSalvar;
    private final long id;
    private boolean ocupado;

    public FormularioMovimentacao(JFrame dono, ServicoFinanceiro servico, List<Categoria> categorias,
                                 Movimentacao original, Runnable aoSalvar) {
        super(dono, original == null ? "Nova movimentação" : "Editar movimentação", true);
        setName("formularioMovimentacao");
        this.servico = servico;
        this.aoSalvar = aoSalvar;
        id = original == null ? 0 : original.getId();
        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 12));
        campos.add(Estilo.campo("Descrição *", descricao));
        JPanel valores = new JPanel(new GridLayout(1, 2, 12, 0));
        valores.add(Estilo.campo("Valor em R$ * (ex.: 1250,50)", valor));
        valores.add(Estilo.campo("Data * (dd/mm/aaaa)", data));
        campos.add(valores);
        JPanel classificacao = new JPanel(new GridLayout(1, 2, 12, 0));
        classificacao.add(Estilo.campo("Tipo *", tipo));
        classificacao.add(Estilo.campo("Categoria *", categoria));
        campos.add(classificacao);
        for (Categoria item : categorias) categoria.addItem(item);
        data.setText(Formatos.data(LocalDate.now()));
        if (original != null) {
            descricao.setText(original.getDescricao());
            valor.setText(original.getValor().toPlainString().replace('.', ','));
            data.setText(Formatos.data(original.getData()));
            tipo.setSelectedItem(original.getTipo());
            for (int i = 0; i < categoria.getItemCount(); i++) {
                if (categoria.getItemAt(i).getId() == original.getCategoria().getId()) categoria.setSelectedIndex(i);
            }
        }
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(cancelar);
        rodape.add(salvar);
        JPanel painel = new JPanel(new BorderLayout(0, 20));
        Estilo.margens(painel, 22);
        painel.add(aviso, BorderLayout.NORTH);
        painel.add(campos, BorderLayout.CENTER);
        painel.add(rodape, BorderLayout.SOUTH);
        setContentPane(painel);
        salvar.addActionListener(evento -> salvar());
        cancelar.addActionListener(evento -> dispose());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent evento) { if (!ocupado) dispose(); }
        });
        getRootPane().setDefaultButton(salvar);
        setSize(600, 390);
        setMinimumSize(new Dimension(560, 390));
        setLocationRelativeTo(dono);
    }
    private void salvar() {
        try {
            Movimentacao movimentacao = new Movimentacao(id, descricao.getText(), Formatos.lerValor(valor.getText()),
                Formatos.lerData(data.getText()), (TipoMovimentacao) tipo.getSelectedItem(), (Categoria) categoria.getSelectedItem());
            ServicoFinanceiro.validar(movimentacao);
            TarefaTela.executar(() -> servico.salvar(movimentacao), chave -> {
                dispose();
                aoSalvar.run();
            }, erro -> {
                aviso.setText("Confira os dados e a mensagem abaixo.");
                Estilo.erro(this, MensagensErro.descrever(erro, true));
            }, this::ocupar);
        } catch (IllegalArgumentException erro) { Estilo.erro(this, erro.getMessage()); }
    }
    private void ocupar(boolean valorOcupado) {
        ocupado = valorOcupado;
        for (JComponent campo : new JComponent[]{descricao, valor, data, tipo, categoria, salvar, cancelar}) campo.setEnabled(!ocupado);
        if (ocupado) aviso.setText("Salvando no banco de dados…");
    }
}
