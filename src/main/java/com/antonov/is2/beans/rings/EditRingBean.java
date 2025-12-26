package com.antonov.is2.beans.rings;

import com.antonov.is2.entities.Ring;
import com.antonov.is2.services.RingService;
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
@Getter
@Setter
public class EditRingBean implements Serializable {

    @Inject
    private RingService ringService;

    private Long ringId;
    private Ring ring;

    public void loadRing() {
        if (ringId != null) {
            ring = ringService.getRingById(ringId).orElse(null);
        }
    }

    public String updateRing() {
        try {
            ringService.updateRing(ring);
            addMessage("Успех", "Кольцо обновлено успешно!");
            return "view-ring?id=" + ringId + "&faces-redirect=true";
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось обновить кольцо: " + e.getMessage());
            return null;
        }
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

}