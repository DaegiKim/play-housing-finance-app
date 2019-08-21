package services;

import models.User;

public interface UserService {
    User findByUsername(String username);
}
