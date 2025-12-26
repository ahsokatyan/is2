package com.antonov.is2.beans.admin;

import com.antonov.is2.entities.UserAccount;
import com.antonov.is2.entities.UserRole;
import com.antonov.is2.services.UserAccountService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@Getter @Setter
public class AdminUsersBean implements Serializable {

    @Inject
    private UserAccountService userAccountService;

    private List<UserAccount> pendingUsers;
    private List<UserAccount> allUsers;

    private String newLogin;
    private String newName;
    private String newPassword;
    private UserRole newRole = UserRole.USER;

    @PostConstruct
    public void init() {
        loadUsers();
    }

    public void loadUsers() {
        pendingUsers = userAccountService.getPendingUsers();
        allUsers = userAccountService.getAllUsers();
    }

    public void approveUser(Long userId, UserRole role) {
        try {
            userAccountService.approveUser(userId, role);
            addMessage("Готово", "Пользователь подтвержден.");
            loadUsers();
        } catch (Exception e) {
            addMessage("Ошибка", e.getMessage());
        }
    }

    public void updateRole(Long userId, UserRole role) {
        try {
            userAccountService.updateRole(userId, role);
            addMessage("Готово", "Роль обновлена.");
            loadUsers();
        } catch (Exception e) {
            addMessage("Ошибка", e.getMessage());
        }
    }

    public void createUser() {
        if (newLogin == null || newLogin.isBlank()
                || newName == null || newName.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            addMessage("Ошибка", "Заполните логин, имя и пароль.");
            return;
        }
        try {
            userAccountService.createByAdmin(newLogin, newName, newPassword, newRole);
            newLogin = null;
            newName = null;
            newPassword = null;
            newRole = UserRole.USER;
            addMessage("Готово", "Пользователь создан.");
            loadUsers();
        } catch (Exception e) {
            addMessage("Ошибка", e.getMessage());
        }
    }

    public UserRole[] getRoles() {
        return UserRole.values();
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }
}
