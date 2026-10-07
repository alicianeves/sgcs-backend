CREATE TABLE IF NOT EXISTS pessoa (
    id BIGINT NOT NULL AUTO_INCREMENT,
    telefone VARCHAR(255),
    cep VARCHAR(255),
    logradouro VARCHAR(255),
    numero VARCHAR(255),
    bairro VARCHAR(255),
    cidade VARCHAR(255),
    estado VARCHAR(255),
    status BIT(1) NOT NULL,
    data_criacao DATETIME(6),
    data_inativacao DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS fisica (
    id BIGINT NOT NULL,
    nome VARCHAR(255),
    cpf VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    idade_informada INT,
    email VARCHAR(255),
    nome_mae VARCHAR(255),
    sexo ENUM ('FEMININO', 'MASCULINO', 'OUTRO', 'PREFERE_NAO_INFORMAR'),
    estado_civil ENUM ('CASADO_A', 'DIVORCIADO_A', 'SOLTEIRO_A', 'UNIAO_ESTAVEL', 'VIUVO_A'),
    rg VARCHAR(255),
    nis VARCHAR(255),
    escolaridade ENUM ('ENSINO_FUNDAMENTAL', 'ENSINO_MEDIO', 'ENSINO_SUPERIOR', 'NAO_ALFABETIZADO_A', 'POS_GRADUACAO'),
    ocupacao VARCHAR(255),
    contato2 VARCHAR(255),
    usuario VARCHAR(255),
    senha VARCHAR(255),
    perfil ENUM ('ADMINISTRADOR', 'ATENDIMENTO_GESTAO', 'COLABORADOR', 'PROFESSOR_INSTRUTOR'),
    tipo_cadastro ENUM ('PESSOA', 'IDOSO') NOT NULL,
    familia_id BIGINT,
    vinculo_familiar ENUM ('ESPOSO_A', 'FILHO_A', 'IRMAO_A', 'MAE', 'NETO_A', 'OUTRO', 'PAI'),
    PRIMARY KEY (id),
    CONSTRAINT uk_fisica_cpf UNIQUE (cpf),
    CONSTRAINT uk_fisica_usuario UNIQUE (usuario),
    CONSTRAINT fk_fisica_pessoa FOREIGN KEY (id) REFERENCES pessoa (id),
    CONSTRAINT fk_fisica_familia FOREIGN KEY (familia_id) REFERENCES familia (id)
);

CREATE TABLE IF NOT EXISTS juridica (
    id BIGINT NOT NULL,
    razao_social VARCHAR(255),
    cnpj VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_juridica_cnpj UNIQUE (cnpj),
    CONSTRAINT fk_juridica_pessoa FOREIGN KEY (id) REFERENCES pessoa (id)
);

DELIMITER //

DROP PROCEDURE IF EXISTS migrar_pessoa_familia_v2//

CREATE PROCEDURE migrar_pessoa_familia_v2()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema = DATABASE() AND table_name = 'fisica' AND column_name = 'tipo_cadastro'
    ) THEN
        ALTER TABLE fisica ADD COLUMN tipo_cadastro ENUM ('PESSOA', 'IDOSO') NULL AFTER perfil;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
         WHERE table_schema = DATABASE() AND table_name = 'atendimento'
    ) THEN
        UPDATE fisica f
           SET f.tipo_cadastro = 'IDOSO'
         WHERE f.tipo_cadastro IS NULL
           AND f.usuario IS NULL
           AND EXISTS (SELECT 1 FROM atendimento a WHERE a.fisica_id = f.id);
    END IF;

    UPDATE fisica SET tipo_cadastro = 'PESSOA' WHERE tipo_cadastro IS NULL;
    ALTER TABLE fisica MODIFY COLUMN tipo_cadastro ENUM ('PESSOA', 'IDOSO') NOT NULL;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema = DATABASE() AND table_name = 'familia' AND column_name = 'residencia'
    ) THEN
        ALTER TABLE familia ADD COLUMN residencia ENUM ('PROPRIA', 'CEDIDA', 'ALUGADA') NULL AFTER avaliacao;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema = DATABASE() AND table_name = 'familia' AND column_name = 'valor_aluguel'
    ) THEN
        ALTER TABLE familia ADD COLUMN valor_aluguel DECIMAL(12, 2) NULL AFTER residencia;
    END IF;
END//

DELIMITER ;

CALL migrar_pessoa_familia_v2();
DROP PROCEDURE migrar_pessoa_familia_v2;

CREATE TABLE IF NOT EXISTS contato_familiar_idoso (
    id BIGINT NOT NULL AUTO_INCREMENT,
    idoso_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    data_nascimento DATE NOT NULL,
    vinculo_familiar ENUM ('ESPOSO_A', 'FILHO_A', 'IRMAO_A', 'MAE', 'NETO_A', 'OUTRO', 'PAI') NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_contato_familiar_idoso (idoso_id),
    CONSTRAINT fk_contato_familiar_idoso FOREIGN KEY (idoso_id) REFERENCES fisica (id)
);
