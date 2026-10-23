# TecnoLoja — Mini TCC

Sistema **desktop em Java Swing** para uma loja de tecnologia. Inclui cadastro e login de usuários, clientes, produtos, controle de estoque, registro e cancelamento de vendas, painel de resumo e exportação CSV.

## Abrir no Windows

**Aplicativo já compilado:** baixe [Loja-Tecnologia-Windows.zip](Loja-Tecnologia-Windows.zip), extraia a pasta inteira e abra `Abrir-Loja.bat`. Requer Java 17 ou superior; não exige compilação nem download de drivers.

Para executar a partir do código-fonte:

1. Instale um **JDK 17 ou superior**. Neste computador já há um JDK instalado.
2. Baixe o projeto inteiro (no GitHub: **Code → Download ZIP**) e extraia a pasta.
3. Dê dois cliques em **`Iniciar.bat`**.
4. Na primeira abertura, crie sua conta. A primeira conta é administradora; as seguintes são de funcionário.

A primeira execução compila o projeto e baixa os drivers oficiais do Maven Central. É necessário acesso à internet nesse primeiro momento. As versões e seus hashes SHA-256 ficam fixados em `build.ps1`.

O banco **H2 local é criado automaticamente**, sem instalar servidor nem configurar senha de MySQL. [Documentação do H2](https://h2database.com/html/features.html#embedded_databases).

### Depois da compilação

Também é possível executar `java -jar dist/loja-tecnologia.jar` na pasta do projeto. Para levar a aplicação para outro computador, copie **a pasta `dist` inteira**, com os arquivos de `lib`, e abra `Abrir-Loja.bat`. Nesse caso basta Java 17 ou superior. A pasta `data` será criada junto do lançador.

Cada pasta de execução possui seus próprios dados. Use sempre o mesmo lançador para acessar o mesmo banco.

## Funcionalidades

| Área | O que faz |
|---|---|
| Usuários | Cadastro, login, logout, e-mail único e senha de 8 a 128 caracteres |
| Perfis | Primeira conta administradora; demais contas de funcionário |
| Clientes | Cadastro, edição, pesquisa, exclusão e validação de CPF |
| Produtos | Cadastro, edição, exclusão, preço e estoque; alterações restritas ao administrador |
| Vendas | Carrinho com vários produtos, PIX/cartão/dinheiro, total calculado no sistema e histórico de itens |
| Estoque | Baixa automática ao concluir e devolução ao cancelar uma venda |
| Resumo | Clientes, produtos, estoque até 5 unidades e valor total de vendas concluídas |
| Exportação | CSV das listas filtradas e dos itens de uma venda, em UTF-8 para Excel |

Os dois perfis podem cadastrar clientes, consultar produtos e registrar/cancelar vendas. A aplicação é voltada ao uso interno da loja; o cadastro de novas contas ocorre na tela de acesso. Não há usuário ou senha padrão.

## Cuidados com os dados

- Senhas usam PBKDF2-HMAC-SHA256, 600.000 iterações e salt aleatório por conta.
- Consultas com dados digitados usam parâmetros SQL.
- Venda, itens e baixa de estoque são gravados em uma única transação. Se uma parte falhar, todas são desfeitas.
- Operações concorrentes bloqueiam os registros envolvidos para impedir venda acima do estoque.
- O cancelamento preserva o histórico e devolve o estoque uma única vez.
- O preço unitário fica registrado nos itens, preservando o valor da venda quando o produto muda de preço.
- Clientes e produtos vinculados a vendas não podem ser excluídos.
- O programa rejeita a finalização se o preço do produto mudou desde a montagem do carrinho.

Os arquivos locais do banco ficam em **`data/`**. Para fazer uma cópia de segurança, feche o programa e copie essa pasta. Para restaurar, feche o programa e substitua a pasta pela cópia. Esses arquivos não são enviados ao GitHub.

## Usar MySQL (opcional)

O modo local é suficiente para usar e apresentar o projeto. Para usar um servidor MySQL 8 ou superior:

1. Crie o banco executando `sql/mysql.sql` no servidor.
2. Crie um usuário do banco com permissão para criar tabelas e realizar `SELECT`, `INSERT`, `UPDATE` e `DELETE` nesse banco.
3. Copie `config.example.properties` para **`config.properties`** na pasta a partir da qual você inicia a aplicação.
4. Configure, removendo o `#` das linhas correspondentes:

```properties
db.url=jdbc:mysql://localhost:3306/BDLOJATECNOLOGIA?connectionTimeZone=LOCAL
db.user=loja
db.password=SUA_SENHA
```

As tabelas são criadas na inicialização, mantendo os nomes do modelo original. Também é possível configurar `LOJA_DB_URL`, `LOJA_DB_USER` e `LOJA_DB_PASSWORD` como variáveis de ambiente; elas têm prioridade sobre o arquivo. `config.properties` é ignorado pelo Git.

Para um servidor remoto, configure TLS conforme as exigências do servidor.

O H2 e o MySQL são bancos independentes. Trocar a configuração não transfere os cadastros entre eles. O script original com dados de exemplo está em `referencias/original/`; seus CPFs são ilustrativos e não passam pela validação do sistema. Não é necessário importá-lo.

## Compilar e verificar

No PowerShell, na pasta do projeto:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build.ps1 -Test
```

O comando compila com compatibilidade Java 17, gera `dist/loja-tecnologia.jar` e executa verificações de integração com H2 real. As verificações cobrem autenticação, perfis, validações, persistência em arquivo, totais, estoque, rollback de transação e duas vendas simultâneas disputando a última unidade.

Não é necessário Maven. O processo automático do GitHub também compila e verifica o código e disponibiliza a pasta `dist` como artefato na aba **Actions**. Os testes usam bancos temporários e não alteram os dados da aplicação.

## Organização

```text
src/main/java/br/com/lojatecnologia/
  Main.java           Inicialização
  Database.java       Configuração e conexão JDBC
  StoreService.java   Regras de negócio, autenticação e persistência
  Models.java         Modelos de usuários, clientes, produtos e vendas
  Passwords.java      Proteção de senhas
  Validation.java     Validações de campos, CPF e valores
  LoginFrame.java     Telas de acesso e cadastro
  MainFrame.java      Painel, listas e formulários
  Ui.java             Componentes, tarefas em segundo plano e exportação
src/main/resources/schema.sql  Estrutura das tabelas
src/test/java/                Verificações de integração
sql/mysql.sql                 Criação opcional do banco MySQL
referencias/original/         Material inicial preservado
```

## Limites desta versão

Este projeto registra vendas internas e formas de pagamento; não processa cobranças, não emite nota fiscal e não faz integração com bancos ou operadoras. Não inclui recuperação de senha por e-mail. O modo H2 deve ser usado por uma instância do programa por vez; para vários computadores, configure MySQL.

A validação automatizada principal é feita com H2 em modo de compatibilidade MySQL. Uma instalação MySQL deve ser verificada com a versão e as credenciais do servidor escolhido.

## Problemas comuns

- **Java não reconhecido ou versão antiga:** instale um JDK 17+ e confirme que sua pasta `bin` está no PATH.
- **Falha no primeiro download:** confira a conexão e o acesso a `repo.maven.apache.org`.
- **Banco local já aberto:** feche a outra instância do programa.
- **Dados diferentes ao abrir:** confira se está iniciando o programa na mesma pasta; o banco local é relativo à pasta de execução.
- **Erro no MySQL:** confira se o banco existe e se o usuário possui as permissões necessárias.
- **Outros erros:** consulte os registros na pasta `logs/`.
