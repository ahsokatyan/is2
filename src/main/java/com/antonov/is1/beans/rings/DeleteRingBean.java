package com.antonov.is1.beans.rings;

import com.antonov.is1.entities.Ring;
import com.antonov.is1.services.RingDeletionService;
import com.antonov.is1.services.RingDeletionAnalysis;
import com.antonov.is1.services.RingService;
import lombok.Getter;
import lombok.Setter;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
@Getter @Setter
public class DeleteRingBean implements Serializable {

    @Inject
    private RingDeletionService ringDeletionService;

    @Inject
    private RingService ringService; // Добавляем для простой загрузки кольца

    private Long ringId;
    private Ring ring;

    // Поля для диалога удаления
    private boolean showDeleteDialog = false;
    private RingDeletionAnalysis deletionAnalysis;
    private Long selectedNewRingId;

    public void loadRing() {
        if (ringId != null) {
            // Используем простой сервис для загрузки кольца (без анализа связей)
            ring = ringService.getRingById(ringId).orElse(null);
        }
    }

    /**
     * Инициирует удаление с анализом связей
     */
    public void initiateDeletion() {
        if (ring == null) {
            addMessage("Ошибка", "Кольцо не найдено.");
            return;
        }

        try {
            this.deletionAnalysis = ringDeletionService.analyzeDeletion(ringId);

            if (deletionAnalysis.requiresReassignment()) {
                // Показываем диалог перепривязки
                this.showDeleteDialog = true;
            } else {
                // Удаляем сразу (кольцо никем не используется)
                performDeletion();
            }
        } catch (Exception e) {
            addMessage("Ошибка", "Ошибка при анализе связей: " + e.getMessage());
        }
    }

    /**
     * Выполняет удаление с учетом выбранных опций
     */
    public String performDeletion() {
        try {
            ringDeletionService.deleteRingWithReassignment(ringId, selectedNewRingId);
            addMessage("Успех", "Кольцо удалено успешно!");
            resetDeleteDialog();
            return "rings?faces-redirect=true";
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось удалить кольцо: " + e.getMessage());
            return null;
        }
    }

    public void cancelDeletion() {
        resetDeleteDialog();
    }

    private void resetDeleteDialog() {
        this.showDeleteDialog = false;
        this.deletionAnalysis = null;
        this.selectedNewRingId = null;
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

}