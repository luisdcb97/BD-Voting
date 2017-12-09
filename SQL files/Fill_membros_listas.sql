INSERT INTO membros_listas(id_lista, id_pessoa)
  SELECT listas.id, pessoas.id
  FROM listas, pessoas
  WHERE listas.tipo = pessoas.funcao
  ORDER BY listas.tipo ASC
  LIMIT 1;

INSERT INTO membros_listas(id_lista, id_pessoa)
  SELECT listas.id, pessoas.id
  FROM listas, pessoas
  WHERE listas.tipo = pessoas.funcao
  ORDER BY listas.tipo DESC
  LIMIT 1;
