package br.edu.financeiro.persistencia;

import br.edu.financeiro.modelo.Categoria;
import br.edu.financeiro.modelo.Filtro;
import br.edu.financeiro.modelo.Movimentacao;
import br.edu.financeiro.modelo.TipoMovimentacao;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MovimentacaoDAO {
    private final ConexaoBanco banco;
    public MovimentacaoDAO(ConexaoBanco banco) { this.banco = banco; }

    public long inserir(Movimentacao movimentacao) throws SQLException {
        String sql = "INSERT INTO movimentacao (descricao, valor, data, tipo, id_categoria) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(comando, movimentacao);
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) return chaves.getLong(1);
            }
        }
        throw new SQLException("O banco não retornou o identificador da movimentação.");
    }
    public void atualizar(Movimentacao movimentacao) throws SQLException {
        String sql = "UPDATE movimentacao SET descricao = ?, valor = ?, data = ?, tipo = ?, id_categoria = ? WHERE id_movimentacao = ?";
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(sql)) {
            preencher(comando, movimentacao);
            comando.setLong(6, movimentacao.getId());
            if (comando.executeUpdate() != 1)
                throw new IllegalArgumentException("Movimentação não encontrada. Atualize a consulta.");
        }
    }
    private void preencher(PreparedStatement comando, Movimentacao movimentacao) throws SQLException {
        comando.setString(1, movimentacao.getDescricao());
        comando.setBigDecimal(2, movimentacao.getValor());
        comando.setDate(3, Date.valueOf(movimentacao.getData()));
        comando.setString(4, movimentacao.getTipo().name());
        comando.setLong(5, movimentacao.getCategoria().getId());
    }
    public void excluir(long id) throws SQLException {
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement("DELETE FROM movimentacao WHERE id_movimentacao = ?")) {
            comando.setLong(1, id);
            if (comando.executeUpdate() != 1)
                throw new IllegalArgumentException("Movimentação não encontrada. Atualize a consulta.");
        }
    }
    public List<Movimentacao> listar(Filtro filtro) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT m.id_movimentacao, m.descricao, m.valor, m.data, m.tipo,
                   c.id_categoria, c.nome AS categoria_nome
            FROM movimentacao m JOIN categoria c ON c.id_categoria = m.id_categoria WHERE 1=1
            """);
        if (filtro.getInicio() != null) sql.append(" AND m.data >= ?");
        if (filtro.getFim() != null) sql.append(" AND m.data <= ?");
        if (filtro.getCategoriaId() != null) sql.append(" AND m.id_categoria = ?");
        if (filtro.getTipo() != null) sql.append(" AND m.tipo = ?");
        sql.append(" ORDER BY m.data DESC, m.id_movimentacao DESC");
        List<Movimentacao> movimentacoes = new ArrayList<>();
        try (Connection conexao = banco.abrir(); PreparedStatement comando = conexao.prepareStatement(sql.toString())) {
            int indice = 1;
            if (filtro.getInicio() != null) comando.setDate(indice++, Date.valueOf(filtro.getInicio()));
            if (filtro.getFim() != null) comando.setDate(indice++, Date.valueOf(filtro.getFim()));
            if (filtro.getCategoriaId() != null) comando.setLong(indice++, filtro.getCategoriaId());
            if (filtro.getTipo() != null) comando.setString(indice++, filtro.getTipo().name());
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    Categoria categoria = new Categoria(resultado.getLong("id_categoria"), resultado.getString("categoria_nome"));
                    movimentacoes.add(new Movimentacao(resultado.getLong("id_movimentacao"),
                        resultado.getString("descricao"), resultado.getBigDecimal("valor"),
                        resultado.getDate("data").toLocalDate(), TipoMovimentacao.valueOf(resultado.getString("tipo")), categoria));
                }
            }
        }
        return movimentacoes;
    }
}
