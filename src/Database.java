import Exceptions.AlreadyExists;
import Exceptions.AlreadyInTable;
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

        System.getProperties().put("java.security.policy", "src/policy.all");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        System.out.println("Security policy read and enforcing...");

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
                "DELETE PESSOA", "DELETE FROM pessoas WHERE id = ?;");

        this.preppedStatements.addStatement(
                "CREATE ELECTION SIMPLE", "INSERT INTO eleicoes(titulo, id_departamento, id_faculdade, tipo) VALUES(?, ?, ?, ?);");
        this.preppedStatements.addStatement(
                "CREATE ELECTION FULL", "INSERT INTO eleicoes(inicio, fim, titulo, descricao, id_departamento, id_faculdade, tipo) VALUES(?, ?, ?, ?, ?, ?, ?);");

        this.preppedStatements.addStatement(
                "CREATE MESA", "INSERT INTO mesas(id_departamento, membro1, membro2, membro3) VALUES(?, ?, ?, ?);");
        this.preppedStatements.addStatement(
                "ASSOCIATE MESA TO ELECTION",
                "INSERT INTO mesas_eleicao(id_mesa, id_eleicao) VALUES(?, ?);");

        this.preppedStatements.addStatement(
                "CREATE VOTE", "INSERT INTO votos(id_mesa, id_eleicao, id_pessoa, data) VALUES(?, ?, ?, ?);");


        this.preppedStatements.addStatement(
                "CREATE LISTA",
                "INSERT INTO listas(nome, eleicao, tipo) VALUES(?, ?, ?);");

        this.preppedStatements.addStatement(
                "ASSOCIATE PERSON TO LISTA",
                "INSERT INTO membros_listas(id_pessoa, id_lista) VALUES(?, ?);");

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

    public synchronized void deletePessoa(int id) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("DELETE PESSOA");
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    public synchronized void changeAddress(int id, String morada) throws SQLException {
        PreparedStatement statement =
                this.databaseConnection.prepareStatement(
                "UPDATE pessoas SET morada = ? WHERE id = ?;");
        statement.setString(1, morada);
        statement.setInt(2, id);
        statement.executeUpdate();
    }

    public synchronized void changePhone(int id, Integer phone) throws SQLException {
        PreparedStatement statement =
                this.databaseConnection.prepareStatement(
                        "UPDATE pessoas SET telefone = ? WHERE id = ?;");
        statement.setInt(1, phone);
        statement.setInt(2, id);
        statement.executeUpdate();
    }

    public synchronized void changeCredentials(int id, String username, String password) throws SQLException {
        PreparedStatement statement =
                this.databaseConnection.prepareStatement(
                        "UPDATE pessoas SET username = ?, password = ? WHERE id = ?;");
        statement.setString(1, username);
        statement.setString(2, password);
        statement.setInt(3, id);
        statement.executeUpdate();
    }

    public synchronized void changeName(int id, String name) throws SQLException {
        PreparedStatement statement =
                this.databaseConnection.prepareStatement(
                        "UPDATE pessoas SET nome = ? WHERE id = ?;");
        statement.setString(1, name);
        statement.setInt(2, id);
        statement.executeUpdate();
    }

    public synchronized void changeCC(int id, String cc, Date validade) throws SQLException {
        PreparedStatement statement =
                this.databaseConnection.prepareStatement(
                        "UPDATE pessoas SET cc = ?, validade_cc = ? WHERE id = ?;");
        if (validade != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            statement.setString(2, dateFormat.format(validade));
        }
        else {
            statement.setString(2, "YYYY-MM-DD HH:MM:SS");
        }
        statement.setString(1, cc);
        statement.setInt(3, id);
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

    public synchronized void createEleicaoFull(Date inicio, Date fim, String titulo,
                                               String descricao, Integer dep_id,
                                               Integer fac_id, ElectionType tipo) throws SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE ELECTION FULL");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (inicio == null){
            statement.setString(1, "YYYY-MM-DD HH:MM:SS");
        }
        else {
            statement.setString(1, dateFormat.format(inicio));
        }

        if (fim == null){
            statement.setString(2, "YYYY-MM-DD HH:MM:SS");
        }
        else {
            statement.setString(2, dateFormat.format(fim));
        }

        statement.setString(3, titulo);
        statement.setString(4, descricao);
        if (dep_id == null){
            if (tipo == ElectionType.NUCLEO_ESTUDANTES || tipo == ElectionType.DEPARTAMENTO){
                throw new java.lang.IllegalArgumentException("Department id cannot be null for department elections");
            }
            else{
                statement.setNull(5, Types.INTEGER);
            }
        }
        else {
            if (tipo == ElectionType.FACULDADE || tipo == ElectionType.CONSELHO_GERAL){
                throw new java.lang.IllegalArgumentException("Department id should be null for faculdade elections");
            }
            else {
                statement.setInt(5, dep_id);
            }
        }

        if (fac_id == null){
            if (tipo == ElectionType.FACULDADE){
                throw new java.lang.IllegalArgumentException("Faculdade id cannot be null for faculdade elections");
            }
            else{
                statement.setNull(6, Types.INTEGER);
            }
        }
        else {
            if (tipo != ElectionType.FACULDADE){
                throw new java.lang.IllegalArgumentException("Faculdade id should be null for non faculdade elections");
            }
            else {
                statement.setInt(6, fac_id);
            }
        }
        statement.setString(7, tipo.getType());
        statement.executeUpdate();
    }


    public synchronized String[][] getAllElections() throws SQLException {
        PreparedStatement statement =
                databaseConnection.prepareStatement("SELECT * FROM eleicoes;");
        ResultSet set = statement.executeQuery();
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
            pessoas[7].add(set.getInt(6));
            pessoas[7].add(set.getInt(7));
            pessoas[5].add(set.getString(8));
        }
        String strings[][] = new String[columnCount][pessoas[0].size()];
        for (int i = 0; i < pessoas.length; i++) {
            for (int j = 0; j < pessoas[i].size(); j++) {
                strings[i][j] = String.valueOf(pessoas[i].get(j));
            }
        }
        return strings;
    }

    public synchronized void deleteEleicao(int id) throws SQLException {
        PreparedStatement statement =
                databaseConnection.prepareStatement("DELETE FROM eleicoes WHERE id = ?;");
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    public synchronized void createMesa(int dep_id, int id_member1,
                                        int id_member2, int id_member3) throws SQLException, AlreadyInTable {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE MESA");
        try{
            statement.setInt(1, dep_id);
            statement.setInt(2, id_member1);
            statement.setInt(3, id_member2);
            statement.setInt(4, id_member3);
            statement.executeUpdate();
        } catch (SQLException exc){
            if (exc.getMessage().contains("Department has reached maximum tables")){
               throw new AlreadyInTable("Department");
            }
            else if(exc.getMessage().contains("different_members")){
                throw new AlreadyInTable("Member");
            }
            else {
                throw exc;
            }
        }
    }

    public synchronized String[][] getAllMesas() throws SQLException {
        PreparedStatement statement =
                databaseConnection.prepareStatement("SELECT * FROM mesas;");
        ResultSet set;
        set = statement.executeQuery();
        int columnCount = set.getMetaData().getColumnCount();
        Vector[] mesas = new Vector[columnCount];
        for (int i = 0; i < columnCount; i++) {
            mesas[i] = new Vector<String>();
            mesas[i].add(set.getMetaData().getColumnName(i + 1));
        }
        while (set.next()) {
            mesas[0].add(set.getInt(1));
            mesas[1].add(set.getInt(2));
            mesas[2].add(set.getInt(3));
            mesas[2].add(set.getInt(4));
            mesas[2].add(set.getInt(5));
        }
        String strings[][] = new String[columnCount][mesas[0].size()];
        for (int i = 0; i < mesas.length; i++) {
            for (int j = 0; j < mesas[i].size(); j++) {
                strings[i][j] = String.valueOf(mesas[i].get(j));
            }
        }
        return strings;
    }

    public synchronized void deleteMesa(int id) throws SQLException {
        PreparedStatement statement =
                databaseConnection.prepareStatement("DELETE FROM mesas WHERE id = ?;");
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    public synchronized void associateMesaToEleicao(int id_mesa, int id_eleicao) throws SQLException, AlreadyInTable {
        PreparedStatement statement =
                this.preppedStatements.getStatement("ASSOCIATE MESA TO ELECTION");
        try {
            statement.setInt(1, id_mesa);
            statement.setInt(2, id_eleicao);
            statement.executeUpdate();
        } catch (SQLException exc) {
            if (exc.getMessage().contains("Table already in election")){
                throw new AlreadyInTable("Election");
            }
            else {
                throw exc;
            }
        }
    }

    public synchronized void personVoted(int id_mesa, int id_eleicao,
                                         int id_pessoa, Date date) throws AlreadyExists, SQLException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE VOTE");

        try{
            statement.setInt(1, id_mesa);
            statement.setInt(2, id_eleicao);
            statement.setInt(3, id_pessoa);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (date == null){
                statement.setString(4, "YYYY-MM-DD HH:MM:SS");
            }
            else {
                statement.setString(4, dateFormat.format(date));
            }
            statement.executeUpdate();
        } catch (SQLException exc){
            if (exc.getMessage().contains("This person already voted on this election")){
                throw new AlreadyExists("Vote on this election");
            }
            else {
                throw exc;
            }
        }
    }


    public synchronized void createLista(String nome, int eleicao, ListType listType) throws AlreadyExists, SQLException, IllegalArgumentException {
        PreparedStatement statement =
                this.preppedStatements.getStatement("CREATE LISTA");
        try{
            statement.setString(1, nome);
            statement.setInt(2, eleicao);
            statement.setString(3, listType.getType());
            statement.executeUpdate();
        } catch (SQLException exc){
            if (exc.getMessage().contains("This type of list cannot be in that type of election")){
                throw new IllegalArgumentException("List type not valid for election");
            }
            else if(exc.getMessage().contains("Type of list already in election")){
                throw new AlreadyExists("List in election");
            }
            else {
                throw exc;
            }
        }
    }

    public synchronized void addPersonToLista(int id_pessoa, int id_lista) throws IllegalArgumentException, SQLException, AlreadyExists {
        PreparedStatement statement =
                this.preppedStatements.getStatement("ASSOCIATE PERSON TO LISTA");
        try {
            statement.setInt(1, id_pessoa);
            statement.setInt(2, id_lista);
            statement.executeUpdate();
        } catch (SQLException exc) {
            if (exc.getMessage().contains("Person has to have same function as list")){
                throw new IllegalArgumentException("Person has to have same function as list");
            }
            else if (exc.getMessage().contains("Person already in List")){
                throw new AlreadyExists("Person in Lista");
            }
            else {
                throw exc;
            }
        }
    }

}
