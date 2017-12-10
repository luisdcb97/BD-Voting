CREATE TABLE membros_listas (
  id_lista INTEGER NOT NULL REFERENCES listas(id) ON DELETE CASCADE,
  id_pessoa INTEGER NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE
);

CREATE TRIGGER check_types_on_insert_membros_listas
  BEFORE INSERT
  ON membros_listas
  FOR EACH ROW
  WHEN (SELECT tipo FROM listas WHERE id = NEW.id_lista)
    NOT IN
    ((SELECT funcao FROM pessoas WHERE id = NEW.id_pessoa), 'BLANK', 'NULL')
BEGIN
  SELECT RAISE(FAIL, 'Person has to have same function as list');
END;

CREATE TRIGGER check_types_on_update_membros_listas
  BEFORE UPDATE
  ON membros_listas
  FOR EACH ROW
  WHEN (SELECT tipo FROM listas WHERE id = NEW.id_lista)
       NOT IN
       ((SELECT funcao FROM pessoas WHERE id = NEW.id_pessoa), 'BLANK', 'NULL')
BEGIN
  SELECT RAISE(FAIL, 'Person has to have same function as list');
END;