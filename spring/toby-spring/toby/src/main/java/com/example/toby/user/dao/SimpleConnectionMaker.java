package com.example.toby.user.dao;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public interface SimpleConnectionMaker {

    Connection makeConnection() throws ClassNotFoundException, SQLException;
}
