import java.util.ArrayList;
import java.util.List;

/**
 * Утилитный класс для форматирования вывода в виде таблицы со всеми полями
 */
public class TableFormatter {

    // Добавьте метод для очистки текста от спецсимволов
    private static String cleanText(String text) {
        if (text == null) return "";
            // Если это URL, обрабатываем иначе
    if (text.startsWith("http://") || text.startsWith("https://")) {
        // Для URL показываем только факт наличия или короткую информацию
        return text.contains("://") ? "URL" : text;
    }
        // Удаляем все не-ASCII и не-кириллические символы, кроме базовой пунктуации
        return text.replaceAll("[^\\x20-\\x7Eа-яА-ЯёЁ0-9\\s.,!?;:()-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static void printMineralTable(List<Mineral> minerals) {
        if (minerals == null || minerals.isEmpty()) {
            System.out.println("Коллекция минералов пуста.");
            return;
        }

        // Увеличим ширины для лучшего отображения
        int[] widths = {4, 18, 15, 15, 23, 18, 18, 10, 10, 18, 18, 30, 20, 28, 28, 20, 20};

        printTableHeader(widths);

        for (Mineral mineral : minerals) {
            printMineralRow(mineral, widths);
            printHorizontalSeparator(widths);
        }

        printTableBottom(widths);
        System.out.println("Всего минералов: " + minerals.size());
    }

    private static void printTableHeader(int[] widths) {
        printHorizontalBorder(widths, "┌", "┬", "┐");

        String[] headers = {
                "ID", "Название", "Формула", "Класс", "Цвет", "Цвет черты",
                "Блеск", "Твердость", "Уд.вес", "Спайность", "Излом",
                "Генезис", "Применение", "Доп.свойства", "Интересные факты", "Месторождение", "Изображение"
        };

        System.out.print("│");
        for (int i = 0; i < headers.length; i++) {
            System.out.printf(" %-" + (widths[i]-1) + "s│", truncate(headers[i], widths[i]-2));
        }
        System.out.println();

        printHorizontalBorder(widths, "├", "┼", "┤");
    }

    private static void printMineralRow(Mineral mineral, int[] widths) {
        // ОЧИЩАЕМ все значения перед выводом
        String[] values = {
                String.valueOf(mineral.getId()),
                cleanText(mineral.getName()),
                cleanText(mineral.getFormula()),
                cleanText(mineral.getMineralClass()),
                cleanText(mineral.getColor()),
                cleanText(mineral.getStreakColor()),
                cleanText(mineral.getLuster()),
                cleanText(mineral.getHardness()),
                cleanText(mineral.getSpecificGravity()),
                cleanText(mineral.getCleavage()),
                cleanText(mineral.getFracture()),
                cleanText(mineral.getGenesis()),
                cleanText(mineral.getApplication()),
                cleanText(mineral.getAdditionalProperties()),
                cleanText(mineral.getInterestingFacts()),
                cleanText(mineral.getLocation()),
                cleanText(mineral.getImageUrl() != null && !mineral.getImageUrl().isEmpty() ? 
                     "Есть ✓" : "Нет ✗")
        };

        int maxLines = 1;
        List<String[]> allLines = new ArrayList<>();

        for (int i = 0; i < values.length; i++) {
            List<String> lines = wordWrap(values[i], widths[i] - 2);
            allLines.add(lines.toArray(new String[0]));
            maxLines = Math.max(maxLines, lines.size());
        }

        for (int lineNum = 0; lineNum < maxLines; lineNum++) {
            System.out.print("│");
            for (int col = 0; col < values.length; col++) {
                String[] lines = allLines.get(col);
                String value = lineNum < lines.length ? lines[lineNum] : "";
                System.out.printf(" %-" + (widths[col]-1) + "s│", value);
            }
            System.out.println();
        }
    }

    private static List<String> wordWrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        // Сначала очистим текст
        String cleanText = cleanText(text);

        if (cleanText.length() <= maxWidth) {
            lines.add(cleanText);
            return lines;
        }

        String[] words = cleanText.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() == 0) {
                if (word.length() > maxWidth) {
                    // Если слово длиннее максимальной ширины - разбиваем его
                    int start = 0;
                    while (start < word.length()) {
                        int end = Math.min(start + maxWidth, word.length());
                        lines.add(word.substring(start, end));
                        start = end;
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                if (currentLine.length() + word.length() + 1 <= maxWidth) {
                    currentLine.append(" ").append(word);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private static void printHorizontalBorder(int[] widths, String left, String middle, String right) {
        System.out.print(left);
        for (int i = 0; i < widths.length; i++) {
            System.out.print("─".repeat(widths[i]));
            if (i < widths.length - 1) {
                System.out.print(middle);
            }
        }
        System.out.println(right);
    }

    private static void printHorizontalSeparator(int[] widths) {
        printHorizontalBorder(widths, "├", "┼", "┤");
    }

    private static void printTableBottom(int[] widths) {
        printHorizontalBorder(widths, "└", "┴", "┘");
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        String cleanText = cleanText(text);
        if (cleanText.length() <= maxLength) return cleanText;
        return cleanText.substring(0, maxLength - 3) + "...";
    }

    // Метод для компактного отображения (только основные поля)
// Метод для компактного отображения (только основные поля)
    public static void printCompactMineralTable(List<Mineral> minerals) {
        if (minerals == null || minerals.isEmpty()) {
            System.out.println("Коллекция минералов пуста.");
            return;
        }

        int[] widths = {6, 25, 20, 25, 22, 30, 25, 10};

        printCompactTableHeader(widths);

        for (int i = 0; i < minerals.size(); i++) {
            printCompactMineralRow(minerals.get(i), widths);
            // Добавляем разделитель между минералами, кроме последнего
            if (i < minerals.size() - 1) {
                System.out.println("├" + "─".repeat(widths[0]) + "┼" +
                        "─".repeat(widths[1]) + "┼" +
                        "─".repeat(widths[2]) + "┼" +
                        "─".repeat(widths[3]) + "┼" +
                        "─".repeat(widths[4]) + "┼" +
                        "─".repeat(widths[5]) + "┼" +
                        "─".repeat(widths[6]) + "┤");
            }
        }

        printCompactTableBottom(widths);
        System.out.println("Всего минералов: " + minerals.size());
    }

    private static void printCompactTableHeader(int[] widths) {
        System.out.println("┌" + "─".repeat(widths[0]) + "┬" +
                "─".repeat(widths[1]) + "┬" +
                "─".repeat(widths[2]) + "┬" +
                "─".repeat(widths[3]) + "┬" +
                "─".repeat(widths[4]) + "┬" +
                "─".repeat(widths[5]) + "┬" +
                "─".repeat(widths[6]) + "┐");

        System.out.printf("│ %-" + (widths[0]-1) + "s│ %-" + (widths[1]-1) + "s│ %-" + (widths[2]-1) +
                    "s│ %-" + (widths[3]-1) + "s│ %-" + (widths[4]-1) + "s│ %-" + (widths[5]-1) +
                    "s│ %-" + (widths[6]-1) + "s│ %-" + (widths[7]-1) + "s│%n", // ← 8 мест
            "ID", "Название", "Класс", "Формула", "Цвет", "Применение", "Месторождение", "Изобр.");

        System.out.println("├" + "─".repeat(widths[0]) + "┼" +
                "─".repeat(widths[1]) + "┼" +
                "─".repeat(widths[2]) + "┼" +
                "─".repeat(widths[3]) + "┼" +
                "─".repeat(widths[4]) + "┼" +
                "─".repeat(widths[5]) + "┼" +
                "─".repeat(widths[6]) + "┤");
    }

    private static void printCompactMineralRow(Mineral mineral, int[] widths) {
        // Получаем очищенные значения
        String[] values = {
                String.valueOf(mineral.getId()),
                cleanText(mineral.getName()),
                cleanText(mineral.getMineralClass()),
                cleanText(mineral.getFormula()),
                cleanText(mineral.getColor()),
                cleanText(mineral.getApplication()),
                cleanText(mineral.getLocation()),
                cleanText(mineral.getImageUrl() != null && !mineral.getImageUrl().isEmpty() ? 
                     "✓" : "✗")
        };

        // Определяем максимальное количество строк для этого ряда
        int maxLines = 1;
        List<String[]> allLines = new ArrayList<>();

        for (int i = 0; i < values.length; i++) {
            List<String> lines = wordWrap(values[i], widths[i] - 2);
            allLines.add(lines.toArray(new String[0]));
            maxLines = Math.max(maxLines, lines.size());
        }

        // Выводим все строки для этого минерала
        for (int lineNum = 0; lineNum < maxLines; lineNum++) {
            System.out.print("│");
            for (int col = 0; col < values.length; col++) {
                String[] lines = allLines.get(col);
                String value = lineNum < lines.length ? lines[lineNum] : "";
                System.out.printf(" %-" + (widths[col]-1) + "s│", value);
            }
            System.out.println();
        }
    }

    private static void printCompactTableBottom(int[] widths) {
        System.out.println("└" + "─".repeat(widths[0]) + "┴" +
                "─".repeat(widths[1]) + "┴" +
                "─".repeat(widths[2]) + "┴" +
                "─".repeat(widths[3]) + "┴" +
                "─".repeat(widths[4]) + "┴" +
                "─".repeat(widths[5]) + "┴" +
                "─".repeat(widths[6]) + "┘");
    }
}