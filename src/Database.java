import org.omg.CORBA.PUBLIC_MEMBER;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class Database {
    Connection databaseConnection;
    private String databaseURL = "jdbc:sqlite:tempbd.sqlite3";
    private PreppedStatements preppedStatements;

    public enum PersonType {
        STUDENT,
        TEACHER,
        JANITOR
    }

    public enum ElectionType {
        NUCLEO_ESTUDANTES("NUCLEO ESTUDANTES"),
        DEPARTAMENTO("DEPARTAMENTO"),
        CONSELHO_GERAL("CONSELHO GERAL"),
        FACULDADE("FACULDADE");

        private final String type;

        ElectionType(String newType) {
            this.type = newType;
        }

        public String getType() {
            return this.type;
        }
    }

    public enum ListType {
        STUDENT("STUDENT"),
        TEACHER("TEACHER"),
        JANITOR("JANITOR"),
        BLANK("BLANK"),
        NULL("NULL");

        private final String type;

        ListType(String newType) {
            this.type = newType;
        }

        public String getType() {
            return this.type;
        }
    }

    public class PreppedStatements {
        HashMap<String, PreparedStatement> statements;
        Connection dbConnection;


        public PreppedStatements(Connection dbCon) {
            this.statements = new HashMap<>();
            this.dbConnection = dbCon;
        }

        public PreparedStatement addStatement(String label, String sqlStatement) throws SQLException {
            PreparedStatement statement = this.dbConnection.prepareStatement(sqlStatement);
            this.statements.put(label, statement);
            return statement;
        }

        public PreparedStatement getStatement(String label) {
            return this.statements.get(label);
        }
    }

    public Database() throws SQLException {
        this.databaseConnection = DriverManager.getConnection(this.databaseURL);
        this.prepareStatements();
        this.toggleForeignKeys(true);
        System.out.println("Database Connected");
    }

    public Database(String databaseFilename) throws SQLException {
        this.databaseConnection = DriverManager.getConnection(
                "jdbc:sqlite:" + databaseFilename);
        this.prepareStatements();
        System.out.println("Database Connected");
    }

    public synchronized void prepareStatements() throws SQLException {
        this.preppedStatements = new PreppedStatements(this.databaseConnection);

        this.preppedStatements.addStatement(
                "ALL FACULDADES", "SELECT * FROM faculdades;");
        this.preppedStatements.addStatement(
                "CREATE FACULDADE", "INSERT INTO faculdades(nome) VALUES(?);");
        this.preppedStatements.addStatement(
                "DELETE FACULDADE", "DELETE FROM faculdades WHERE id = ?;");

        this.preppedStatements.addStatement(
                "ALL DEPARTAMENTOS", "SELECT * FROM departamentos;");
        this.preppedStatements.addStatement(
                "DEPARTAMENTOS OF FACULDADE",
                "SELECT * FROM departamentos WHERE faculdades_id = ?;");
        this.preppedStatements.addStatement(
                "CHANGE FACULDADE OF DEPARTAMENTO",
                "UPDATE departamentos SET faculdades_id = ? WHERE id = ?");
        this.preppedStatements.addStatement(
                "CREATE DEPARTAMENTO", "INSERT INTO departamentos(nome) VALUES(?);");
        this.preppedStatements.addStatement(
                "CREATE DEPARTAMENTO OF FACULDADE", "INSERT INTO departamentos(nome, faculdades_id) VALUES(?, ?);");
        this.preppedStatements.addStatement(
                "DELETE DEPARTAMENTO", "DELETE FROM departamentos WHERE id = ?;");
    }

    private synchronized void toggleForeignKeys(boolean lever) throws SQLException {
        Statement statement =
                this.databaseConnection.createStatement();
        String s = "PRAGMA FOREIGN_KEYS = ";
        if (lever){
            s += "ON;";
        }
        else{
            s += "OFF;";
        }
        statement.executeUpdate(s);
    }

    public synchronized void createFaculdade(String nome) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE FACULDADE");
        statement.setString(1, nome);
        statement.executeUpdate();
    }

    public synchronized String[][] getAllFaculdades() throws SQLException {
        ResultSet set;
        set = this.preppedStatements.getStatement("ALL FACULDADES").executeQuery();
        Vector[] faculdades = new Vector[2];
        faculdades[0] = new Vector<String>();
        faculdades[1] = new Vector<String>();
        int columnCount = set.getMetaData().getColumnCount();
        for (int i = 0; i < columnCount; i++){
            faculdades[i].add(set.getMetaData().getColumnName(i+1));
        }
        while(set.next()){
            faculdades[0].add(set.getInt(1));
            faculdades[1].add(set.getString(2));
        }
        String strings[][] = new String[columnCount][faculdades[0].size()];
        for (int i = 0; i < faculdades.length; i++) {
            for (int j = 0; j < faculdades[i].size(); j++) {
                strings[i][j] = String.valueOf(faculdades[i].get(j));
            }
        }
        return strings;
    }

    public synchronized void deleteFaculdade(int id) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("DELETE FACULDADE");
        statement.setInt(1, id);
        statement.executeUpdate();
    }



    public synchronized void createDepartamento(String nome) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE DEPARTAMENTO");
        statement.setString(1, nome);
        statement.executeUpdate();
    }

    public synchronized void createDepartamento(String nome, int idFaculdade) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE DEPARTAMENTO OF FACULDADE");
        statement.setString(1, nome);
        statement.setInt(2, idFaculdade);
        statement.executeUpdate();
    }

    public synchronized String[][] getAllDepartamentos() throws SQLException {
        ResultSet set;
        set = this.preppedStatements.getStatement("ALL DEPARTAMENTOS").executeQuery();
        int columnCount = set.getMetaData().getColumnCount();
        Vector[] departamentos = new Vector[columnCount];
        for (int i = 0; i < columnCount; i++){
            departamentos[i] = new Vector<String>();
            departamentos[i].add(set.getMetaData().getColumnName(i+1));
        }
        while(set.next()){
            departamentos[0].add(set.getInt(1));
            departamentos[1].add(set.getString(2));
            departamentos[2].add(set.getInt(3));
        }
        String strings[][] = new String[columnCount][departamentos[0].size()];
        for (int i = 0; i < departamentos.length; i++) {
            for (int j = 0; j < departamentos[i].size(); j++) {
                strings[i][j] = String.valueOf(departamentos[i].get(j));
            }
        }
        return strings;
    }

    public synchronized String[][] getDepartamentosByFaculdade(int idFaculdade) throws SQLException {
        PreparedStatement statement = this.preppedStatements.getStatement("DEPARTAMENTOS OF FACULDADE");
        statement.setInt(1, idFaculdade);
        ResultSet set = statement.executeQuery();
        int columnCount = set.getMetaData().getColumnCount();
        Vector[] departamentos = new Vector[columnCount];
        for (int i = 0; i < columnCount; i++){
            departamentos[i] = new Vector<String>();
            departamentos[i].add(set.getMetaData().getColumnName(i+1));
        }
        while(set.next()){
            departamentos[0].add(set.getInt(1));
            departamentos[1].add(set.getString(2));
            departamentos[2].add(set.getInt(3));
        }
        String strings[][] = new String[columnCount][departamentos[0].size()];
        for (int i = 0; i < departamentos.length; i++) {
            for (int j = 0; j < departamentos[i].size(); j++) {
                strings[i][j] = String.valueOf(departamentos[i].get(j));
            }
        }
        return strings;
    }

    public synchronized String[][] getFreeDepartamentos() throws SQLException {
        Statement statement = this.databaseConnection.createStatement();
        ResultSet set = statement.executeQuery("SELECT * FROM departamentos WHERE faculdades_id IS NULL;");
        int columnCount = set.getMetaData().getColumnCount();
        Vector[] departamentos = new Vector[columnCount];
        for (int i = 0; i < columnCount; i++){
            departamentos[i] = new Vector<String>();
            departamentos[i].add(set.getMetaData().getColumnName(i+1));
        }
        while(set.next()){
            departamentos[0].add(set.getInt(1));
            departamentos[1].add(set.getString(2));
            departamentos[2].add(set.getInt(3));
        }
        String strings[][] = new String[columnCount][departamentos[0].size()];
        for (int i = 0; i < departamentos.length; i++) {
            for (int j = 0; j < departamentos[i].size(); j++) {
                strings[i][j] = String.valueOf(departamentos[i].get(j));
            }
        }
        return strings;
    }

    public synchronized void deleteDepartamento(int id) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("DELETE DEPARTAMENTO");
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    public synchronized void changeFaculdadeOfDepartamento(int dep_id, int fac_id) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CHANGE FACULDADE OF DEPARTAMENTO");
        statement.setInt(1, fac_id);
        statement.setInt(2, dep_id);
        statement.executeUpdate();
    }



}
