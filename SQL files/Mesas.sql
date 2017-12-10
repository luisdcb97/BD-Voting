CREATE TABLE mesas(
  id INTEGER PRIMARY KEY NOT NULL,
  id_departamento INTEGER NOT NULL REFERENCES departamentos ON DELETE RESTRICT
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