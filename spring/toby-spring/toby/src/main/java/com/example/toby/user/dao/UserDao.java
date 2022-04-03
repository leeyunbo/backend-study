package com.example.toby.user.dao;

import com.example.toby.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.*;

@RequiredArgsConstructor
@Component
public class UserDao {

    private final SimpleConnectionMaker simpleConnectionMaker;

    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = simpleConnectionMaker.makeConnection();

        PreparedStatement ps = c.prepareStatement(
                "insert into users(id, name, passowrd) values(?,?,?)");
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        close(ps, c);
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = simpleConnectionMaker.makeConnection();

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
        Connection c = simpleConnectionMaker.makeConnection();

        PreparedStatement ps = c.prepareStatement(
                "delete from users");

        ps.executeUpdate();

        close(ps, c);
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException{
        UserDao dao = new UserDao(new NConnectionMaker());
        dao.initializedUsers();

        User user = new User();
        user.setId("fffffff");
        user.setName("이윤복");
        user.setPassword("pass");

        dao.add(user);

        System.out.println(user.getId() + " 등록 성공");

        User user2 = dao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + " 조회 성공");
    }
}
