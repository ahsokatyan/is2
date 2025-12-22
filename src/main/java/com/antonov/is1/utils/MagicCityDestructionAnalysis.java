package com.antonov.is1.utils;

import com.antonov.is1.entities.BookCreature;
import com.antonov.is1.entities.MagicCity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MagicCityDestructionAnalysis {
    private List<MagicCity> elfCities; // Города эльфов, подлежащие уничтожению
    private List<BookCreature> affectedCreatures; // Существа, живущие в этих городах
    private List<MagicCity> availableCities; // Доступные города для переселения

    public boolean hasAffectedCreatures() {
        return affectedCreatures != null && !affectedCreatures.isEmpty();
    }
}