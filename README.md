Migration Project (Legacy MySQL to PostgreSQL 2.0)

Este projeto consiste num programa de migração de dados desenvolvido em Java com Spring Boot, projetado para migrar o sistema interno de chamados de TI legado (HelpDesk v1 - MySQL 5.5) para o novo ecossistema moderno (HelpDesk 2.0 - PostgreSQL 16), cumprindo rigorosas regras de negócio, tratamento de inconsistências e idempotência.

🛠️ Tecnologias Utilizadas

Java 17+

Spring Boot (Spring JDBC, DataSource Múltiplo)

Docker & Docker Compose (para ambientes MySQL e PostgreSQL)

JUnit 5 (Testes automatizados)

📋 Pré-requisitos

Certifique-se de ter instalado na sua máquina:

Docker Desktop (em execução)

Java JDK 17 ou superior

Maven

🚀 Como Preparar o Ambiente

O projeto utiliza portas personalizadas para evitar conflitos com serviços locais já existentes:

MySQL Legado: Porta 3307

PostgreSQL Novo: Porta 5433

1. Subir os Bancos de Dados via Docker

Na raiz do projeto onde se encontra o ficheiro docker-compose.yml, execute:

docker compose up -d


Este comando vai criar os contentores, restaurar automaticamente o dump do MySQL legado (legado/backup_helpdesk_legado.sql) e inicializar o schema vazio do PostgreSQL (novo/schema.sql).

Acompanhe os logs do MySQL até ver a mensagem de prontidão:

docker compose logs -f legado


⚙️ Como Executar a Migração

Após garantir que ambos os bancos estão ativos e acessíveis nas portas 3307 (MySQL) e 5433 (PostgreSQL), execute a aplicação Spring Boot através da sua IDE favorita (IntelliJ IDEA, Eclipse) ou via linha de comando com o Maven:

mvn spring-boot:run


O programa executará o processo de de-para respeitando a ordem correta de dependências (Setores -> Categorias -> Usuários -> Chamados -> Interações -> Anexos), garantindo:

Idempotência (R02): Pode ser executado múltiplas vezes sem duplicar registos.

Registo de Issues (R03): Dados inválidos ou corrigidos geram ocorrências na tabela migration_issues.

Mapeamento de IDs (R04): Todos os cruzamentos antigos e novos são guardados em migration_id_map.

🧪 Como Executar os Testes Automatizados

Para validar as regras de negócio críticas (transformação de e-mails, normalização de telefones, fuso horário, regras de nomes, etc.), execute os testes unitários e de integração com o Maven:

mvn test


📂 Estrutura do Projeto

src/main/java/com/db/migration/

model/: Entidades de mapeamento do legado e do banco novo.

repository/: Repositórios de acesso aos dados via JdbcTemplate (separados para legado e novo).

service/: Regras de negócio e motores de migração de de-para.

novo/schema.sql: Estrutura relacional do banco de destino (PostgreSQL).

legado/: Contém o backup SQL do sistema antigo.

🔄 Reiniciar o Ambiente do Zero

Se precisar de limpar todos os dados inseridos no PostgreSQL e recomeçar a migração de forma limpa:

docker compose down -v
docker compose up -d
