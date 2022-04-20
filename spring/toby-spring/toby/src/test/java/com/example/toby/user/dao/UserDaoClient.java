package com.example.toby.user.dao;


import com.example.toby.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;


@SpringBootTest
public class UserDaoClient {

    @Autowired
    private UserDao dao;

    @Test
    public void 실행한다() throws SQLException, ClassNotFoundException {
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

    @Test
    public void 줘팬다() {

    }
}