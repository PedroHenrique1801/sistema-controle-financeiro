-- Execute como administrador no MySQL 8.4. Nao apaga dados existentes.
CREATE DATABASE IF NOT EXISTS controle_financeiro
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci;
USE controle_financeiro;

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(60) NOT NULL,
    PRIMARY KEY (id_categoria),
    CONSTRAINT uk_categoria_nome UNIQUE (nome),
    CONSTRAINT ck_categoria_nome CHECK (CHAR_LENGTH(TRIM(nome)) BETWEEN 1 AND 60)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movimentacao (
    id_movimentacao BIGINT NOT NULL AUTO_INCREMENT,
    descricao VARCHAR(150) NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    data DATE NOT NULL,
    tipo VARCHAR(7) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    id_categoria BIGINT NOT NULL,
    PRIMARY KEY (id_movimentacao),
    CONSTRAINT fk_movimentacao_categoria FOREIGN KEY (id_categoria)
      REFERENCES categoria (id_categoria) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_movimentacao_valor CHECK (valor > 0),
    CONSTRAINT ck_movimentacao_tipo CHECK (tipo IN ('RECEITA', 'DESPESA')),
    CONSTRAINT ck_movimentacao_descricao CHECK (CHAR_LENGTH(TRIM(descricao)) BETWEEN 1 AND 150),
    INDEX idx_movimentacao_data (data)
) ENGINE=InnoDB;
