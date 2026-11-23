package br.edu.financeiro;

import br.edu.financeiro.persistencia.ConexaoBanco;
import br.edu.financeiro.servico.ServicoFinanceiro;
import br.edu.financeiro.ui.Estilo;
import br.edu.financeiro.ui.JanelaPrincipal;
import java.io.IOException;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() { }
    public static void main(String[] argumentos) {
        SwingUtilities.invokeLater(() -> {
            Estilo.configurar();
            try {
                ConexaoBanco banco = ConexaoBanco.carregar();
                new JanelaPrincipal(new ServicoFinanceiro(banco)).setVisible(true);
            } catch (IOException erro) { Estilo.erro(null, erro.getMessage()); }
        });
    }
}
