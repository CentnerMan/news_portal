package ru.geek.news_portal.base.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.geek.news_portal.base.entities.PasswordResetToken;

/**
 * GeekBrains Java, news_portal.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 30.04.2020
 * @link https://github.com/Centnerman
 */

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

}
