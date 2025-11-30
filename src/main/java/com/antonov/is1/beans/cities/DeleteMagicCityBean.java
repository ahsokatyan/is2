package com.antonov.is1.beans.cities;

import com.antonov.is1.entities.MagicCity;
import com.antonov.is1.services.MagicCityDeletionService;
import com.antonov.is1.services.MagicCityDeletionAnalysis;
import com.antonov.is1.services.MagicCityService;
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
public class DeleteMagicCityBean implements Serializable {

    @Inject
    private MagicCityDeletionService magicCityDeletionService;

    @Inject
    private MagicCityService magicCityService;

    private Long cityId;
    private MagicCity city;

    // Поля для диалога удаления
    private boolean showDeleteDialog = false;
    private MagicCityDeletionAnalysis deletionAnalysis;
    private Long selectedNewCityId;

    public void loadCity() {
        if (cityId != null) {
            city = magicCityService.getMagicCityById(cityId).orElse(null);
        }
    }

    /**
     * Инициирует удаление с анализом связей
     */
    public void initiateDeletion() {
        this.deletionAnalysis = magicCityDeletionService.analyzeDeletion(cityId);

        if (deletionAnalysis.requiresReassignment()) {
            // Показываем диалог перепривязки
            this.showDeleteDialog = true;
        } else {
            // Удаляем сразу (в городе нет существ)
            performDeletion();
        }
    }

    /**
     * Выполняет удаление с учетом выбранных опций
     */
    public String performDeletion() {
        try {
            magicCityDeletionService.deleteCityWithReassignment(cityId, selectedNewCityId);
            addMessage("Успех", "Город удален успешно!");
            resetDeleteDialog();
            return "cities?faces-redirect=true";
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось удалить город: " + e.getMessage());
            return null;
        }
    }

    public void cancelDeletion() {
        resetDeleteDialog();
    }

    private void resetDeleteDialog() {
        this.showDeleteDialog = false;
        this.deletionAnalysis = null;
        this.selectedNewCityId = null;
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

}