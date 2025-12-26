package com.antonov.is2.utils;

import com.antonov.is2.entities.BookCreature;
import com.antonov.is2.entities.MagicCity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MagicCityDeletionAnalysis {
    private MagicCity cityToDelete;
    private List<BookCreature> affectedCreatures; // Существа в этом городе
    private List<MagicCity> availableCities; // Доступные города для переселения

    public boolean requiresReassignment() {
        return affectedCreatures != null && !affectedCreatures.isEmpty();
    }

    public int getAffectedCreaturesCount() {
        return affectedCreatures != null ? affectedCreatures.size() : 0;
    }
}