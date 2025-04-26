package ru.telegrambot.telegram_bot_support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.telegrambot.telegram_bot_support.model.UserFollowing;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<UserFollowing, Long> {

    public UserFollowing findByUserId(Long userId);

    public List<UserFollowing> findByDateEndedBeforeAndSentFalse(LocalDateTime date);
}
