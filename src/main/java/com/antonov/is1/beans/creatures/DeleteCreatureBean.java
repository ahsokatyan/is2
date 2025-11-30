package com.antonov.is1.beans.creatures;

import com.antonov.is1.entities.BookCreature;
import com.antonov.is1.services.BookCreatureDeletionService;
import com.antonov.is1.services.BookCreatureDeletionAnalysis;
import com.antonov.is1.services.BookCreatureService;
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
public class DeleteCreatureBean implements Serializable {

    @Inject
    private BookCreatureDeletionService creatureDeletionService;

    @Inject
    private BookCreatureService bookCreatureService;

    private Long creatureId;
    private BookCreature creature;

    // Поля для диалога удаления
    private boolean showDeleteDialog = false;
    private BookCreatureDeletionAnalysis deletionAnalysis;
    private Long selectedNewRingOwnerId;

    public void loadCreature() {
        if (creatureId != null) {
            creature = bookCreatureService.getBookCreatureById(creatureId).orElse(null);
        }
    }

    /**
     * Инициирует удаление с анализом связей
     */
    public void initiateDeletion() {
        this.deletionAnalysis = creatureDeletionService.analyzeDeletion(creatureId);

        if (deletionAnalysis.requiresReassignment()) {
            // Показываем диалог перепривязки
            this.showDeleteDialog = true;
        } else {
            // Удаляем сразу (кольцо не используется или его нет)
            performDeletion();
        }
    }

    /**
     * Выполняет удаление с учетом выбранных опций
     */
    public String performDeletion() {
        try {
            creatureDeletionService.deleteCreatureWithReassignment(creatureId, selectedNewRingOwnerId);
            addMessage("Успех", "Существо удалено успешно!");
            resetDeleteDialog();
            return "creatures?faces-redirect=true";
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось удалить существо: " + e.getMessage());
            return null;
        }
    }

    public void cancelDeletion() {
        resetDeleteDialog();
    }

    private void resetDeleteDialog() {
        this.showDeleteDialog = false;
        this.deletionAnalysis = null;
        this.selectedNewRingOwnerId = null;
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

}