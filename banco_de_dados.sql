-- ============================================================
--  SISTEMA DE GERENCIAMENTO DE MEDICAMENTOS HOSPITALARES
--  Banco de Dados: PostgreSQL
-- ============================================================

-- ------------------------------------------------------------
-- 1. CRIAR E CONECTAR AO BANCO
-- ------------------------------------------------------------
CREATE DATABASE farmacia_db
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'pt_BR.UTF-8'
    LC_CTYPE   = 'pt_BR.UTF-8'
    TEMPLATE   = template0;

\c farmacia_db;

-- ------------------------------------------------------------
-- 2. TIPOS ENUMERADOS (ENUMS)
-- ------------------------------------------------------------

CREATE TYPE forma_administracao AS ENUM (
    'COMPRIMIDO',
    'LIQUIDO',
    'INJETAVEL',
    'CAPSULA',
    'POMADA',
    'SUPOSITORIO',
    'INALADOR',
    'PATCH',
    'GOTAS'
);

CREATE TYPE condicao_armazenamento AS ENUM (
    'TEMPERATURA_AMBIENTE',
    'REFRIGERADO',
    'TEMPERATURA_CONTROLADA',
    'CONGELADO',
    'PROTEGIDO_LUZ',
    'PROTEGIDO_UMIDADE'
);

CREATE TYPE tipo_movimentacao AS ENUM (
    'ENTRADA',
    'SAIDA'
);

-- ------------------------------------------------------------
-- 3. TABELA: medicamentos
-- ------------------------------------------------------------

CREATE TABLE medicamentos (
    id                    SERIAL          PRIMARY KEY,
    nome                  VARCHAR(150)    NOT NULL,
    descricao             TEXT,
    lote                  VARCHAR(50)     NOT NULL,
    validade              DATE            NOT NULL,
    dosagem               VARCHAR(100)    NOT NULL,
    forma_administracao   forma_administracao   NOT NULL,
    condicao_armazenamento condicao_armazenamento NOT NULL,
    quantidade_estoque    INTEGER         NOT NULL DEFAULT 0,
    quantidade_minima     INTEGER         NOT NULL DEFAULT 1,
    criado_em             TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em         TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_quantidade_estoque  CHECK (quantidade_estoque  >= 0),
    CONSTRAINT chk_quantidade_minima   CHECK (quantidade_minima   >= 1),
    CONSTRAINT chk_validade_futura     CHECK (validade > CURRENT_DATE)
);

COMMENT ON TABLE  medicamentos                       IS 'Cadastro de medicamentos da farmácia hospitalar';
COMMENT ON COLUMN medicamentos.lote                  IS 'Número do lote do medicamento';
COMMENT ON COLUMN medicamentos.validade              IS 'Data de vencimento do medicamento';
COMMENT ON COLUMN medicamentos.quantidade_estoque    IS 'Quantidade atual em estoque';
COMMENT ON COLUMN medicamentos.quantidade_minima     IS 'Quantidade mínima antes de gerar alerta';

-- ------------------------------------------------------------
-- 4. TABELA: movimentacoes
-- ------------------------------------------------------------

CREATE TABLE movimentacoes (
    id                  SERIAL           PRIMARY KEY,
    medicamento_id      INTEGER          NOT NULL,
    tipo                tipo_movimentacao NOT NULL,
    quantidade          INTEGER          NOT NULL,
    data_movimentacao   TIMESTAMP        NOT NULL DEFAULT NOW(),
    responsavel         VARCHAR(150)     NOT NULL,
    observacao          TEXT,

    CONSTRAINT fk_movimentacao_medicamento
        FOREIGN KEY (medicamento_id)
        REFERENCES medicamentos(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_quantidade_positiva CHECK (quantidade >= 1)
);

COMMENT ON TABLE  movimentacoes                      IS 'Histórico de entradas e saídas de medicamentos';
COMMENT ON COLUMN movimentacoes.tipo                 IS 'ENTRADA = recebimento | SAIDA = dispensação/uso';
COMMENT ON COLUMN movimentacoes.responsavel          IS 'Nome do profissional responsável pela movimentação';
COMMENT ON COLUMN movimentacoes.data_movimentacao    IS 'Data e hora exata da movimentação';

-- ------------------------------------------------------------
-- 5. ÍNDICES PARA PERFORMANCE
-- ------------------------------------------------------------

-- Medicamentos
CREATE INDEX idx_medicamentos_nome       ON medicamentos (LOWER(nome));
CREATE INDEX idx_medicamentos_lote       ON medicamentos (lote);
CREATE INDEX idx_medicamentos_validade   ON medicamentos (validade);
CREATE INDEX idx_medicamentos_estoque    ON medicamentos (quantidade_estoque);

-- Movimentações
CREATE INDEX idx_movimentacoes_medicamento   ON movimentacoes (medicamento_id);
CREATE INDEX idx_movimentacoes_tipo          ON movimentacoes (tipo);
CREATE INDEX idx_movimentacoes_data          ON movimentacoes (data_movimentacao DESC);
CREATE INDEX idx_movimentacoes_responsavel   ON movimentacoes (LOWER(responsavel));

-- ------------------------------------------------------------
-- 6. TRIGGER: atualiza "atualizado_em" automaticamente
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_atualiza_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_medicamentos_atualiza_ts
    BEFORE UPDATE ON medicamentos
    FOR EACH ROW
    EXECUTE FUNCTION fn_atualiza_timestamp();

-- ------------------------------------------------------------
-- 7. TRIGGER: atualiza estoque automaticamente ao registrar movimentação
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_atualiza_estoque()
RETURNS TRIGGER AS $$
DECLARE
    estoque_atual INTEGER;
BEGIN
    -- Busca o estoque atual
    SELECT quantidade_estoque INTO estoque_atual
    FROM medicamentos
    WHERE id = NEW.medicamento_id;

    IF NEW.tipo = 'ENTRADA' THEN
        UPDATE medicamentos
        SET quantidade_estoque = quantidade_estoque + NEW.quantidade
        WHERE id = NEW.medicamento_id;

    ELSIF NEW.tipo = 'SAIDA' THEN
        IF estoque_atual < NEW.quantidade THEN
            RAISE EXCEPTION 'Estoque insuficiente. Disponível: %, Solicitado: %',
                estoque_atual, NEW.quantidade;
        END IF;

        UPDATE medicamentos
        SET quantidade_estoque = quantidade_estoque - NEW.quantidade
        WHERE id = NEW.medicamento_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_movimentacao_atualiza_estoque
    AFTER INSERT ON movimentacoes
    FOR EACH ROW
    EXECUTE FUNCTION fn_atualiza_estoque();

-- ------------------------------------------------------------
-- 8. VIEWS ÚTEIS
-- ------------------------------------------------------------

-- 8.1 Medicamentos com estoque abaixo do mínimo
CREATE VIEW vw_estoque_abaixo_minimo AS
SELECT
    id,
    nome,
    lote,
    validade,
    quantidade_estoque,
    quantidade_minima,
    (quantidade_minima - quantidade_estoque) AS quantidade_faltante
FROM medicamentos
WHERE quantidade_estoque < quantidade_minima
ORDER BY quantidade_faltante DESC;

-- 8.2 Medicamentos próximos do vencimento (próximos 30 dias)
CREATE VIEW vw_proximos_vencimento AS
SELECT
    id,
    nome,
    lote,
    validade,
    (validade - CURRENT_DATE)  AS dias_para_vencer,
    quantidade_estoque
FROM medicamentos
WHERE validade BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'
ORDER BY validade ASC;

-- 8.3 Medicamentos vencidos
CREATE VIEW vw_medicamentos_vencidos AS
SELECT
    id,
    nome,
    lote,
    validade,
    (CURRENT_DATE - validade) AS dias_vencido,
    quantidade_estoque
FROM medicamentos
WHERE validade < CURRENT_DATE
ORDER BY validade ASC;

-- 8.4 Histórico completo de movimentações (com nome do medicamento)
CREATE VIEW vw_historico_movimentacoes AS
SELECT
    mv.id,
    m.nome          AS medicamento,
    m.lote,
    mv.tipo,
    mv.quantidade,
    mv.data_movimentacao,
    mv.responsavel,
    mv.observacao
FROM movimentacoes mv
INNER JOIN medicamentos m ON m.id = mv.medicamento_id
ORDER BY mv.data_movimentacao DESC;

-- 8.5 Resumo de alertas
CREATE VIEW vw_resumo_alertas AS
SELECT
    (SELECT COUNT(*) FROM vw_estoque_abaixo_minimo)  AS alertas_estoque_baixo,
    (SELECT COUNT(*) FROM vw_proximos_vencimento)    AS alertas_vencimento_proximo,
    (SELECT COUNT(*) FROM vw_medicamentos_vencidos)  AS alertas_vencidos;

-- ------------------------------------------------------------
-- 9. DADOS DE EXEMPLO (SEED)
-- ------------------------------------------------------------

INSERT INTO medicamentos
    (nome, descricao, lote, validade, dosagem, forma_administracao, condicao_armazenamento, quantidade_estoque, quantidade_minima)
VALUES
    ('Paracetamol',      'Analgésico e antitérmico de uso comum',              'L001-2024', '2026-08-01', '500mg',  'COMPRIMIDO', 'TEMPERATURA_AMBIENTE',   200, 50),
    ('Amoxicilina',      'Antibiótico de amplo espectro',                       'L002-2024', '2026-06-15', '500mg',  'CAPSULA',    'TEMPERATURA_AMBIENTE',   80,  30),
    ('Dipirona',         'Analgésico e antitérmico em solução oral',            'L003-2024', '2026-09-20', '500mg',  'LIQUIDO',    'TEMPERATURA_AMBIENTE',   15,  20),
    ('Insulina NPH',     'Insulina de ação intermediária para diabetes',        'L004-2024', '2025-12-10', '100UI',  'INJETAVEL',  'REFRIGERADO',            40,  15),
    ('Omeprazol',        'Inibidor de bomba de prótons para úlceras gástricas', 'L005-2024', '2027-03-05', '20mg',   'CAPSULA',    'TEMPERATURA_AMBIENTE',   5,   25),
    ('Morfina',          'Opioide para controle de dor intensa',               'L006-2024', '2026-11-30', '10mg/ml','INJETAVEL',  'TEMPERATURA_CONTROLADA', 30,  10),
    ('Salbutamol',       'Broncodilatador para tratamento de asma',            'L007-2024', '2026-07-22', '100mcg', 'INALADOR',   'TEMPERATURA_AMBIENTE',   50,  15),
    ('Vitamina C',       'Suplemento vitamínico antioxidante',                 'L008-2024', '2027-01-15', '1000mg', 'COMPRIMIDO', 'PROTEGIDO_UMIDADE',      300, 50);

-- Movimentações iniciais (sem trigger de estoque pois os valores já foram inseridos)
-- Após as inserções acima, registra o histórico de entrada inicial:
INSERT INTO movimentacoes
    (medicamento_id, tipo, quantidade, data_movimentacao, responsavel, observacao)
VALUES
    (1, 'ENTRADA', 200, NOW() - INTERVAL '10 days', 'Dr. Carlos Silva',    'Entrada inicial de estoque'),
    (2, 'ENTRADA', 80,  NOW() - INTERVAL '10 days', 'Dr. Carlos Silva',    'Entrada inicial de estoque'),
    (3, 'ENTRADA', 20,  NOW() - INTERVAL '10 days', 'Enf. Ana Souza',      'Entrada inicial de estoque'),
    (3, 'SAIDA',   5,   NOW() - INTERVAL '5 days',  'Enf. Ana Souza',      'Dispensação para UTI - Leito 3'),
    (4, 'ENTRADA', 40,  NOW() - INTERVAL '8 days',  'Farm. João Lima',     'Entrada inicial de estoque'),
    (5, 'ENTRADA', 30,  NOW() - INTERVAL '8 days',  'Farm. João Lima',     'Entrada inicial de estoque'),
    (5, 'SAIDA',   25,  NOW() - INTERVAL '3 days',  'Enf. Maria Costa',    'Dispensação para enfermaria'),
    (6, 'ENTRADA', 30,  NOW() - INTERVAL '7 days',  'Dr. Carlos Silva',    'Entrada inicial de estoque'),
    (7, 'ENTRADA', 50,  NOW() - INTERVAL '6 days',  'Farm. João Lima',     'Entrada inicial de estoque'),
    (8, 'ENTRADA', 300, NOW() - INTERVAL '6 days',  'Enf. Ana Souza',      'Entrada inicial de estoque');

-- ============================================================
-- FIM DO SCRIPT
-- ============================================================
