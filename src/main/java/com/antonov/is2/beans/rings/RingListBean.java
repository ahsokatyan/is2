package com.antonov.is2.beans.rings;

import com.antonov.is2.entities.Ring;
import com.antonov.is2.services.RingService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@Named
@ViewScoped
@Getter @Setter
public class RingListBean implements Serializable {

    @Inject
    private RingService ringService;

    private List<Ring> rings;
    private final int pageSize = 20;
    private int currentPage = 1;
    private long totalCount;

    // Поля для сортировки
    private String sortBy = "id"; // по умолчанию сортируем по ID
    private boolean ascending = true; // по умолчанию по возрастанию

    // Поле для фильтрации
    private String filterName = "";

    @PostConstruct
    public void init() {
        loadData();
    }

    public void loadData() {
        List<Ring> allRings = ringService.getAllRings();

        // Применяем фильтрацию
        allRings = applyFiltering(allRings);
        totalCount = allRings.size();

        // Применяем сортировку
        allRings = applySorting(allRings);

        // Применяем пагинацию к отфильтрованным и отсортированным данным
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allRings.size());

        if (startIndex < allRings.size()) {
            rings = allRings.subList(startIndex, endIndex);
        } else {
            rings = List.of(); // если страница пуста
        }
    }

    private List<Ring> applyFiltering(List<Ring> rings) {
        if (filterName == null || filterName.trim().isEmpty()) {
            return rings;
        }

        String filterLower = filterName.toLowerCase().trim();
        return rings.stream()
                .filter(ring -> ring.getName().toLowerCase().contains(filterLower))
                .collect(Collectors.toList());
    }

    private List<Ring> applySorting(List<Ring> rings) {
        Comparator<Ring> comparator;

        switch (sortBy) {
            case "id":
                comparator = Comparator.comparing(Ring::getId);
                break;
            case "name":
                comparator = Comparator.comparing(Ring::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "power":
                comparator = Comparator.comparing(Ring::getPower,
                    Comparator.nullsFirst(Comparator.naturalOrder()));
                break;
            case "weight":
                comparator = Comparator.comparing(Ring::getWeight);
                break;
            default:
                comparator = Comparator.comparing(Ring::getId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return rings.stream().sorted(comparator).collect(Collectors.toList());
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

    public void filterRings() {
        currentPage = 1; // Сброс на первую страницу при фильтрации
        loadData();
    }

    public void resetFilter() {
        filterName = "";
        currentPage = 1; // Сброс на первую страницу при очистке фильтра
        loadData();
    }

    // Пагинация
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

    public int getTotalPages() {
        if (totalCount == 0) return 1;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    // Методы для отображения индикатора сортировки
    public String getSortIndicator(String field) {
        if (field.equals(sortBy)) {
            return ascending ? " ▲" : " ▼"; // стрелки для индикации направления сортировки
        }
        return "";
    }

}