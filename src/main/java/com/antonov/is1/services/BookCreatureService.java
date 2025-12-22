package com.antonov.is1.services;


import com.antonov.is1.entities.*;
import com.antonov.is1.repos.*;
import com.antonov.is1.utils.AttackLevelDeletionAnalysis;
import com.antonov.is1.websocket.CreaturesWebSocket;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class BookCreatureService {

    @Inject
    private BookCreatureRepository bookCreatureRepo;

    @Inject
    private CoordinatesRepository coordinatesRepo;

    @Inject
    private MagicCityRepository magicCityRepo;

    @Inject
    private RingRepository ringRepo;


    /**
     * Получение BookCreature по ID
     */
    public Optional<BookCreature> getBookCreatureById(Long id) {
        return bookCreatureRepo.findById(id);
    }

    /**
     * Получение всех BookCreature с пагинацией
     */
    public List<BookCreature> getAllBookCreatures(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return bookCreatureRepo.findAll(offset, pageSize);
    }

    /**
     * Получение всех BookCreature без пагинации
     */
    public List<BookCreature> getAllBookCreatures() {
        return bookCreatureRepo.findAll();
    }


    /**
     * Создание BookCreature со связями (координаты, город, кольцо)
     * Все связанные объекты сохраняются каскадно
     */

    public BookCreature createBookCreatureWithoutRelations(String name,
                                                        Coordinates coordinates,
                                                        Long age,
                                                        BookCreatureType creatureType,
                                                        MagicCity creatureLocation,
                                                        Long attackLevel,
                                                        Ring ring) {
        Coordinates managedCoordinates = coordinatesRepo.findById(coordinates.getId())
                .orElseThrow(() -> new EntityNotFoundException("Coordinates not found"));

        MagicCity managedCreatureLocation = magicCityRepo.findById(creatureLocation.getId())
                .orElseThrow(() -> new EntityNotFoundException("City not found"));

        Ring managedRing = ringRepo.findById(ring.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ring not found"));

        BookCreature creature = new BookCreature();
        creature.setName(name);
        creature.setCoordinates(managedCoordinates);
        creature.setAge(age);
        creature.setCreatureType(creatureType);
        creature.setCreatureLocation(managedCreatureLocation);
        creature.setAttackLevel(attackLevel);
        creature.setRing(managedRing);

        return bookCreatureRepo.save(creature);
    }
    /**
     * Получение количества всех BookCreature (для пагинации)
     */
    public long getBookCreaturesCount() {
        return bookCreatureRepo.count();
    }


    public boolean assignRingToCreature(Long creatureId, Long ringId) {
        // Проверяем, свободно ли кольцо
        if (ringRepo.isRingUsed(ringId)) {
            throw new IllegalStateException("Кольцо уже используется другим существом");
        }

        BookCreature creature = bookCreatureRepo.findById(creatureId).orElse(null);
        Ring ring = ringRepo.findById(ringId).orElse(null);

        creature.setRing(ring);
        bookCreatureRepo.merge(creature);

        return true;
    }
    /**
     * Создание нового BookCreature с автоматической установкой creationDate
     */
    public BookCreature createBookCreature(BookCreature creature) {

        // creationDate автоматически установится благодаря @PrePersist
        if (creature.getCreatureLocation() != null) {
            MagicCity managedCreatureLocation = magicCityRepo.findById(creature.getCreatureLocation().getId())
                    .orElseThrow(() -> new EntityNotFoundException("City not found"));
            creature.setCreatureLocation(managedCreatureLocation);
        }
        if (creature.getRing() != null) {
            Ring managedRing = ringRepo.findById(creature.getRing().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Ring not found"));
            creature.setRing(managedRing);
        }


        BookCreature savedCreature = bookCreatureRepo.save(creature);
        // Уведомляем клиентов о создании существа
        CreaturesWebSocket.notifyCreatureCreated(savedCreature.getId());
        return savedCreature;

    }
    /**
     * Обновление BookCreature
     */
    public BookCreature updateBookCreature(BookCreature creature) {
        BookCreature updatedCreature = bookCreatureRepo.update(creature);
        // Уведомляем клиентов об обновлении существа
        CreaturesWebSocket.notifyCreatureUpdated(updatedCreature.getId());
        return updatedCreature;
    }

    /**
     * Удаление BookCreature по ID
     */
    public boolean deleteBookCreature(Long id) {
        Optional<BookCreature> creature = bookCreatureRepo.findById(id);
        if (creature.isPresent()) {
            bookCreatureRepo.delete(creature.get());
            // Уведомляем клиентов об удалении существа
            CreaturesWebSocket.notifyCreatureDeleted(id);
            return true;
        }
        return false;
    }

    public List<BookCreature> findBookCreaturesByType(BookCreatureType type) {
        return bookCreatureRepo.findByType(type);
    }

    /**
     * Забирает все кольца у хоббитов и оставляет их без хозяина
     */
    public int takeRingsFromHobbits() {
        List<BookCreature> hobbitsWithRings = bookCreatureRepo.findByTypeWithRings(BookCreatureType.HOBBIT);

        int ringsTaken = 0;
        for (BookCreature hobbit : hobbitsWithRings) {
            // Освобождаем кольцо от хоббита
            hobbit.setRing(null);
            bookCreatureRepo.update(hobbit);
            ringsTaken++;
        }

        return ringsTaken;
    }

    /**
     * Находит всех существ, проживающих в указанном городе
     */
    public List<BookCreature> findCreaturesByCity(MagicCity city) {
        return bookCreatureRepo.findByCity(city);
    }

    /**
     * Обновляет список существ в базе данных
     */
    public void updateCreatures(List<BookCreature> creatures) {
        for (BookCreature creature : creatures) {
            bookCreatureRepo.update(creature);
        }
    }

    /**
     * Находит всех существ с заданным значением attackLevel
     */
    public List<BookCreature> findBookCreaturesByAttackLevel(Long attackLevel) {
        return bookCreatureRepo.findAll().stream()
                .filter(creature -> creature.getAttackLevel() == attackLevel)
                .collect(Collectors.toList());
    }

    /**
     * Анализирует данные для операции удаления по attackLevel
     */
    public AttackLevelDeletionAnalysis analyzeDeletionByAttackLevel(Long attackLevel) {
        AttackLevelDeletionAnalysis analysis = new AttackLevelDeletionAnalysis();
        analysis.setTargetAttackLevel(attackLevel);

        // Находим существ, подлежащие удалению
        List<BookCreature> creaturesToDelete = findBookCreaturesByAttackLevel(attackLevel);
        analysis.setCreaturesToDelete(creaturesToDelete);

        // Находим кольца от удаляемых существ
        List<Ring> ringsFromDeletedCreatures = creaturesToDelete.stream()
                .filter(creature -> creature.getRing() != null)
                .map(BookCreature::getRing)
                .collect(Collectors.toList());
        analysis.setRingsFromDeletedCreatures(ringsFromDeletedCreatures);

        // Находим существ без колец (не включая тех, что будут удалены)
        List<BookCreature> allCreatures = bookCreatureRepo.findAll();
        List<BookCreature> creaturesWithoutRings = allCreatures.stream()
                .filter(creature -> creature.getRing() == null)
                .filter(creature -> !creaturesToDelete.contains(creature))
                .collect(Collectors.toList());
        analysis.setCreaturesWithoutRings(creaturesWithoutRings);

        return analysis;
    }

    /**
     * Выполняет удаление существ по attackLevel с переназначением колец
     */
    public void performDeletionByAttackLevel(Long attackLevel) {
        AttackLevelDeletionAnalysis analysis = analyzeDeletionByAttackLevel(attackLevel);

        // Сначала освобождаем кольца от существ, подлежащих удалению
        List<BookCreature> creaturesToDelete = analysis.getCreaturesToDelete();
        List<Ring> ringsFromDeletedCreatures = analysis.getRingsFromDeletedCreatures();
        List<BookCreature> creaturesWithoutRings = analysis.getCreaturesWithoutRings();

        // Освобождаем кольца у существ, которые будут удалены
        for (BookCreature creature : creaturesToDelete) {
            if (creature.getRing() != null) {
                creature.setRing(null);
                bookCreatureRepo.update(creature);
            }
        }

        // Переназначаем кольца существам без колец
        int ringsAssigned = 0;
        for (int i = 0; i < ringsFromDeletedCreatures.size(); i++) {
            if (i < creaturesWithoutRings.size()) {
                // Есть существа, которым можно назначить кольцо
                BookCreature targetCreature = creaturesWithoutRings.get(i);
                targetCreature.setRing(ringsFromDeletedCreatures.get(i));
                bookCreatureRepo.update(targetCreature);
            }
            // Если колец больше, чем существ без колец, то оставшиеся кольца остаются без хозяина
        }

        // Наконец, удаляем существ по заданному attackLevel
        for (BookCreature creature : creaturesToDelete) {
            bookCreatureRepo.delete(creature);
        }
    }

}