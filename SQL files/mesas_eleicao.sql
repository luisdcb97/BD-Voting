CREATE TABLE mesas_eleicao(
  id_mesa INTEGER NOT NULL REFERENCES mesas(id) ON DELETE CASCADE,
  id_eleicao INTEGER NOT NULL REFERENCES eleicoes(id) ON DELETE CASCADE
);

CREATE TRIGGER no_table_copies_in_election_update
  BEFORE UPDATE
  ON mesas_eleicao
  FOR EACH ROW
  WHEN (SELECT COUNT(*) FROM mesas_eleicao
        WHERE new.id_mesa = id_mesa AND new.id_eleicao = id_eleicao) > 0
       AND (old.id_mesa, old.id_eleicao) != (new.id_mesa, new.id_eleicao)
BEGIN
  SELECT RAISE(FAIL, 'Table already in election');
END;

CREATE TRIGGER no_table_copies_in_election_insert
  BEFORE INSERT
  ON mesas_eleicao
  FOR EACH ROW
  WHEN (SELECT COUNT(*) FROM mesas_eleicao
        WHERE new.id_mesa = id_mesa AND new.id_eleicao = id_eleicao) > 0
BEGIN
  SELECT RAISE(FAIL, 'Table already in election');
END;