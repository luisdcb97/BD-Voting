import java.sql.*;


public class sqlite {

    public static void main(String[] args) {
        Database db;

        try{
            db = new Database();
            String strings[][] = db.getDepartamentosByFaculdade(2);
            for (int i = 0; i < strings[0].length; i++) {
                for (int j = 0; j < strings.length; j++) {
                    System.out.print(strings[j][i] + "\t");
                }
                System.out.println();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
