package ru.telegrambot.telegram_bot_support.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class UserFollowing {

    @Id
    private Long chatId;

    private boolean payment;
    private LocalDateTime dateStarted;
    private LocalDateTime dateNotification;
    private boolean sentNotification;
    private LocalDateTime dateEnded;
    private boolean sentEnded;

    public UserFollowing() {
    }

    public UserFollowing(Long chatId, LocalDateTime dateStarted, LocalDateTime dateNotification, LocalDateTime dateEnded) {
        this.chatId = chatId;
        this.payment = false;
        this.dateStarted = dateStarted;
        this.dateNotification = dateNotification;
        this.sentNotification = false;
        this.dateEnded = dateEnded;
        this.sentEnded = false;
    }

    public boolean isSentNotification() {
        return sentNotification;
    }

    public void setSentNotification(boolean sent_notification) {
        this.sentNotification = sent_notification;
    }

    public LocalDateTime getDateNotification() {
        return dateNotification;
    }

    public void setDateNotification(LocalDateTime dateNotification) {
        this.dateNotification = dateNotification;
    }

    public boolean isSentEnded() {
        return sentEnded;
    }

    public void setSentEnded(boolean sent_ended) {
        this.sentEnded = sent_ended;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public boolean isPayment() {
        return payment;
    }

    public void setPayment(boolean payment) {
        this.payment = payment;
    }

    public LocalDateTime getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(LocalDateTime dateStarted) {
        this.dateStarted = dateStarted;
    }

    public LocalDateTime getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(LocalDateTime dateEnded) {
        this.dateEnded = dateEnded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFollowing that = (UserFollowing) o;
        return Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }

    @Override
    public String toString() {
        return "User{" +
                ", chatId=" + chatId +
                ", payment=" + payment +
                ", dateStarted=" + dateStarted +
                ", dateEnded=" + dateEnded +
                '}';
    }
}
