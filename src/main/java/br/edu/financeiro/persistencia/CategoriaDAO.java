package br.edu.financeiro.persistencia;

import br.edu.financeiro.modelo.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {
    private final ConexaoBanco banco;
    public CategoriaDAO(ConexaoBanco banco) { this.banco = banco; }

    public List<Categoria> listar() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria, nome FROM categoria ORDER BY nome, id_categoria";
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                categorias.add(new Categoria(resultado.getLong("id_categoria"), resultado.getString("nome")));
            }
        }
        return categorias;
    }
    public long inserir(String nome) throws SQLException {
        String sql = "INSERT INTO categoria (nome) VALUES (?)";
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, nome);
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) return chaves.getLong(1);
            }
        }
        throw new SQLException("O banco não retornou o identificador da categoria.");
    }
    public void renomear(long id, String nome) throws SQLException {
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement("UPDATE categoria SET nome = ? WHERE id_categoria = ?")) {
            comando.setString(1, nome);
            comando.setLong(2, id);
            if (comando.executeUpdate() != 1)
                throw new IllegalArgumentException("Categoria não encontrada. Atualize a consulta.");
        }
    }
    public void excluir(long id) throws SQLException {
        // A chave estrangeira impede excluir uma categoria que ainda possui movimentacoes.
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement("DELETE FROM categoria WHERE id_categoria = ?")) {
            comando.setLong(1, id);
            if (comando.executeUpdate() != 1)
                throw new IllegalArgumentException("Categoria não encontrada. Atualize a consulta.");
        }
    }
}
