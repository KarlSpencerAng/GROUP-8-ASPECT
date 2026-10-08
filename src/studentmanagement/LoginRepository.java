/**
 * LoginRepository.java
 * Purpose: Login database operations.
 * Organization note: Executable code and original formatting below are unchanged.
 */

package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class LoginRepository {

    private LoginRepository() {}

    // Classroom demonstration authentication only.
    // Production applications should use password hashes.
    public static String authenticate(
            String username,
            String password,
            String role
    ) throws SQLException {

    	String sql =
    		    "SELECT UserID, StudentNumber, Password FROM Users "
    		    + "WHERE Username = ? "
    		    + "AND Role = ?";

        try (
            Connection connection =
                DatabaseConnection.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
        	
        	System.out.println(
        		    "JAVA DATABASE: " + connection.getCatalog()
        		);
        	
            statement.setString(1, username);
            statement.setString(2, role);

            try (
                ResultSet result =
                    statement.executeQuery()
            ) {
                if (!result.next()) {
                    return null;
                }

                boolean passwordMatches = PasswordSecurity.matches(
                	    password,
                	    result.getString("Password")
                	);

                	System.out.println("Account found: " + username);
                	System.out.println("Role: " + role);
                	System.out.println("Password matches: " + passwordMatches);

                	if (!passwordMatches) {
                	    return null;
                	}

                if ("Student".equals(role)) {
                    return result.getString(
                        "StudentNumber"
                    );
                }

                return String.valueOf(result.getInt("UserID"));
            }
        }
    }
}
