CREATE TABLE eleicoes(
  id INTEGER PRIMARY KEY NOT NULL,
  inicio DATETIME NOT NULL DEFAULT 'YYYY-MM-DD HH:MM:SS',
  fim DATETIME NOT NULL DEFAULT 'YYYY-MM-DD HH:MM:SS',
  titulo TEXT NOT NULL DEFAULT '<EMPTY>',
  descricao TEXT NOT NULL DEFAULT '<EMPTY>',
  id_departamento INTEGER DEFAULT NULL REFERENCES departamentos(id) ON DELETE SET DEFAULT,
  id_faculdade INTEGER DEFAULT NULL REFERENCES faculdades(id) ON DELETE SET DEFAULT,
  tipo VARCHAR(64) NOT NULL DEFAULT 'CONSELHO GERAL' CONSTRAINT "elections_valid_type"
    CHECK ( tipo IN ('NUCLEO ESTUDANTES', 'DEPARTAMENTO', 'FACULDADE', 'CONSELHO GERAL')),
  CONSTRAINT "validate_election_type" CHECK (
    (tipo = 'NUCLEO ESTUDANTES' AND id_departamento IS NOT NULL AND id_faculdade IS NULL)
    OR (tipo = 'DEPARTAMENTO' AND id_departamento IS NOT NULL AND id_faculdade IS NULL)
    OR (tipo = 'FACULDADE' AND id_departamento IS NULL AND id_faculdade IS NOT NULL)
    OR (tipo = 'CONSELHO GERAL' AND id_departamento IS NULL AND id_faculdade IS NULL)
  )
);


CREATE TRIGGER validate_datespan
  BEFORE INSERT
  ON eleicoes
  FOR EACH ROW
  WHEN (new.inicio > new.fim)
BEGIN
  SELECT raise(FAIL, 'Beginning has to be before the end') WHERE new.inicio > new.fim;
END;

CREATE TRIGGER insert_default_election_begin_date
  AFTER INSERT
  ON eleicoes
  FOR EACH ROW
  WHEN (NEW.inicio IS 'YYYY-MM-DD HH:MM:SS')
BEGIN
  UPDATE eleicoes
  SET inicio = datetime('now')
  WHERE inicio = 'YYYY-MM-DD HH:MM:SS';
END;

CREATE TRIGGER insert_default_election_end_date
  AFTER INSERT
  ON eleicoes
  FOR EACH ROW
  WHEN (NEW.fim IS 'YYYY-MM-DD HH:MM:SS')
BEGIN
  UPDATE eleicoes
  SET fim = datetime('now', '+3 days')
  WHERE fim = 'YYYY-MM-DD HH:MM:SS';
END;