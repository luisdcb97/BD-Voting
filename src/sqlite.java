import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class sqlite {

    public static void main(String[] args) {
        Database db;

        try {
            db = new Database();
            db.createEleicaoSimplified("vamos ganhar", 2, null, Database.ElectionType.DEPARTAMENTO);
            db.createEleicaoSimplified("vamos ganhar?", 4, null, Database.ElectionType.NUCLEO_ESTUDANTES);
            db.createEleicaoSimplified("vamos ganhar!!!!!", null, 3, Database.ElectionType.FACULDADE);
            db.createEleicaoSimplified("bora para a AAC", null, null, Database.ElectionType.CONSELHO_GERAL);
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
