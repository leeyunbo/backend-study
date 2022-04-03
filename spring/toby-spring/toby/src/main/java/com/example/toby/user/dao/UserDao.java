package com.example.toby.user.dao;

import com.example.toby.user.domain.User;
import java.sql.*;

public abstract class UserDao {
    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = getConnection();

        PreparedStatement ps = c.prepareStatement(
                "insert into users(id, name, passowrd) values(?,?,?)");
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        close(ps, c);
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = getConnection();

        PreparedStatement ps = c.prepareStatement(
                "select * from users where id = ?");
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();
        rs.next();
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("passowrd"));

        close(rs, ps, c);

        return user;
    }

    protected abstract Connection getConnection() throws ClassNotFoundException, SQLException;

    private void close(PreparedStatement ps, Connection c) throws SQLException {
        ps.close();
        c.close();
    }

    private void close(ResultSet rs, PreparedStatement ps, Connection c) throws SQLException {
        rs.close();
        ps.close();
        c.close();
    }

    private void initializedUsers() throws ClassNotFoundException, SQLException {
        Connection c = getConnection();

        PreparedStatement ps = c.prepareStatement(
                "delete from users");

        ps.executeUpdate();

        ps.close();
    }
}
