package com.antonov.is1.beans.rings;


import com.antonov.is1.entities.Ring;
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
public class RingCreateBean implements Serializable {

    @Inject
    private RingService ringService;

    private Ring newRing;

    public RingCreateBean() {
        initNewRing();
    }

    private void initNewRing() {
        newRing = new Ring();
    }

    public String createRing() {
        try {
            Ring savedRing = ringService.createRing(newRing.getName(), newRing.getPower(), newRing.getWeight());
            addMessage("Успех", "Кольцо создано успешно!");
            initNewRing();
            return "view-ring?id=" + savedRing.getId() + "&faces-redirect=true";
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось создать кольцо: " + e.getMessage());
            return null;
        }
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

}
