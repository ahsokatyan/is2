package com.antonov.is1.beans.creatures;

import com.antonov.is1.entities.BookCreature;
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
public class EditCreatureBean implements Serializable {

    @Inject
    private BookCreatureService bookCreatureService;

    private Long creatureId;
    private BookCreature creature;

    public void loadCreature() {
        if (creatureId != null) {
            creature = bookCreatureService.getBookCreatureById(creatureId).orElse(null);
        }
    }

    public String updateCreature() {
        try {
            bookCreatureService.updateBookCreature(creature);
            addMessage("Успех", "Существо обновлено успешно!");
            return "view-creature?id=" + creatureId + "&faces-redirect=true";
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось обновить существо: " + e.getMessage());
            return null;
        }
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

}