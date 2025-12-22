package com.antonov.is1.utils;

import com.antonov.is1.entities.BookCreature;
import com.antonov.is1.entities.Ring;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttackLevelDeletionAnalysis {
    private Long targetAttackLevel;
    private List<BookCreature> creaturesToDelete;
    private List<Ring> ringsFromDeletedCreatures;
    private List<BookCreature> creaturesWithoutRings;
    
    public boolean hasCreaturesToDelete() {
        return creaturesToDelete != null && !creaturesToDelete.isEmpty();
    }
    
    public boolean hasRingsToReassign() {
        return ringsFromDeletedCreatures != null && !ringsFromDeletedCreatures.isEmpty();
    }
    
    public boolean hasCreaturesToReassignTo() {
        return creaturesWithoutRings != null && !creaturesWithoutRings.isEmpty();
    }
    
    public int getRingsCount() {
        return ringsFromDeletedCreatures != null ? ringsFromDeletedCreatures.size() : 0;
    }
    
    public int getAvailableCreaturesCount() {
        return creaturesWithoutRings != null ? creaturesWithoutRings.size() : 0;
    }
}