package com.antonov.is2.beans.imports;

import com.antonov.is2.beans.auth.UserSessionBean;
import com.antonov.is2.entities.ImportOperation;
import com.antonov.is2.services.ImportOperationService;
import com.antonov.is2.services.ImportService;
import com.antonov.is2.utils.ImportResult;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Named
@ViewScoped
@Getter
@Setter
public class ImportBean implements Serializable {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    private ImportService importService;

    @Inject
    private ImportOperationService importOperationService;

    @Inject
    private UserSessionBean userSessionBean;

    private Part file;
    private List<ImportOperation> operations;

    @PostConstruct
    public void init() {
        loadOperations();
    }

    public void importFile() {
        if (!userSessionBean.isLoggedIn()) {
            addMessage("ОШИБКА.", "НЕОБХОДИМА АВТОРИЗАЦИЯ.");
            return;
        }
        if (file == null) {
            addMessage("Ошибка.", "Выберите JSON файл.");
            return;
        }

        try (InputStream inputStream = file.getInputStream()) {
            ImportResult result = importService.importBookCreatures(inputStream, userSessionBean.getUsername());
            if (result.isSuccess()) {
                addMessage("Готово.", "Импорт завершен. Добавлено объектов: " + result.getAddedCount());
            } else {
                addMessage("Ошибка.", "Импорт не выполнен: " + result.getMessage());
            }
        } catch (IOException e) {
            addMessage("Ошибка.", "Не удалось прочитать файл: " + e.getMessage());
        } finally {
            loadOperations();
        }
    }

    public void loadOperations() {
        if (userSessionBean.isAdmin()) {
            operations = importOperationService.getAllOperations();
        } else if (userSessionBean.getUsername() != null && !userSessionBean.getUsername().isBlank()) {
            operations = importOperationService.getOperationsByUser(userSessionBean.getUsername());
        } else {
            operations = List.of();
        }
    }

    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return FORMATTER.format(dateTime);
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }
}
