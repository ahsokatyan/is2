package com.antonov.is1.beans.special;

import lombok.Getter;
import lombok.Setter;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
@Getter @Setter
public class NavigationBean implements Serializable {
    private Long searchId;

    public String goToCreatures() {
        return "/creatures/creatures?faces-redirect=true";
    }

    public String goToCities() {
        return "/cities/cities?faces-redirect=true";
    }

    public String goToRings() {
        return "/rings/rings?faces-redirect=true";
    }

    public String goToHome() {
        return "/index?faces-redirect=true";
    }

    public String goToCreateCreature() {
        return "/creatures/create-creature?faces-redirect=true";
    }

    public String goToEditCreature() {
        return "/creatures/edit-creature?faces-redirect=true";
    }


    public String searchCreature() {
        if (searchId != null) {
            return "/creatures/view-creature?id=" + searchId + "&faces-redirect=true";
        }
        return null;
    }

    public String goToCreateRing() {
        return "/rings/create-ring?faces-redirect=true";
    }

    public String searchRing() {
        if (searchId != null) {
            return "/rings/view-ring?id=" + searchId + "&faces-redirect=true";
        }
        return null;
    }

    public String goToCreateCity() {
        return "/cities/create-city?faces-redirect=true";
    }

    public String searchCity() {
        if (searchId != null) {
            return "/cities/view-city?id=" + searchId + "&faces-redirect=true";
        }
        return null;
    }

    public String goToSpecialOperations() {
        return "/special/special-operations?faces-redirect=true";
    }
}