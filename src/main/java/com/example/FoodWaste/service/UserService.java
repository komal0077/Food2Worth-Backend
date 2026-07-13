package com.example.FoodWaste.service;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.exception.NotFoundException;
import com.example.FoodWaste.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_PAGE_SIZE = 200;

    private final UserRepository userRepository;

    // Get All Users
    public List<User> getAllUsers(Integer page, Integer size) {

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : MAX_PAGE_SIZE;

        return userRepository.findAll(PageRequest.of(pageNumber, pageSize)).getContent();
    }

    // Get User By Id
    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
    }

    // Get Users By Role
    public List<User> getUsersByRole(String role) {

        return userRepository.findByRole(role);
    }

    // Delete User
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User Not Found");
        }

        userRepository.deleteById(id);
    }
}
