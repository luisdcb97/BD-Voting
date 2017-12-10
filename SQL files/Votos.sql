CREATE TABLE votos(
  id INTEGER PRIMARY KEY NOT NULL,
  data DATETIME NOT NULL DEFAULT 'YYYY-MM-DD HH:MM:SS',
  id_mesa NOT NULL REFERENCES mesas ON DELETE RESTRICT,
  id_eleicao NOT NULL REFERENCES eleicoes ON DELETE RESTRICT,
  id_pessoa NOT NULL REFERENCES pessoas ON DELETE RESTRICT
);

CREATE TRIGGER insert_default_date_votes
  AFTER INSERT
  ON votos
  FOR EACH ROW
  WHEN (NEW.data IS 'YYYY-MM-DD HH:MM:SS')
BEGIN
  UPDATE votos
  SET data = datetime('now')
  WHERE data = 'YYYY-MM-DD HH:MM:SS';
END;

CREATE TRIGGER one_vote_per_person_update
  BEFORE UPDATE
  ON votos
  FOR EACH ROW
  WHEN ((new.id_pessoa, new.id_eleicao) IN (SELECT id_pessoa, id_eleicao FROM votos))
    AND (old.id_pessoa, old.id_eleicao) != (new.id_pessoa, new.id_eleicao)
BEGIN
  SELECT RAISE(FAIL, 'This person already voted on this election');
END;

CREATE TRIGGER one_vote_per_person_insert
  BEFORE INSERT
  ON votos
  FOR EACH ROW
  WHEN ((new.id_pessoa, new.id_eleicao) IN (SELECT id_pessoa, id_eleicao FROM votos))
BEGIN
  SELECT RAISE(FAIL, 'This person already voted on this election');
END;
