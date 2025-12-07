import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с CSV файлами минералов
 */
public class FileService {

    public List<Mineral> importFromCSV(String filename) {
        List<Mineral> minerals = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    Mineral mineral = parseCSVLine(line);
                    if (mineral != null) {
                        minerals.add(mineral);
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка парсинга строки: " + line);
                }
            }

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            return null;
        }

        return minerals;
    }

   private Mineral parseCSVLine(String line) {
    try {
        List<String> fields = parseCSVFields(line);
        if (fields.size() >= 17) { // ← ИЗМЕНИТЕ С 15 НА 16
            return new Mineral(0,
                    fields.get(0),  // name
                    fields.get(1),  // formula
                    fields.get(2),  // mineralClass
                    fields.get(3),  // color
                    fields.get(4),  // streakColor
                    fields.get(5),  // luster
                    fields.get(6),  // hardness
                    fields.get(7),  // specificGravity
                    fields.get(8),  // cleavage
                    fields.get(9),  // fracture
                    fields.get(10), // genesis
                    fields.get(11), // application
                    fields.get(12), // additionalProperties
                    fields.get(13), // interestingFacts
                    fields.get(14), // location
                    fields.get(15),
                    fields.get(16)   // valueCategory ← ДОБАВЬТЕ!
            );
        } else if (fields.size() == 16) {
            // Для CSV файлов с 16 полями (старая версия без imageUrl)
            return new Mineral(0,
                    fields.get(0), fields.get(1), fields.get(2), fields.get(3),
                    fields.get(4), fields.get(5), fields.get(6), fields.get(7),
                    fields.get(8), fields.get(9), fields.get(10), fields.get(11),
                    fields.get(12), fields.get(13), fields.get(14),
                    fields.get(15), // valueCategory
                    "" // imageUrl по умолчанию (пустая строка)
            );
        }
    } catch (Exception e) {
        System.out.println("Ошибка при разборе строки: " + e.getMessage());
    }
    return null;
}

private List<String> parseCSVFields(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder currentField = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);

        if (c == '"') {
            inQuotes = !inQuotes;
        } else if (c == ';' && !inQuotes) {
            fields.add(currentField.toString().trim());
            currentField = new StringBuilder();
        } else {
            currentField.append(c);
        }
    }

    fields.add(currentField.toString().trim());
    return fields;
}
    public void exportToFile(List<Mineral> minerals, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Название;Формула;Класс;Цвет;Цвет черты;Блеск;Твердость;Удельный вес;Спайность;Излом;Генезис;Применение;Дополнительные свойства;Интересные факты;Месторождение;Категория ценности; Изображение");
            for (Mineral mineral : minerals) {
                writer.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%n", // 17 мест вместо 15
                    escapeCSV(mineral.getName()),
                    escapeCSV(mineral.getFormula()),
                    escapeCSV(mineral.getMineralClass()),
                    escapeCSV(mineral.getColor()),
                    escapeCSV(mineral.getStreakColor()),
                    escapeCSV(mineral.getLuster()),
                    escapeCSV(mineral.getHardness()),
                    escapeCSV(mineral.getSpecificGravity()),
                    escapeCSV(mineral.getCleavage()),
                    escapeCSV(mineral.getFracture()),
                    escapeCSV(mineral.getGenesis()),
                    escapeCSV(mineral.getApplication()),
                    escapeCSV(mineral.getAdditionalProperties()),
                    escapeCSV(mineral.getInterestingFacts()),
                    escapeCSV(mineral.getLocation()),
                    escapeCSV(mineral.getValueCategory()),
                    escapeCSV(mineral.getImageUrl())
                 // 16-й параметр
            );

        } 
        
    } catch (IOException e) {
            System.out.println("Ошибка при экспорте в файл: " + e.getMessage());
        }
    }

    private String escapeCSV(String field) {
        if (field == null) return "";
        if (field.contains(";") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}