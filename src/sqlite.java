import java.sql.*;


public class sqlite {

    public static void main(String[] args) {
        System.out.println("HELLO");

        Connection c = null;

        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:tempbd.sqlite3");
            System.out.println("Database Connected");
            PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM departamentos");
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println(resultSet.getMetaData().getColumnCount());
            while (resultSet.next()){
                System.out.println(resultSet.getInt("id"));
                System.out.println(resultSet.getString("nome"));
                System.out.println(resultSet.getString("faculdades_id"));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
