USE controle_financeiro;
SELECT m.id_movimentacao, m.descricao, m.valor, m.data, m.tipo, c.nome AS categoria
FROM movimentacao m JOIN categoria c ON c.id_categoria = m.id_categoria
ORDER BY m.data DESC, m.id_movimentacao DESC;

-- Mesma regra dos calculos feitos em Java, para comparar durante a apresentacao.
SELECT COALESCE(SUM(CASE WHEN tipo = 'RECEITA' THEN valor ELSE 0 END), 0) AS receitas,
       COALESCE(SUM(CASE WHEN tipo = 'DESPESA' THEN valor ELSE 0 END), 0) AS despesas,
       COALESCE(SUM(CASE WHEN tipo = 'RECEITA' THEN valor ELSE -valor END), 0) AS saldo
FROM movimentacao;

SELECT c.nome,
       COALESCE(SUM(CASE WHEN m.tipo = 'RECEITA' THEN m.valor ELSE 0 END), 0) AS receitas,
       COALESCE(SUM(CASE WHEN m.tipo = 'DESPESA' THEN m.valor ELSE 0 END), 0) AS despesas,
       COALESCE(SUM(CASE WHEN m.tipo = 'RECEITA' THEN m.valor ELSE -m.valor END), 0) AS saldo
FROM categoria c LEFT JOIN movimentacao m ON m.id_categoria = c.id_categoria
GROUP BY c.id_categoria, c.nome ORDER BY c.nome;
