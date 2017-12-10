import org.omg.CORBA.PUBLIC_MEMBER;
import org.omg.CORBA.ServerRequest;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class Database extends UnicastRemoteObject implements DataProvider {
    Connection databaseConnection;
    private Registry registo;
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

    public static void main(String[] args) {
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(1099);
            registry.list();
        } catch (RemoteException e) {
            System.err.println("Could not retrieve the RMI registry! Make sure it is running");
            return;
        }
        System.out.println("RMI registry obtained!");

        Database bd = null;
        try {
            bd = new Database();
        } catch (SQLException e) {
            System.err.println("There was a problem connecting to the database:" + e.getMessage());
            return;
        } catch (RemoteException e) {
            System.err.println("There was a remote exception: " + e.getMessage());
            return;
        }
        bd.registo = registry;
        try {
            bd.registo.rebind("database", bd);
            System.out.println("Name bound");
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public Database() throws SQLException, RemoteException {
        super();
        this.databaseConnection = DriverManager.getConnection(this.databaseURL);
        this.prepareStatements();
        this.toggleForeignKeys(true);
        System.out.println("Database Connected");
    }

    public Database(String databaseFilename) throws SQLException, RemoteException {
        super();
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

        this.preppedStatements.addStatement(
                "CREATE DEFAULT PESSOA",
                "INSERT INTO pessoas(nome, cc, username, password, departamento_id, faculdade_id) VALUES(?, ?, ?, ?, ?, ?)"
        );
        this.preppedStatements.addStatement(
                "CREATE FULL PESSOA",
                "INSERT INTO pessoas(nome, cc, validade_cc, username, password, morada, telefone, funcao, departamento_id, faculdade_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
        this.preppedStatements.addStatement(
                "ALL PESSOAS", "SELECT * FROM pessoas;");

        this.preppedStatements.addStatement(
                "CREATE ELECTION SIMPLE", "INSERT INTO eleicoes(titulo, id_departamento, id_faculdade, tipo) VALUES(?, ?, ?, ?);");

    }

    private synchronized void toggleForeignKeys(boolean lever) throws SQLException {
        Statement statement =
                this.databaseConnection.createStatement();
        String s = "PRAGMA FOREIGN_KEYS = ";
        if (lever) {
            s += "ON;";
        } else {
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
        for (int i = 0; i < columnCount; i++) {
            faculdades[i].add(set.getMetaData().getColumnName(i + 1));
        }
        while (set.next()) {
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
        for (int i = 0; i < columnCount; i++) {
            departamentos[i] = new Vector<String>();
            departamentos[i].add(set.getMetaData().getColumnName(i + 1));
        }
        while (set.next()) {
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
        for (int i = 0; i < columnCount; i++) {
            departamentos[i] = new Vector<String>();
            departamentos[i].add(set.getMetaData().getColumnName(i + 1));
        }
        while (set.next()) {
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
        for (int i = 0; i < columnCount; i++) {
            departamentos[i] = new Vector<String>();
            departamentos[i].add(set.getMetaData().getColumnName(i + 1));
        }
        while (set.next()) {
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



    public synchronized void createPessoa(String nome, String cc, String username,
                                          String password, Integer dep_id, Integer fac_id)
            throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE DEFAULT PESSOA");
        statement.setString(1, nome);
        statement.setString(2, cc);
        statement.setString(3, username);
        statement.setString(4, password);
        if (dep_id != null){
            statement.setInt(5, dep_id);
        }
        else {
            statement.setNull(5, Types.INTEGER);
        }

        if (fac_id != null){
            statement.setInt(6, fac_id);
        }
        else {
            statement.setNull(6, Types.INTEGER);
        }
        statement.executeUpdate();

    }

    public synchronized void createPessoaFullData(String nome, String cc,
                                                  Date validadeCC, String username,
                                                  String password, String morada,
                                                  Integer telefone, PersonType funcao,
                                                  Integer dep_id, Integer fac_id)
            throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE FULL PESSOA");
        statement.setString(1, nome);
        statement.setString(2, cc);
        if (validadeCC != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            statement.setString(3, dateFormat.format(validadeCC));
        }
        else {
            statement.setString(3, "YYYY-MM-DD HH:MM:SS");
        }
        statement.setString(4, username);
        statement.setString(5, password);
        if (morada != null) {
            statement.setString(6, morada);
        }
        else {
            statement.setNull(6, Types.VARCHAR);
        }
        if (telefone != null){
            statement.setInt(7, telefone);
        }
        else{
            statement.setNull(7, Types.VARCHAR);
        }
        if (funcao != null){
            statement.setString(8, funcao.toString());
        }
        else{
            statement.setString(8, PersonType.STUDENT.toString());
        }

        if (dep_id != null){
            statement.setInt(9, dep_id);
        }
        else {
            statement.setNull(9, Types.INTEGER);
        }

        if (fac_id != null){
            statement.setInt(10, fac_id);
        }
        else {
            statement.setNull(10, Types.INTEGER);
        }
        statement.executeUpdate();

    }
    public synchronized String[][] getAllPessoas() throws SQLException {
        ResultSet set;
        set = this.preppedStatements.getStatement("ALL PESSOAS").executeQuery();
        int columnCount = set.getMetaData().getColumnCount();
        Vector[] pessoas = new Vector[columnCount];
        int[] columnTypes = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
            pessoas[i] = new Vector<String>();
            pessoas[i].add(set.getMetaData().getColumnName(i + 1));
        }
        while (set.next()) {
            pessoas[0].add(set.getInt(1));
            pessoas[1].add(set.getString(2));
            pessoas[2].add(set.getString(3));
            pessoas[3].add(set.getString(4));
            pessoas[4].add(set.getString(5));
            pessoas[5].add(set.getString(6));
            pessoas[6].add(set.getString(7));
            pessoas[7].add(set.getInt(8));
            pessoas[8].add(set.getString(9));
            pessoas[9].add(set.getInt(10));
            pessoas[10].add(set.getInt(11));
        }
        String strings[][] = new String[columnCount][pessoas[0].size()];
        for (int i = 0; i < pessoas.length; i++) {
            for (int j = 0; j < pessoas[i].size(); j++) {
                strings[i][j] = String.valueOf(pessoas[i].get(j));
            }
        }
        return strings;
    }



    public synchronized void createEleicaoSimplified(String titulo, Integer dep_id,
                                                     Integer fac_id, ElectionType tipo) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE ELECTION SIMPLE");
        statement.setString(1, titulo);
        if (dep_id == null){
            if (tipo == ElectionType.NUCLEO_ESTUDANTES || tipo == ElectionType.DEPARTAMENTO){
                throw new java.lang.IllegalArgumentException("Department id cannot be null for department elections");
            }
            else{
                statement.setNull(2, Types.INTEGER);
            }
        }
        else {
            if (tipo == ElectionType.FACULDADE || tipo == ElectionType.CONSELHO_GERAL){
                throw new java.lang.IllegalArgumentException("Department id should be null for faculdade elections");
            }
            else {
                statement.setInt(2, dep_id);
            }
        }

        if (fac_id == null){
            if (tipo == ElectionType.FACULDADE){
                throw new java.lang.IllegalArgumentException("Faculdade id cannot be null for faculdade elections");
            }
            else{
                statement.setNull(3, Types.INTEGER);
            }
        }
        else {
            if (tipo != ElectionType.FACULDADE){
                throw new java.lang.IllegalArgumentException("Faculdade id should be null for non faculdade elections");
            }
            else {
                statement.setInt(3, fac_id);
            }
        }
        statement.setString(4, tipo.getType());
        statement.executeUpdate();
    }





}
