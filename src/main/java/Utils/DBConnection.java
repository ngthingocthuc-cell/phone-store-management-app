package Utils;
import java.nio.file.*;
import java.sql.*;

public final class DBConnection {
    private static final Path DIR=Path.of(System.getProperty("novamobile.dataDir", "data")).toAbsolutePath();
    private DBConnection() {}
    public static Connection getConnection() throws SQLException {
        try { Files.createDirectories(DIR); Class.forName("org.h2.Driver"); }
        catch(Exception e) {throw new SQLException("Không mở được dữ liệu: "+DIR,e);}
        String defaultUrl="jdbc:h2:file:"+DIR.resolve("QuanLyDienThoai").toString().replace('\\','/')+";DB_CLOSE_ON_EXIT=FALSE";
        return DriverManager.getConnection(System.getProperty("novamobile.jdbcUrl",defaultUrl), "sa", "");
    }
    public static Path getDatabaseDirectory() {return DIR;}
    public static boolean isPostgresConfigured() {return false;}
}
