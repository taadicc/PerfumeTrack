package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // konekcija sa bazom
    private static final String URL = "jdbc:mysql://localhost:3306/perfume_track";
    private static final String USER = "root";
    private static final String PASSWORD = "marija123";

    // privatnni konstruktor da sprijeci instanciranje klase
    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver nije pronađen.");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
