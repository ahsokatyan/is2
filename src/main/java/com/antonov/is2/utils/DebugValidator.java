package com.antonov.is2.utils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;

@FacesValidator("debugValidator")
public class DebugValidator implements Validator {
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        // БРЕЙКПОИНТ ЗДЕСЬ
        System.out.println("Validating value: " + value + " class: " +
                (value != null ? value.getClass() : "null"));
    }
}