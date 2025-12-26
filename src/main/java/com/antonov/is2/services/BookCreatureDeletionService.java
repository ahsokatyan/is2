package com.antonov.is2.services;

import com.antonov.is2.entities.BookCreature;
import com.antonov.is2.entities.Ring;
import com.antonov.is2.repos.BookCreatureRepository;
import com.antonov.is2.repos.RingRepository;
import com.antonov.is2.utils.BookCreatureDeletionAnalysis;
import com.antonov.is2.websocket.CreaturesWebSocket;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class BookCreatureDeletionService {

    @Inject
    private BookCreatureRepository bookCreatureRepo;

    @Inject
    private RingRepository ringRepo;

    /**
     * Анализирует связи перед удалением существа
     */
    public BookCreatureDeletionAnalysis analyzeDeletion(Long creatureId) {
        BookCreature creature = bookCreatureRepo.findById(creatureId)
                .orElseThrow(() -> new IllegalArgumentException("Существо не найдено"));

        BookCreatureDeletionAnalysis analysis = new BookCreatureDeletionAnalysis();
        analysis.setCreatureToDelete(creature);

        // Анализируем кольцо - если у существа есть кольцо, нужно его перепривязать
        if (creature.getRing() != null) {
            Ring ring = creature.getRing();
            analysis.setOrphanedRing(ring);

            // Находим существ без колец для перепривязки (кроме удаляемого)
            List<BookCreature> creaturesWithoutRings = bookCreatureRepo.findAll().stream()
                    .filter(c -> c.getRing() == null && !c.getId().equals(creatureId))
                    .collect(Collectors.toList());
            analysis.setAvailableCreaturesForRing(creaturesWithoutRings);
        }

        return analysis;
    }

    /**
     * Находит существа без колец
     */
    private List<BookCreature> findCreaturesWithoutRings(Long excludeCreatureId) {
        List<BookCreature> allCreatures = bookCreatureRepo.findAll();
        return allCreatures.stream()
                .filter(creature -> creature.getRing() == null &&
                        !creature.getId().equals(excludeCreatureId))
                .collect(Collectors.toList());
    }

    /**
     * Удаляет существо с перепривязкой кольца
     */
    public void deleteCreatureWithReassignment(Long creatureId, Long newRingOwnerId) {
        BookCreature creature = bookCreatureRepo.findById(creatureId)
                .orElseThrow(() -> new IllegalArgumentException("Существо не найдено"));

        // Обрабатываем кольцо
        if (creature.getRing() != null) {
            Ring ring = creature.getRing();

            if (newRingOwnerId != null) {
                // Если выбрано новое существо для перепривязки
                BookCreature newOwner = bookCreatureRepo.findById(newRingOwnerId)
                        .orElseThrow(() -> new IllegalArgumentException("Новый владелец кольца не найден"));

                // Перепривязываем кольцо
                creature.setRing(null); // Отвязываем от старого владельца
                newOwner.setRing(ring); // Привязываем к новому владельцу

                bookCreatureRepo.update(newOwner);
            } else {
                // Оставляем кольцо без владельца
                creature.setRing(null);
            }
        }
        // Удаляем существо (Coordinates удалятся каскадно)
        bookCreatureRepo.delete(creature);
        // Уведомляем клиентов об удалении существа
        CreaturesWebSocket.notifyCreatureDeleted(creatureId);
    }
}