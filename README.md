# SGCS

API do Sistema de Gestão do Centro Social, feita com Spring Boot, MySQL e autenticação JWT.

## Opção 1: executar tudo com Docker

Essa é a forma mais simples, pois inicia o MySQL e a API juntos.

### 1. Iniciar o projeto

Na pasta do projeto, execute:

```bash
docker compose up --build -d
```

Na primeira execução, o Docker precisa baixar as imagens e as dependências. As próximas execuções são mais rápidas.

### 2. Verificar os serviços

```bash
docker compose ps
```

Os serviços `mysql` e `app` devem aparecer como ativos. A API fica disponível em `http://localhost:8080` e o MySQL usa a porta `3306`.

Para acompanhar os logs da API:

```bash
docker compose logs -f app
```

Pressione `Ctrl+C` para sair dos logs. Isso não desliga os serviços.

## Opção 2: executar a API pelo IntelliJ

Use essa opção quando quiser depurar o código Java pela IDE.

### 1. Iniciar somente o MySQL

```bash
docker compose up -d mysql
```

### 2. Executar a aplicação

No IntelliJ, abra `SgcsApplication.java` e clique em **Run**. A configuração padrão conecta a aplicação ao MySQL em `localhost:3306`.

## Testar a API

### 1. Criar o primeiro administrador

Esse endpoint funciona uma única vez enquanto o banco possuir usuários.

```bash
curl -i -X POST http://localhost:8080/api/auth/setup \
  -H 'Content-Type: application/json' \
  -d '{
    "telefone": "11999999999",
    "cep": "01001000",
    "logradouro": "Praca da Se",
    "numero": "100",
    "bairro": "Se",
    "cidade": "Sao Paulo",
    "estado": "SP",
    "nome": "Administrador de Teste",
    "cpf": "12345678901",
    "dataNascimento": "1990-01-01",
    "email": "admin@teste.local",
    "usuario": "admin",
    "senha": "admin123"
  }'
```

A resposta esperada é `HTTP/1.1 201`.

Se a resposta for `409 Conflict`, o administrador inicial já foi criado. Nesse caso, siga para o login ou apague o banco de desenvolvimento conforme a seção **Parar ou reiniciar**.

### 2. Fazer login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"admin","senha":"admin123"}'
```

A resposta contém um campo `token`. Copie seu valor, sem as aspas.

### 3. Consultar as pessoas cadastradas

Substitua `COLE_O_TOKEN_AQUI` pelo token recebido no login:

```bash
curl http://localhost:8080/api/pessoas \
  -H 'Authorization: Bearer COLE_O_TOKEN_AQUI'
```

A resposta esperada é uma lista com o administrador cadastrado.

## Conferir o banco diretamente

Abra o cliente do MySQL dentro do contêiner:

```bash
docker exec -it mysql-sgcs mysql -uroot -proot123 sgcs_santa_rita
```

No console do MySQL, experimente:

```sql
SHOW TABLES;
SELECT id, nome, usuario, perfil FROM fisica;
exit;
```

## Executar os testes automatizados

```bash
./mvnw test
```

Os testes usam um banco H2 temporário em memória. Ele é criado no início dos testes e apagado no final, sem alterar os dados do MySQL.

## Parar ou reiniciar

Para parar os serviços e preservar os dados:

```bash
docker compose down
```

Para iniciar novamente:

```bash
docker compose up -d
```

Para apagar todo o banco de desenvolvimento e começar do zero:

```bash
docker compose down -v
docker compose up --build -d
```

O parâmetro `-v` remove o volume do MySQL e apaga definitivamente os dados locais.
