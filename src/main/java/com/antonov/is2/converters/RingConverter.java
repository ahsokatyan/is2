package com.antonov.is2.converters;

import com.antonov.is2.entities.Ring;
import com.antonov.is2.services.RingService;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.logging.Logger;

@FacesConverter(forClass = Ring.class, managed = true)
public class RingConverter implements Converter<Ring> {

    private static final Logger LOG = Logger.getLogger(RingConverter.class.getName());
    private RingService getRingService() {
        return CDI.current().select(RingService.class).get();
    }

    @Override
    public Ring getAsObject(FacesContext context, UIComponent component, String value) {
        LOG.info("RingConverter getAsObject called with value: " + value);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Long id = Long.valueOf(value);
            return getRingService().getRingById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Ring value) {
        if (value == null) {
            return "";
        }
        return value.getId().toString();
    }
}