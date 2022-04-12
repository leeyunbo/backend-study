package com.example.toby.user.dao;

public class DaoFactory {
    public UserDao userDao() {
        return new UserDao(new NConnectionMaker());
    }
}
