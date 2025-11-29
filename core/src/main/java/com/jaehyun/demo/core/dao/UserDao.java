package com.jaehyun.demo.core.dao;

import com.jaehyun.demo.core.entity.User;
import com.jaehyun.demo.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Repository
public class UserDao {

    private final UserRepository userRepository;

    public User save(User user){ return userRepository.save(user);}

    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
