/**
 * Бизнес логика User
 *
 * @author fix Dmitriy Ostrovskiy 19.03.2020
 * created on
 */

package ru.geek.news_portal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.geek.news_portal.base.entities.Article;
import ru.geek.news_portal.base.entities.PasswordResetToken;
import ru.geek.news_portal.base.entities.Role;
import ru.geek.news_portal.base.entities.User;
import ru.geek.news_portal.base.repo.PasswordResetTokenRepository;
import ru.geek.news_portal.base.repo.UserRepository;
import ru.geek.news_portal.base.repo.RoleRepository;
import ru.geek.news_portal.dto.ArticleDto;
import ru.geek.news_portal.dto.UserAccountDTO;
import ru.geek.news_portal.dto.UserModifyDTO;
import ru.geek.news_portal.utils.SystemUser;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordResetTokenRepository passwordTokenRepo;
    private BCryptPasswordEncoder passwordEncoder;
    private ArticleService articleService;
    private List<Article> articleList;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setArticleService(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    @Transactional
    public User findByUsername(String username) {
        return userRepository.findOneByUsername(username);
    }

    @Override
    @Transactional
    public User findById(Long id) {
        return userRepository.findOneById(id);
    }

    /**
     * @author Ostrovskiy Dmitriy
     * created 17/04/2020
     * Метод возвращает статьи написанные пользователем
     * v1.0
     */
    @Transactional
    public List<Article> findArticlesByUser(String username) {
        return articleService.findArticlesByAuthor(username);
    }

    /**
     * @author Ostrovskiy Dmitriy
     * created 17/04/2020
     * Метод возвращает статьи написанные пользователем
     * v1.0
     */
    @Transactional
    public List<ArticleDto> findArticlesByUserDto(String username) {
        return articleService.findArticlesDtoByAuthor(username);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findOneByUsername(userName);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public boolean isUserExist(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User save(SystemUser systemUser) {
        User user = new User();
        if (findByUsername(systemUser.getUsername()) != null) {
            throw new RuntimeException("User with username " + systemUser.getUsername() + " is already exist");
        }
        user.setUsername(systemUser.getUsername());

        if (systemUser.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(systemUser.getPassword()));
        }
        user.setFirstName(systemUser.getFirstName());
        user.setLastName(systemUser.getLastName());
        user.setEmail(systemUser.getEmail());
        user.setRoles(Collections.singletonList(roleRepository.findOneByName("READER")));
        return userRepository.save(user);
    }

    @Override
    public User update(SystemUser systemUser) {
        return null;
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean updatePassword(User user, String password) {
        if (findByUsername(user.getUsername()) == null) {
            throw new RuntimeException("User " + user + " not found");
        }
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean checkPassword(User user, String password) {
        if (findByUsername(user.getUsername()) == null) {
            throw new RuntimeException("User " + user + " not found");
        }
        String userPassword = user.getPassword();
        return passwordEncoder.matches(password, userPassword);
    }

    @Override
    public User saveDTO(UserAccountDTO userAccountDTO) {
        User user = userRepository.findOneByUsername(userAccountDTO.getUsername());
        user.setUsername(userAccountDTO.getUsername());

        if (findByUsername(user.getUsername()) == null) {
            throw new RuntimeException("User " + user + " not found");
        }

        user.setFirstName(userAccountDTO.getFirstName());
        user.setLastName(userAccountDTO.getLastName());
        user.setEmail(userAccountDTO.getEmail());

        return userRepository.save(user);
    }

    @Override
    public void updateDTO(UserModifyDTO userDTO) {
        User user;

        if (findByUsername(userDTO.getUsername()) == null) {
            user = new User();
        } else {
            user = userRepository.findOneByUsername(userDTO.getUsername());
        }

        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setRoles(userDTO.getRoles());

        userRepository.save(user);
    }

    @Override
    public UserAccountDTO userToDTO(String username) {
        UserAccountDTO userDTO = new UserAccountDTO();

        if (!userRepository.existsByUsername(username)) {
            throw new RuntimeException("User " + username + " not found");
        }

        User user = userRepository.findOneByUsername(username);

        userDTO.setUsername(user.getUsername());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setArticleLikes(user.getArticleLikes());
        userDTO.setArticleRatings(user.getArticleRatings());
        userDTO.setComments(user.getComments());
        userDTO.setCommentLikes(user.getCommentLikes());
        userDTO.setArticleList(findArticlesByUser(username));

        return userDTO;
    }

    public void createPasswordResetToken(User user, String token) {
        PasswordResetToken prt = new PasswordResetToken(token, user);
        passwordTokenRepo.save(prt);
    }
}
