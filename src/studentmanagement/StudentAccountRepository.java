package studentmanagement;

import java.sql.*;
import java.util.Locale;

/** Registrar-managed student account creation, and mandatory first-login password changes. */
public final class StudentAccountRepository {
    private StudentAccountRepository() {}

    public static String createStudent(int registrarId, String first, String last, String number, String section) throws SQLException {
        first = first.trim(); last = last.trim(); number = number.trim(); section = section.trim();
        if (first.isEmpty() || last.isEmpty() || number.isEmpty()) throw new IllegalArgumentException("First name, last name and student number are required.");
        if (first.length()>50 || last.length()>50 || number.length()>20 || section.length()>50) throw new IllegalArgumentException("One or more fields are too long.");
        String base = (first + last).replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
        if (base.isEmpty() || base.length()>45) throw new IllegalArgumentException("Name cannot produce a valid username (maximum 45 characters).");
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s=c.prepareStatement("SELECT UserID FROM Users WHERE UserID=? AND Role='Registrar'")) {
                    s.setInt(1,registrarId); try(ResultSet r=s.executeQuery()) { if(!r.next()) throw new SecurityException("Only authenticated registrars may create students."); }
                }
                try (PreparedStatement s=c.prepareStatement("SELECT UserID FROM Users WHERE StudentNumber=?")) {
                    s.setString(1,number); try(ResultSet r=s.executeQuery()) { if(r.next()) throw new IllegalArgumentException("Student number already exists."); }
                }
                String username=base;
                try(PreparedStatement s=c.prepareStatement("SELECT Username FROM Users WHERE Username=?")) {
                    for(int suffix=1;;suffix++) {
                        s.setString(1,username); try(ResultSet r=s.executeQuery()) { if(!r.next()) break; }
                        username=base+suffix;
                    }
                }
                try (PreparedStatement s=c.prepareStatement("INSERT INTO Users (FirstName,LastName,Username,Password,Role,StudentNumber,Section,MustChangePassword) VALUES (?,?,?,?,'Student',?,?,1)")) {
                    s.setString(1,first);s.setString(2,last);s.setString(3,username);
                    s.setString(4,PasswordSecurity.hash("1234".toCharArray()));s.setString(5,number);s.setString(6,section);
                    s.executeUpdate();
                }
                c.commit(); return username;
            } catch(SQLException|RuntimeException ex) { c.rollback(); throw ex; }
        }
    }

    public static boolean mustChangePassword(String username) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement s=c.prepareStatement("SELECT MustChangePassword FROM Users WHERE Username=? AND Role='Student'")) {
            s.setString(1,username); try(ResultSet r=s.executeQuery()) { return r.next() && r.getBoolean(1); }
        }
    }
    public static void changeInitialPassword(String username, String oldPassword, char[] newPassword) throws SQLException {
        if(newPassword.length<8) throw new IllegalArgumentException("New password must contain at least 8 characters.");
        String candidate=new String(newPassword);
        if(candidate.equals("1234") || candidate.equals(oldPassword)) throw new IllegalArgumentException("Choose a new password different from your temporary password.");
        try(Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String stored;
                try(PreparedStatement s=c.prepareStatement("SELECT Password FROM Users WHERE Username=? AND Role='Student' AND MustChangePassword=1 FOR UPDATE")) {
                    s.setString(1,username);try(ResultSet r=s.executeQuery()) { if(!r.next()) throw new SecurityException("Account is not eligible for first-login password change.");stored=r.getString(1); }
                }
                if(!PasswordSecurity.matches(oldPassword,stored)) throw new SecurityException("Temporary password is incorrect.");
                try(PreparedStatement s=c.prepareStatement("UPDATE Users SET Password=?,MustChangePassword=0 WHERE Username=? AND Role='Student' AND MustChangePassword=1")) {
                    s.setString(1,PasswordSecurity.hash(newPassword));s.setString(2,username);
                    if(s.executeUpdate()!=1) throw new SQLException("Password change was not saved.");
                }
                c.commit();
            } catch(SQLException|RuntimeException ex) { c.rollback(); throw ex; }
        }
    }
}
