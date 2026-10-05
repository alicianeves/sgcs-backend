CREATE TABLE IF NOT EXISTS familia (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    possui_renda_trabalho BIT(1) NOT NULL,
    valor_renda_trabalho DECIMAL(12, 2),
    possui_aposentadoria BIT(1) NOT NULL,
    valor_aposentadoria DECIMAL(12, 2),
    possui_transferencia_renda BIT(1) NOT NULL,
    valor_transferencia_renda DECIMAL(12, 2),
    possui_beneficio_municipal BIT(1) NOT NULL,
    valor_beneficio_municipal DECIMAL(12, 2),
    possui_bpc_idoso BIT(1) NOT NULL,
    valor_bpc_idoso DECIMAL(12, 2),
    possui_bpc_deficiencia BIT(1) NOT NULL,
    valor_bpc_deficiencia DECIMAL(12, 2),
    possui_bolsa_familia BIT(1) NOT NULL,
    valor_bolsa_familia DECIMAL(12, 2),
    relatos TEXT,
    avaliacao ENUM ('APROVADA', 'REPROVADA'),
    status BIT(1) NOT NULL,
    data_inativacao DATETIME(6),
    PRIMARY KEY (id)
);

DELIMITER //

DROP PROCEDURE IF EXISTS migrar_estrutura_familia//

CREATE PROCEDURE migrar_estrutura_familia()
BEGIN
    DECLARE fk_responsavel VARCHAR(64) DEFAULT NULL;

    IF NOT EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = DATABASE()
           AND table_name = 'familia'
           AND column_name = 'nome'
    ) THEN
        ALTER TABLE familia ADD COLUMN nome VARCHAR(150) NULL AFTER id;
    END IF;

    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = DATABASE()
           AND table_name = 'familia'
           AND column_name = 'responsavel_id'
    ) THEN
        UPDATE familia familia_legada
        LEFT JOIN fisica responsavel ON responsavel.id = familia_legada.responsavel_id
           SET familia_legada.nome = COALESCE(
               NULLIF(TRIM(familia_legada.nome), ''),
               CONCAT('Família de ', responsavel.nome),
               CONCAT('Família ', familia_legada.id)
           );

        SELECT constraint_name
          INTO fk_responsavel
          FROM information_schema.key_column_usage
         WHERE table_schema = DATABASE()
           AND table_name = 'familia'
           AND column_name = 'responsavel_id'
           AND referenced_table_name IS NOT NULL
         LIMIT 1;

        IF fk_responsavel IS NOT NULL THEN
            SET @sql_drop_fk = CONCAT(
                'ALTER TABLE familia DROP FOREIGN KEY `',
                REPLACE(fk_responsavel, '`', '``'),
                '`'
            );
            PREPARE drop_fk FROM @sql_drop_fk;
            EXECUTE drop_fk;
            DEALLOCATE PREPARE drop_fk;
        END IF;

        ALTER TABLE familia DROP COLUMN responsavel_id;
    END IF;

    UPDATE familia
       SET nome = CONCAT('Família ', id)
     WHERE nome IS NULL OR TRIM(nome) = '';

    ALTER TABLE familia MODIFY COLUMN nome VARCHAR(150) NOT NULL;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema = DATABASE() AND table_name = 'familia' AND column_name = 'data_cadastro'
    ) THEN
        ALTER TABLE familia DROP COLUMN data_cadastro;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema = DATABASE() AND table_name = 'familia' AND column_name = 'situacao_moradia'
    ) THEN
        ALTER TABLE familia DROP COLUMN situacao_moradia;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
         WHERE table_schema = DATABASE() AND table_name = 'familia' AND column_name = 'valor_aluguel'
    ) THEN
        ALTER TABLE familia DROP COLUMN valor_aluguel;
    END IF;
END//

DELIMITER ;

CALL migrar_estrutura_familia();
DROP PROCEDURE migrar_estrutura_familia;
