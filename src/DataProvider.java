import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

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


}
