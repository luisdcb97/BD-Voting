CREATE TABLE listas (
  id    INTEGER PRIMARY KEY NOT NULL,
  nome  VARCHAR(128)        NOT NULL,
  votos INTEGER             NOT NULL DEFAULT 0,
  tipo  VARCHAR(10) NOT NULL DEFAULT 'BLANK' CONSTRAINT "lists_valid_type"
  CHECK ( tipo IN ('STUDENT', 'TEACHER', 'JANITOR', 'BLANK', 'NULL')),
  eleicao INTEGER NOT NULL REFERENCES eleicoes(id) ON DELETE CASCADE
);

CREATE TRIGGER election_list_same_type_update
  BEFORE UPDATE
  ON listas
  FOR EACH ROW
  WHEN (NEW.tipo = 'STUDENT' AND (SELECT tipo from eleicoes WHERE eleicoes.id = new.eleicao) NOT IN ('DEPARTAMENTO', 'CONSELHO GERAL'))
    OR  (NEW.tipo = 'JANITOR' AND (SELECT tipo from eleicoes WHERE eleicoes.id = new.eleicao) NOT IN ('CONSELHO GERAL'))
    OR  (NEW.tipo = 'TEACHER' AND (SELECT tipo from eleicoes WHERE eleicoes.id = new.eleicao) IN ('DEPARTAMENTO'))
BEGIN
  SELECT RAISE(FAIL, 'This type of list cannot be in that type of election');
END;

CREATE TRIGGER election_list_same_type_insert
  BEFORE INSERT
  ON listas
  FOR EACH ROW
  WHEN (NEW.tipo = 'STUDENT' AND (SELECT tipo from eleicoes WHERE eleicoes.id = new.eleicao) NOT IN ('DEPARTAMENTO', 'CONSELHO GERAL'))
       OR  (NEW.tipo = 'JANITOR' AND (SELECT tipo from eleicoes WHERE eleicoes.id = new.eleicao) NOT IN ('CONSELHO GERAL'))
       OR  (NEW.tipo = 'TEACHER' AND (SELECT tipo from eleicoes WHERE eleicoes.id = new.eleicao) IN ('DEPARTAMENTO'))
BEGIN
  SELECT RAISE(FAIL, 'This type of list cannot be in that type of election');
END;