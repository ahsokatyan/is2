package com.antonov.is2.beans.cities;

import com.antonov.is2.entities.MagicCity;
import com.antonov.is2.services.MagicCityService;
import lombok.Getter;
import lombok.Setter;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
@Getter @Setter
public class ViewMagicCityBean implements Serializable {

    @Inject
    private MagicCityService magicCityService;

    private Long cityId;
    private MagicCity city;

    public void loadCity() {
        if (cityId != null) {
            city = magicCityService.getMagicCityById(cityId).orElse(null);
        }
    }

}