package ru.geek.news_portal.services;


import org.springframework.security.core.userdetails.UserDetailsService;

import ru.geek.news_portal.base.entities.User;
import ru.geek.news_portal.dto.UserAccountDTO;
import ru.geek.news_portal.dto.UserModifyDTO;
import ru.geek.news_portal.utils.SystemUser;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    User findById(Long id);

    boolean isUserExist(String username);

    User update(SystemUser systemUser);

    List<User> findAll();

    void delete(Long id);

    User findByUsername(String username);

    boolean isUsernameExist(String username);

    boolean isEmailExist(String email);

    User save(SystemUser systemUser);

    User saveDTO(UserAccountDTO userAccountDTO);

    boolean updatePassword(User user, String password);

    public boolean checkPassword(User user, String password);

    UserAccountDTO userToDTO(String username);

    void updateDTO(UserModifyDTO userDTO);

    public void createPasswordResetToken(User user, String token);
}
