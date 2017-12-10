CREATE TABLE pessoas (
  id              INTEGER PRIMARY KEY NOT NULL,
  nome            VARCHAR(128)        NOT NULL,
  cc              VARCHAR(16)         NOT NULL UNIQUE,
  validade_cc     DATE                NOT NULL DEFAULT 'YYYY-MM-DD HH:MM:SS',
  username        VARCHAR(64)         NOT NULL UNIQUE,
  password        VARCHAR(64)         NOT NULL,
  morada          VARCHAR(128),
  telefone        INTEGER,
  funcao          VARCHAR(10)         NOT NULL DEFAULT 'STUDENT' CONSTRAINT "valid_job"
  CHECK ( funcao IN ('STUDENT', 'TEACHER', 'JANITOR')),
  departamento_id INTEGER                      DEFAULT NULL REFERENCES departamentos
    ON DELETE SET DEFAULT
);

CREATE TRIGGER insert_default_date
  AFTER INSERT
  ON pessoas
  FOR EACH ROW
  WHEN (NEW.validade_cc IS 'YYYY-MM-DD HH:MM:SS')
BEGIN
  UPDATE pessoas
  SET validade_cc = datetime('now', 'start of month', '+1 year')
  WHERE validade_cc = 'YYYY-MM-DD HH:MM:SS';
END;