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

        this.preppedStatements.addStatement("ALL FACULDADES", "SELECT * FROM faculdades;");
        this.preppedStatements.addStatement("ALL DEPARTAMENTOS", "SELECT * FROM departamentos;");
        this.preppedStatements.addStatement("DEPARTAMENTOS OF FACULDADE", "SELECT * FROM departamentos WHERE faculdades_id = ?;");
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

    public synchronized String[][] getAllDepartamentos() throws SQLException {
        ResultSet set;
        set = this.preppedStatements.getStatement("ALL DEPARTAMENTOS").executeQuery();
        int columnCount = set.getMetaData().getColumnCount();
        Vector[] faculdades = new Vector[columnCount];
        faculdades[0] = new Vector<String>();
        faculdades[1] = new Vector<String>();
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
}
