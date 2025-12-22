package com.antonov.is1.utils;

import com.antonov.is1.entities.BookCreature;
import com.antonov.is1.entities.Ring;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RingDeletionAnalysis {
    private Ring ringToDelete;
    private BookCreature currentOwner; // Текущий владелец кольца
    private List<Ring> availableRings; // Свободные кольца для перепривязки

    public boolean requiresReassignment() {
        return currentOwner != null;
    }
}