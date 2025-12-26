package com.antonov.is2.services;

import com.antonov.is2.entities.BookCreature;
import com.antonov.is2.entities.Ring;
import com.antonov.is2.repos.BookCreatureRepository;
import com.antonov.is2.repos.RingRepository;
import com.antonov.is2.utils.RingDeletionAnalysis;
import com.antonov.is2.websocket.CreaturesWebSocket;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class RingDeletionService {

    @Inject
    private RingRepository ringRepo;

    @Inject
    private BookCreatureRepository bookCreatureRepo;

    /**
     * Анализирует связи перед удалением кольца
     */
    public RingDeletionAnalysis analyzeDeletion(Long ringId) {
        Ring ring = ringRepo.findById(ringId).orElse(null);

        if (ring == null) {
            throw new IllegalArgumentException("Кольцо с ID " + ringId + " не найдено");
        }

        RingDeletionAnalysis analysis = new RingDeletionAnalysis();
        analysis.setRingToDelete(ring);

        // Находим существо, которое владеет этим кольцом
        BookCreature currentOwner = findRingOwner(ringId);
        if (currentOwner != null) {
            analysis.setCurrentOwner(currentOwner);

            // Находим свободные кольца для перепривязки
            List<Ring> freeRings = findFreeRings(ringId);
            analysis.setAvailableRings(freeRings);
        }

        return analysis;
    }

    /**
     * Находит владельца кольца
     */
    private BookCreature findRingOwner(Long ringId) {
        List<BookCreature> allCreatures = bookCreatureRepo.findAll();
        return allCreatures.stream()
                .filter(creature -> creature.getRing() != null &&
                        creature.getRing().getId().equals(ringId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Находит свободные кольца (не привязанные к существам)
     */
    private List<Ring> findFreeRings(Long excludeRingId) {
        List<Ring> allRings = ringRepo.findAll();
        List<BookCreature> creaturesWithRings = bookCreatureRepo.findAll().stream()
                .filter(creature -> creature.getRing() != null)
                .collect(Collectors.toList());

        return allRings.stream()
                .filter(ring -> !ring.getId().equals(excludeRingId)) // исключаем удаляемое кольцо
                .filter(ring -> creaturesWithRings.stream()
                        .noneMatch(creature ->
                                creature.getRing() != null &&
                                        creature.getRing().getId().equals(ring.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Удаляет кольцо с перепривязкой владельца
     */
    public void deleteRingWithReassignment(Long ringId, Long newRingId) {
        Ring ring = ringRepo.findById(ringId).orElse(null);

        if (ring == null) {
            throw new IllegalArgumentException("Кольцо с ID " + ringId + " не найдено");
        }

        // Находим текущего владельца
        BookCreature currentOwner = findRingOwner(ringId);

        if (currentOwner != null) {
            // Если выбрано новое кольцо для перепривязки
            if (newRingId != null) {
                Ring newRing = ringRepo.findById(newRingId).orElse(null);

                if (newRing != null) {
                    // Перепривязываем к новому кольцу
                    currentOwner.setRing(newRing);
                    bookCreatureRepo.update(currentOwner);
                }
            } else {
                // Оставляем без кольца
                currentOwner.setRing(null);
                bookCreatureRepo.update(currentOwner);
            }
        }

        // Удаляем кольцо
        ringRepo.delete(ring);
        // Уведомляем клиентов об удалении кольца
        CreaturesWebSocket.notifyRingDeleted(ringId);
    }
}