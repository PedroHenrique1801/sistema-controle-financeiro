# Roteiro para demonstrar e explicar

Antes da apresentação, confirme o MySQL iniciado, a conexão configurada e o JAR compilado. Use dados fictícios em um banco reservado à demonstração. Para reproduzir os totais do README, comece sem outras movimentações.

## 1. Apresentar o problema e o produto

Explique que o programa organiza receitas e despesas que seriam anotadas de forma dispersa. Mostre a tela e diga quais resultados ela entrega: histórico, totais de receitas e despesas, saldo da seleção e saldo por categoria.

Mostre a distinção entre um lançamento e uma categoria. Alimentação é uma categoria; a compra no mercado é uma movimentação vinculada a ela. Um reembolso pode entrar na mesma categoria como receita.

## 2. Mostrar a integração acontecendo

Abra o cliente MySQL ou o Workbench ao lado da aplicação. Na interface, cadastre uma categoria e registre uma movimentação. Depois execute:

```sql
USE controle_financeiro;
SELECT id_categoria, nome FROM categoria;
SELECT id_movimentacao, descricao, valor, data, tipo, id_categoria
FROM movimentacao ORDER BY id_movimentacao DESC;
```

Mostre o registro recém-criado e o identificador gerado pelo banco. Edite o valor na interface e repita o SELECT. O identificador deve permanecer igual e o valor deve mudar. Feche e reabra a aplicação para mostrar a recuperação dos dados persistidos.

## 3. Acompanhar uma gravação no código

| Ordem | Arquivo e método | O que explicar |
|---|---|---|
| 1 | `FormularioMovimentacao.salvar` | Lê os campos, converte valor e data e cria o objeto `Movimentacao`. |
| 2 | `ServicoFinanceiro.validar` | Verifica descrição, valor, data, tipo e categoria. |
| 3 | `ServicoFinanceiro.salvar` | Um ID zero indica cadastro novo; um ID existente indica edição. |
| 4 | `MovimentacaoDAO.inserir` ou `atualizar` | Escolhe o SQL, preenche cada `?` e chama `executeUpdate`. |
| 5 | `ConexaoBanco.abrir` | Obtém a conexão JDBC com o servidor MySQL configurado. |
| 6 | `JanelaPrincipal.consultar` | Recupera os registros e atualiza a visualização após a confirmação de gravação. |

No depurador do VS Code, coloque um ponto de parada em `comando.executeUpdate()` dentro do DAO. Ao salvar na tela, acompanhe a execução e compare o banco antes e depois de liberar essa linha. `executeUpdate()` informa quantas linhas foram afetadas; no cadastro, `getGeneratedKeys()` recupera o ID criado pelo MySQL.

## 4. Demonstrar cálculos e filtros

Cadastre os quatro exemplos do README. Confira receitas de 3.050,00, despesas de 600,00 e saldo de 2.450,00. Na aba por categoria, Alimentação terá receitas de 50,00, despesas de 450,00 e saldo de −400,00.

Filtre Alimentação. Depois acrescente o tipo Receita. Limpe os filtros e consulte de 07/11/2026 a 08/11/2026. Explique que a tabela e os resumos são feitos sobre a mesma lista filtrada, e que os dois limites do período são incluídos.

Mostre o laço de `ServicoFinanceiro.resumir` e a subtração em `ResumoFinanceiro.getSaldo`. Compare com as consultas de `sql/03_conferir_dados.sql`, observando que esse arquivo mostra os totais gerais sem filtros.

## 5. Demonstrar regras e erros

| Ação | Resultado esperado |
|---|---|
| Tentar salvar valor zero | Aviso e formulário mantido; nenhum novo registro. |
| Informar 31/02/2026 | Data rejeitada. |
| Tentar criar ` alimentação ` quando Alimentação já existe | Categoria duplicada rejeitada. |
| Tentar excluir Alimentação com lançamentos | Operação bloqueada pela chave estrangeira, com mensagem compreensível. |
| Cancelar uma exclusão | Registro preservado. |
| Editar Mercado de 450,00 para 400,00 | Saldo passa de 2.450,00 para 2.500,00. |
| Depois excluir Mercado | Saldo passa para 2.900,00. |

Se for demonstrar interrupção do MySQL, faça isso somente no servidor reservado à apresentação. A consulta deve exibir um aviso de falha e preservar os dados anteriores como desatualizados. Reinicie o servidor e clique em Atualizar. Uma falha de conexão não significa que o usuário ficou sem receitas ou despesas.

## 6. Perguntas que a equipe deve saber responder

| Pergunta | Ideia central da resposta |
|---|---|
| Por que existe um DAO? | Para concentrar o SQL e a conversão entre linhas do banco e objetos Java. |
| Por que usar `PreparedStatement`? | Para passar dados separados do SQL e trabalhar corretamente com valores, datas e textos com aspas. |
| Por que `BigDecimal`? | Para somar e subtrair valores decimais com precisão, sem depender de aproximações de `double`. |
| O saldo fica salvo? | Não. Ele é calculado a partir dos registros selecionados. |
| Como o programa sabe que é edição? | A movimentação já possui um identificador; por isso o DAO executa UPDATE com `WHERE id_movimentacao = ?`. |
| Por que não excluir uma categoria em uso? | As movimentações precisam continuar vinculadas a uma categoria existente. A chave estrangeira protege esse vínculo. |
| O que acontece se a consulta falha? | A tela informa que não foi atualizada, mantém os resultados anteriores e bloqueia gravações até nova consulta bem-sucedida. |
| Por que o acesso ao banco usa `SwingWorker`? | Porque uma consulta pode demorar; ela deve ocorrer fora da thread responsável pela resposta da interface. |
| Qual é a limitação principal? | O programa atende uso local individual, sem integração bancária nem controle de múltiplos usuários simultâneos. |

Para dividir a apresentação entre três pessoas, uma pode demonstrar o funcionamento e as regras, outra explicar os modelos e cálculos, e a terceira mostrar JDBC e SQL. Cada integrante deve conseguir acompanhar pelo menos uma operação completa da tela ao banco.
