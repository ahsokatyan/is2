package com.antonov.is2.beans.special;

import com.antonov.is2.entities.BookCreature;
import com.antonov.is2.entities.MagicCity;
import com.antonov.is2.entities.Ring;
import com.antonov.is2.utils.AttackLevelDeletionAnalysis;
import com.antonov.is2.services.BookCreatureService;
import com.antonov.is2.services.MagicCityService;
import com.antonov.is2.services.RingService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Named
@ViewScoped
@Getter @Setter
public class SpecialOperationsBean implements Serializable {

    @Inject
    private BookCreatureService bookCreatureService;

    @Inject
    private RingService ringService;

    @Inject
    private MagicCityService magicCityService;

    // Результаты операций
    private BookCreature creatureWithMaxCreationDate;
    private List<Ring> uniqueRings;
    private Long attackLevelToDelete;
    private String operationResult;

    // Поля для операции удаления по attackLevel
    private AttackLevelDeletionAnalysis deletionAnalysis;
    private boolean showAttackLevelConfirmation = false;
    private List<BookCreature> ringsToReassignTo; // Существа, которым будут назначены кольца
    private List<Ring> ringsToReassign; // Кольца, которые будут переназначены
    private List<Ring> ringsWithoutOwner; // Кольца, которые будут без хозяина

    // Поля для операции уничтожения городов эльфов
    private Long targetCityId; // null означает оставить существ без города
    private List<MagicCity> destroyedElfCities; // Список уничтоженных городов для отображения

    @PostConstruct
    public void init() {
        // Инициализация при необходимости
    }

    // ===== РАБОЧИЕ ОПЕРАЦИИ =====

    /**
     * Вернуть один (любой) объект, значение поля creationDate которого является максимальным
     */
    public void findCreatureWithMaxCreationDate() {
        try {
            List<BookCreature> allCreatures = bookCreatureService.getAllBookCreatures();
            if (!allCreatures.isEmpty()) {
                creatureWithMaxCreationDate = allCreatures.stream()
                        .max((c1, c2) -> c1.getCreationDate().compareTo(c2.getCreationDate()))
                        .orElse(null);

                if (creatureWithMaxCreationDate != null) {
                    addMessage("Успех",
                            "Найдено существо с максимальной датой создания: " +
                                    creatureWithMaxCreationDate.getName() +
                                    " (ID: " + creatureWithMaxCreationDate.getId() + ")");
                }
            } else {
                addMessage("Информация", "В базе нет существ");
            }
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось выполнить операцию: " + e.getMessage());
        }
    }

    /**
     * Вернуть массив уникальных значений поля ring по всем объектам
     */
    public void findUniqueRings() {
        try {
            List<BookCreature> allCreatures = bookCreatureService.getAllBookCreatures();
            Set<Ring> rings = allCreatures.stream()
                    .filter(creature -> creature.getRing() != null)
                    .map(BookCreature::getRing)
                    .collect(Collectors.toSet());

            uniqueRings = rings.stream().collect(Collectors.toList());

            if (!uniqueRings.isEmpty()) {
                addMessage("Успех", "Найдено уникальных колец: " + uniqueRings.size());
            } else {
                addMessage("Информация", "Нет существ с кольцами");
            }
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось выполнить операцию: " + e.getMessage());
        }
    }

    // ===== ЗАГЛУШКИ =====

    /**
     * Удалить все объекты, значение поля attackLevel которого эквивалентно заданному
     */
    public void deleteByAttackLevel() {
        if (attackLevelToDelete == null) {
            addMessage("Ошибка", "Введите значение attackLevel");
            return;
        }

        try {
            // Проводим анализ
            deletionAnalysis = bookCreatureService.analyzeDeletionByAttackLevel(attackLevelToDelete);

            if (!deletionAnalysis.hasCreaturesToDelete()) {
                addMessage("Информация", "Не найдено существ с attackLevel = " + attackLevelToDelete);
                showAttackLevelConfirmation = false;
                return;
            }

            // Подготовим списки для отображения переназначения
            List<Ring> ringsFromDeleted = deletionAnalysis.getRingsFromDeletedCreatures();
            List<BookCreature> creaturesWithoutRings = deletionAnalysis.getCreaturesWithoutRings();

            // Разделяем кольца на те, что будут переназначены, и те, что останутся без хозяина
            ringsToReassign = ringsFromDeleted.stream()
                    .limit(creaturesWithoutRings.size())
                    .collect(Collectors.toList());

            ringsWithoutOwner = ringsFromDeleted.size() > creaturesWithoutRings.size() ?
                    ringsFromDeleted.subList(creaturesWithoutRings.size(), ringsFromDeleted.size()) :
                    java.util.Collections.emptyList();

            ringsToReassignTo = creaturesWithoutRings.stream()
                    .limit(ringsToReassign.size())
                    .collect(Collectors.toList());

            // Показываем подтверждение
            showAttackLevelConfirmation = true;

        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось выполнить анализ операции: " + e.getMessage());
        }
    }

    /**
     * Подтверждает и выполняет удаление по attackLevel
     */
    public void confirmDeleteByAttackLevel() {
        if (deletionAnalysis == null || deletionAnalysis.getTargetAttackLevel() == null) {
            addMessage("Ошибка", "Нет данных для выполнения операции");
            return;
        }

        try {
            bookCreatureService.performDeletionByAttackLevel(deletionAnalysis.getTargetAttackLevel());

            addMessage("Успех", String.format("Удалено %d существ с attackLevel = %d, переназначено %d колец",
                    deletionAnalysis.getCreaturesToDelete().size(),
                    deletionAnalysis.getTargetAttackLevel(),
                    Math.min(deletionAnalysis.getRingsFromDeletedCreatures().size(),
                            deletionAnalysis.getCreaturesWithoutRings().size())));

            // Сбрасываем состояние
            resetAttackLevelOperation();

        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось выполнить операцию: " + e.getMessage());
        }
    }

    /**
     * Отменяет операцию удаления по attackLevel
     */
    public void cancelDeleteByAttackLevel() {
        resetAttackLevelOperation();
        addMessage("Информация", "Операция удаления отменена");
    }

    private void resetAttackLevelOperation() {
        showAttackLevelConfirmation = false;
        deletionAnalysis = null;
        ringsToReassignTo = null;
        ringsToReassign = null;
        ringsWithoutOwner = null;
        attackLevelToDelete = null;
    }

    /**
     * Забрать все кольца у хоббитов
     */
    public void takeRingsFromHobbits() {
        try {
            int ringsTaken = bookCreatureService.takeRingsFromHobbits();
            addMessage("Успех",
                    "Отобрано " + ringsTaken + " колец у хоббитов. Кольца теперь не привязаны к существам.");

        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось выполнить операцию: " + e.getMessage());
        }
    }

    /**
     * Уничтожить города эльфов
     */
    public void destroyElfCities() {
        try {
            // Находим города эльфов
            List<MagicCity> elfCities = magicCityService.findCitiesWithElfGovernor();

            if (elfCities.isEmpty()) {
                addMessage("Информация", "Не найдено городов с эльфами-правителями");
                return;
            }

            // Находим существ, проживающих в этих городах
            List<BookCreature> affectedCreatures = bookCreatureService.getAllBookCreatures().stream()
                    .filter(creature -> creature.getCreatureLocation() != null)
                    .filter(creature -> elfCities.contains(creature.getCreatureLocation()))
                    .collect(Collectors.toList());

            // Если есть существа, которых нужно переселить или оставить без города
            if (!affectedCreatures.isEmpty()) {
                if (targetCityId != null) {
                    // Если нужно переселить, находим целевой город
                    MagicCity targetCity = magicCityService.getMagicCityById(targetCityId)
                            .orElseThrow(() -> new IllegalArgumentException("Целевой город не найден"));

                    // Переселяем всех существ в целевой город
                    for (BookCreature creature : affectedCreatures) {
                        creature.setCreatureLocation(targetCity);
                    }

                    bookCreatureService.updateCreatures(affectedCreatures);
                    addMessage("Успех", String.format("Уничтожено %d городов эльфов. %d существ переселены в город %s",
                            elfCities.size(),
                            affectedCreatures.size(),
                            targetCity.getName()));
                } else {
                    // Оставляем существ без города
                    for (BookCreature creature : affectedCreatures) {
                        creature.setCreatureLocation(null);
                    }

                    bookCreatureService.updateCreatures(affectedCreatures);
                    addMessage("Успех", String.format("Уничтожено %d городов эльфов. %d существ оставлены без города",
                            elfCities.size(),
                            affectedCreatures.size()));
                }
            } else {
                addMessage("Успех", String.format("Уничтожено %d городов эльфов. Никто не пострадал.",
                        elfCities.size()));
            }

            // Удаляем города эльфов и сохраняем для отображения
            for (MagicCity city : elfCities) {
                magicCityService.deleteMagicCity(city.getId());
            }

            // Сохраняем уничтоженные города для отображения (до 20)
            this.destroyedElfCities = elfCities.stream()
                    .limit(20)
                    .collect(Collectors.toList());

            // Сбрасываем параметры
            targetCityId = null;

        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось выполнить операцию: " + e.getMessage());
        }
    }

    // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====

    private void addMessage(String summary, String detail) {
        operationResult = detail;
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

    public List<MagicCity> getAvailableCities() {
        return magicCityService.getAllMagicCities();
    }

    public List<MagicCity> getNonElfCities() {
        return magicCityService.getAllMagicCities().stream()
                .filter(city -> city.getGovernor() == null || city.getGovernor() != com.antonov.is2.entities.BookCreatureType.ELF)
                .limit(20) // Ограничиваем первыми 20 городами
                .collect(Collectors.toList());
    }

    public List<MagicCity> getDestroyedElfCities() {
        return destroyedElfCities != null ? destroyedElfCities : java.util.Collections.emptyList();
    }

    // Getters для операции удаления по attackLevel
    public boolean isShowAttackLevelConfirmation() {
        return showAttackLevelConfirmation;
    }

    public AttackLevelDeletionAnalysis getDeletionAnalysis() {
        return deletionAnalysis;
    }

    public List<BookCreature> getRingsToReassignTo() {
        return ringsToReassignTo != null ? ringsToReassignTo : java.util.Collections.emptyList();
    }

    public List<Ring> getRingsToReassign() {
        return ringsToReassign != null ? ringsToReassign : java.util.Collections.emptyList();
    }

    public List<Ring> getRingsWithoutOwner() {
        return ringsWithoutOwner != null ? ringsWithoutOwner : java.util.Collections.emptyList();
    }

}