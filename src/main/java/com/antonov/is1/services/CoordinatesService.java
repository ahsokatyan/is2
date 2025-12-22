package com.antonov.is1.services;

import com.antonov.is1.entities.Coordinates;
import com.antonov.is1.repos.CoordinatesRepository;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Stateless
public class CoordinatesService {

    @Inject
    private CoordinatesRepository coordinatesRepo;

    public Coordinates createCoordinates(Double x, Long y) {
        Coordinates coordinates = new Coordinates();
        coordinates.setX(x);
        coordinates.setY(y);
        return coordinatesRepo.save(coordinates);
    }

    public Optional<Coordinates> getCoordinatesById(Long id) {
        return coordinatesRepo.findById(id);
    }

    public List<Coordinates> getAllCoordinates() {
        return coordinatesRepo.findAll();
    }

    public Coordinates updateCoordinates(Coordinates coordinates) {
        return coordinatesRepo.update(coordinates);
    }

    public boolean deleteCoordinates(Long id) {
        Optional<Coordinates> coordinates = coordinatesRepo.findById(id);
        if (coordinates.isPresent()) {
            coordinatesRepo.delete(coordinates.get());
            return true;
        }
        return false;
    }
}