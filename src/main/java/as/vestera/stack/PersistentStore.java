package as.vestera.stack;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

//TODO: Fix vulnerability to SQL-injection

public class PersistentStore {
    public boolean createStack(String name) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("create table " + name + "(value text not null)");
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public List<String> listStacks() {
        List<String> result = new LinkedList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select name from sqlite_master where type = 'table'");
            while (rs.next()) {
                result.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getStack(String stackName) {
        List<String> result = new LinkedList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select value from " + stackName);
            while (rs.next()) {
                result.add(rs.getString("value"));
            }
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
        return result;
    }

    public void push(String stackName, String value) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("insert into " + stackName + " values('" + value + "')");
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
    }

    public String pop(String stackName) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Configuration.getDbFile())) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select rowid, value from " + stackName + " order by rowid desc limit 1;");
            if (!rs.next()) throw new NoSuchElementException("Stack " + stackName + " is empty");
            int rowid = rs.getInt("rowid");
            String value = rs.getString("value");
            statement.executeUpdate("delete from " + stackName + " where rowid = " + rowid);
            return value;
        } catch (SQLException e) {
            throw new NoSuchStackException("No stack with name " + stackName);
        }
    }
}
