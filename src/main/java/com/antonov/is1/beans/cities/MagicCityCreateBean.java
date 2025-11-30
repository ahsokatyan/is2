package com.antonov.is1.beans.cities;

import com.antonov.is1.entities.MagicCity;
import com.antonov.is1.entities.BookCreatureType;
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
public class MagicCityCreateBean implements Serializable {

    @Inject
    private MagicCityService magicCityService;

    private MagicCity newCity;

    public MagicCityCreateBean() {
        initNewCity();
    }

    private void initNewCity() {
        newCity = new MagicCity();
        newCity.setCapital(false);
    }

    public String createCity() {
        try {
            MagicCity savedCity = magicCityService.createMagicCity(
                    newCity.getName(), newCity.getArea(), newCity.getPopulation(),
                    newCity.getEstablishmentDate(), newCity.getGovernor(),
                    newCity.getCapital(), newCity.getPopulationDensity()
            );
            addMessage("Успех", "Город создан успешно!");
            initNewCity();
            return "view-city?id=" + savedCity.getId() + "&faces-redirect=true";
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось создать город: " + e.getMessage());
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