package as.vestera.stack;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class PersistentStore {
    public boolean createStack(String stackName) {
        stackName = stackName.replaceAll("[^A-Za-z0-9]", "");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            String query = "create table stacks_" + stackName + "(" +
                "rowid integer primary key autoincrement," +
                "value text not null)";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public List<String> listStacks() {
        List<String> result = new LinkedList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(
                "select name from sqlite_master " +
                    "where type = 'table' " +
                    "and name like 'stacks_%'"
            );
            while (rs.next()) {
                result.add(rs.getString("name"));
            }
            result.replaceAll(s -> s.substring("stacks_".length()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getStack(String stackName) {
        stackName = stackName.replaceAll("[^A-Za-z0-9]", "");
        List<String> result = new LinkedList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select value from stacks_" + stackName);
            while (rs.next()) {
                result.add(rs.getString("value"));
            }
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
        return result;
    }

    public void push(String stackName, String value) {
        stackName = stackName.replaceAll("[^A-Za-z0-9]", "");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            String query = "insert into stacks_" + stackName + "(value) values(?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, value);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
    }

    public String pop(String stackName) {
        stackName = stackName.replaceAll("[^A-Za-z0-9]", "");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            String query = "select rowid, value from stacks_" + stackName + " order by rowid desc limit 1;";
            ResultSet rs = statement.executeQuery(query);
            if (!rs.next()) throw new NoSuchElementException("Stack " + stackName + " is empty");
            int rowid = rs.getInt("rowid");
            String value = rs.getString("value");
            statement.executeUpdate("delete from stacks_" + stackName + " where rowid = " + rowid);
            return value;
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
    }
}
