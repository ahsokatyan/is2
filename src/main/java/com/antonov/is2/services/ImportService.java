package com.antonov.is2.services;

import com.antonov.is2.entities.*;
import com.antonov.is2.repos.BookCreatureRepository;
import com.antonov.is2.repos.CoordinatesRepository;
import com.antonov.is2.repos.MagicCityRepository;
import com.antonov.is2.repos.RingRepository;
import com.antonov.is2.utils.ImportResult;
import com.antonov.is2.websocket.CreaturesWebSocket;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Stateless
public class ImportService {

    @Inject
    private BookCreatureRepository bookCreatureRepo;

    @Inject
    private CoordinatesRepository coordinatesRepo;

    @Inject
    private MagicCityRepository magicCityRepo;

    @Inject
    private RingRepository ringRepo;

    @Inject
    private ImportOperationService importOperationService;

    @Inject
    private Validator validator;

    @Resource
    private SessionContext sessionContext;

    public ImportResult importBookCreatures(InputStream inputStream, String username) {
        ImportOperation operation = importOperationService.startOperation(username);
        int createdCount = 0;
        List<Long> createdIds = new ArrayList<>();

        try (JsonReader reader = Json.createReader(inputStream)) {
            JsonArray array = reader.readArray();
            List<BookCreature> creatures = new ArrayList<>();
            java.util.Set<String> nameTypeKeys = new java.util.HashSet<>();
            java.util.Set<String> nameCoordKeys = new java.util.HashSet<>();

            for (int i = 0; i < array.size(); i++) {
                JsonObject creatureJson = array.getJsonObject(i);
                BookCreature creature = parseCreature(creatureJson, i);
                validateCreature(creature, i);
                validateUniqueness(creature, i, nameTypeKeys, nameCoordKeys);
                creatures.add(creature);
            }

            for (BookCreature creature : creatures) {
                Coordinates coordinates = creature.getCoordinates();
                if (coordinates != null) {
                    coordinatesRepo.save(coordinates);
                }

                MagicCity city = creature.getCreatureLocation();
                if (city != null) {
                    magicCityRepo.save(city);
                }

                Ring ring = creature.getRing();
                if (ring != null) {
                    ringRepo.save(ring);
                }

                bookCreatureRepo.save(creature);
                createdIds.add(creature.getId());
                createdCount++;
            }

            importOperationService.markSuccess(operation.getId(), createdCount);
            for (Long id : createdIds) {
                CreaturesWebSocket.notifyCreatureCreated(id);
            }
            return new ImportResult(true, "Import completed successfully", createdCount);
        } catch (Exception e) {
            sessionContext.setRollbackOnly();
            String errorMessage = e.getMessage() == null ? "Unknown import error" : e.getMessage();
            importOperationService.markFailure(operation.getId(), errorMessage);
            return new ImportResult(false, errorMessage, 0);
        }
    }

    private BookCreature parseCreature(JsonObject json, int index) {
        BookCreature creature = new BookCreature();

        creature.setName(requireString(json, "name", index));
        creature.setAge(requireLong(json, "age", index));
        creature.setAttackLevel(requireLong(json, "attackLevel", index));
        creature.setCreationDate(java.time.LocalDate.now());

        String type = optionalString(json, "creatureType");
        if (type != null && !type.isBlank()) {
            creature.setCreatureType(parseCreatureType(type, index));
        }

        JsonObject coordinatesJson = requireObject(json, "coordinates", index);
        Coordinates coordinates = new Coordinates();
        coordinates.setX(requireDouble(coordinatesJson, "x", index));
        coordinates.setY(requireLong(coordinatesJson, "y", index));
        creature.setCoordinates(coordinates);

        JsonObject cityJson = optionalObject(json, "creatureLocation");
        if (cityJson != null) {
            MagicCity city = new MagicCity();
            city.setName(requireString(cityJson, "name", index));
            city.setArea(requireFloat(cityJson, "area", index));
            city.setPopulation(requireLong(cityJson, "population", index));
            city.setPopulationDensity(requireFloat(cityJson, "populationDensity", index));

            String establishmentDate = optionalString(cityJson, "establishmentDate");
            if (establishmentDate != null && !establishmentDate.isBlank()) {
                city.setEstablishmentDate(LocalDateTime.parse(establishmentDate));
            }

            String governor = optionalString(cityJson, "governor");
            if (governor != null && !governor.isBlank()) {
                city.setGovernor(parseCreatureType(governor, index));
            }

            if (cityJson.containsKey("capital") && !cityJson.isNull("capital")) {
                city.setCapital(cityJson.getBoolean("capital"));
            }

            creature.setCreatureLocation(city);
        }

        JsonObject ringJson = optionalObject(json, "ring");
        if (ringJson != null) {
            Ring ring = new Ring();
            ring.setName(requireString(ringJson, "name", index));

            if (ringJson.containsKey("power") && !ringJson.isNull("power")) {
                ring.setPower(ringJson.getJsonNumber("power").longValue());
            }

            ring.setWeight(requireDouble(ringJson, "weight", index));
            creature.setRing(ring);
        }

        return creature;
    }

    private void validateUniqueness(BookCreature creature,
                                    int index,
                                    java.util.Set<String> nameTypeKeys,
                                    java.util.Set<String> nameCoordKeys) {
        String name = creature.getName();
        BookCreatureType type = creature.getCreatureType();
        Coordinates coordinates = creature.getCoordinates();

        String typeKey = name + "|" + (type == null ? "null" : type.name());
        if (!nameTypeKeys.add(typeKey)
                || bookCreatureRepo.existsByNameAndType(name, type, null)) {
            throw new IllegalArgumentException("Duplicate name + creatureType at index " + index);
        }

        String coordKey = name + "|" + coordinates.getX() + "|" + coordinates.getY();
        if (!nameCoordKeys.add(coordKey)
                || bookCreatureRepo.existsByNameAndCoordinates(name, coordinates.getX(), coordinates.getY(), null)) {
            throw new IllegalArgumentException("Duplicate name + coordinates at index " + index);
        }
    }

    private void validateCreature(BookCreature creature, int index) {
        validateEntity(creature.getCoordinates(), "coordinates", index);
        validateEntity(creature.getRing(), "ring", index);
        validateEntity(creature.getCreatureLocation(), "creatureLocation", index);
        validateEntity(creature, "creature", index);
    }

    private <T> void validateEntity(T entity, String name, int index) {
        if (entity == null) {
            return;
        }
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("Validation failed at index ").append(index)
                    .append(" for ").append(name).append(": ");
            for (ConstraintViolation<T> violation : violations) {
                message.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(message.toString());
        }
    }

    private BookCreatureType parseCreatureType(String value, int index) {
        try {
            return BookCreatureType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid creatureType at index " + index + ": " + value);
        }
    }

    private String requireString(JsonObject obj, String key, int index) {
        String value = optionalString(obj, key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing or blank field '" + key + "' at index " + index);
        }
        return value;
    }

    private String optionalString(JsonObject obj, String key) {
        if (!obj.containsKey(key) || obj.isNull(key)) {
            return null;
        }
        JsonValue value = obj.get(key);
        if (value.getValueType() == JsonValue.ValueType.STRING) {
            return ((JsonString) value).getString();
        }
        return String.valueOf(value);
    }

    private JsonObject requireObject(JsonObject obj, String key, int index) {
        JsonObject value = optionalObject(obj, key);
        if (value == null) {
            throw new IllegalArgumentException("Missing object '" + key + "' at index " + index);
        }
        return value;
    }

    private JsonObject optionalObject(JsonObject obj, String key) {
        if (!obj.containsKey(key) || obj.isNull(key)) {
            return null;
        }
        return obj.getJsonObject(key);
    }

    private long requireLong(JsonObject obj, String key, int index) {
        if (!obj.containsKey(key) || obj.isNull(key)) {
            throw new IllegalArgumentException("Missing field '" + key + "' at index " + index);
        }
        return obj.getJsonNumber(key).longValue();
    }

    private double requireDouble(JsonObject obj, String key, int index) {
        if (!obj.containsKey(key) || obj.isNull(key)) {
            throw new IllegalArgumentException("Missing field '" + key + "' at index " + index);
        }
        return obj.getJsonNumber(key).doubleValue();
    }

    private Float requireFloat(JsonObject obj, String key, int index) {
        if (!obj.containsKey(key) || obj.isNull(key)) {
            throw new IllegalArgumentException("Missing field '" + key + "' at index " + index);
        }
        return (float) obj.getJsonNumber(key).doubleValue();
    }
}
