# Testes e resultados

Validação realizada em 23/11/2026. Foram executados **28 casos de lógica, 10 casos de integração com MySQL e um fluxo completo da interface**, todos concluídos sem falhas e sem casos ignorados. Os casos parametrizados contam como execuções separadas nessa soma.

## Ambiente utilizado

| Item | Ambiente da validação |
|---|---|
| Sistema operacional | Linux x86_64 |
| JDK | Eclipse Temurin 21.0.13.1+2 LTS |
| Maven | 3.9.9 |
| MySQL Server | 8.4.13, servidor real local |
| Driver JDBC | MySQL Connector/J 26.10.0 |
| Testes | JUnit Jupiter 5.11.4 |
| Interface | Swing, tema Nimbus, tela virtual Xvfb de 1280 × 960 |

As imagens foram capturadas da aplicação em execução. O teste de interface aciona os componentes Swing na thread apropriada, aguarda as operações e confere o MySQL; ele não simula o DAO. `Robot` é usado para capturar a tela. A falha de consulta nesse fluxo é provocada por uma conexão de teste que lança `SQLException`; há também um teste de integração que tenta uma conexão TCP indisponível.

Não foi realizada validação em uma máquina Windows ou macOS, nem dentro do VS Code. As instruções desses ambientes usam as ferramentas oficiais e o mesmo projeto Maven, mas essa limitação deve ser considerada ao apresentar a entrega. A execução do JAR e os testes desta entrega ocorreram no Linux.

## Casos verificados

| Grupo | Verificação | Resultado |
|---|---|---|
| Cálculos | Receitas 3.050,00, despesas 600,00, saldo 2.450,00; Alimentação −400,00 | Passou |
| Precisão | 0,10 + 0,20 = 0,30 | Passou |
| Consulta vazia | Receitas, despesas e saldo iguais a zero | Passou |
| Valores | Zero, negativo, excesso de casas decimais e valor acima do limite rejeitados | Passou |
| Campos e datas | Obrigatoriedade, data inexistente, data futura e intervalo invertido | Passou |
| Formatos | Vírgula/ponto decimal aceitos; entradas ambíguas e separador de milhar rejeitados | Passou |
| CRUD no MySQL | Inserção, leitura, edição com o mesmo ID e exclusão | Passou |
| Persistência | Nova conexão e janela reaberta recuperam os mesmos registros | Passou |
| Filtros | Categoria, tipo, combinação e limites de datas inclusivos | Passou |
| Ordenação | Data decrescente e desempate pelo ID decrescente | Passou |
| Categorias | Duplicata rejeitada, renomeação preserva vínculo, exclusão bloqueada quando em uso | Passou |
| SQL | Aspas tratadas como texto, chave estrangeira e CHECK aplicados | Passou |
| Interface | Cadastro e edição por formulário; tabela e cartões atualizados | Passou |
| Confirmação | Cancelar exclusão preserva registro; confirmar remove | Passou |
| Falha de consulta | Aviso de desatualização, valores anteriores preservados e recuperação após Atualizar | Passou |

No fluxo de interface, editar Mercado de 450,00 para 400,00 produziu saldo 2.500,00. Cancelar a exclusão preservou os quatro registros. Confirmar a exclusão produziu saldo 2.900,00, conferido por uma consulta SQL independente.

Os testes não esgotam todos os cenários. Não foram medidos desempenho com grandes volumes, concorrência entre várias instâncias, recuperação após queda do computador, perda de resposta exatamente no momento do commit ou acessibilidade com leitor de tela. Esses itens não são apresentados como validados.

## Executar testes de lógica

No Windows, na raiz do projeto:

```powershell
.\mvnw.cmd test
```

No Linux/macOS:

```bash
./mvnw test
```

São os 28 casos de `CalculosEValidacoesTest`. Eles não dependem de banco, senha, arquivo de configuração ou sessão gráfica. `clean package` também executa esse grupo antes de gerar o JAR.

## Executar testes com MySQL

Crie um banco separado, cujo nome termine em `_teste`. Os testes recusam nomes que não atendam a essa condição. Eles criam a estrutura a partir do mesmo SQL da aplicação, portanto o usuário de testes precisa de permissão `CREATE` além das permissões de uso das tabelas.

Como administrador do MySQL:

```sql
CREATE DATABASE controle_financeiro_teste
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci;
CREATE USER 'financeiro_teste'@'localhost' IDENTIFIED BY 'SUA_SENHA_DE_TESTE';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES
ON controle_financeiro_teste.* TO 'financeiro_teste'@'localhost';
```

No PowerShell, substitua a senha:

```powershell
$env:MYSQL_TEST_URL = 'jdbc:mysql://127.0.0.1:3306/controle_financeiro_teste?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=10000'
$env:MYSQL_TEST_USUARIO = 'financeiro_teste'
$env:MYSQL_TEST_SENHA = 'SUA_SENHA_DE_TESTE'
.\mvnw.cmd clean verify -Pintegracao
```

No Linux/macOS:

```bash
export MYSQL_TEST_URL='jdbc:mysql://127.0.0.1:3306/controle_financeiro_teste?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=10000'
export MYSQL_TEST_USUARIO='financeiro_teste'
export MYSQL_TEST_SENHA='SUA_SENHA_DE_TESTE'
./mvnw clean verify -Pintegracao
```

Esse perfil executa os 28 casos de lógica e os 10 de integração, totalizando 38. A falta de configuração ou um erro de conexão faz o teste falhar; não é tratado como teste ignorado. As categorias de cada teste recebem um identificador exclusivo e a limpeza remove apenas os registros criados para aquele caso.

As tabelas de teste são criadas na primeira execução e permanecem depois. Não utilize um banco de dados pessoais para esses testes, mesmo que o nome termine em `_teste`.

## Executar o fluxo de interface

Use um banco `_teste` **vazio e exclusivo**. Após a execução de integração acima, os registros criados por ela já foram removidos, e as tabelas devem estar vazias. O teste de interface recusa um banco que já contenha categorias.

Com as mesmas três variáveis de ambiente configuradas, no Windows:

```powershell
.\mvnw.cmd test -Pinterface
```

Em uma sessão gráfica Linux/macOS:

```bash
./mvnw test -Pinterface
```

Em um ambiente Linux sem monitor, com Xvfb instalado:

```bash
xvfb-run -a -s '-screen 0 1280x960x24' ./mvnw test -Pinterface
```

As janelas serão abertas, preenchidas e fechadas automaticamente. Não interaja com elas enquanto o teste estiver em execução. O teste cadastra quatro movimentações, verifica os filtros, edita, cancela e confirma exclusão, reabre a janela e confere a consulta final no banco. Ao terminar, remove os registros de teste.

Esse perfil executa um teste integrado de interface com várias etapas. Não confunda “um teste” com “uma única ação de tela”. Os arquivos gerados ficam em `target/evidencias/`; `clean` remove essa pasta. Os relatórios do JUnit ficam em `target/surefire-reports/`.

## Evidências incluídas

| Arquivo | Conteúdo |
|---|---|
| [01-movimentacoes.png](evidencias/01-movimentacoes.png) | Quatro registros do exemplo e saldo de 2.450,00. |
| [02-saldo-por-categoria.png](evidencias/02-saldo-por-categoria.png) | Receitas, despesas e saldo de cada categoria. |
| [03-edicao.png](evidencias/03-edicao.png) | Formulário real editando Mercado para 400,00. |
| [04-falha-de-consulta.png](evidencias/04-falha-de-consulta.png) | Dados preservados, aviso de consulta desatualizada e ações bloqueadas. |
| [interface.txt](evidencias/interface.txt) | Etapas efetivamente concluídas pelo teste da interface. |
| [validacao.txt](evidencias/validacao.txt) | Resultado do JUnit, versões e conferência do pacote. |

Esses arquivos pertencem ao projeto prático. O relatório acadêmico foi preservado sem alterações.
