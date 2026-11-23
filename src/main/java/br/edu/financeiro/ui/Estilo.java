package br.edu.financeiro.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class Estilo {
    public static final Color FUNDO = new Color(242, 245, 250);
    public static final Color AZUL = new Color(28, 56, 92);
    public static final Color VERDE = new Color(13, 112, 85);
    public static final Color VERMELHO = new Color(169, 45, 53);
    private Estilo() { }

    public static void configurar() {
        try {
            for (UIManager.LookAndFeelInfo tema : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(tema.getName())) { UIManager.setLookAndFeel(tema.getClassName()); break; }
            }
        } catch (Exception erro) { /* O tema padrao do Java continua disponivel. */ }
        UIManager.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        UIManager.put("OptionPane.messageFont", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
    }
    public static <T extends JComponent> T nome(T componente, String nome) {
        componente.setName(nome);
        return componente;
    }
    public static JButton botao(String texto, String nome) { return nome(new JButton(texto), nome); }
    public static JPanel campo(String titulo, JComponent componente) {
        JPanel painel = new JPanel(new BorderLayout(0, 4));
        painel.setOpaque(false);
        JLabel rotulo = new JLabel(titulo);
        rotulo.setLabelFor(componente);
        painel.add(rotulo, BorderLayout.NORTH);
        painel.add(componente, BorderLayout.CENTER);
        return painel;
    }
    public static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int linha, int coluna) { return false; }
        };
    }
    public static JTable tabela(DefaultTableModel modelo, String nome) {
        JTable tabela = nome(new JTable(modelo), nome);
        tabela.setRowHeight(32);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setShowGrid(false);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 36));
        tabela.getAccessibleContext().setAccessibleName(nome);
        return tabela;
    }
    public static void alinharDireita(JTable tabela, int coluna) {
        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer();
        renderizador.setHorizontalAlignment(SwingConstants.RIGHT);
        tabela.getColumnModel().getColumn(coluna).setCellRenderer(renderizador);
    }
    public static void erro(Component dono, String mensagem) {
        JOptionPane.showMessageDialog(dono, mensagem, "Confira a operação", JOptionPane.WARNING_MESSAGE);
    }
    public static void margens(JComponent componente, int margem) {
        componente.setBorder(new EmptyBorder(margem, margem, margem, margem));
    }
}
