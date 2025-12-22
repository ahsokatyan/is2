package com.antonov.is1.services;

import com.antonov.is1.entities.BookCreature;
import com.antonov.is1.entities.MagicCity;
import com.antonov.is1.repos.BookCreatureRepository;
import com.antonov.is1.repos.MagicCityRepository;
import com.antonov.is1.utils.MagicCityDeletionAnalysis;
import com.antonov.is1.websocket.CreaturesWebSocket;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class MagicCityDeletionService {

    @Inject
    private MagicCityRepository magicCityRepo;

    @Inject
    private BookCreatureRepository bookCreatureRepo;

    /**
     * Анализирует связи перед удалением города
     */
    public MagicCityDeletionAnalysis analyzeDeletion(Long cityId) {
        MagicCity city = magicCityRepo.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Город не найден"));

        MagicCityDeletionAnalysis analysis = new MagicCityDeletionAnalysis();
        analysis.setCityToDelete(city);

        // Находим всех существ в этом городе
        List<BookCreature> creaturesInCity = bookCreatureRepo.findByCityId(cityId);
        analysis.setAffectedCreatures(creaturesInCity);

        // Находим доступные города для переселения (первые 20, исключая удаляемый)
        List<MagicCity> availableCities = magicCityRepo.findAll().stream()
                .filter(c -> !c.getId().equals(cityId))
                .limit(20)
                .collect(Collectors.toList());
        analysis.setAvailableCities(availableCities);

        return analysis;
    }

    /**
     * Удаляет город с переселением существ
     */
    public void deleteCityWithReassignment(Long cityId, Long newCityId) {
        MagicCity city = magicCityRepo.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("Город не найдено"));

        // Находим всех существ в этом городе
        List<BookCreature> creaturesInCity = bookCreatureRepo.findByCityId(cityId);

        // Переселяем существ
        if (!creaturesInCity.isEmpty()) {
            MagicCity newCity = null;
            if (newCityId != null) {
                newCity = magicCityRepo.findById(newCityId)
                        .orElseThrow(() -> new IllegalArgumentException("Новый город не найден"));
            }

            // Обновляем город для всех существ
            for (BookCreature creature : creaturesInCity) {
                creature.setCreatureLocation(newCity);
                bookCreatureRepo.update(creature);
            }
        }

        // Удаляем город
        magicCityRepo.delete(city);
        // Уведомляем клиентов об удалении города
        CreaturesWebSocket.notifyCityDeleted(cityId);
    }
}