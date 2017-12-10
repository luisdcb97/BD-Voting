CREATE TABLE mesas(
  id INTEGER PRIMARY KEY NOT NULL,
  id_departamento INTEGER NOT NULL REFERENCES departamentos ON DELETE RESTRICT,
  membro1 INTEGER NOT NULL REFERENCES pessoas ON DELETE RESTRICT,
  membro2 INTEGER NOT NULL REFERENCES pessoas ON DELETE RESTRICT,
  membro3 INTEGER NOT NULL REFERENCES pessoas ON DELETE RESTRICT,
  CONSTRAINT "different_members" CHECK (
    membro1 != mesas.membro2 AND membro2 != mesas.membro3 AND membro1 != mesas.membro3
  )
);

CREATE TRIGGER maximum_tables_per_department_update
  BEFORE UPDATE
  ON mesas
  FOR EACH ROW
  WHEN (SELECT COUNT(id_departamento) FROM mesas WHERE mesas.id_departamento = NEW.id_departamento) > 0
BEGIN
  SELECT RAISE(FAIL, 'Department has reached maximum tables');
END;

CREATE TRIGGER maximum_tables_per_department_insert
  BEFORE INSERT
  ON mesas
  FOR EACH ROW
  WHEN (SELECT COUNT(id_departamento) FROM mesas WHERE mesas.id_departamento = NEW.id_departamento) > 0
BEGIN
  SELECT RAISE(FAIL, 'Department has reached maximum tables');
END;