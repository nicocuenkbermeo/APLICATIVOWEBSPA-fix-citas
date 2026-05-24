import java.sql.*;

public class CheckDb {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/spa_db?serverTimezone=UTC";
        String user = "root";
        String pass = "Juandavidpalomo12";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT u.username, r.name FROM users u JOIN user_roles ur ON u.id = ur.user_id JOIN roles r ON ur.role_id = r.id");
            System.out.println("--- USERS AND ROLES ---");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " - " + rs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
