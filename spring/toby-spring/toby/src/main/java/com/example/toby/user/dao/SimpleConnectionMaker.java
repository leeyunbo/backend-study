package com.example.toby.user.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface SimpleConnectionMaker {

    Connection makeConnection() throws ClassNotFoundException, SQLException;
}
