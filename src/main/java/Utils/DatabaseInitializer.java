package Utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class DatabaseInitializer {

    private static final String SCHEMA_RESOURCE = "/database/schema.sql";

    private DatabaseInitializer() {
    }

    /**
     * H2 remains self-initializing. PostgreSQL setup and data migration are
     * explicit operations, so normal app launches never need database-owner
     * privileges and never seed/overwrite a shared server.
     */
    public static void initialize() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            if (DBConnection.isPostgresConfigured()) {
                verifyPostgresSchema(conn);
                System.out.println("Đã kết nối database PostgreSQL dùng chung.");
            } else {
                initializeSchema(conn);
                initializeSeedData(conn);
                hashLegacyPasswords(conn);
                setExistingStaffPasswordsToDefaultOnce(conn);
                System.out.println("Khởi tạo database H2 thành công: "
                        + DBConnection.getDatabaseDirectory());
            }
        }
    }

    /** Create tables and indexes only; never insert sample data. */
    public static void initializeSchema(Connection conn) throws Exception {
        executeStatements(conn, false);
    }

    /** Add default rows after a fresh schema or after importing existing data. */
    public static void initializeSeedData(Connection conn) throws Exception {
        executeStatements(conn, true);
    }

    private static void hashLegacyPasswords(Connection conn) throws SQLException {
        try (Statement s=conn.createStatement();java.sql.ResultSet r=s.executeQuery("SELECT MaTK,MatKhau FROM TaiKhoan");java.sql.PreparedStatement update=conn.prepareStatement("UPDATE TaiKhoan SET MatKhau=? WHERE MaTK=?")) {
            while(r.next()) {String value=r.getString(2);if(!value.startsWith("pbkdf2$")){update.setString(1,Passwords.hash(value));update.setLong(2,r.getLong(1));update.addBatch();}}update.executeBatch();
        }
    }

    private static void setExistingStaffPasswordsToDefaultOnce(Connection conn)
            throws SQLException {
        final String migrationKey = "staff-default-password-123456-v1";
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS NovaAppMigration ("
                    + "MigrationKey VARCHAR(120) PRIMARY KEY, "
                    + "AppliedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }

        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM NovaAppMigration WHERE MigrationKey = ?")) {
            check.setString(1, migrationKey);
            try (java.sql.ResultSet result = check.executeQuery()) {
                if (result.next()) return;
            }
        }

        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement select = conn.prepareStatement(
                    "SELECT MaTK FROM TaiKhoan WHERE UPPER(VaiTro) <> 'ADMIN'");
                 java.sql.ResultSet accounts = select.executeQuery();
                 PreparedStatement update = conn.prepareStatement(
                         "UPDATE TaiKhoan SET MatKhau = ? WHERE MaTK = ?")) {
                while (accounts.next()) {
                    update.setString(1, Passwords.hash("123456"));
                    update.setLong(2, accounts.getLong("MaTK"));
                    update.addBatch();
                }
                update.executeBatch();
            }
            try (PreparedStatement mark = conn.prepareStatement(
                    "INSERT INTO NovaAppMigration (MigrationKey) VALUES (?)")) {
                mark.setString(1, migrationKey);
                mark.executeUpdate();
            }
            conn.commit();
        } catch (SQLException | RuntimeException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private static void verifyPostgresSchema(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.executeQuery("SELECT 1 FROM TaiKhoan WHERE 1 = 0").close();
        } catch (SQLException e) {
            throw new SQLException(
                    "Đã kết nối PostgreSQL nhưng chưa có bảng của ứng dụng. "
                    + "Cần khởi tạo schema và chuyển dữ liệu trước khi chạy app.", e
            );
        }

        try (Statement statement = conn.createStatement()) {
            statement.executeQuery("SELECT Email FROM TaiKhoan WHERE 1 = 0").close();
        } catch (SQLException e) {
            throw new SQLException(
                    "Database PostgreSQL đang thiếu cột Gmail tài khoản. "
                    + "Admin cần chạy một lần: ALTER TABLE TaiKhoan "
                    + "ADD COLUMN IF NOT EXISTS Email VARCHAR(150);", e
            );
        }
    }

    private static void executeStatements(Connection conn, boolean seedStatements)
            throws Exception {
        String script = readSchema();
        if (isPostgres(conn)) {
            script = adaptSchemaForPostgres(script);
        }

        for (String statementSql : splitStatements(script)) {
            boolean isInsert = statementSql.stripLeading()
                    .toUpperCase().startsWith("INSERT ");
            if (isInsert == seedStatements) {
                try (Statement statement = conn.createStatement()) {
                    statement.execute(statementSql);
                }
            }
        }
    }

    private static boolean isPostgres(Connection conn) throws SQLException {
        return conn.getMetaData().getDatabaseProductName()
                .toLowerCase().contains("postgresql");
    }

    private static String adaptSchemaForPostgres(String script) {
        return script
                .replace(
                        "ALTER TABLE SanPham ALTER COLUMN DungLuong VARCHAR(30)",
                        "ALTER TABLE SanPham ALTER COLUMN DungLuong TYPE VARCHAR(30)"
                )
                .replace(
                        "ALTER TABLE HoaDonChiTiet ALTER COLUMN DungLuong VARCHAR(30)",
                        "ALTER TABLE HoaDonChiTiet ALTER COLUMN DungLuong TYPE VARCHAR(30)"
                );
    }

    private static String readSchema() throws Exception {
        try (InputStream input = DatabaseInitializer.class
                .getResourceAsStream(SCHEMA_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException(
                        "Không tìm thấy " + SCHEMA_RESOURCE
                                + ". Kiểm tra thư mục src/main/resources."
                );
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String> splitStatements(String script) {
        // This schema has no stored procedures; splitting on semicolons is safe.
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        try (Scanner scanner = new Scanner(script)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.stripLeading().startsWith("--")) {
                    continue;
                }
                current.append(line).append('\n');
            }
        }

        for (String part : current.toString().split(";")) {
            String sql = part.trim();
            if (!sql.isEmpty()) {
                statements.add(sql);
            }
        }
        return statements;
    }
}
