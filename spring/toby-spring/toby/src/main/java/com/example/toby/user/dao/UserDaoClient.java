package com.example.toby.user.dao;

import com.example.toby.user.domain.User;

import java.sql.SQLException;

public class UserDaoClient {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
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
