import java.rmi.RemoteException;
import java.sql.*;


public class sqlite {

    public static void main(String[] args) {
        Database db;

        try{
            db = new Database();
            db.changeFaculdadeOfDepartamento(4,2);
            /*String[][] sts = db.getFreeDepartamentos();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }*/
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getErrorCode());
            System.out.println(e.getSQLState());
            System.out.println(e.getMessage());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
