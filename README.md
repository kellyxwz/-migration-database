# Migração de Banco de Dados — MySQL para PostgreSQL

Projeto de migração de dados de um sistema legado de HelpDesk, desenvolvido com **Java e Spring Boot**, realizando a transferência e transformação de dados de um banco MySQL para uma nova estrutura em PostgreSQL.

O objetivo foi trabalhar com um cenário próximo de uma migração real, considerando diferenças entre os bancos, mudanças no modelo de dados, integridade referencial e regras de negócio.

## Sobre o projeto

O sistema legado armazenava os dados em MySQL e precisava ser substituído por uma nova estrutura em PostgreSQL.

O desafio consistiu em desenvolver uma aplicação responsável por:

- Ler os dados da base legada.
- Transformar informações conforme as regras de negócio.
- Mapear os identificadores antigos para os novos.
- Preservar os relacionamentos entre as entidades.
- Tratar registros inválidos, duplicados e inconsistentes.
- Registrar ocorrências encontradas durante a migração.
- Persistir os dados na nova estrutura.

A implementação foi desenvolvida em Java com Spring Boot, utilizando JDBC para executar as consultas e operações de persistência.

## Tecnologias utilizadas

| Tecnologia | Utilização |
|---|---|
| Java | Linguagem de programação |
| Spring Boot | Estrutura da aplicação |
| JdbcTemplate | Acesso e manipulação dos dados |
| MySQL | Banco de dados legado |
| PostgreSQL | Banco de dados de destino |
| Docker Compose | Preparação do ambiente dos bancos |
| SQL | Consultas e manipulação dos dados |
| Maven | Gerenciamento do projeto |

## Arquitetura da migração

O processo utiliza duas bases de dados independentes:

```text
       MYSQL (LEGADO)
              |
              | Leitura dos dados
              v
     JAVA + SPRING BOOT
              |
              | Transformação
              | Validação
              | Mapeamento de IDs
              v
      POSTGRESQL (NOVO)
```

A aplicação conecta-se aos dois bancos simultaneamente: consulta os dados do MySQL e realiza as operações de migração no PostgreSQL.

## Funcionalidades implementadas

- Migração de dados entre SGBDs diferentes.
- Transformação de dados conforme regras de negócio.
- Conversão e padronização de informações.
- Tratamento de inconsistências e registros inválidos.
- Mapeamento entre identificadores legados e novos IDs.
- Preservação de relacionamentos entre tabelas.
- Registro de problemas encontrados durante o processo.
- Migração de metadados de anexos.
- Organização do processo por serviços e repositórios.

## Como executar o projeto

### Pré-requisitos

Antes de começar, você precisará ter instalado:

- [Java JDK](https://www.oracle.com/java/technologies/downloads/)
- [Maven](https://maven.apache.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/)

### 1. Clone o repositório

```bash
git clone https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git
```

Entre na pasta do projeto:

```bash
cd migration
```

### 2. Prepare os bancos de dados

O repositório disponibiliza o `docker-compose.yml` e os arquivos necessários para preparar as duas bases.

Execute:

```bash
docker compose up -d
```

Esse comando inicia os containers, restaura o backup do MySQL e prepara a estrutura do PostgreSQL.

A primeira inicialização pode levar alguns minutos.

Para acompanhar a inicialização:

```bash
docker compose logs -f legado
```

Aguarde a mensagem indicando que o MySQL está pronto para receber conexões.

### 3. Confira os bancos

**MySQL — banco legado**

```bash
docker compose exec legado mysql -uroot -plegado helpdesk -e "SHOW TABLES;"
```

**PostgreSQL — banco de destino**

```bash
docker compose exec novo psql -U helpdesk -d helpdesk -c "\dt"
```

### 4. Execute a aplicação

Na raiz do projeto, execute:

```bash
mvn spring-boot:run
```

A aplicação iniciará o processo de migração conforme a configuração implementada.

## Configuração das conexões

O ambiente Docker utiliza as seguintes configurações:

| Configuração | MySQL | PostgreSQL |
|---|---|---|
| Host | localhost | localhost |
| Porta | 3307 | 5433 |
| Database | helpdesk | helpdesk |
| Usuário | root | helpdesk |
| Senha | legado | novo |

As portas foram configuradas de forma diferente das portas padrão para evitar conflitos com instalações locais.

> As credenciais acima são destinadas exclusivamente ao ambiente local de desenvolvimento.

## Arquivos disponibilizados

Além do código-fonte, o repositório contém os arquivos necessários para reproduzir o ambiente do desafio:

- `docker-compose.yml`: configuração dos containers dos bancos.
- Backup do banco legado: arquivo SQL utilizado para restaurar os dados no MySQL.
- Schema do banco novo: estrutura utilizada para preparar o PostgreSQL.

Assim, qualquer pessoa pode subir as duas bases localmente e desenvolver sua própria solução de migração.

## Como reproduzir o desafio

Quer tentar implementar sua própria solução?

1. Clone o repositório.
2. Suba os bancos utilizando Docker Compose.
3. Explore os dados do banco legado.
4. Analise a estrutura do PostgreSQL.
5. Desenvolva sua própria estratégia de migração.

Você pode utilizar Java, Python, C#, Node.js ou outra linguagem de sua preferência.

A proposta é justamente explorar os desafios envolvidos na migração de dados entre estruturas diferentes.

## Aprendizados

Esse projeto proporcionou uma experiência prática com:

- Integração de aplicações com múltiplos bancos de dados.
- Diferenças entre MySQL e PostgreSQL.
- Manipulação de dados utilizando JDBC.
- Modelagem e integridade referencial.
- Tratamento de dados inconsistentes.
- Regras de negócio aplicadas à migração.
- Organização de código e separação de responsabilidades.

---

**Desenvolvido por [Kelly Ramos](https://github.com/kellyxwz)**

[GitHub](https://github.com/kellyxwz) • [LinkedIn](https://www.linkedin.com/in/kellyxwz/)
