package as.vestera.stack;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistentStore {
    public boolean createStack(String stackName) {
        try {
            runUpdate("create table " + safeName(stackName) + "(" +
                    "rowid integer primary key autoincrement, " +
                    "value text not null)");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<String> listStacks() {
        try {
            List<String> result = runQuery(
                "select name from sqlite_master where type = 'table' and name like 'stacks_%'",
                "name",
                null);
            result.replaceAll(s -> s.substring("stacks_".length()));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getStack(String stackName) {
        try {
            return runQuery("select value from " + safeName(stackName), "value", null);
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
    }

    public void push(String stackName, String value) {
        try {
            runUpdate("insert into " + safeName(stackName) + "(value) values(?)", value);
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
    }

    public String pop(String stackName) {
        try  {
            AtomicInteger rowid = new AtomicInteger();
            List<String> result = runQuery(
                "select rowid, value from " + safeName(stackName) + " order by rowid desc limit 1",
                "value",
                rowid);
            if (result.isEmpty()) throw new NoSuchElementException("Stack is empty");
            runUpdate("delete from " + safeName(stackName) + " where rowid = " + rowid.get());
            return result.get(0);
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
    }

    private String safeName(String stackName) {
        String alphanumerical = stackName.replaceAll("[^A-Za-z0-9]", "");
        if (alphanumerical.isEmpty()) {
            throw new IllegalArgumentException("\"" + stackName + "\" is not a valid stack name");
        }
        return "stacks_" + alphanumerical;
    }

    private List<String> runQuery(String query, String field, AtomicInteger lastId) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            List<String> result = new LinkedList<>();
            while (rs.next()) {
                result.add(rs.getString(field));
                if (lastId != null) lastId.set(rs.getInt("rowid"));
            }
            return result;
        }
    }

    private void runUpdate(String query, String... variables) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < variables.length; i++) {
                statement.setString(i + 1, variables[i]);
            }
            statement.executeUpdate();
        }
    }
}
