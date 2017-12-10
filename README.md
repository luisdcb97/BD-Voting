**Setting up the SQLite Database:**

1. Using the executable sqlite3.exe create a file named 'tempbd.sqlite3' with the command `sqlite3.exe tempbd.sqlite3`.
2. Run the files in the 'SQL files' folder with the command `.read FILENAME` in the following order:
    
    2.1 - _Table Creation_
    
       2.1.1 - Faculdade.sql 
       2.1.2 - Departamento.sql 
       2.1.3 - Pessoa.sql 
       2.1.4 - Eleicoes.sql 
       2.1.5 - Mesas.sql 
       2.1.6 - Lista.sql 
       2.1.7 - membros_lista.sql 
       2.1.8 - mesas_eleicao.sql 
       2.1.9 - Votos.sql 
       
    2.2 - _Table Filling_
    
        2.2.1 - Fill_Faculdade.sql 
        2.2.2 - Fill_Departamento.sql 
        2.2.3 - Fill_Pessoas.sql 
        2.2.4 - Fill_eleicoes.sql 
        2.2.5 - Fill_mesas.sql 
        2.2.6 - Fill_Lista.sql 
        2.2.7 - Fill_membros_listas.sql 
        2.2.8 - Fill_mesas_eleicao.sql 
        2.2.9 - Fill_votos.sql 
        
Note: The SQL scripts in **2.2** were auto-generated from a live database with using IntelliJ IDEA and as such we cannot guarantee they will meet the tables constraints or those of their triggers