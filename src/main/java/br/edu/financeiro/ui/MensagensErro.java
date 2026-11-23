package br.edu.financeiro.ui;

import java.sql.SQLException;

public final class MensagensErro {
    private MensagensErro() { }
    public static String descrever(Throwable erro, boolean escrita) {
        if (erro instanceof IllegalArgumentException) return erro.getMessage();
        if (erro instanceof SQLException sql) {
            if (sql.getErrorCode() == 1062) return "Já existe uma categoria com esse nome. Maiúsculas e minúsculas não diferenciam categorias.";
            if (sql.getErrorCode() == 1451) return "Esta categoria possui movimentações. Altere a categoria desses registros antes de excluí-la.";
            if (sql.getErrorCode() == 1452) return "A categoria selecionada não existe mais. Atualize a consulta.";
            if (sql.getErrorCode() == 1045) return "O MySQL recusou o usuário ou a senha. Confira config.properties.";
            if (sql.getErrorCode() == 1049 || sql.getErrorCode() == 1146)
                return "O banco ou as tabelas não foram encontrados. Execute sql/01_estrutura.sql conforme o README.";
            return "Não foi possível concluir a operação no MySQL. Confira se o servidor está ligado e se a configuração está correta."
                + (escrita ? "\nA gravação pode ter ocorrido. Consulte o histórico antes de repetir a operação." : "\nOs resultados não foram atualizados.");
        }
        return "Não foi possível concluir a operação. Feche e abra o sistema novamente.";
    }
}
