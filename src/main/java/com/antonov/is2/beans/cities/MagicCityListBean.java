package com.antonov.is2.beans.cities;

import com.antonov.is2.entities.MagicCity;
import com.antonov.is2.services.MagicCityService;
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
@Getter
@Setter
public class MagicCityListBean implements Serializable {

    @Inject
    private MagicCityService magicCityService;

    private List<MagicCity> cities;
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
        List<MagicCity> allCities = magicCityService.getAllMagicCities();

        // Применяем фильтрацию
        allCities = applyFiltering(allCities);
        totalCount = allCities.size();

        // Применяем сортировку
        allCities = applySorting(allCities);

        // Применяем пагинацию к отфильтрованным и отсортированным данным
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allCities.size());

        if (startIndex < allCities.size()) {
            cities = allCities.subList(startIndex, endIndex);
        } else {
            cities = List.of(); // если страница пуста
        }
    }

    private List<MagicCity> applyFiltering(List<MagicCity> cities) {
        if (filterName == null || filterName.trim().isEmpty()) {
            return cities;
        }

        String filterLower = filterName.toLowerCase().trim();
        return cities.stream()
                .filter(city -> city.getName().toLowerCase().contains(filterLower))
                .collect(Collectors.toList());
    }

    private List<MagicCity> applySorting(List<MagicCity> cities) {
        Comparator<MagicCity> comparator;

        switch (sortBy) {
            case "id":
                comparator = Comparator.comparing(MagicCity::getId);
                break;
            case "name":
                comparator = Comparator.comparing(MagicCity::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "area":
                comparator = Comparator.comparing(MagicCity::getArea,
                    Comparator.nullsFirst(Comparator.naturalOrder()));
                break;
            case "population":
                comparator = Comparator.comparing(MagicCity::getPopulation);
                break;
            case "governor":
                comparator = Comparator.comparing(MagicCity::getGovernor,
                    Comparator.nullsFirst(Comparator.naturalOrder()));
                break;
            case "capital":
                comparator = Comparator.comparing(MagicCity::getCapital,
                    Comparator.nullsFirst(Comparator.naturalOrder()));
                break;
            default:
                comparator = Comparator.comparing(MagicCity::getId);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return cities.stream().sorted(comparator).collect(Collectors.toList());
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

    public void filterCities() {
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

    public long getTotalPages() {
        if (totalCount == 0) return 1;
        return (long) Math.ceil((double) totalCount / pageSize);
    }

}