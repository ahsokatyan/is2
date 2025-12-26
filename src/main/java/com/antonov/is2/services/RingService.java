package com.antonov.is2.services;

import com.antonov.is2.entities.Ring;
import com.antonov.is2.repos.RingRepository;
import com.antonov.is2.websocket.CreaturesWebSocket;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Stateless
public class RingService {

    @Inject
    private RingRepository ringRepo;

    public Ring createRing(String name, Long power, Double weight) {
        Ring ring = new Ring();
        ring.setName(name);
        ring.setPower(power);
        ring.setWeight(weight);
        Ring savedRing = ringRepo.save(ring);
        // Уведомляем клиентов о создании кольца
        CreaturesWebSocket.notifyRingCreated(savedRing.getId());
        return savedRing;
    }

    public List<Ring> getAllRings(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return ringRepo.findAll(offset, pageSize);
    }

    public Optional<Ring> getRingById(Long id) {
        return ringRepo.findById(id);
    }

    public List<Ring> getAllRings() {
        return ringRepo.findAll();
    }

    public Ring updateRing(Ring ring) {
        Ring updatedRing = ringRepo.update(ring);
        // Уведомляем клиентов об обновлении кольца
        CreaturesWebSocket.notifyRingUpdated(updatedRing.getId());
        return updatedRing;
    }

    public boolean deleteRing(Long id) {
        Optional<Ring> ring = ringRepo.findById(id);
        if (ring.isPresent()) {
            ringRepo.delete(ring.get());
            // Уведомляем клиентов об удалении кольца
            CreaturesWebSocket.notifyRingDeleted(id);
            return true;
        }
        return false;
    }

    public List<Ring> getFreeRings(){
        return ringRepo.getFreeRings();
    }
}
