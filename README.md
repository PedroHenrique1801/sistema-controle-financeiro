# Sistema de controle financeiro pessoal

Aplicação desktop em Java para registrar receitas e despesas, organizar categorias e consultar o saldo geral e por categoria. Os registros ficam em um banco MySQL local e continuam disponíveis depois que o programa é fechado.

O projeto usa os conteúdos de modularização, listas, orientação a objetos e integração com banco estudados em aula. O SQL fica explícito nas classes DAO, para que seja possível acompanhar o caminho de uma informação da tela até a tabela no MySQL.

![Histórico e totais da aplicação](docs/evidencias/01-movimentacoes.png)

## O que o sistema faz

| Área | Funcionamento |
|---|---|
| Movimentações | Cadastrar, consultar, editar e excluir receitas e despesas. |
| Categorias | Cadastrar, renomear e excluir categorias sem movimentações vinculadas. |
| Filtros | Combinar datas inicial e final, categoria e tipo. Datas dos limites entram na consulta. |
| Resumo | Somar receitas, despesas e saldo da seleção; apresentar também o resultado de cada categoria. |
| Validações | Rejeitar campos obrigatórios vazios, valores inválidos, datas futuras e categorias duplicadas. |
| Persistência | Gravar no MySQL usando JDBC e comandos SQL com parâmetros. |

O saldo é calculado como receitas menos despesas dos registros selecionados. Uma categoria pode receber ambos os tipos: por exemplo, uma compra e um reembolso em Alimentação. Uma categoria só com despesas tem saldo negativo. Não há saldo inicial de conta bancária.

O uso previsto é individual, local, com uma instância da aplicação por vez. Não há login, conexão com bancos comerciais, pagamentos, parcelamentos, investimentos, lançamentos recorrentes ou previsões.

## Tecnologias e versões

As versões das dependências estão fixadas no [pom.xml](pom.xml). A linguagem alvo é Java 21; não é necessário instalar manualmente o driver JDBC nem o JUnit.

| Componente | Versão do projeto | Para que serve |
|---|---|---|
| Java | 21 LTS; validado com Temurin 21.0.13.1+2 | Linguagem, compilação e execução. |
| Swing e AWT | Incluídos no Java 21 | Janelas, formulários, tabelas e eventos. |
| JDBC | API incluída no Java 21 | Comunicação entre Java e banco de dados. |
| MySQL Community Server | 8.4.13, linha 8.4 LTS | Banco relacional, tabelas, restrições e persistência. |
| MySQL Connector/J | 26.10.0 | Driver JDBC compatível com MySQL 8.4 ou posterior. |
| Protocol Buffers Java | 4.36.1 | Dependência transitiva do driver, resolvida pelo Maven; o código do projeto não a usa diretamente. |
| Apache Maven | 3.9.9 | Compilação, dependências, testes e geração do JAR. |
| Maven Wrapper | 3.3.2, modalidade only-script | Baixa a versão de Maven indicada pelo projeto. |
| JUnit Jupiter | 5.11.4 | Framework de testes, usado somente no desenvolvimento. |

Não há framework de aplicação adicional. A interface usa Swing e a persistência usa JDBC, sem Spring, Hibernate ou JPA.

| Plugin de compilação | Versão |
|---|---|
| Maven Clean | 3.4.0 |
| Maven Resources | 3.3.1 |
| Maven Compiler | 3.13.0 |
| Maven Surefire | 3.5.2 |
| Maven JAR | 3.4.2 |
| Maven Shade | 3.6.0 |

O Shade gera um JAR que já inclui as dependências de execução. O MySQL é um servidor separado e precisa estar instalado e iniciado.

## Preparar a máquina: Windows e VS Code

Este é o caminho principal de instalação. Use os comandos na pasta que contém `pom.xml`.

### 1. Instalar o JDK 21

Baixe um **JDK 21 LTS**, como o [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=21). Escolha o sistema e a arquitetura da sua máquina, e o tipo **JDK**. O JRE sozinho não basta para compilar.

No instalador, habilite a inclusão no `PATH` e, se disponível, a definição de `JAVA_HOME`. Feche e abra o terminal após a instalação. Confira no PowerShell:

```powershell
java -version
javac -version
```

Ambos devem mostrar a versão principal `21`. Se aparecer outra versão, confira `where.exe java` e `where.exe javac`. Em Variáveis de Ambiente do Windows, `JAVA_HOME` deve apontar para a pasta do JDK, sem o sufixo `bin`; o `PATH` deve conter `%JAVA_HOME%\bin` antes de versões antigas.

### 2. Instalar e iniciar o MySQL

Instale o [MySQL Community Server 8.4 LTS](https://dev.mysql.com/downloads/mysql/8.4.html). A versão usada nos testes foi 8.4.13. Configure o servidor local na porta `3306`, defina a senha de administrador (`root`) e mantenha o serviço iniciado. Guarde essa senha para a configuração inicial.

O MySQL Workbench é opcional: ele ajuda a visualizar as tabelas durante a apresentação, mas não substitui o MySQL Server. MariaDB e SQLite não foram utilizados neste projeto.

Confira no PowerShell:

```powershell
mysql --version
```

Se o comando não for encontrado, acrescente a pasta `bin` da instalação ao `PATH`. Para a instalação padrão, apenas no terminal atual:

```powershell
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.4\bin"
```

Adapte o caminho se instalou em outra pasta. Se houver outra aplicação usando a porta 3306, use a porta configurada para o seu MySQL tanto nos comandos quanto em `config.properties`.

### 3. Abrir o projeto no VS Code

Baixe o código ou extraia o ZIP. Abra o **diretório inteiro do projeto**, onde estão `pom.xml`, `mvnw.cmd`, `sql` e `src`, em **File > Open Folder**.

Instale o [VS Code](https://code.visualstudio.com/) e a extensão [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack), da Microsoft. O arquivo `.vscode/extensions.json` já recomenda essa extensão.

Abra a paleta de comandos com `Ctrl+Shift+P`, execute **Java: Configure Java Runtime** e confira se o projeto usa JDK 21. Aguarde a importação do projeto Maven. Não é necessário criar outro projeto Java nem copiar os arquivos para uma pasta diferente.

IntelliJ IDEA e Eclipse também podem importar o `pom.xml` como projeto Maven. O VS Code é a opção documentada aqui. Editores e extensões não são dependências do programa; suas versões não fazem parte do build e não foram usadas como ambiente de teste nesta entrega.

### 4. Criar as tabelas

No terminal integrado do VS Code, a partir da raiz do projeto:

```powershell
mysql -h 127.0.0.1 -P 3306 -u root -p --default-character-set=utf8mb4
```

Digite a senha quando solicitada. No prompt `mysql>`, execute:

```sql
SOURCE sql/01_estrutura.sql;
SHOW TABLES;
```

Devem aparecer `categoria` e `movimentacao`. O script cria o banco `controle_financeiro` e suas tabelas, sem apagar registros existentes. Ele é a estrutura inicial, não um mecanismo de atualização de esquemas de versões antigas.

Se já houver um banco homônimo de outro projeto com estrutura diferente, use uma instalação separada ou faça uma cópia do script e ajuste o nome do banco e a URL. Não apague os dados antigos para instalar esta aplicação.

### 5. Criar o usuário da aplicação

Ainda no prompt do MySQL, execute os comandos abaixo, substituindo `SUA_SENHA_LOCAL` por uma senha escolhida por você:

```sql
CREATE USER 'financeiro_app'@'localhost' IDENTIFIED BY 'SUA_SENHA_LOCAL';
GRANT SELECT, INSERT, UPDATE, DELETE
ON controle_financeiro.* TO 'financeiro_app'@'localhost';
EXIT;
```

O arquivo `sql/02_usuario.exemplo.sql` contém esse modelo. Se o usuário já existir, use a senha existente; para trocá-la, o administrador pode executar `ALTER USER 'financeiro_app'@'localhost' IDENTIFIED BY 'NOVA_SENHA_LOCAL';`. A aplicação não precisa utilizar `root` nem ter permissão para apagar tabelas.

Confira o acesso:

```powershell
mysql -h 127.0.0.1 -P 3306 -u financeiro_app -p controle_financeiro
```

No MySQL, `SHOW TABLES;` deve funcionar. Use `EXIT;` para voltar ao PowerShell.

### 6. Configurar a conexão

No PowerShell:

```powershell
Copy-Item config.exemplo.properties config.properties
```

Abra `config.properties` e substitua `COLOQUE_SUA_SENHA_AQUI` pela senha do usuário `financeiro_app`. Mantenha o arquivo na raiz do projeto, com codificação UTF-8.

```properties
db.url=jdbc:mysql://127.0.0.1:3306/controle_financeiro?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=10000&connectionTimeZone=LOCAL
db.usuario=financeiro_app
db.senha=COLOQUE_SUA_SENHA_AQUI
```

Não coloque aspas em volta da senha. Como este é um arquivo Java Properties, uma barra invertida literal precisa ser escrita como `\\`. Os parâmetros de conexão fornecidos são para um banco na própria máquina; a implantação em servidor remoto não faz parte do escopo.

`config.properties` está no `.gitignore` e não deve ser publicado. Também é possível usar `FINANCEIRO_DB_URL`, `FINANCEIRO_DB_USUARIO` e `FINANCEIRO_DB_SENHA`; variáveis de ambiente, quando presentes, têm prioridade sobre o arquivo.

### 7. Compilar e executar

Na raiz do projeto:

```powershell
.\mvnw.cmd -version
.\mvnw.cmd clean package
.\executar.bat
```

A primeira execução precisa de internet para baixar o Maven e as dependências. O primeiro comando deve indicar Maven 3.9.9 e Java 21. O segundo executa os testes de lógica e gera `target/controle-financeiro-1.0.0.jar`; aguarde `BUILD SUCCESS`.

Também é possível abrir diretamente, ainda na raiz do projeto:

```powershell
java -jar target/controle-financeiro-1.0.0.jar
```

O programa abre uma janela, não uma página no navegador. Mantenha o serviço MySQL ligado. Uma consulta inicial vazia é normal: cadastre as categorias antes da primeira movimentação.

O ZIP desta entrega inclui o JAR já compilado em `target/`. Nesse pacote, depois de preparar Java, banco e configuração, você pode executar `executar.bat` diretamente. O código versionado no Git não inclui a pasta `target`: ao obtê-lo pelo GitHub, compile com Maven como indicado acima.

Para executar pelo VS Code, abra **Run and Debug**, selecione **Executar controle financeiro** e pressione `F5`. A configuração incluída usa a raiz do projeto como diretório de trabalho, onde está `config.properties`. A tecla `F9` coloca um ponto de parada para acompanhar métodos e comandos SQL no depurador.

## Linux e macOS

Instale JDK 21, MySQL Server 8.4 e, se desejar, VS Code. No Linux, é necessário um ambiente gráfico para abrir a interface Swing. Os instaladores e serviços variam conforme a distribuição; utilize os [downloads oficiais do MySQL](https://dev.mysql.com/downloads/mysql/8.4.html) e [do JDK](https://adoptium.net/temurin/releases/?version=21).

Crie o banco e o usuário com os mesmos comandos SQL das etapas anteriores. Na raiz do projeto:

```bash
chmod +x mvnw executar.sh
cp config.exemplo.properties config.properties
# Edite config.properties e preencha a senha antes de continuar.
./mvnw -version
./mvnw clean package
./executar.sh
```

Também pode usar `java -jar target/controle-financeiro-1.0.0.jar`. Se houver várias versões do Java, ajuste `JAVA_HOME` para o JDK 21. `mvnw` usa essa variável quando definida, mesmo que `java -version` no terminal indique outra instalação.

## Como usar e demonstrar

1. Em **Categorias**, cadastre Trabalho, Alimentação e Transporte. Para renomear, selecione uma linha, altere o campo de nome e pressione **Renomear selecionada**.
2. Em **Nova movimentação**, informe descrição, valor, data, tipo e categoria. Use valores como `450,00` ou `450.00`, sem `R$` e sem separador de milhar.
3. A tabela mostra os registros por data, do mais recente para o mais antigo. Para editar ou excluir, selecione uma linha na aba Movimentações.
4. Preencha os filtros e pressione **Aplicar filtros**. **Limpar filtros** volta a todos os registros. **Atualizar** repete a última seleção aplicada.
5. Consulte os cartões de totais e a aba **Saldo por categoria**. Ambos usam a mesma seleção. O texto “Seleção aplicada” identifica quais filtros produziram os resultados, mesmo se você tiver começado a digitar outros valores.
6. Feche e reabra a janela. Os registros devem continuar lá. Execute `sql/03_conferir_dados.sql` no MySQL para comparar os registros e os totais diretamente no banco.

Exemplo usado na validação, para cadastrar em um banco vazio:

| Data | Descrição | Tipo | Categoria | Valor |
|---|---|---|---|---:|
| 05/11/2026 | Salário | Receita | Trabalho | 3.000,00 |
| 06/11/2026 | Mercado | Despesa | Alimentação | 450,00 |
| 07/11/2026 | Reembolso | Receita | Alimentação | 50,00 |
| 08/11/2026 | Ônibus | Despesa | Transporte | 150,00 |

Resultado esperado: receitas de R$ 3.050,00, despesas de R$ 600,00 e saldo de R$ 2.450,00. Alimentação tem saldo de −R$ 400,00. No período de 07/11 a 08/11, o saldo é −R$ 100,00. Os dados de exemplo não são inseridos automaticamente no banco pessoal.

## Organização e estudo do código

| Caminho | Responsabilidade |
|---|---|
| `src/main/java/br/edu/financeiro/Main.java` | Iniciar a interface e carregar a configuração. |
| `modelo/` | Representar categorias, movimentações, filtros e resumos. |
| `servico/` | Validar dados, coordenar operações e calcular totais. |
| `persistencia/` | Abrir conexões e executar INSERT, SELECT, UPDATE e DELETE. |
| `ui/` | Exibir telas, receber ações do usuário e apresentar resultados e erros. |
| `src/test/java/` | Testar cálculos, regras, JDBC/MySQL e fluxo da interface. |
| `sql/` | Criar a estrutura e conferir informações diretamente no banco. |
| `docs/` | Explicar o projeto, orientar a apresentação e registrar testes reais. |

Os diretórios `modelo/`, `servico/`, `persistencia/` e `ui/` ficam dentro de `src/main/java/br/edu/financeiro/`.

Veja [ARQUITETURA.md](docs/ARQUITETURA.md) para o desenho do sistema, o relacionamento entre tabelas, o fluxograma e a relação com os conteúdos das aulas. O [roteiro de apresentação](docs/APRESENTACAO.md) acompanha uma operação da interface ao banco. Esses documentos complementam o código; o relatório da faculdade não foi alterado.

## Testes e evidências

```powershell
.\mvnw.cmd test
```

Esse comando verifica a lógica sem precisar de MySQL. A verificação com banco real é separada e exige um banco de testes. Consulte [TESTES.md](docs/TESTES.md) para configurar e executar os testes de integração e de interface, com seus resultados e limitações. Uma compilação comum não substitui esses testes.

As imagens em `docs/evidencias/` foram capturadas da aplicação executada. Novas execuções dos testes de interface gravam suas próprias evidências em `target/evidencias/`.

## Problemas comuns

| Sintoma | O que conferir |
|---|---|
| `java` ou `javac` não encontrado | Instale o JDK e ajuste `PATH`/`JAVA_HOME`. Reabra o terminal. |
| `release version 21 not supported` | Execute `mvnw.cmd -version`: o Maven precisa estar usando um JDK 21 completo. |
| `UnsupportedClassVersionError` | O Java usado para abrir o programa é antigo. Execute com Java 21. |
| Maven não consegue baixar arquivos | Confira a internet e a configuração de proxy da sua rede. Não desative a verificação de certificados. |
| Mensagem pedindo `config.properties` | Copie o exemplo, preencha-o e execute na raiz do projeto; os scripts `executar` já usam essa pasta. |
| MySQL recusou usuário ou senha | Teste o acesso pelo cliente MySQL e confira as credenciais do usuário da aplicação. |
| Banco ou tabelas não encontrados | Execute `sql/01_estrutura.sql` no servidor configurado na URL. |
| Falha de conexão | Confira serviço MySQL, endereço e porta. No Windows, veja o serviço em `services.msc`. |
| A janela informa “consulta não atualizada” | Os valores anteriores estão preservados, mas podem estar desatualizados. Restabeleça a conexão e clique em Atualizar. |
| Gravação interrompida por falha de conexão | Consulte o histórico antes de repetir: o servidor pode ter recebido a operação antes da conexão cair. |
| Categoria não pode ser excluída | Ela possui movimentações. Reclassifique os registros antes de removê-la. |
| Não aparece janela em servidor Linux | Swing precisa de sessão gráfica. O teste automatizado pode usar Xvfb. |

## Referências das ferramentas

[Java no VS Code](https://code.visualstudio.com/docs/java/java-tutorial), [Maven Wrapper](https://maven.apache.org/tools/wrapper/), [Connector/J 26.10.0](https://dev.mysql.com/doc/relnotes/connector-j/en/news-26-10-0.html), [documentação do MySQL 8.4](https://dev.mysql.com/doc/refman/8.4/en/) e [API Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/).
