import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class sqlite {

    public static void main(String[] args) {
        Database db;

        try {
            db = new Database();
//            db.associateMesaToEleicao(1, 2);
            db.associateMesaToEleicao(2, 3);
            db.associateMesaToEleicao(4, 5);
            System.out.println("END");
            /*String[][] sts = db.getAllPessoas();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }
}
