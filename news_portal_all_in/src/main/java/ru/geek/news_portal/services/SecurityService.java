package ru.geek.news_portal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.geek.news_portal.base.entities.PasswordResetToken;
import ru.geek.news_portal.base.entities.User;
import ru.geek.news_portal.base.repo.PasswordResetTokenRepository;

import java.util.Calendar;
import java.util.Date;

import static java.util.Collections.singletonList;

/**
 * GeekBrains Java, news_portal.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 30.04.2020
 * @link https://github.com/Centnerman
 */

@Service("SecurServ")
public class SecurityService {

    private PasswordResetTokenRepository passwordTokenRepo;
    public static final String CHANGE_PASSWORD_PRIVILEGE = "CHANGE__PASSWORD__PRIVILEGE";

    @Autowired
    public void setPasswordTokenRepo(PasswordResetTokenRepository repo) {
        passwordTokenRepo = repo;
    }

    public void validatePasswordResetToken(long userId, String token)
            throws PasswordResetTokenException {
        PasswordResetToken passToken = passwordTokenRepo.findByToken(token);

        if (passToken == null || passToken.getUser().getId() != userId)
            throw new PasswordResetTokenException("некорректный токен сброса пароля");

        Date exp = passToken.getExpiryDate();
        Date now = Calendar.getInstance().getTime();

        if (exp.compareTo(now) <= 0)
            throw new PasswordResetTokenException("истекший токен сброса пароля");

        User user = passToken.getUser();
        GrantedAuthority sga = new SimpleGrantedAuthority(CHANGE_PASSWORD_PRIVILEGE);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, singletonList(sga));
        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(auth);
    }

    public static class PasswordResetTokenException
            extends Exception {

        PasswordResetTokenException(String msg) {
            super(msg);
        }

    }

}