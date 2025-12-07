import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления коллекцией минералов
 */
public class MineralService {
    private final List<Mineral> minerals = new ArrayList<>();
    private int nextId = 1;

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\p{Sm}\\p{Sc}\\p{Sk}]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // CRUD операции
    public void addMineral(String name, String formula, String mineralClass, String color,
                           String streakColor, String luster, String hardness, String specificGravity,
                           String cleavage, String fracture, String genesis, String application,
                           String additionalProperties, String interestingFacts, String location, String imageUrl) {
        Mineral newMineral = new Mineral(nextId++, name, formula, mineralClass, color, streakColor,
                luster, hardness, specificGravity, cleavage, fracture, genesis, application,
                additionalProperties, interestingFacts, location, "", imageUrl); // Добавлено: пустая строка для valueCategory
        minerals.add(newMineral);
    }

    public void addMineral(Mineral mineral) {
        Mineral newMineral = new Mineral(nextId++,
                mineral.getName(),
                mineral.getFormula(),
                mineral.getMineralClass(),
                mineral.getColor(),
                mineral.getStreakColor(),
                mineral.getLuster(),
                mineral.getHardness(),
                mineral.getSpecificGravity(),
                mineral.getCleavage(),
                mineral.getFracture(),
                mineral.getGenesis(),
                mineral.getApplication(),
                mineral.getAdditionalProperties(),
                mineral.getInterestingFacts(),
                mineral.getLocation(),
                mineral.getValueCategory(), 
                mineral.getImageUrl()); 
        minerals.add(newMineral);
    }

    public void addAllMinerals(List<Mineral> mineralsToAdd) {
        for (Mineral mineral : mineralsToAdd) {
            addMineral(mineral);
        }
    }

    // ↓↓↓↓↓ МЕТОДЫ УДАЛЕНИЯ И ПОЛУЧЕНИЯ ↓↓↓↓↓

    /**
     * Удалить минерал по ID
     */
    public boolean removeMineralById(int id) {
        return minerals.removeIf(mineral -> mineral.getId() == id);
    }

    /**
     * Удалить минерал по названию (точное совпадение)
     */
    public boolean removeMineralByName(String name) {
        return minerals.removeIf(mineral -> mineral.getName().equalsIgnoreCase(name));
    }

    /**
     * Получить минерал по ID
     */
    public Mineral getMineralById(int id) {
        return minerals.stream()
                .filter(mineral -> mineral.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Получить все ID минералов
     */
    public List<Integer> getAllMineralIds() {
        return minerals.stream()
                .map(Mineral::getId)
                .collect(Collectors.toList());
    }

    // ↑↑↑↑↑ МЕТОДЫ УДАЛЕНИЯ И ПОЛУЧЕНИЯ ↑↑↑↑↑

    // Методы поиска
    public List<Mineral> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return minerals.stream()
                .filter(mineral -> mineral.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Mineral> filterByClass(String mineralClass) {
        if (mineralClass == null || mineralClass.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return minerals.stream()
                .filter(mineral -> mineral.getMineralClass().equalsIgnoreCase(mineralClass))
                .collect(Collectors.toList());
    }

    public List<Mineral> filterByColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return minerals.stream()
                .filter(mineral -> mineral.getColor().toLowerCase().contains(color.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Mineral> filterByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return minerals.stream()
                .filter(mineral -> mineral.getLocation().toLowerCase().contains(location.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public List<Mineral> filterByValueCategory(String valueCategory) {
        if (valueCategory == null || valueCategory.trim().isEmpty()) {
            return new ArrayList<>(minerals);
        }
        
        String searchTerm = valueCategory.toLowerCase().trim();
        return minerals.stream()
                .filter(m -> m.getValueCategory() != null && 
                             m.getValueCategory().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
    
    // Методы сортировки
    public List<Mineral> sortByName() {
        return minerals.stream()
                .sorted(Comparator.comparing(Mineral::getName))
                .collect(Collectors.toList());
    }

    public List<Mineral> sortByHardness() {
        return minerals.stream()
                .sorted((m1, m2) -> {
                    Double hardness1 = extractHardnessValue(m1.getHardness());
                    Double hardness2 = extractHardnessValue(m2.getHardness());
                    return Double.compare(hardness1, hardness2);
                })
                .collect(Collectors.toList());
    }

    private Double extractHardnessValue(String hardness) {
        if (hardness == null || hardness.isEmpty()) {
            return 0.0;
        }

        try {
            // Убираем лишние пробелы
            String clean = hardness.trim();

            // Заменяем запятые на точки
            clean = clean.replace(',', '.');

            // Убираем все нечисловые символы, кроме точек, дефисов и пробелов
            clean = clean.replaceAll("[^0-9.\\-\\s]", "");

            // Если есть диапазон через дефис
            if (clean.contains("-")) {
                String[] parts = clean.split("-");
                if (parts.length == 2) {
                    double part1 = parseDoubleSafe(parts[0].trim());
                    double part2 = parseDoubleSafe(parts[1].trim());
                    return Math.min(part1, part2); // Используем минимальное значение
                }
            }

            // Если есть пробел, берем первое число
            if (clean.contains(" ")) {
                String[] parts = clean.split(" ");
                if (parts.length > 0) {
                    return parseDoubleSafe(parts[0].trim());
                }
            }

            return parseDoubleSafe(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double parseDoubleSafe(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Методы для получения минералов
    public List<Mineral> getAllMinerals() {
        return new ArrayList<>(minerals);
    }

    public int getCollectionSize() {
        return minerals.size();
    }

    public boolean isEmpty() {
        return minerals.isEmpty();
    }

    // Статистика
    public void displayCollectionStats() {
        System.out.println("\n=== СТАТИСТИКА КОЛЛЕКЦИИ ===");
        System.out.println("Всего минералов: " + minerals.size());

        if (!minerals.isEmpty()) {
            // Статистика по классам
            Map<String, Long> classStats = minerals.stream()
                    .collect(Collectors.groupingBy(Mineral::getMineralClass, Collectors.counting()));
            System.out.println("\nРаспределение по классам:");
            classStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue()));

            // Статистика по месторождениям
            Map<String, Long> locationStats = minerals.stream()
                    .collect(Collectors.groupingBy(Mineral::getLocation, Collectors.counting()));
            System.out.println("\nТоп-5 месторождений:");
            locationStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue()));

            // Статистика по твердости
            System.out.println("\nДиапазон твердости:");
            double minHardness = minerals.stream()
                    .mapToDouble(mineral -> {
                        try {
                            String hard = mineral.getHardness().split("-")[0].replace(",", ".");
                            return Double.parseDouble(hard);
                        } catch (Exception e) {
                            return 0.0;
                        }
                    })
                    .min()
                    .orElse(0.0);
            double maxHardness = minerals.stream()
                    .mapToDouble(mineral -> {
                        try {
                            String hard = mineral.getHardness().split("-")[0].replace(",", ".");
                            return Double.parseDouble(hard);
                        } catch (Exception e) {
                            return 0.0;
                        }
                    })
                    .max()
                    .orElse(0.0);
            System.out.println("  От " + minHardness + " до " + maxHardness + " по шкале Мооса");
        }
    }

    // Поиск по всем полям
    public List<Mineral> searchInAllFields(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String term = searchTerm.toLowerCase();
        return minerals.stream()
                .filter(mineral ->
                        mineral.getName().toLowerCase().contains(term) ||
                                mineral.getFormula().toLowerCase().contains(term) ||
                                mineral.getMineralClass().toLowerCase().contains(term) ||
                                mineral.getColor().toLowerCase().contains(term) ||
                                mineral.getLocation().toLowerCase().contains(term) ||
                                mineral.getApplication().toLowerCase().contains(term) ||
                                mineral.getInterestingFacts().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    // Получить все уникальные классы минералов
    public Set<String> getAllMineralClasses() {
        return minerals.stream()
                .map(Mineral::getMineralClass)
                .collect(Collectors.toSet());
    }

    // Получить все уникальные месторождения
    public Set<String> getAllLocations() {
        return minerals.stream()
                .map(Mineral::getLocation)
                .collect(Collectors.toSet());
    }

    // Очистить коллекцию
    public void clearCollection() {
        minerals.clear();
        nextId = 1;
    }
    
    // Новые методы для статистики по категориям ценности
    public Map<String, Integer> getValueCategoryStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Mineral mineral : minerals) {
            String category = mineral.getValueCategory();
            if (category == null || category.trim().isEmpty()) {
                category = "Не указана";
            }
            stats.put(category, stats.getOrDefault(category, 0) + 1);
        }
        return stats;
    }
    
    public List<String> getAllValueCategories() {
        return minerals.stream()
                .map(Mineral::getValueCategory)
                .filter(cat -> cat != null && !cat.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}