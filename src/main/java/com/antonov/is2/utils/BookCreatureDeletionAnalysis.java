package com.antonov.is2.utils;

import com.antonov.is2.entities.BookCreature;
import com.antonov.is2.entities.Ring;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookCreatureDeletionAnalysis {
    private BookCreature creatureToDelete;
    private Ring orphanedRing; // Кольцо, которое освободится при удалении
    private List<BookCreature> availableCreaturesForRing; // Существа для перепривязки кольца

    public boolean requiresReassignment() {
        return orphanedRing != null;
    }
}