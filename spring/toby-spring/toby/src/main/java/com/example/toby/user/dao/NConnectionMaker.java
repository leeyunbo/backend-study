package com.example.toby.user.dao;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class NConnectionMaker implements SimpleConnectionMaker {
    @Override
    public Connection makeConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.mariadb.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mariadb://localhost/toby", "root", "dbsqhr1!62719");    }
}
