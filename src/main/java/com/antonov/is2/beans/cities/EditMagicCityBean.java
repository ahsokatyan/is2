package com.antonov.is2.beans.cities;

import com.antonov.is2.entities.MagicCity;
import com.antonov.is2.entities.BookCreatureType;
import com.antonov.is2.services.MagicCityService;
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
public class EditMagicCityBean implements Serializable {

    @Inject
    private MagicCityService magicCityService;

    private Long cityId;
    private MagicCity city;

    public void loadCity() {
        if (cityId != null) {
            city = magicCityService.getMagicCityById(cityId).orElse(null);
        }
    }

    public String updateCity() {
        try {
            magicCityService.updateMagicCity(city);
            addMessage("Успех", "Город обновлен успешно!");
            return "view-city?id=" + cityId + "&faces-redirect=true";
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось обновить город: " + e.getMessage());
            return null;
        }
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

    // Для выпадающих списков
    public BookCreatureType[] getCreatureTypes() {
        return BookCreatureType.values();
    }


}