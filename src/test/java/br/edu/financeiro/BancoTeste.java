package br.edu.financeiro;

import br.edu.financeiro.persistencia.ConexaoBanco;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

/** Usa um banco separado; nunca aceita o banco normal da aplicacao. */
final class BancoTeste {
    private BancoTeste() { }
    static ConexaoBanco abrirConfiguracao() throws Exception {
        String url = System.getenv("MYSQL_TEST_URL");
        String usuario = System.getenv("MYSQL_TEST_USUARIO");
        String senha = System.getenv("MYSQL_TEST_SENHA");
        if (url == null || usuario == null || senha == null)
            throw new IllegalStateException("Defina MYSQL_TEST_URL, MYSQL_TEST_USUARIO e MYSQL_TEST_SENHA. Veja docs/TESTES.md.");
        ConexaoBanco banco = new ConexaoBanco(url, usuario, senha);
        try (Connection conexao = banco.abrir()) {
            String nome = conexao.getCatalog();
            if (nome == null || !nome.matches("[a-zA-Z0-9_]+_teste"))
                throw new IllegalStateException("Os testes exigem um banco cujo nome termine em _teste.");
            // Reutiliza o DDL de producao, sem CREATE DATABASE nem USE.
            String script = Files.readString(Path.of("sql/01_estrutura.sql"));
            script = script.substring(script.indexOf("CREATE TABLE"));
            try (Statement comando = conexao.createStatement()) {
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) comando.execute(sql);
                }
            }
        }
        return banco;
    }
}
