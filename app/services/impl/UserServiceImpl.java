package services.impl;

import models.User;
import services.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User findByUsername(String username) {
        return User.find.query().where().eq("username", username).findOne();
    }
}