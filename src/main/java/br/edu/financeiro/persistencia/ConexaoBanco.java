package br.edu.financeiro.persistencia;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoBanco {
    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoBanco(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }
    public static ConexaoBanco carregar() throws IOException {
        return carregar(Path.of("config.properties"));
    }
    public static ConexaoBanco carregar(Path arquivo) throws IOException {
        Properties propriedades = new Properties();
        if (Files.exists(arquivo)) {
            try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                propriedades.load(leitor);
            }
        }
        String url = obter("FINANCEIRO_DB_URL", propriedades, "db.url");
        String usuario = obter("FINANCEIRO_DB_USUARIO", propriedades, "db.usuario");
        String senha = obter("FINANCEIRO_DB_SENHA", propriedades, "db.senha");
        if (url == null || url.isBlank() || usuario == null || usuario.isBlank() || senha == null)
            throw new IOException("Copie config.exemplo.properties para config.properties, preencha os dados do MySQL e execute na pasta do projeto.");
        return new ConexaoBanco(url, usuario, senha);
    }
    private static String obter(String variavel, Properties propriedades, String chave) {
        String valor = System.getenv(variavel);
        return valor == null ? propriedades.getProperty(chave) : valor;
    }
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
