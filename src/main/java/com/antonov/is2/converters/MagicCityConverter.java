package com.antonov.is2.converters;

import com.antonov.is2.entities.MagicCity;
import com.antonov.is2.services.MagicCityService;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.logging.Logger;

@FacesConverter(forClass = MagicCity.class, managed = true)
public class MagicCityConverter implements Converter<MagicCity> {
    private static final Logger LOG = Logger.getLogger(RingConverter.class.getName());
    private MagicCityService getMagicCityService() {
        return CDI.current().select(MagicCityService.class).get();
    }

    @Override
    public MagicCity getAsObject(FacesContext context, UIComponent component, String value) {
        LOG.info("MagicCityConverter getAsObject called with value: " + value);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Long id = Long.valueOf(value);
            return getMagicCityService().getMagicCityById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, MagicCity value) {
        if (value == null) {
            return "";
        }
        return value.getId().toString();
    }
}