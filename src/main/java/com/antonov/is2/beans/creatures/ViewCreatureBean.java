package com.antonov.is2.beans.creatures;

import com.antonov.is2.entities.BookCreature;
import com.antonov.is2.services.BookCreatureService;
import lombok.Getter;
import lombok.Setter;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
@Getter @Setter
public class ViewCreatureBean implements Serializable {

    @Inject
    private BookCreatureService bookCreatureService;

    private Long creatureId;
    private BookCreature creature;

    public void loadCreature() {
        if (creatureId != null) {
            creature = bookCreatureService.getBookCreatureById(creatureId).orElse(null);
        }
    }

}