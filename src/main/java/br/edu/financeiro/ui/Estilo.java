package br.edu.financeiro.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class Estilo {
    public static final Color FUNDO = new Color(245, 247, 250);
    public static final Color AZUL = new Color(37, 99, 235);
    public static final Color VERDE = new Color(22, 128, 87);
    public static final Color VERMELHO = new Color(195, 66, 57);
    public static final Color TEXTO = new Color(32, 43, 61);
    public static final Color CINZA = new Color(100, 113, 132);
    public static final Color BORDA = new Color(222, 228, 236);
    public static final Color SELECAO = new Color(232, 240, 255);

    private Estilo() { }

    public static void configurar() {
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName()
            );
        } catch (Exception erro) {
            /* Mantem o tema disponivel. */
        }

        FontUIResource fonte =
            new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 13);

        for (Object chave : UIManager.getLookAndFeelDefaults().keySet()) {
            if (chave.toString().endsWith(".font")) {
                UIManager.put(chave, fonte);
            }
        }

        UIManager.put("Panel.background", FUNDO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("OptionPane.background", FUNDO);
        UIManager.put("Button.disabledText", CINZA);
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
    }

    public static <T extends JComponent> T nome(T componente, String nome) {
        componente.setName(nome);
        return componente;
    }

    public static JButton botao(String texto, String nome) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics desenho) {
                Graphics2D g = (Graphics2D) desenho.create();

                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color fundo = isSelected() ? SELECAO : getBackground();

                if (!isEnabled()) {
                    fundo = FUNDO;
                } else if (getModel().isPressed()) {
                    fundo = fundo.darker();
                } else if (getModel().isRollover()) {
                    fundo = fundo.equals(Color.WHITE)
                        ? FUNDO
                        : fundo.darker();
                }

                g.setColor(fundo);
                g.fillRoundRect(
                    0, 0, getWidth(), getHeight(), 12, 12
                );

                g.setColor(hasFocus() ? AZUL : BORDA);

                if (hasFocus() || getBackground().equals(Color.WHITE)) {
                    g.drawRoundRect(
                        0, 0, getWidth() - 1, getHeight() - 1, 12, 12
                    );
                }

                g.dispose();
                super.paintComponent(desenho);
            }
        };

        botao.setUI(new BasicButtonUI());
        botao.setContentAreaFilled(false);
        botao.setOpaque(false);
        botao.setFocusPainted(false);
        botao.setRolloverEnabled(true);
        botao.setBackground(Color.WHITE);
        botao.setForeground(TEXTO);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        botao.setBorder(new EmptyBorder(10, 14, 10, 14));

        return nome(botao, nome);
    }

    public static void destacar(JButton botao, Color cor) {
        botao.setBackground(cor);
        botao.setForeground(Color.WHITE);
    }

    public static JPanel painel(Color cor, int margem) {
        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics desenho) {
                super.paintComponent(desenho);

                Graphics2D g = (Graphics2D) desenho.create();

                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setColor(getBackground());
                g.fillRoundRect(
                    0, 0, getWidth(), getHeight(), 16, 16
                );

                g.dispose();
            }
        };

        painel.setOpaque(false);
        painel.setBackground(cor);
        margens(painel, margem);

        return painel;
    }

    public static JPanel campo(String titulo, JComponent componente) {
        componente.setBackground(Color.WHITE);
        componente.setForeground(TEXTO);
        componente.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

        if (componente instanceof JComboBox<?> combo) {
            combo.setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton seta = new JButton("▾");
                    seta.setUI(new BasicButtonUI());
                    seta.setContentAreaFilled(false);
                    seta.setFocusPainted(false);
                    seta.setForeground(CINZA);
                    seta.setBorder(new EmptyBorder(0, 5, 0, 5));
                    return seta;
                }
            });
        }

        componente.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                new EmptyBorder(7, 8, 7, 8)
            )
        );

        JPanel painel = new JPanel(new BorderLayout(0, 6));
        painel.setOpaque(false);

        JLabel rotulo = new JLabel(titulo);
        rotulo.setForeground(CINZA);
        rotulo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        rotulo.setLabelFor(componente);

        painel.add(rotulo, BorderLayout.NORTH);
        painel.add(componente, BorderLayout.CENTER);

        return painel;
    }

    public static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    public static JTable tabela(DefaultTableModel modelo, String nome) {
        JTable tabela = new JTable(modelo) {
            @Override
            public Component prepareRenderer(
                javax.swing.table.TableCellRenderer renderizador,
                int linha,
                int coluna
            ) {
                Component celula =
                    super.prepareRenderer(renderizador, linha, coluna);

                celula.setBackground(
                    isRowSelected(linha) ? SELECAO : Color.WHITE
                );
                celula.setForeground(TEXTO);

                if (celula instanceof JComponent item) {
                    item.setBorder(
                        BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(
                                0, 0, 1, 0, FUNDO
                            ),
                            new EmptyBorder(0, 12, 0, 12)
                        )
                    );
                }

                return celula;
            }
        };

        nome(tabela, nome);
        tabela.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        tabela.setBackground(Color.WHITE);
        tabela.setRowHeight(42);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setShowGrid(false);
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer cabecalho =
            new DefaultTableCellRenderer();

        cabecalho.setBackground(FUNDO);
        cabecalho.setForeground(CINZA);
        cabecalho.setBorder(new EmptyBorder(0, 12, 0, 12));

        tabela.getTableHeader().setDefaultRenderer(cabecalho);
        tabela.getAccessibleContext().setAccessibleName(nome);

        return tabela;
    }

    public static JScrollPane rolagem(JTable tabela) {
        JScrollPane painel = new JScrollPane(tabela);
        painel.setBorder(BorderFactory.createEmptyBorder());
        painel.getViewport().setBackground(Color.WHITE);
        return painel;
    }

    public static void alinharDireita(JTable tabela, int coluna) {
        DefaultTableCellRenderer renderizador =
            new DefaultTableCellRenderer();

        renderizador.setHorizontalAlignment(SwingConstants.RIGHT);

        tabela.getColumnModel()
            .getColumn(coluna)
            .setCellRenderer(renderizador);
    }

    public static void erro(Component dono, String mensagem) {
        JOptionPane.showMessageDialog(
            dono,
            mensagem,
            "Confira a operação",
            JOptionPane.WARNING_MESSAGE
        );
    }

    public static void margens(JComponent componente, int margem) {
        componente.setBorder(
            new EmptyBorder(margem, margem, margem, margem)
        );
    }
}