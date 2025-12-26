package com.antonov.is2.services;


import com.antonov.is2.entities.MagicCity;
import com.antonov.is2.entities.BookCreatureType;
import com.antonov.is2.repos.MagicCityRepository;
import com.antonov.is2.websocket.CreaturesWebSocket;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Stateless
public class MagicCityService {

    @Inject
    private MagicCityRepository magicCityRepo;

    public MagicCity createMagicCity(String name, Float area, Long population,
                                     LocalDateTime establishmentDate, BookCreatureType governor,
                                     Boolean capital, Float populationDensity) {
        MagicCity city = new MagicCity();
        city.setName(name);
        city.setArea(area);
        city.setPopulation(population);
        city.setEstablishmentDate(establishmentDate);
        city.setGovernor(governor);
        city.setCapital(capital);
        city.setPopulationDensity(populationDensity);

        MagicCity savedCity = magicCityRepo.save(city);
        // Уведомляем клиентов о создании города
        CreaturesWebSocket.notifyCityCreated(savedCity.getId());
        return savedCity;
    }

    public Optional<MagicCity> getMagicCityById(Long id) {
        return magicCityRepo.findById(id);
    }

    public List<MagicCity> getAllMagicCities() {
        return magicCityRepo.findAll();
    }

    public List<MagicCity> getAllMagicCities(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return magicCityRepo.findAll(offset, pageSize);
    }

    public MagicCity updateMagicCity(MagicCity city) {
        MagicCity updatedCity = magicCityRepo.update(city);
        // Уведомляем клиентов об обновлении города
        CreaturesWebSocket.notifyCityUpdated(updatedCity.getId());
        return updatedCity;
    }

    public boolean deleteMagicCity(Long id) {
        Optional<MagicCity> city = magicCityRepo.findById(id);
        if (city.isPresent()) {
            magicCityRepo.delete(city.get());
            // Уведомляем клиентов об удалении города
            CreaturesWebSocket.notifyCityDeleted(id);
            return true;
        }
        return false;
    }

    public List<MagicCity> findCitiesWithElfGovernor() {
        return magicCityRepo.findCitiesWithElfGovernor();
    }

}
