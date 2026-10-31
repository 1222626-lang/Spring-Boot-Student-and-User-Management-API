
package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractCrudService<
        User, Long, UserRequest, UserResponse> {

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper) {

        super(userRepository, userMapper, "User");
    }
}
