package com.example.toby.user.dao;

import com.example.toby.user.domain.User;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class NUserDao extends UserDao {
    @Override
    protected Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.mariadb.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mariadb://localhost/toby", "root", "dbsqhr1!62719");
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException{
        UserDao dao = new NUserDao();

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
