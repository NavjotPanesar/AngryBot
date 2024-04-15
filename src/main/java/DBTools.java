import java.sql.*;

public class DBTools {
    private static Connection connection;


    protected static void openConnection() throws SQLException {
        try {
            connection = DriverManager.getConnection(Config.DB_URL(), Config.DB_USER(), Config.DB_PASS());
            System.out.println("VALID: " + connection.isValid(5));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            closeConnection();
        }
    }

    protected static void closeConnection() throws SQLException {
        System.out.println("Closing Connection...");
        connection.close();
        System.out.println("VALID: " + connection.isValid(5));

    }

    protected static void insertGUILD_USER(String GUILD, String UID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO GUILD_USER(GUILD, UID,BANANA_TOTAL,BANANA_CURRENT,GUNKED,GUNKS) VALUES (?,?,?,?,?,?)")) {
            statement.setString(1, GUILD);
            statement.setString(2, UID);
            statement.setInt(3, 10);
            statement.setInt(4, 10);
            statement.setInt(5, 0);
            statement.setInt(6, 0);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    protected static void updateGUILD_USER(String guild, String uid, Integer bananaTotal, Integer bananaCurrent, Integer gunked, Integer gunks,Integer timeout) {
        try (PreparedStatement statement = connection.prepareStatement(buildUpdateQuery(bananaTotal, bananaCurrent, gunked, gunks, timeout))) {
            int parameterIndex = 1;

            // Set values based on non-null parameters
            if (bananaTotal != null) {
                statement.setInt(parameterIndex++, bananaTotal);
            }
            if (bananaCurrent != null) {
                statement.setInt(parameterIndex++, bananaCurrent);
            }
            if (gunked != null) {
                statement.setInt(parameterIndex++, gunked);
            }
            if (gunks != null) {
                statement.setInt(parameterIndex++, gunks);
            }
            if (timeout != null) {
                statement.setInt(parameterIndex++, timeout);
            }

            // Set key parameters
            statement.setString(parameterIndex++, guild);
            statement.setString(parameterIndex, uid);
            System.out.println("Executing SQL: " + statement.toString());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    // Helper method to dynamically build the update query
    private static String buildUpdateQuery(Integer bananaTotal, Integer bananaCurrent, Integer gunked, Integer gunks,Integer timeout) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE GUILD_USER SET ");

        // Append non-null parameters to the query
        appendToQuery(queryBuilder, "BANANA_TOTAL", bananaTotal);
        appendToQuery(queryBuilder, "BANANA_CURRENT", bananaCurrent);
        appendToQuery(queryBuilder, "GUNKED", gunked);
        appendToQuery(queryBuilder, "GUNKS", gunks);
        appendToQuery(queryBuilder, "TIMEOUT", timeout);

        // Remove the trailing comma, if any
        if (queryBuilder.lastIndexOf(",") == queryBuilder.length() - 2) {
            queryBuilder.deleteCharAt(queryBuilder.length() - 2);
        }

        // Append the WHERE clause
        queryBuilder.append(" WHERE GUILD = ? AND UID = ?");

        return queryBuilder.toString();
    }

    // Helper method to append column names to the query if the value is non-null
    private static void appendToQuery(StringBuilder queryBuilder, String columnName, Object value) {
        if (value != null) {
            queryBuilder.append(columnName).append(" = ?, ");
        }
    }



    protected static ResultSet selectGUILD_USER(String GUILD, String UID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM GUILD_USER WHERE GUILD = ? AND UID = ?")) {
            statement.setString(1, GUILD);
            statement.setString(2, UID);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet;
            } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    protected static int selectJACKPOT(String GUILD) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT BANANA_JACKPOT FROM GAMBLE where GUILD=?");
            statement.setString(1,GUILD);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("BANANA_JACKPOT");
            } else {
                // Handle the case where no result is found (return a default value or throw an exception)
                return 0; // Replace with appropriate handling
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 0; // Replace with appropriate handling
        }
    }

    protected static void insertJACKPOT(String GUILD) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO GAMBLE(GUILD) VALUES (?)")) {
            statement.setString(1, GUILD);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected static ResultSet  getCOMMAND_KEYWORD(String CONDITION) throws SQLException{
        try (PreparedStatement statement = connection.prepareStatement(
                "select KEYWORD as KEYWORD from COMMAND_KEYWORD where COMMAND=?")) {
            statement.setString(1, CONDITION);
            System.out.println(statement);
            return statement.executeQuery();
            } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

    }

    protected static void updateCOMMAND_KEYWORD(String COMMAND, String KEYWORD) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE COMMAND_KEYWORD set KEYWORD=? WHERE COMMAND=? ")) {
            statement.setString(1, KEYWORD);
            statement.setString(2, COMMAND);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected static void updateJACKPOT(int bananaJackpot) {
        try {
            PreparedStatement statement = connection.prepareStatement(buildUpdateBananaJackpotQuery());
            statement.setInt(1, bananaJackpot);
            System.out.println("Executing SQL: " + statement.toString());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected static void resetJACKPOT(int baseBananaJackpot) {
        try {
            // Update the jackpot value to the base value
            PreparedStatement statement = connection.prepareStatement(buildUpdateBananaJackpotQuery());
            statement.setInt(1, baseBananaJackpot);
        
            System.out.println("Executing SQL: " + statement.toString());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    
    
    
    private static String buildUpdateBananaJackpotQuery() {
        return "UPDATE GAMBLE SET BANANA_JACKPOT = ?";
    }
    
}


