import java.util.List;
import java.util.Scanner;

/**
 * Класс для взаимодействия с пользователем через консоль
 */
public class ConsoleInterface {
    private final MineralService mineralService;
    private final FileService fileService;
    private final Scanner scanner;

    public ConsoleInterface(MineralService mineralService, FileService fileService) {
        this.mineralService = mineralService;
        this.fileService = fileService;
        this.scanner = new Scanner(System.in);
    }

    public void displayMainMenu() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("КАТАЛОГ МИНЕРАЛОВ - ГЛАВНОЕ МЕНЮ");
        System.out.println("=".repeat(100));
        System.out.println("1. Просмотреть все минералы (компактно)");
        System.out.println("2. Просмотреть все минералы (полная версия)");
        System.out.println("3. Добавить новый минерал");
        System.out.println("4. Найти минерал по названию");
        System.out.println("5. Фильтровать минералы по классу");
        System.out.println("6. Фильтровать минералы по цвету");
        System.out.println("7. Фильтровать минералы по месторождению");
        System.out.println("8. Сортировать минералы по названию");
        System.out.println("9. Сортировать минералы по твердости");
        System.out.println("10. Удалить минерал");
        System.out.println("11. Импорт данных из CSV файла");
        System.out.println("12. Экспорт данных в CSV файл");
        System.out.println("13. Показать статистику");
        System.out.println("0. Выход");
        System.out.println("-".repeat(100));
        System.out.print("Выберите действие: ");
    }

    public void handleUserInput(int choice) {
        switch (choice) {
            case 1:
                displayAllMineralsCompact();
                break;
            case 2:
                displayAllMineralsFull();
                break;
            case 3:
                addNewMineral();
                break;
            case 4:
                searchMineralByName();
                break;
            case 5:
                filterMineralsByClass();
                break;
            case 6:
                filterMineralsByColor();
                break;
            case 7:
                filterMineralsByLocation();
                break;
            case 8:
                sortMineralsByName();
                break;
            case 9:
                sortMineralsByHardness();
                break;
            case 10:
                removeMineral(); // НОВЫЙ ПУНКТ
                break;
            case 11:
                importFromCSV();
                break;
            case 12:
                exportToCSV();
                break;
            case 13:
                showStatistics();
                break;
            case 0:
                System.out.println("До свидания!");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор. Попробуйте снова.");
        }
    }

    // НОВЫЙ МЕТОД ДЛЯ УДАЛЕНИЯ МИНЕРАЛА
    private void removeMineral() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("УДАЛЕНИЕ МИНЕРАЛА");
        System.out.println("=".repeat(100));

        List<Mineral> allMinerals = mineralService.getAllMinerals();
        if (allMinerals.isEmpty()) {
            System.out.println("Коллекция минералов пуста.");
            return;
        }

        System.out.println("Список минералов:");
        TableFormatter.printCompactMineralTable(allMinerals);

        System.out.println("\nВыберите способ удаления:");
        System.out.println("1. Удалить по ID");
        System.out.println("2. Удалить по названию");
        System.out.print("Ваш выбор: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                removeMineralById();
                break;
            case 2:
                removeMineralByName();
                break;
            default:
                System.out.println("Неверный выбор.");
        }
    }

    private void removeMineralById() {
        System.out.print("Введите ID минерала для удаления: ");
        int id = getIntInput();

        Mineral mineral = mineralService.getMineralById(id);
        if (mineral == null) {
            System.out.println("Минерал с ID " + id + " не найден.");
            return;
        }

        System.out.println("\nВы собираетесь удалить минерал:");
        System.out.println("ID: " + mineral.getId());
        System.out.println("Название: " + mineral.getName());
        System.out.println("Формула: " + mineral.getFormula());
        System.out.println("Месторождение: " + mineral.getLocation());

        System.out.print("Подтвердите удаление (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("y") || confirmation.equals("yes") || confirmation.equals("да")) {
            boolean removed = mineralService.removeMineralById(id);
            if (removed) {
                System.out.println("✓ Минерал успешно удален!");
            } else {
                System.out.println("Ошибка при удалении минерала.");
            }
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private void removeMineralByName() {
        System.out.print("Введите название минерала для удаления: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Название не может быть пустым.");
            return;
        }

        List<Mineral> foundMinerals = mineralService.searchByName(name);
        if (foundMinerals.isEmpty()) {
            System.out.println("Минералы с названием '" + name + "' не найдены.");
            return;
        }

        if (foundMinerals.size() == 1) {
            Mineral mineral = foundMinerals.get(0);
            System.out.println("\nНайден минерал:");
            System.out.println("ID: " + mineral.getId());
            System.out.println("Название: " + mineral.getName());
            System.out.println("Формула: " + mineral.getFormula());

            System.out.print("Подтвердите удаление (y/n): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (confirmation.equals("y") || confirmation.equals("yes") || confirmation.equals("да")) {
                boolean removed = mineralService.removeMineralById(mineral.getId());
                if (removed) {
                    System.out.println("✓ Минерал успешно удален!");
                } else {
                    System.out.println("Ошибка при удалении минерала.");
                }
            } else {
                System.out.println("Удаление отменено.");
            }
        } else {
            System.out.println("Найдено несколько минералов с таким названием:");
            TableFormatter.printCompactMineralTable(foundMinerals);
            System.out.println("Используйте удаление по ID для точного выбора.");
        }
    }

    // Остальные методы без изменений
    private void displayAllMineralsCompact() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("ВСЕ МИНЕРАЛЫ В КОЛЛЕКЦИИ (КОМПАКТНЫЙ ВИД)");
        System.out.println("=".repeat(100));

        List<Mineral> allMinerals = mineralService.getAllMinerals();
        TableFormatter.printCompactMineralTable(allMinerals);
    }

    private void displayAllMineralsFull() {
        System.out.println("\n" + "=".repeat(150));
        System.out.println("ВСЕ МИНЕРАЛЫ В КОЛЛЕКЦИИ (ПОЛНАЯ ВЕРСИЯ)");
        System.out.println("=".repeat(150));

        List<Mineral> allMinerals = mineralService.getAllMinerals();
        TableFormatter.printMineralTable(allMinerals);
    }

    private void addNewMineral() {
        System.out.println("\n--- ДОБАВЛЕНИЕ НОВОГО МИНЕРАЛА ---");

        System.out.print("Название: ");
        String name = scanner.nextLine();

        System.out.print("Формула: ");
        String formula = scanner.nextLine();

        System.out.print("Класс: ");
        String mineralClass = scanner.nextLine();

        System.out.print("Цвет: ");
        String color = scanner.nextLine();

        System.out.print("Цвет черты: ");
        String streakColor = scanner.nextLine();

        System.out.print("Блеск: ");
        String luster = scanner.nextLine();

        System.out.print("Твердость: ");
        String hardness = scanner.nextLine();

        System.out.print("Удельный вес: ");
        String specificGravity = scanner.nextLine();

        System.out.print("Спайность: ");
        String cleavage = scanner.nextLine();

        System.out.print("Излом: ");
        String fracture = scanner.nextLine();

        System.out.print("Генезис: ");
        String genesis = scanner.nextLine();

        System.out.print("Применение: ");
        String application = scanner.nextLine();

        System.out.print("Дополнительные свойства: ");
        String additionalProperties = scanner.nextLine();

        System.out.print("Интересные факты: ");
        String interestingFacts = scanner.nextLine();

        System.out.print("Месторождение: ");
        String location = scanner.nextLine();

        System.out.print("URL изображения (не обязательно): ");
        String imageUrl = scanner.nextLine();

        try {
            mineralService.addMineral(name, formula, mineralClass, color, streakColor, luster,
                    hardness, specificGravity, cleavage, fracture, genesis, application,
                    additionalProperties, interestingFacts, location, imageUrl);
            System.out.println("✓ Минерал успешно добавлен!");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void searchMineralByName() {
        System.out.print("\nВведите название минерала для поиска: ");
        String name = scanner.nextLine();

        List<Mineral> results = mineralService.searchByName(name);
        displaySearchResults(results, "ПОИСК ПО НАЗВАНИЮ: '" + name + "'");
    }

    private void filterMineralsByClass() {
        System.out.print("\nВведите класс для фильтрации: ");
        String mineralClass = scanner.nextLine();

        List<Mineral> results = mineralService.filterByClass(mineralClass);
        displaySearchResults(results, "МИНЕРАЛЫ КЛАССА: '" + mineralClass + "'");
    }

    private void filterMineralsByColor() {
        System.out.print("\nВведите цвет для фильтрации: ");
        String color = scanner.nextLine();

        List<Mineral> results = mineralService.filterByColor(color);
        displaySearchResults(results, "МИНЕРАЛЫ ЦВЕТА: '" + color + "'");
    }

    private void filterMineralsByLocation() {
        System.out.print("\nВведите месторождение для фильтрации: ");
        String location = scanner.nextLine();

        List<Mineral> results = mineralService.filterByLocation(location);
        displaySearchResults(results, "МИНЕРАЛЫ С МЕСТОРОЖДЕНИЯ: '" + location + "'");
    }

    private void displaySearchResults(List<Mineral> results, String title) {
        if (results.isEmpty()) {
            System.out.println("Минералы не найдены.");
        } else {
            System.out.println("\n" + title);
            TableFormatter.printCompactMineralTable(results);
        }
    }

    private void sortMineralsByName() {
        List<Mineral> sorted = mineralService.sortByName();
        System.out.println("\nМИНЕРАЛЫ, ОТСОРТИРОВАННЫЕ ПО НАЗВАНИЮ");
        TableFormatter.printCompactMineralTable(sorted);
    }

    private void sortMineralsByHardness() {
        List<Mineral> sorted = mineralService.sortByHardness();
        System.out.println("\nМИНЕРАЛЫ, ОТСОРТИРОВАННЫЕ ПО ТВЕРДОСТИ");
        System.out.println("=".repeat(150));
        TableFormatter.printMineralTable(sorted);
    }

    private void importFromCSV() {
        System.out.print("\nВведите имя CSV файла для импорта: ");
        String filename = scanner.nextLine();
        List<Mineral> imported = fileService.importFromCSV(filename);
        if (imported != null && !imported.isEmpty()) {
            mineralService.addAllMinerals(imported);
            System.out.println("✓ Импортировано " + imported.size() + " минералов из файла: " + filename);
        } else {
            System.out.println("Не удалось импортировать данные из файла: " + filename);
        }
    }

    private void exportToCSV() {
        System.out.print("\nВведите имя файла для экспорта: ");
        String filename = scanner.nextLine();
        fileService.exportToFile(mineralService.getAllMinerals(), filename);
        System.out.println("✓ Данные экспортированы в файл: " + filename);
    }

    private void showStatistics() {
        mineralService.displayCollectionStats();
    }

    public int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Неверный ввод. Введите целое число: ");
            }
        }
    }
}