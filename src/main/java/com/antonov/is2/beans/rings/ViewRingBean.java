package com.antonov.is2.beans.rings;

import com.antonov.is2.entities.Ring;
import com.antonov.is2.services.RingService;
import lombok.Getter;
import lombok.Setter;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
@Getter @Setter
public class ViewRingBean implements Serializable {

    @Inject
    private RingService ringService;

    private Long ringId;
    private Ring ring;

    public void loadRing() {
        if (ringId != null) {
            ring = ringService.getRingById(ringId).orElse(null);
        }
    }

}