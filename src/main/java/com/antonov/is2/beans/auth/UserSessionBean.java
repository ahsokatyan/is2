package com.antonov.is2.beans.auth;

import com.antonov.is2.entities.UserAccount;
import com.antonov.is2.entities.UserRole;
import com.antonov.is2.services.UserAccountService;
import lombok.Getter;
import lombok.Setter;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Optional;

@Named
@SessionScoped
@Getter
@Setter
public class UserSessionBean implements Serializable {
    @EJB
    private UserAccountService userAccountService;

    private Long userId;
    private String username;
    private String name;
    private UserRole role;

    private String login;
    private String password;

    private String registerLogin;
    private String registerName;
    private String registerPassword;

    private String oldPassword;
    private String newPassword;

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public void login() {
        if (login == null || login.isBlank() || password == null) {
            addMessage("Ошибка", "Введите логин и пароль.");
            return;
        }
        Optional<UserAccount> userOpt = userAccountService.authenticate(login, password);
        if (userOpt.isEmpty()) {
            addMessage("Ошибка", "Неверный логин/пароль или аккаунт не подтвержден.");
            return;
        }
        UserAccount user = userOpt.get();
        userId = user.getId();
        username = user.getLogin();
        name = user.getName();
        role = user.getRole();
        password = null;
        login = null;
        addMessage("Готово", "Вы вошли в систему.");
    }

    public void register() {
        if (registerLogin == null || registerLogin.isBlank()
                || registerName == null || registerName.isBlank()
                || registerPassword == null || registerPassword.isBlank()) {
            addMessage("Ошибка", "Заполните логин, имя и пароль.");
            return;
        }
        try {
            userAccountService.register(registerLogin, registerName, registerPassword);
            registerLogin = null;
            registerName = null;
            registerPassword = null;
            addMessage("Готово", "Заявка на регистрацию отправлена.");
        } catch (Exception e) {
            addMessage("Ошибка", e.getMessage());
        }
    }

    public void changePassword() {
        if (userId == null) {
            addMessage("Ошибка", "Сначала войдите в систему.");
            return;
        }
        if (oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            addMessage("Ошибка", "Введите текущий и новый пароль.");
            return;
        }
        try {
            userAccountService.changePassword(userId, oldPassword, newPassword);
            oldPassword = null;
            newPassword = null;
            addMessage("Готово", "Пароль обновлен.");
        } catch (Exception e) {
            addMessage("Ошибка", e.getMessage());
        }
    }

    public void logout() {
        userId = null;
        username = null;
        name = null;
        role = null;
        login = null;
        password = null;
        registerLogin = null;
        registerName = null;
        registerPassword = null;
        oldPassword = null;
        newPassword = null;
        addMessage("Готово", "Вы вышли из системы.");
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

    public boolean isLoggedIn() {
        return userId != null;
    }
}
