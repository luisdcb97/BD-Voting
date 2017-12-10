import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class sqlite {

    public static void main(String[] args) {
        Database db;

        try {
            db = new Database();
            Date date = new Date();
            Date date2 = new Date();
            date2.setTime(date2.getTime() + 1000 *60 *60 *24);
            System.out.println(date.getTime());
            System.out.println(date2.getTime());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(dateFormat.format(date));
            System.out.println(dateFormat.format(date2));
            db.createEleicaoFull(date, date2, "elec_1",
                    "generic description here", 5, null,
                    Database.ElectionType.DEPARTAMENTO);
            /*String[][] sts = db.getAllPessoas();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }*/
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getErrorCode());
            System.out.println(e.getSQLState());
            System.out.println(e.getMessage());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
