package com.antonov.is2.services;

import com.antonov.is2.entities.UserAccount;
import com.antonov.is2.entities.UserRole;
import com.antonov.is2.repos.UserAccountRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Stateless
public class UserAccountService {

    @Inject
    private UserAccountRepository userRepo;

    @Inject
    private PasswordHasher passwordHasher;

    public UserAccount register(String login, String name, String password) {
        ensureLoginAvailable(login);
        String salt = passwordHasher.generateSalt();
        String hash = passwordHasher.hash(salt, password);

        UserAccount user = new UserAccount();
        user.setLogin(login);
        user.setName(name);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        user.setRole(UserRole.USER);
        user.setApproved(false);
        return userRepo.save(user);
    }

    public UserAccount createByAdmin(String login, String name, String password, UserRole role) {
        ensureLoginAvailable(login);
        String salt = passwordHasher.generateSalt();
        String hash = passwordHasher.hash(salt, password);

        UserAccount user = new UserAccount();
        user.setLogin(login);
        user.setName(name);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        user.setRole(role);
        user.setApproved(true);
        return userRepo.save(user);
    }

    public Optional<UserAccount> authenticate(String login, String password) {
        Optional<UserAccount> userOpt = userRepo.findByLogin(login);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        UserAccount user = userOpt.get();
        if (!user.isApproved()) {
            return Optional.empty();
        }
        String expectedHash = passwordHasher.hash(user.getSalt(), password);
        if (!expectedHash.equals(user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public void approveUser(Long userId, UserRole role) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setApproved(true);
        user.setRole(role);
        userRepo.update(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String expectedHash = passwordHasher.hash(user.getSalt(), oldPassword);
        if (!expectedHash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный текущий пароль.");
        }
        String newSalt = passwordHasher.generateSalt();
        String newHash = passwordHasher.hash(newSalt, newPassword);
        user.setSalt(newSalt);
        user.setPasswordHash(newHash);
        userRepo.update(user);
    }

    public void updateRole(Long userId, UserRole role) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setRole(role);
        userRepo.update(user);
    }

    public List<UserAccount> getPendingUsers() {
        return userRepo.findPending();
    }

    public List<UserAccount> getAllUsers() {
        return userRepo.findAllOrdered();
    }

    private void ensureLoginAvailable(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым.");
        }
        if (userRepo.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Логин уже занят.");
        }
    }
}
