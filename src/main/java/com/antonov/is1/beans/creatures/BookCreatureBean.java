package com.antonov.is1.beans.creatures;

import com.antonov.is1.entities.*;
import com.antonov.is1.services.*;
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
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;


@Named
@ViewScoped
@Getter @Setter
public class BookCreatureBean implements Serializable {

    @Inject
    private BookCreatureService bookCreatureService;

    @Inject
    private CoordinatesService coordinatesService;

    @Inject
    private MagicCityService magicCityService;

    @Inject
    private RingService ringService;

    // Данные для отображения
    private List<BookCreature> creatures;
    private List<MagicCity> availableCities;
    private List<Ring> availableRings;

    // Поля для сортировки
    private String sortBy = "id"; // по умолчанию сортируем по ID
    private boolean ascending = true; // по умолчанию по возрастанию

    // Поле для фильтрации
    private String filterName = "";

    // Выбранные объекты для операций
    private BookCreature selectedCreature;
    private BookCreature newCreature;


    private Long selectedCityId;
    private Long selectedRingId;

    // Пагинация
    private int currentPage = 1;
    private int pageSize = 20;
    private long totalCount;



    @PostConstruct
    public void init() {
        loadData();
        initNewCreature();
    }

    public void loadData() {
        List<BookCreature> allCreatures = bookCreatureService.getAllBookCreatures();

        // Применяем фильтрацию
        allCreatures = applyFiltering(allCreatures);
        totalCount = allCreatures.size();

        // Применяем сортировку
        allCreatures = applySorting(allCreatures);

        // Применяем пагинацию к отфильтрованным и отсортированным данным
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allCreatures.size());

        if (startIndex < allCreatures.size()) {
            creatures = allCreatures.subList(startIndex, endIndex);
        } else {
            creatures = List.of(); // если страница пуста
        }

        availableCities = magicCityService.getAllMagicCities();
        availableRings = ringService.getFreeRings();
    }

    private List<BookCreature> applyFiltering(List<BookCreature> creatures) {
        if (filterName == null || filterName.trim().isEmpty()) {
            return creatures;
        }

        String filterLower = filterName.toLowerCase().trim();
        return creatures.stream()
                .filter(creature -> creature.getName().toLowerCase().contains(filterLower))
                .collect(Collectors.toList());
    }

    private List<BookCreature> applySorting(List<BookCreature> creatures) {
        Comparator<BookCreature> comparator;

        switch (sortBy) {
            case "id":
                comparator = Comparator.comparing(BookCreature::getId);
                break;
            case "name":
                comparator = Comparator.comparing(BookCreature::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "creatureType":
                comparator = Comparator.comparing(BookCreature::getCreatureType,
                    Comparator.nullsFirst(Comparator.naturalOrder()));
                break;
            case "age":
                comparator = Comparator.comparing(BookCreature::getAge);
                break;
            case "attackLevel":
                comparator = Comparator.comparing(BookCreature::getAttackLevel);
                break;
            default:
                comparator = Comparator.comparing(BookCreature::getId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return creatures.stream().sorted(comparator).collect(Collectors.toList());
    }

    public void sortBy(String field) {
        if (field.equals(sortBy)) {
            // Если уже сортируем по этому полю, меняем направление
            ascending = !ascending;
        } else {
            // Если сортируем по новому полю, устанавливаем направление по возрастанию
            sortBy = field;
            ascending = true;
        }
        currentPage = 1; // Сброс на первую страницу при изменении сортировки
        loadData();
    }

    public void filterCreatures() {
        currentPage = 1; // Сброс на первую страницу при фильтрации
        loadData();
    }

    public void resetFilter() {
        filterName = "";
        currentPage = 1; // Сброс на первую страницу при очистке фильтра
        loadData();
    }

    // Методы для отображения индикатора сортировки
    public String getSortIndicator(String field) {
        if (field.equals(sortBy)) {
            return ascending ? " ▲" : " ▼"; // стрелки для индикации направления сортировки
        }
        return "";
    }

    private void initNewCreature() {
        newCreature = new BookCreature();
        newCreature.setCoordinates(new Coordinates());
    }

    // ===== CRUD OPERATIONS =====

    public void createCreature() {
        try {
            bookCreatureService.createBookCreature(newCreature);
            addMessage("Успех", "Существо создано успешно!");
            loadData();
            initNewCreature();
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось создать существо: " + e.getMessage());
        }
    }


    public void updateCreature() {
        try {
            bookCreatureService.updateBookCreature(selectedCreature);
            addMessage("Успех", "Существо обновлено успешно!");
            loadData();
            selectedCreature = null;
        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось обновить существо: " + e.getMessage());
        }
    }

    public void deleteCreature() {
        if (selectedCreature != null) {
            try {
                bookCreatureService.deleteBookCreature(selectedCreature.getId());
                addMessage("Успех", "Существо удалено успешно!");
                loadData();
                selectedCreature = null;
            } catch (Exception e) {
                addMessage("Ошибка", "Не удалось удалить существо: " + e.getMessage());
            }
        }
    }

    // ===== PAGINATION =====

    public void nextPage() {
        if (hasNextPage()) {
            currentPage++;
            loadData();
        }
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
            loadData();
        }
    }

    public void firstPage() {
        currentPage = 1;
        loadData();
    }

//    public void lastPage() {
//        currentPage = (int) Math.ceil((double) totalCount / pageSize);
//        loadData();
//    }

    public void lastPage() {
        currentPage = Math.toIntExact(getTotalPages());
        loadData();
    }

    public boolean hasNextPage() {
        return currentPage < getTotalPages();
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }

    public long getTotalPages() {
        if (totalCount == 0) return 1;
        return (long) Math.ceil((double) totalCount / pageSize);
    }

    // ===== UTILITY METHODS =====

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail)
        );
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        firstPage(); // Сброс на первую страницу при изменении размера
    }


    public String createCreatureInSeparatePage() {
        try {
            // Валидация обязательных полей
            if (newCreature.getName() == null || newCreature.getName().trim().isEmpty()) {
                addMessage("Ошибка", "Имя обязательно");
                return null;
            }

            if (newCreature.getAge() == null || newCreature.getAge() <= 0) {
                addMessage("Ошибка", "Возраст должен быть больше 0");
                return null;
            }

            if (newCreature.getCoordinates() == null) {
                addMessage("Ошибка", "Координаты обязательны");
                return null;
            }

            // Устанавливаем связи по ID

            if (selectedCityId != null) {
                Optional<MagicCity> city = magicCityService.getMagicCityById(selectedCityId);
                city.ifPresent(newCreature::setCreatureLocation);
            }

            if (selectedRingId != null) {
                Optional<Ring> ring = ringService.getRingById(selectedRingId);
                ring.ifPresent(newCreature::setRing);
            }

            // Сохраняем
            BookCreature savedCreature = bookCreatureService.createBookCreature(newCreature);

            // Сбрасываем форму
            initNewCreature();
            selectedCityId = null;
            selectedRingId = null;

            // Редирект на страницу просмотра созданного существа
            return "view-creature?id=" + savedCreature.getId() + "&faces-redirect=true";

        } catch (Exception e) {
            addMessage("Ошибка", "Не удалось создать существо: " + e.getMessage());
            return null;
        }
    }



}