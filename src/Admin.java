import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

public class Admin {
    private Registry registry;
    public DataProvider db;

    public Admin(Registry r, DataProvider dp){
        this.registry = r;
        this.db = dp;
    }

    public static void main(String[] args) {
        Registry registo = null;
        try {
            registo = LocateRegistry.getRegistry(args[0]);
            registo.list();
        } catch (RemoteException e) {
            System.err.println("Could not retrieve the RMI registry! Make sure it is running");
            return;
        }
        System.out.println("RMI registry obtained!");

        DataProvider dataProvider = null;
        try{
            dataProvider = (DataProvider) registo.lookup("database");
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        } catch (NotBoundException e) {
            e.printStackTrace();
            return;
        }

        Admin admin = new Admin(registo, dataProvider);
        try {
            String[][] sts = admin.db.getAllDepartamentos();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
