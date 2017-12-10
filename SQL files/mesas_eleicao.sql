CREATE TABLE mesas_eleicao(
  id_mesa INTEGER NOT NULL REFERENCES mesas(id) ON DELETE CASCADE,
  id_eleicao INTEGER NOT NULL REFERENCES eleicoes(id) ON DELETE CASCADE
);

CREATE TRIGGER no_table_copies_in_election_update
  BEFORE UPDATE
  ON mesas_eleicao
  FOR EACH ROW
  WHEN (new.id_mesa, new.id_eleicao) IN (SELECT * FROM mesas_eleicao)
BEGIN
  SELECT RAISE(FAIL, 'Table already in election');
END;

CREATE TRIGGER no_table_copies_in_election_insert
  BEFORE INSERT
  ON mesas_eleicao
  FOR EACH ROW
  WHEN (new.id_mesa, new.id_eleicao) IN (SELECT * FROM mesas_eleicao)
BEGIN
  SELECT RAISE(FAIL, 'Table already in election');
END;