import Exceptions.AlreadyExists;
import Exceptions.AlreadyInTable;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Date;

public interface DataProvider extends Remote {

    void createFaculdade(String nome) throws SQLException, RemoteException;
    String[][] getAllFaculdades() throws SQLException, RemoteException;
    void deleteFaculdade(int id) throws SQLException, RemoteException;

    void createDepartamento(String nome) throws SQLException, RemoteException;
    void createDepartamento(String nome, int idFaculdade) throws SQLException, RemoteException;
    String[][] getAllDepartamentos() throws SQLException, RemoteException;
    String[][] getDepartamentosByFaculdade(int idFaculdade) throws SQLException, RemoteException;
    String[][] getFreeDepartamentos() throws SQLException, RemoteException;
    void deleteDepartamento(int id) throws SQLException, RemoteException;
    void changeFaculdadeOfDepartamento(int dep_id, int fac_id) throws SQLException, RemoteException;
    void createPessoa(String nome, String cc, String username, String password,
                      Integer dep_id, Integer fac_id) throws SQLException, RemoteException;
    void createPessoaFullData(String nome, String cc, Date validadeCC, String username,
                              String password, String morada, Integer telefone,
                              Database.PersonType funcao, Integer dep_id, Integer fac_id)
            throws SQLException, RemoteException;
    String[][] getAllPessoas() throws SQLException, RemoteException;
    void createEleicaoSimplified(String titulo, Integer dep_id, Integer fac_id,
                                 Database.ElectionType tipo) throws SQLException, RemoteException;
    void createEleicaoFull(Date inicio, Date fim, String titulo, String descricao,
                           Integer dep_id, Integer fac_id, Database.ElectionType tipo)
            throws SQLException, RemoteException;
    void createMesa(int dep_id, int id_member1, int id_member2, int id_member3)
            throws SQLException, AlreadyInTable, RemoteException;
    void associateMesaToEleicao(int id_mesa, int id_eleicao) throws SQLException,
            AlreadyInTable, RemoteException;
    void personVoted(int id_mesa, int id_eleicao, int id_pessoa, Date date)
            throws AlreadyExists, SQLException, RemoteException;
    void createLista(String nome, int eleicao, Database.ListType listType)
            throws AlreadyExists, SQLException, IllegalArgumentException, RemoteException;
    void addPersonToLista(int id_pessoa, int id_lista) throws IllegalArgumentException,
            SQLException, AlreadyExists, RemoteException;
    void prepareStatements() throws SQLException, RemoteException;
    void deletePessoa(int id) throws SQLException, RemoteException;
    void changeAddress(int id, String morada) throws SQLException, RemoteException;
    void changePhone(int id, Integer phone) throws SQLException, RemoteException;
    void changeName(int id, String name) throws SQLException, RemoteException;
    void changeCredentials(int id, String username, String password) throws SQLException, RemoteException;
    void changeCC(int id, String cc, Date validade) throws SQLException, RemoteException;
    String[][] getAllMesas() throws SQLException, RemoteException;
    void deleteMesa(int id) throws SQLException, RemoteException;
    String[][] getAllElections() throws SQLException, RemoteException;
    void deleteEleicao(int id) throws SQLException, RemoteException;
}
