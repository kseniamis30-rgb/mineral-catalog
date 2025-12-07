import java.util.List;
// ... другие импорты
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;


/**
 * Веб-сервис для каталога минералов
 */
public class WebService {

    static MineralService mineralService = new MineralService();
    static AuthService authService = new AuthService();

    public static void sendUtf8Response(HttpExchange exchange, String content, String contentType) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public static void main(String[] args) throws IOException {
        
        initializeMinerals();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Настройка маршрутов
        server.createContext("/", new HomeHandler());
        server.createContext("/minerals", new MineralsHandler());
        server.createContext("/add", new AddMineralHandler());
        server.createContext("/delete", new DeleteMineralHandler());
        server.createContext("/stats", new StatsHandler());
        server.createContext("/export", new ExportHandler());
        server.createContext("/api/minerals", new ApiMineralsHandler());
        server.createContext("/mineral", new MineralDetailsHandler());
        server.createContext("/search", new SearchHandler());
        server.createContext("/filter", new FilterHandler());
        server.createContext("/sort", new SortHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/images", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("🚀 Сервер запущен на http://localhost:" + port);
        System.out.println("💎 Загружено минералов: " + mineralService.getCollectionSize());
    }

      private static void initializeMinerals() {
    try {
        System.out.println("🔄 Инициализация SQLite базы данных...");
        
        // 1. Создаем таблицы (если их нет)
        DatabaseService.createTables();
        
        // 2. Проверяем, есть ли данные в БД
        if (DatabaseService.isDatabaseEmpty()) {
            System.out.println("📭 База данных пуста");
            
            // Если нужно - можно заполнить тестовыми данными
            // DatabaseService.seedWithTestData(); // раскомментируй если хочешь тестовые данные
            
        } else {
            // 3. Загружаем данные ИЗ БД (не из кода!)
            List<Mineral> dbMinerals = DatabaseService.loadAllFromDatabase();
            
            // 4. Очищаем и заполняем сервис
            mineralService.clearCollection();
            mineralService.addAllMinerals(dbMinerals);
            
            System.out.println("✅ Загружено " + mineralService.getCollectionSize() + " минералов ИЗ SQLite базы");
        }
        
        // 5. Показываем статистику
        DatabaseService.printDatabaseStats();
        
    } catch (Exception e) {
        System.out.println("❌ Ошибка инициализации: " + e.getMessage());
        e.printStackTrace();
    }
}  
       


            
public static String generateMineralsList(List<Mineral> minerals, boolean isAdmin) {
    if (minerals.isEmpty()) {
        return "<div style='background: white; padding: 2rem; border-radius: 10px; text-align: center;'><p>📭 Коллекция минералов пуста</p></div>";
    }

    StringBuilder html = new StringBuilder();
    for (Mineral mineral : minerals) {
        String deleteButton = isAdmin ?
                "<button onclick='deleteMineral(" + mineral.getId() + ")' class='btn-danger'>🗑️ Удалить</button>" : "";
        
        // Получаем URL изображения
        String imageUrl = mineral.getImageUrl() != null && !mineral.getImageUrl().isEmpty() ? 
                mineral.getImageUrl() : "images/no-image.png";
        
        html.append(String.format(
                "<div class='mineral-card'>" +
                "    <div class='mineral-header'>" +
                "        <h3>%s</h3>" +
                "        <span class='mineral-id'>#%d</span>" +
                "    </div>" +
                "    <div class='mineral-content'>" +
                "        <div class='mineral-image'>" +
                "            <img src='%s' alt='%s' onerror=\"this.src='images/no-image.png'\">" +
                "        </div>" +
                "        <div class='mineral-info'>" +
                "            <p><strong>🧪 Формула:</strong> %s</p>" +
                "            <p><strong>📚 Класс:</strong> %s</p>" +
                "            <p><strong>🎨 Цвет:</strong> %s</p>" +
                "            <p><strong>💰 Категория ценности:</strong> %s</p>" +
                "            <p><strong>💪 Твердость:</strong> %s</p>" +
                "            <p><strong>📍 Месторождение:</strong> %s</p>" +
                "            <p><strong>🏭 Применение:</strong> %s</p>" +
                "        </div>" +
                "    </div>" +
                "    <div class='mineral-actions'>" +
                "        <button onclick='showMineralDetails(%d)' class='btn-info'>👁️ Подробнее</button>" +
                "        %s" +
                "    </div>" +
                "</div>",
                mineral.getName(), mineral.getId(),
                imageUrl, mineral.getName(),
                mineral.getFormula() != null ? mineral.getFormula() : "",
                mineral.getMineralClass() != null ? mineral.getMineralClass() : "",
                mineral.getColor() != null ? mineral.getColor() : "",
                getValueCategoryIcon(mineral.getValueCategory()) + " " + 
                    (mineral.getValueCategory() != null && !mineral.getValueCategory().isEmpty() ? 
                     mineral.getValueCategory() : "Не указана"),
                mineral.getHardness() != null ? mineral.getHardness() : "",
                mineral.getLocation() != null ? mineral.getLocation() : "",
                mineral.getApplication() != null ? mineral.getApplication() : "",
                mineral.getId(), deleteButton
        ));
    }
    return html.toString();
}

// Метод должен быть объявлен в том же классе
private static String getValueCategoryIcon(String category) {
        if (category == null) return "📊";
        
        switch (category.toLowerCase()) {
            case "драгоценный":
                return "💎";
            case "полудрагоценный":
                return "✨";
            case "полудрагоценный/поделочный":
                return "✨";   
            case "поделочный":
                return "🪨";
            case "коллекционный":
                return "📚";
            case "руда":
                return "🏭";
            default:
                return "📊";
    }
}
    public static Map<String, Integer> getValueCategoryStats() {
        Map<String, Integer> stats = new HashMap<>();
        List<Mineral> minerals = mineralService.getAllMinerals();
        
        for (Mineral mineral : minerals) {
            String category = mineral.getValueCategory();
            if (category == null || category.trim().isEmpty()) {
                category = "Не указана";
            }
            stats.put(category, stats.getOrDefault(category, 0) + 1);
        }
        
        return stats;
    }
    public static List<String> getUniqueValueCategories() {
        return mineralService.getAllMinerals().stream()
                .map(Mineral::getValueCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    public static int getUniqueLocationsCount() {
        return (int) mineralService.getAllMinerals().stream()
                .map(Mineral::getLocation)
                .filter(location -> location != null && !location.trim().isEmpty())
                .distinct()
                .count();
    }

    public static int getUniqueColorsCount() {
        return (int) mineralService.getAllMinerals().stream()
                .map(Mineral::getColor)
                .filter(color -> color != null && !color.trim().isEmpty())
                .distinct()
                .count();
    }

}

class HandlerUtils {
    static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    try {
                        String key = java.net.URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                        String value = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        result.put(key, value);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        return result;
    }

static String convertMineralsToJson(List<Mineral> minerals) {
    return minerals.stream()
            .map(m -> String.format(
                    "{\"id\":%d,\"name\":\"%s\",\"formula\":\"%s\",\"class\":\"%s\",\"color\":\"%s\",\"hardness\":\"%s\",\"location\":\"%s\",\"application\":\"%s\",\"imageUrl\":\"%s\"}",
                    m.getId(), escapeJson(m.getName()), escapeJson(m.getFormula()),
                    escapeJson(m.getMineralClass()), escapeJson(m.getColor()),
                    escapeJson(m.getHardness()), escapeJson(m.getLocation()),
                    escapeJson(m.getApplication()), escapeJson(m.getImageUrl()))) // ← Добавьте imageUrl
            .collect(Collectors.joining(",", "[", "]"));
}

    static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    static String getSessionId(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie != null) {
            for (String part : cookie.split(";")) {
                if (part.trim().startsWith("session=")) {
                    return part.trim().substring(8);
                }
            }
        }
        return null;
    }

    static boolean isAdmin(HttpExchange exchange) {
        String sessionId = getSessionId(exchange);
        return sessionId != null && WebService.authService.isAdminSession(sessionId);
    }

    static boolean isLoggedIn(HttpExchange exchange) {
        String sessionId = getSessionId(exchange);
        return sessionId != null && WebService.authService.isValidSession(sessionId);
    }
}

class HomeHandler implements HttpHandler {
    private static String getValueCategoryIconStatic(String category) {
    if (category == null) return "📊";
    
    switch (category.toLowerCase()) {
        case "драгоценный":
            return "💎";
        case "полудрагоценный":
            return "✨";
        case "полудрагоценный/поделочный":
            return "✨";   
        case "поделочный":
            return "🪨";
        case "коллекционный":
            return "📚";
        case "руда":
            return "🏭";
        default:
            return "📊";
    }
}
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        boolean isAdmin = HandlerUtils.isAdmin(exchange);
        boolean isLoggedIn = HandlerUtils.isLoggedIn(exchange);

        String authBlock = "<div style='position: absolute; top: 20px; right: 20px; background: #34495e; color: white; padding: 10px; border-radius: 5px;'>" +
                (isLoggedIn ?
                        "<div>Вход выполнен как: <strong>" + (isAdmin ? "Админ" : "Пользователь") + "</strong></div>" +
                                "<div><a href='/logout' style='color: #e74c3c; text-decoration: none;'>Выйти</a></div>" :
                        "<div><button onclick=\"openAuthModal()\" style='background: #3498db; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;'>Войти</button></div>") +
                "</div>";

        // Получаем параметры URL для определения текущего состояния
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = HandlerUtils.parseQuery(query);
        String currentSection = params.getOrDefault("section", "all");
        String sortType = params.getOrDefault("sort", "");
        String searchQuery = params.getOrDefault("search", "");
        String filterType = params.getOrDefault("filterType", "");
        String filterValue = params.getOrDefault("filterValue", "");
        String valueCategoryFilter = params.getOrDefault("valueCategory", ""); 

        // Определяем какие минералы показывать
        List<Mineral> mineralsToShow = WebService.mineralService.getAllMinerals();
        String sectionTitle = "🪨 Все минералы в коллекции";
        String additionalInfo = "";
        String resultsHtml = "";

        // Обработка для секции "Все минералы" - только сортировка
        if (currentSection.equals("all")) {
            if (!sortType.isEmpty()) {
                switch (sortType) {
                    case "name":
                        mineralsToShow = WebService.mineralService.sortByName();
                        sectionTitle = "📝 Отсортировано по названию";
                        additionalInfo = "<div class='sort-info' style='background: #e8f4fd; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border-left: 4px solid #e67e22;'><strong>Сортировка:</strong> По названию (А-Я)</div>";
                        break;
                    case "hardness":
                        mineralsToShow = WebService.mineralService.sortByHardness();
                        sectionTitle = "💪 Отсортировано по твердости";
                        additionalInfo = "<div class='sort-info' style='background: #e8f4fd; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border-left: 4px solid #e67e22;'><strong>Сортировка:</strong> По твердости (по возрастанию)</div>";
                        break;
                }
            }
            resultsHtml = WebService.generateMineralsList(mineralsToShow, isAdmin);
        }
        // Обработка для секции "Поиск и фильтры" - поиск и фильтрация
        else if (currentSection.equals("search")) {
            if (!searchQuery.isEmpty()) {
                mineralsToShow = WebService.mineralService.searchByName(searchQuery);
                sectionTitle = "🔍 Результаты поиска";
                additionalInfo = "<div class='search-info' style='background: #e8f4fd; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border-left: 4px solid #3498db;'><strong>Поисковый запрос:</strong> \"" + escapeHtml(searchQuery) + "\" | <strong>Найдено:</strong> " + mineralsToShow.size() + " минералов</div>";
            } else if (!filterValue.isEmpty()) {
                switch (filterType) {
                    case "color":
                        mineralsToShow = WebService.mineralService.filterByColor(filterValue);
                        sectionTitle = "🎨 Фильтр по цвету";
                        additionalInfo = "<div class='filter-info' style='background: #e8f4fd; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border-left: 4px solid #9b59b6;'><strong>Фильтр:</strong> Цвет содержит \"" + escapeHtml(filterValue) + "\" | <strong>Найдено:</strong> " + mineralsToShow.size() + " минералов</div>";
                        break;
                    case "location":
                        mineralsToShow = WebService.mineralService.filterByLocation(filterValue);
                        sectionTitle = "📍 Фильтр по месторождению";
                        additionalInfo = "<div class='filter-info' style='background: #e8f4fd; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border-left: 4px solid #9b59b6;'><strong>Фильтр:</strong> Месторождение содержит \"" + escapeHtml(filterValue) + "\" | <strong>Найдено:</strong> " + mineralsToShow.size() + " минералов</div>";
                        break;
                    case "valueCategory":
                        mineralsToShow = WebService.mineralService.filterByValueCategory(valueCategoryFilter);
                        sectionTitle = "💰 Фильтр по категории ценности";
                        additionalInfo = "<div class='filter-info' style='background: #e8f4fd; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border-left: 4px solid #f39c12;'><strong>Фильтр:</strong> Категория ценности = \"" + escapeHtml(valueCategoryFilter) + "\" | <strong>Найдено:</strong> " + mineralsToShow.size() + " минералов</div>";
                        break;
                }
            }
            resultsHtml = WebService.generateMineralsList(mineralsToShow, isAdmin);
        }

        int uniqueLocations = WebService.getUniqueLocationsCount();
        int uniqueColors = WebService.getUniqueColorsCount();
        Map<String, Integer> valueCategoryStats = WebService.getValueCategoryStats();
        StringBuilder valueCategoryStatsHtml = new StringBuilder();
        int totalMinerals = WebService.mineralService.getCollectionSize();
        for (Map.Entry<String, Integer> entry : valueCategoryStats.entrySet()) {
            String category = entry.getKey();
            int count = entry.getValue();
            double percentage = totalMinerals > 0 ? (count * 100.0 / totalMinerals) : 0;
            
            String icon = getValueCategoryIconStatic(category);
            valueCategoryStatsHtml.append(String.format(
                "<div class='stat-card'>" +
                "    <h3>%s %d</h3>" +
                "    <p>%s (%.1f%%)</p>" +
                "</div>",
                icon, count, category, percentage
            ));
        }
        List<String> uniqueCategories = WebService.getUniqueValueCategories();
        StringBuilder categoryOptions = new StringBuilder();
        for (String category : uniqueCategories) {
            String selected = category.equals(valueCategoryFilter) ? "selected" : "";
            categoryOptions.append(String.format("<option value=\"%s\" %s>%s %s</option>", 
    escapeHtml(category), selected, getValueCategoryIconStatic(category), category));
        }

        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"ru\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Каталог Минералов</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body { \n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; \n" +
                "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                "            min-height: 100vh;\n" +
                "            color: #333;\n" +
                "        }\n" +
                "        .container { max-width: 1200px; margin: 0 auto; padding: 20px; position: relative; }\n" +
                "        \n" +
                "        .header { \n" +
                "            background: white; \n" +
                "            padding: 2rem; \n" +
                "            border-radius: 15px;\n" +
                "            box-shadow: 0 10px 30px rgba(0,0,0,0.1);\n" +
                "            margin-bottom: 2rem;\n" +
                "            text-align: center;\n" +
                "            position: relative;\n" +
                "        }\n" +
                "        .header h1 { \n" +
                "            color: #2c3e50; \n" +
                "            font-size: 2.5rem; \n" +
                "            margin-bottom: 0.5rem;\n" +
                "        }\n" +
                "        .stats-bar {\n" +
                "            background: #34495e;\n" +
                "            color: white;\n" +
                "            padding: 1rem;\n" +
                "            border-radius: 10px;\n" +
                "            margin-top: 1rem;\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "        \n" +
                "        .main-nav {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
                "            gap: 1rem;\n" +
                "            margin-bottom: 2rem;\n" +
                "        }\n" +
                "        .main-nav button {\n" +
                "            background: white;\n" +
                "            border: none;\n" +
                "            padding: 1.5rem 1rem;\n" +
                "            border-radius: 10px;\n" +
                "            font-size: 1.1rem;\n" +
                "            cursor: pointer;\n" +
                "            transition: all 0.3s ease;\n" +
                "            box-shadow: 0 5px 15px rgba(0,0,0,0.1);\n" +
                "        }\n" +
                "        .main-nav button:hover {\n" +
                "            transform: translateY(-5px);\n" +
                "            box-shadow: 0 10px 25px rgba(0,0,0,0.2);\n" +
                "            background: #3498db;\n" +
                "            color: white;\n" +
                "        }\n" +
                "        \n" +
                "        .section { display: none; }\n" +
                "        .section.active { display: block; }\n" +
                "        \n" +
                "        .minerals-controls {\n" +
                "            background: white;\n" +
                "            padding: 1.5rem;\n" +
                "            border-radius: 10px;\n" +
                "            margin-bottom: 1rem;\n" +
                "            display: flex;\n" +
                "            gap: 1rem;\n" +
                "            flex-wrap: wrap;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        .minerals-controls button {\n" +
                "            background: #3498db;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            padding: 0.8rem 1.5rem;\n" +
                "            border-radius: 5px;\n" +
                "            cursor: pointer;\n" +
                "            transition: background 0.3s;\n" +
                "        }\n" +
                "        .minerals-controls button:hover { background: #2980b9; }\n" +
                "        .btn-clear {\n" +
                "            background: #95a5a6 !important;\n" +
                "        }\n" +
                "        .btn-clear:hover { background: #7f8c8d !important; }\n" +
                "        \n" +
                "        .section-header {\n" +
                "            display: flex;\n" +
                "            justify-content: space-between;\n" +
                "            align-items: center;\n" +
                "            margin-bottom: 1rem;\n" +
                "            flex-wrap: wrap;\n" +
                "            gap: 1rem;\n" +
                "        }\n" +
                "        \n" +
                "        .mineral-card {\n" +
                "            background: white;\n" +
                "            border-radius: 10px;\n" +
                "            padding: 1.5rem;\n" +
                "            margin-bottom: 1rem;\n" +
                "            box-shadow: 0 5px 15px rgba(0,0,0,0.1);\n" +
                "            border-left: 4px solid #3498db;\n" +
                "        }\n" +
                "        .mineral-header {\n" +
                "            display: flex;\n" +
                "            justify-content: space-between;\n" +
                "            align-items: center;\n" +
                "            margin-bottom: 1rem;\n" +
                "            border-bottom: 2px solid #ecf0f1;\n" +
                "            padding-bottom: 0.5rem;\n" +
                "        }\n" +
                "        .mineral-header h3 { \n" +
                "            color: #2c3e50; \n" +
                "            font-size: 1.4rem; \n" +
                "        }\n" +
                "        .mineral-id {\n" +
                "            background: #ecf0f1;\n" +
                "            color: #7f8c8d;\n" +
                "            padding: 0.3rem 0.8rem;\n" +
                "            border-radius: 20px;\n" +
                "            font-size: 0.9rem;\n" +
                "        }\n" +
                "        .mineral-info {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n" +
                "            gap: 1rem;\n" +
                "            margin-bottom: 1rem;\n" +
                "        }\n" +
// ↓↓↓ ВСТАВЬТЕ ЗДЕСЬ ↓↓↓
                "        .mineral-content {\n" +
                "            display: flex;\n" +
                "            gap: 1rem;\n" +
                "            margin-bottom: 1rem;\n" +
                "        }\n" +
                "        .mineral-image {\n" +
                "            flex-shrink: 0;\n" +
                "            width: 150px;\n" +
                "            height: 150px;\n" +
                "            overflow: hidden;\n" +
                "            border-radius: 8px;\n" +
                "            border: 2px solid #ecf0f1;\n" +
                "        }\n" +
                "        .mineral-image img {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            object-fit: cover;\n" +
                "            transition: transform 0.3s;\n" +
                "        }\n" +
                "        .mineral-image img:hover {\n" +
                "            transform: scale(1.05);\n" +
                "        }\n" +
                "        .mineral-actions {\n" +
                "            display: flex;\n" +
                "            gap: 0.5rem;\n" +
                "            flex-wrap: wrap;\n" +
                "        }\n" +
                "        .btn-info, .btn-danger {\n" +
                "            padding: 0.5rem 1rem;\n" +
                "            border: none;\n" +
                "            border-radius: 5px;\n" +
                "            cursor: pointer;\n" +
                "            transition: all 0.3s;\n" +
                "        }\n" +
                "        .btn-info { \n" +
                "            background: #3498db; \n" +
                "            color: white; \n" +
                "        }\n" +
                "        .btn-info:hover { background: #2980b9; }\n" +
                "        .btn-danger { \n" +
                "            background: #e74c3c; \n" +
                "            color: white; \n" +
                "        }\n" +
                "        .btn-danger:hover { background: #c0392b; }\n" +
                "        \n" +
                "        /* Стили для форм поиска и фильтрации */\n" +
                "        .search-filter-section {\n" +
                "            background: white;\n" +
                "            padding: 1.5rem;\n" +
                "            border-radius: 10px;\n" +
                "            margin-bottom: 1rem;\n" +
                "        }\n" +
                "        .form-row {\n" +
                "            display: flex;\n" +
                "            gap: 0.5rem;\n" +
                "            margin-bottom: 1rem;\n" +
                "            align-items: flex-end;\n" +
                "        }\n" +
                "        .form-group {\n" +
                "            flex: 1;\n" +
                "        }\n" +
                "        .form-group label {\n" +
                "            display: block;\n" +
                "            margin-bottom: 0.5rem;\n" +
                "            color: #2c3e50;\n" +
                "            font-weight: 500;\n" +
                "        }\n" +
                "        .form-input {\n" +
                "            width: 100%;\n" +
                "            padding: 10px;\n" +
                "            border: 1px solid #ddd;\n" +
                "            border-radius: 5px;\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "        .form-input:focus {\n" +
                "            outline: none;\n" +
                "            border-color: #3498db;\n" +
                "            box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);\n" +
                "        }\n" +
                "        .btn-primary {\n" +
                "            background: #3498db;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            padding: 10px 20px;\n" +
                "            border-radius: 5px;\n" +
                "            cursor: pointer;\n" +
                "            transition: background 0.3s;\n" +
                "        }\n" +
                "        .btn-primary:hover { background: #2980b9; }\n" +
                "        .btn-secondary {\n" +
                "            background: #9b59b6;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            padding: 10px 20px;\n" +
                "            border-radius: 5px;\n" +
                "            cursor: pointer;\n" +
                "            transition: background 0.3s;\n" +
                "        }\n" +
                "        .btn-secondary:hover { background: #8e44ad; }\n" +
                "        \n" +
                "        #authModal {\n" +
                "            display: none; \n" +
                "            position: fixed; \n" +
                "            top: 0; \n" +
                "            left: 0; \n" +
                "            width: 100%; \n" +
                "            height: 100%; \n" +
                "            background: rgba(0,0,0,0.5);\n" +
                "        }\n" +
                "        .form-grid {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: 1fr 1fr;\n" +
                "            gap: 1rem;\n" +
                "        }\n" +
                "        .form-section {\n" +
                "            background: #f8f9fa;\n" +
                "            padding: 1.5rem;\n" +
                "            border-radius: 10px;\n" +
                "            border-left: 4px solid #3498db;\n" +
                "        }\n" +
                "        .form-section h3 {\n" +
                "            color: #2c3e50;\n" +
                "            margin-bottom: 1rem;\n" +
                "            border-bottom: 2px solid #ecf0f1;\n" +
                "            padding-bottom: 0.5rem;\n" +
                "        }\n" +
                "        .form-textarea {\n" +
                "            width: 100%;\n" +
                "            padding: 10px;\n" +
                "            margin: 5px 0;\n" +
                "            border: 1px solid #ddd;\n" +
                "            border-radius: 5px;\n" +
                "            font-size: 14px;\n" +
                "            font-family: inherit;\n" +
                "            resize: vertical;\n" +
                "            min-height: 80px;\n" +
                "        }\n" +
                "        .stats-grid {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
                "            gap: 1rem;\n" +
                "            margin-bottom: 1rem;\n" +
                "        }\n" +
                "        .stat-card {\n" +
                "            background: white;\n" +
                "            padding: 2rem;\n" +
                "            border-radius: 10px;\n" +
                "            text-align: center;\n" +
                "            box-shadow: 0 5px 15px rgba(0,0,0,0.1);\n" +
                "        }\n" +
                "        .stat-card h3 {\n" +
                "            font-size: 2rem;\n" +
                "            color: #3498db;\n" +
                "            margin-bottom: 0.5rem;\n" +
                "        }\n" +
                "        .stat-card p {\n" +
                "            color: #7f8c8d;\n" +
                "            font-size: 1rem;\n" +
                "        }\n" +
                " /* Новые стили для фильтров по категории ценности */\n" +
                "        .category-filters {\n" +
                "            display: flex;\n" +
                "            flex-wrap: wrap;\n" +
                "            gap: 0.5rem;\n" +
                "            margin-bottom: 1rem;\n" +
                "        }\n" +
                "        .category-filter-btn {\n" +
                "            background: #f39c12;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            padding: 0.5rem 1rem;\n" +
                "            border-radius: 5px;\n" +
                "            cursor: pointer;\n" +
                "            transition: background 0.3s;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            gap: 0.5rem;\n" +
                "        }\n" +
                "        .category-filter-btn:hover {\n" +
                "            background: #e67e22;\n" +
                "        }\n" +
                "        .category-select {\n" +
                "            width: 100%;\n" +
                "            padding: 10px;\n" +
                "            border: 1px solid #ddd;\n" +
                "            border-radius: 5px;\n" +
                "            font-size: 14px;\n" +
                "            background: white;\n" +
                "            margin-bottom: 0.5rem;\n" +
                "        }\n" +
                 "        .btn-gold {\n" +
                "            background: #f39c12 !important;\n" +
                "            color: white !important;\n" +
                "        }\n" +
                "        .btn-gold:hover {\n" +
                "            background: #e67e22 !important;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            " + authBlock + "\n" +
                "            <h1>💎 Каталог Минералов</h1>\n" +
                "            <p>Умная система управления коллекцией минералов</p>\n" +
                "            <div class=\"stats-bar\">\n" +
                "                <strong>Загружено минералов: " + WebService.mineralService.getCollectionSize() + "</strong> | \n" +
                "                <strong>Доступно классов: " + WebService.mineralService.getAllMineralClasses().size() + "</strong>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <nav class=\"main-nav\">\n" +
                "            <button onclick=\"showSection('all')\">📋 Все минералы</button>\n" +
                "            <button onclick=\"showSection('search')\">🔍 Поиск и фильтры</button>\n" +
                "            " + (isAdmin ? "<button onclick=\"showSection('add')\">➕ Добавить минерал</button>" : "") + "\n" +
                "            <button onclick=\"showSection('stats')\">📊 Статистика</button>\n" +
                "            <button onclick=\"showSection('export')\">📁 Экспорт</button>\n" +
                "        </nav>\n" +
                "        \n" +
                "        <main>\n" +
                "            <!-- Секция всех минералов -->\n" +
                "            <div id=\"all\" class=\"section " + (currentSection.equals("all") ? "active" : "") + "\">\n" +
                "                <div class=\"section-header\">\n" +
                "                    <h2 style=\"color: white;\">" + sectionTitle + "</h2>\n" +
                "                    " + (!additionalInfo.isEmpty() ? "<button onclick=\"clearFilters()\" class=\"btn-clear\" style=\"padding: 0.5rem 1rem;\">❌ Сбросить фильтры</button>" : "") + "\n" +
                "                </div>\n" +
                "                " + additionalInfo + "\n" +
                "                <div class=\"minerals-controls\">\n" +
                "                    <button onclick=\"performSort('name')\">📝 Сортировать по названию</button>\n" +
                "                    <button onclick=\"performSort('hardness')\">💪 Сортировать по твердости</button>\n" +
                "                    <span style=\"margin-left: auto; color: #7f8c8d; font-size: 0.9rem;\">\n" +
                "                        Показано: " + mineralsToShow.size() + " минералов\n" +
                "                    </span>\n" +
                "                </div>\n" +
                "                <div id=\"mineralsList\">" + resultsHtml + "</div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <!-- Секция поиска и фильтров -->\n" +
                "             <div id=\"search\" class=\"section " + (currentSection.equals("search") ? "active" : "") + "\">\n" +
                "                <div class=\"section-header\">\n" +
                "                    <h2 style=\"color: white;\">" + sectionTitle + "</h2>\n" +
                "                    " + (!additionalInfo.isEmpty() ? "<button onclick=\"clearFilters()\" class=\"btn-clear\" style=\"padding: 0.5rem 1rem;\">❌ Сбросить фильтры</button>" : "") + "\n" +
                "                </div>\n" +
                "                " + additionalInfo + "\n" +
                "                \n" +
                "                <div class=\"search-filter-section\">\n" +
                "                    <h3 style=\"color: #2c3e50; margin-bottom: 1rem;\">🔎 Поиск по названию</h3>\n" +
                "                    <div class=\"form-row\">\n" +
                "                         <div class=\"form-group\">\n" +
                "                            <input type=\"text\" id=\"searchInput\" placeholder=\"Введите название минерала\" value=\"" + escapeHtml(searchQuery) + "\" class=\"form-input\">\n" +
                "                        </div>\n" +
                "                        <button onclick=\"performSearch()\" class=\"btn-primary\">Найти</button>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                \n" +
                "                <div class=\"search-filter-section\">\n" +
                "                    <h3 style=\"color: #2c3e50; margin-bottom: 1rem;\">🎨 Фильтрация по свойствам</h3>\n" +
                "                    <div style=\"display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1rem;\">\n" +
                "                        <div>\n" +
                "                            <div class=\"form-group\">\n" +
                "                                <label for=\"colorFilter\">По цвету:</label>\n" +
                "                                <input type=\"text\" id=\"colorFilter\" placeholder=\"Например: зеленый\" value=\"" + (filterType.equals("color") ? escapeHtml(filterValue) : "") + "\" class=\"form-input\">\n" +
                "                            </div>\n" +
                "                            <button onclick=\"performFilter('color')\" class=\"btn-secondary\" style=\"width: 100%; margin-top: 0.5rem;\">Фильтровать по цвету</button>\n" +
                "                        </div>\n" +
                "                        <div>\n" +
                "                            <div class=\"form-group\">\n" +
                "                                <label for=\"locationFilter\">По месторождению:</label>\n" +
                "                                <input type=\"text\" id=\"locationFilter\" placeholder=\"Например: Урал\" value=\"" + (filterType.equals("location") ? escapeHtml(filterValue) : "") + "\" class=\"form-input\">\n" +
                "                        </div>\n" +
                "                            <button onclick=\"performFilter('location')\" class=\"btn-secondary\" style=\"width: 100%; margin-top: 0.5rem;\">Фильтровать по месторождению</button>\n" +
"                        </div>\n" +
                "                        <div>\n" +
                "                            <div class=\"form-group\">\n" +
                "                                <label for=\"valueCategoryFilter\">По категории ценности:</label>\n" +
                "                                <select id=\"valueCategoryFilter\" class=\"category-select\">\n" +
                "                                    <option value=\"\">-- Все категории --</option>\n" +
                "                                    " + categoryOptions.toString() + "\n" +
                "                                </select>\n" +
                "                            </div>\n" +
                "                            <button onclick=\"performFilter('valueCategory')\" class=\"btn-gold\" style=\"width: 100%; margin-top: 0.5rem; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer;\">\n" +
                 "                                💰 Фильтровать по категории\n" +
                "                            </button>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                    \n" +
                "                    <div style=\"margin-top: 1rem; padding: 1rem; background: #fff8e1; border-radius: 5px; border-left: 4px solid #f39c12;\">\n" +
                "                        <h4 style=\"color: #e67e22; margin-bottom: 0.5rem;\">Быстрые фильтры по категориям:</h4>\n" +
                "                        <div class=\"category-filters\">\n" +
                "                            <button onclick=\"quickFilterByCategory('драгоценный')\" class=\"category-filter-btn\">💎 Драгоценные</button>\n" +
                "                            <button onclick=\"quickFilterByCategory('полудрагоценный')\" class=\"category-filter-btn\">✨ Полудрагоценные</button>\n" +
                "                            <button onclick=\"quickFilterByCategory('поделочный')\" class=\"category-filter-btn\">🪨 Поделочные</button>\n" +
                "                            <button onclick=\"quickFilterByCategory('коллекционный')\" class=\"category-filter-btn\">📚 Коллекционные</button>\n" +
                "                            <button onclick=\"quickFilterByCategory('руда')\" class=\"category-filter-btn\">🏭 Руда</button>\n" +
                "                       </div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                \n" +
                "                <div id=\"searchResults\">" + resultsHtml + "</div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <!-- Остальные секции (add, stats, export) -->\n" +
                "            " + (isAdmin ? "<div id=\"add\" class=\"section " + (currentSection.equals("add") ? "active" : "") + "\">\n" +
                "    <h2 style=\"color: white; margin-bottom: 1rem;\">➕ Добавить новый минерал</h2>\n" +
                "    <form id=\"addMineralForm\" onsubmit=\"addMineral(event)\" style=\"background: white; padding: 2rem; border-radius: 10px;\">\n" +
                "        <div class=\"form-grid\">\n" +
                "            <div class=\"form-section\">\n" +
                "                <h3>Основная информация</h3>\n" +
                "                <input type=\"text\" name=\"name\" placeholder=\"Название*\" required class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"formula\" placeholder=\"Химическая формула\" class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"mineralClass\" placeholder=\"Класс минерала*\" required class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"color\" placeholder=\"Цвет\" class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"streakColor\" placeholder=\"Цвет черты\" class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"luster\" placeholder=\"Блеск\" class=\"form-input\">\n" +
"                <input type=\"text\" name=\"imageUrl\" placeholder=\"URL изображения (images/название.png)\" class=\"form-input\">\n" +
                "            </div>\n" +
                "            <div class=\"form-section\">\n" +
                "                <h3>Физические свойства</h3>\n" +
                "                <input type=\"text\" name=\"hardness\" placeholder=\"Твердость по Моосу\" class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"specificGravity\" placeholder=\"Удельный вес\" class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"cleavage\" placeholder=\"Спайность\" class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"fracture\" placeholder=\"Излом\" class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"genesis\" placeholder=\"Генезис (происхождение)\" class=\"form-input\">\n" +
                "                <input type=\"text\" name=\"location\" placeholder=\"Месторождение\" class=\"form-input\">\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div style=\"margin-top: 1rem;\">\n" +
                "            <h3 style=\"color: #2c3e50; margin-bottom: 1rem;\">Дополнительная информация</h3>\n" +
                "            <textarea name=\"application\" placeholder=\"Применение\" class=\"form-textarea\"></textarea>\n" +
                "            <textarea name=\"additionalProperties\" placeholder=\"Дополнительные свойства\" class=\"form-textarea\"></textarea>\n" +
                "            <textarea name=\"interestingFacts\" placeholder=\"Интересные факты\" class=\"form-textarea\"></textarea>\n" +
                "        </div>\n" +
                "        \n" +
                "        <button type=\"submit\" style=\"width: 100%; padding: 12px; background: #27ae60; color: white; border: none; border-radius: 5px; cursor: pointer; margin-top: 1rem; font-size: 1.1rem;\">\n" +
                "            💎 Добавить минерал\n" +
                "        </button>\n" +
                "    </form>\n" +
                "</div>" : "") + "\n" +
                "            \n" +
                "                <div id=\"stats\" class=\"section " + (currentSection.equals("stats") ? "active" : "") + "\">\n" +
                "                <h2 style=\"color: white; margin-bottom: 1rem;\">📊 Статистика коллекции</h2>\n" +
                "                \n" +
                "                    <div class=\"search-filter-section\">\n" +
                "                    <h3 style=\"color: #2c3e50; margin-bottom: 1rem;\">📈 Общая статистика</h3>\n" +
                "                    <div class=\"stats-grid\">\n" +
                "                        <div class=\"stat-card\">\n" +
                "                            <h3>" + WebService.mineralService.getCollectionSize() + "</h3>\n" +
                "                        <p>Всего минералов</p>\n" +
                "                    </div>\n" +
                "                    <div class=\"stat-card\">\n" +
                "                        <h3>" + WebService.mineralService.getAllMineralClasses().size() + "</h3>\n" +
                "                        <p>Разных классов</p>\n" +
                "                    </div>\n" +
                "                    <div class=\"stat-card\">\n" +
                "                        <h3>" + uniqueLocations + "</h3>\n" +
                "                        <p>Месторождений</p>\n" +
                "                    </div>\n" +
                "                    <div class=\"stat-card\">\n" +
                "                        <h3>" + uniqueColors + "</h3>\n" +
                "                        <p>Разных цветов</p>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                   "                <div class=\"search-filter-section\">\n" +
                "                    <h3 style=\"color: #2c3e50; margin-bottom: 1rem;\">💰 Распределение по категориям ценности</h3>\n" +
                "                    <div class=\"stats-grid\">\n" +
                "                        " + valueCategoryStatsHtml.toString() + "\n" +
                "                    </div>\n" +
                "                    <div style=\"margin-top: 1rem; padding: 1rem; background: #e8f4fd; border-radius: 5px; font-size: 0.9rem; color: #2c3e50;\">\n" +
                "                        <strong>Всего категорий:</strong> " + valueCategoryStats.size() + " | \n" +
                "                        <strong>Самая частая категория:</strong> " + getMostFrequentCategory(valueCategoryStats) + "\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div id=\"export\" class=\"section " + (currentSection.equals("export") ? "active" : "") + "\">\n" +
                "                <h2 style=\"color: white; margin-bottom: 1rem;\">📁 Экспорт данных</h2>\n" +
                "                <div style=\"background: white; padding: 2rem; border-radius: 10px;\">\n" +
                "                    <h3 style=\"color: #2c3e50; margin-bottom: 1rem;\">Выберите формат экспорта:</h3>\n" +
                "                    <div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 1rem;\">\n" +
                "                        <button onclick=\"exportMinerals('html')\" style=\"padding: 1rem; background: #3498db; color: white; border: none; border-radius: 5px; cursor: pointer;\">📄 HTML</button>\n" +
                "                        <button onclick=\"exportMinerals('csv')\" style=\"padding: 1rem; background: #27ae60; color: white; border: none; border-radius: 5px; cursor: pointer;\">📊 CSV</button>\n" +
                "                        <button onclick=\"exportMinerals('txt')\" style=\"padding: 1rem; background: #e67e22; color: white; border: none; border-radius: 5px; cursor: pointer;\">📝 TXT</button>\n" +
                "                    </div>\n" +
                "                    <p style=\"margin-top: 1rem; color: #7f8c8d; font-size: 0.9rem;\">Файл будет скачан автоматически в выбранном формате</p>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </main>\n" +
                "    </div>\n" +
                "\n" +
                "    <!-- Модальное окно авторизации -->\n" +
                "    <div id=\"authModal\">\n" +
                "        <div style=\"background: white; margin: 100px auto; padding: 20px; width: 350px; border-radius: 10px;\">\n" +
                "            <div style=\"display: flex; justify-content: space-between; margin-bottom: 20px;\">\n" +
                "                <button id=\"loginTab\" onclick=\"showAuthTab('login')\" style=\"flex: 1; padding: 10px; background: #3498db; color: white; border: none; border-radius: 5px 0 0 5px;\">Вход</button>\n" +
                "                <button id=\"registerTab\" onclick=\"showAuthTab('register')\" style=\"flex: 1; padding: 10px; background: #95a5a6; color: white; border: none; border-radius: 0 5px 5px 0;\">Регистрация</button>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div id=\"loginForm\">\n" +
                "                <h2>Вход в систему</h2>\n" +
                "                <form onsubmit=\"handleLogin(event)\">\n" +
                "                    <input type=\"text\" name=\"username\" placeholder=\"Имя пользователя\" required style=\"width: 100%; padding: 8px; margin: 5px 0;\">\n" +
                "                    <input type=\"password\" name=\"password\" placeholder=\"Пароль\" required style=\"width: 100%; padding: 8px; margin: 5px 0;\">\n" +
                "                    <button type=\"submit\" style=\"width: 100%; padding: 10px; background: #3498db; color: white; border: none; border-radius: 5px;\">Войти</button>\n" +
                "                </form>\n" +
                "                <p><strong>Admin:</strong> admin / admin123</p>\n" +
                "                <p><strong>User:</strong> user / user123</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div id=\"registerForm\" style=\"display: none;\">\n" +
                "                <h2>Регистрация</h2>\n" +
                "                <form onsubmit=\"handleRegister(event)\">\n" +
                "                    <input type=\"text\" name=\"username\" placeholder=\"Имя пользователя\" required style=\"width: 100%; padding: 8px; margin: 5px 0;\">\n" +
                "                    <input type=\"password\" name=\"password\" placeholder=\"Пароль\" required style=\"width: 100%; padding: 8px; margin: 5px 0;\">\n" +
                "                    <button type=\"submit\" style=\"width: 100%; padding: 10px; background: #27ae60; color: white; border: none; border-radius: 5px;\">Зарегистрироваться</button>\n" +
                "                </form>\n" +
                "            </div>\n" +
                "            \n" +
                "            <button onclick=\"closeAuthModal()\" style=\"width: 100%; padding: 5px; margin-top: 10px;\">Отмена</button>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "\n" +
                "    <script>\n" +
                "        function showSection(sectionId) {\n" +
                "            window.location.href = '/?section=' + sectionId;\n" +
                "        }\n" +
                "\n" +
                "        function performSort(field) {\n" +
                "            window.location.href = '/?section=all&sort=' + field;\n" +
                "        }\n" +
                "\n" +
                "        function performSearch() {\n" +
                "            const query = document.getElementById('searchInput').value;\n" +
                "            if (!query.trim()) {\n" +
                "                showSection('search');\n" +
                "                return;\n" +
                "            }\n" +
                "            window.location.href = '/?section=search&search=' + encodeURIComponent(query);\n" +
                "        }\n" +
                "\n" +
                "        function performFilter(type) {\n" +
                "            let value = '';\n" +
                "            if (type === 'color') {\n" +
                "                value = document.getElementById('colorFilter').value;\n" +
                "            } else if (type === 'location') {\n" +
                "                value = document.getElementById('locationFilter').value;\n" +
                "            } else if (type === 'valueCategory') {\n" +
                "                value = document.getElementById('valueCategoryFilter').value;\n" +
                "            }\n" +
                "            \n" +
                "            if (!value.trim() && type !== 'valueCategory') {\n" +
                "                showSection('search');\n" +
                "                return;\n" +
                "            }\n" +
                "            window.location.href = '/?section=search&filterType=' + type + '&filterValue=' + encodeURIComponent(value) + '&valueCategory=' + encodeURIComponent(value);\n" +
                "           }\n" +
                "        function clearFilters() {\n" +
                "            window.location.href = '/';\n" +
                "        }\n" +
                "\n" +
                 "        function quickFilterByCategory(category) {\n" +
                "            window.location.href = '/?section=search&filterType=valueCategory&filterValue=' + encodeURIComponent(category) + '&valueCategory=' + encodeURIComponent(category);\n" +
                "        }\n" +
                "        \n" +
                "        // Обработка нажатия Enter в новых полях\n" +
                "        document.addEventListener('DOMContentLoaded', function() {\n" +
                "            // ... существующие обработчики ...\n" +
                "            const valueCategoryFilter = document.getElementById('valueCategoryFilter');\n" +
                "            if (valueCategoryFilter) {\n" +
                "                valueCategoryFilter.addEventListener('change', function() {\n" +
                "                    if (this.value) {\n" +
                "                        performFilter('valueCategory');\n" +
                "                    }\n" +
                "                });\n" +
                "            }\n" +
                "        });\n" +
                "        \n" +
                "        // Обработка нажатия Enter в полях поиска\n" +
                "        document.addEventListener('DOMContentLoaded', function() {\n" +
                "            const searchInput = document.getElementById('searchInput');\n" +
                "            const colorFilter = document.getElementById('colorFilter');\n" +
                "            const locationFilter = document.getElementById('locationFilter');\n" +
                "            \n" +
                "            if (searchInput) {\n" +
                "                searchInput.addEventListener('keypress', function(e) {\n" +
                "                    if (e.key === 'Enter') performSearch();\n" +
                "                });\n" +
                "            }\n" +
                "            if (colorFilter) {\n" +
                "                colorFilter.addEventListener('keypress', function(e) {\n" +
                "                    if (e.key === 'Enter') performFilter('color');\n" +
                "                });\n" +
                "            }\n" +
                "            if (locationFilter) {\n" +
                "                locationFilter.addEventListener('keypress', function(e) {\n" +
                "                    if (e.key === 'Enter') performFilter('location');\n" +
                "                });\n" +
                "            }\n" +
                "        });\n" +
                "\n" +
                "        // Функции для работы с минералами\n" +
                "        function showMineralDetails(id) {\n" +
                "            window.open('/mineral?id=' + id, '_blank');\n" +
                "        }\n" +
                "\n" +
                "        function exportMinerals(format) {\n" +
                "            window.open('/export?format=' + format, '_blank');\n" +
                "        }\n" +
                "\n" +
                "        async function addMineral(event) {\n" +
                "            event.preventDefault();\n" +
                "            const formData = new FormData(event.target);\n" +
                "            \n" +
                "            try {\n" +
                "                const response = await fetch('/add', {\n" +
                "                    method: 'POST',\n" +
                "                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },\n" +
                "                    body: new URLSearchParams(formData)\n" +
                "                });\n" +
                "                \n" +
                "                if (response.ok) {\n" +
                "                    alert('Минерал успешно добавлен!');\n" +
                "                    event.target.reset();\n" +
                "                    showSection('all');\n" +
                "                } else {\n" +
                "                    alert('Ошибка при добавлении минерала');\n" +
                "                }\n" +
                "            } catch (error) {\n" +
                "                alert('Ошибка при добавлении минерала: ' + error);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        async function deleteMineral(id) {\n" +
                "            if (!confirm('Вы уверены, что хотите удалить этот минерал?')) return;\n" +
                "            \n" +
                "            try {\n" +
                "                const response = await fetch('/delete?id=' + id, { method: 'POST' });\n" +
                "                if (response.ok) {\n" +
                "                    alert('Минерал удален!');\n" +
                "                    location.reload();\n" +
                "                }\n" +
                "            } catch (error) {\n" +
                "                alert('Ошибка при удалении минерала');\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        // Функции для модального окна авторизации\n" +
                "        function openAuthModal() {\n" +
                "            document.getElementById('authModal').style.display = 'block';\n" +
                "        }\n" +
                "        \n" +
                "        function closeAuthModal() {\n" +
                "            document.getElementById('authModal').style.display = 'none';\n" +
                "        }\n" +
                "\n" +
                "        function showAuthTab(tab) {\n" +
                "            if (tab === 'login') {\n" +
                "                document.getElementById('loginForm').style.display = 'block';\n" +
                "                document.getElementById('registerForm').style.display = 'none';\n" +
                "                document.getElementById('loginTab').style.background = '#3498db';\n" +
                "                document.getElementById('registerTab').style.background = '#95a5a6';\n" +
                "            } else {\n" +
                "                document.getElementById('loginForm').style.display = 'none';\n" +
                "                document.getElementById('registerForm').style.display = 'block';\n" +
                "                document.getElementById('loginTab').style.background = '#95a5a6';\n" +
                "                document.getElementById('registerTab').style.background = '#27ae60';\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        async function handleLogin(event) {\n" +
                "            event.preventDefault();\n" +
                "            const formData = new FormData(event.target);\n" +
                "            try {\n" +
                "                const response = await fetch('/login', {\n" +
                "                    method: 'POST',\n" +
                "                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },\n" +
                "                    body: new URLSearchParams(formData)\n" +
                "                });\n" +
                "                if (response.ok) {\n" +
                "                    closeAuthModal();\n" +
                "                    location.reload();\n" +
                "                } else {\n" +
                "                    alert('Ошибка входа');\n" +
                "                }\n" +
                "            } catch (error) {\n" +
                "                alert('Ошибка: ' + error);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        async function handleRegister(event) {\n" +
                "            event.preventDefault();\n" +
                "            const formData = new FormData(event.target);\n" +
                "            try {\n" +
                "                const response = await fetch('/register', {\n" +
                "                    method: 'POST',\n" +
                "                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },\n" +
                "                    body: new URLSearchParams(formData)\n" +
                "                });\n" +
                "                if (response.ok) {\n" +
                "                    closeAuthModal();\n" +
                "                    location.reload();\n" +
                "                } else {\n" +
                "                    alert('Ошибка регистрации. Возможно, пользователь уже существует.');\n" +
                "                }\n" +
                "            } catch (error) {\n" +
                "                alert('Ошибка: ' + error);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        window.onclick = function(event) {\n" +
                "            const modal = document.getElementById('authModal');\n" +
                "            if (event.target === modal) {\n" +
                "                closeAuthModal();\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, htmlBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(htmlBytes);
        os.close();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
    
     private String getMostFrequentCategory(Map<String, Integer> stats) {
        if (stats.isEmpty()) return "Нет данных";
        
        String mostFrequent = null;
        int maxCount = 0;
        
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        
        return mostFrequent + " (" + maxCount + " шт.)";
    }
}

// Остальные классы-обработчики остаются без изменений...

class SearchHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = HandlerUtils.parseQuery(query);

        String type = params.get("type");
        String searchQuery = params.get("query");

        List<Mineral> results = new ArrayList<>();

        if ("name".equals(type) && searchQuery != null && !searchQuery.trim().isEmpty()) {
            results = WebService.mineralService.searchByName(searchQuery);
        }

        String json = HandlerUtils.convertMineralsToJson(results);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}

class FilterHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = HandlerUtils.parseQuery(query);

        String type = params.get("type");
        String filterQuery = params.get("query");

        List<Mineral> results = new ArrayList<>();

        if (filterQuery != null && !filterQuery.trim().isEmpty()) {
            switch (type) {
                case "color":
                    results = WebService.mineralService.filterByColor(filterQuery);
                    break;
                case "location":
                    results = WebService.mineralService.filterByLocation(filterQuery);
                    break;
                case "valueCategory": // Новый тип фильтра
                    results = WebService.mineralService.filterByValueCategory(filterQuery);
                    break;
            }
        }

        String json = HandlerUtils.convertMineralsToJson(results);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}

class SortHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = HandlerUtils.parseQuery(query);

        String field = params.get("field");
        System.out.println("Sort request received for field: " + field);

        List<Mineral> results = new ArrayList<>();

        if ("name".equals(field)) {
            results = WebService.mineralService.sortByName();
            System.out.println("Sorted by name, results: " + results.size());
        } else if ("hardness".equals(field)) {
            results = WebService.mineralService.sortByHardness();
            System.out.println("Sorted by hardness, results: " + results.size());
            for (int i = 0; i < Math.min(5, results.size()); i++) {
                Mineral m = results.get(i);
                System.out.println("Mineral: " + m.getName() + ", Hardness: " + m.getHardness());
            }
        } else {
            results = WebService.mineralService.getAllMinerals();
        }

        String json = HandlerUtils.convertMineralsToJson(results);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}

class MineralsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<Mineral> minerals = WebService.mineralService.getAllMinerals();
        String json = HandlerUtils.convertMineralsToJson(minerals);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}

class AddMineralHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!HandlerUtils.isAdmin(exchange)) {
            exchange.sendResponseHeaders(403, 0);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            Map<String, String> params = parseFormData(requestBody);

            String name = params.get("name");
            String formula = params.get("formula");
            String mineralClass = params.get("mineralClass");
            String color = params.get("color");
            String streakColor = params.get("streakColor");
            String luster = params.get("luster");
            String hardness = params.get("hardness");
            String specificGravity = params.get("specificGravity");
            String cleavage = params.get("cleavage");
            String fracture = params.get("fracture");
            String genesis = params.get("genesis");
            String application = params.get("application");
            String additionalProperties = params.get("additionalProperties");
            String interestingFacts = params.get("interestingFacts");
            String location = params.get("location");
            String imageUrl = params.get("imageUrl"); // Получаем URL изображения

            if (name != null && mineralClass != null) {
                try {
                    WebService.mineralService.addMineral(
                            name, formula, mineralClass, color, streakColor, luster,
                            hardness, specificGravity, cleavage, fracture, genesis,
                            application, additionalProperties, interestingFacts, location,
                            imageUrl  // ← Передаем imageUrl
                    );

                    String response = "Минерал успешно добавлен";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    String response = "Ошибка при добавлении минерала: " + e.getMessage();
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(400, 0);
            }
        }
    }

    private Map<String, String> parseFormData(String formData) {
        Map<String, String> result = new HashMap<>();
        if (formData != null) {
            for (String param : formData.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    try {
                        String key = java.net.URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                        String value = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        result.put(key, value);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        return result;
    }
}

class DeleteMineralHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!HandlerUtils.isAdmin(exchange)) {
            exchange.sendResponseHeaders(403, 0);
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String idStr = query != null ? query.replace("id=", "") : null;

            if (idStr != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    boolean deleted = WebService.mineralService.removeMineralById(id);

                    if (deleted) {
                        String response = "Минерал удален";
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else {
                        exchange.sendResponseHeaders(404, 0);
                    }
                } catch (NumberFormatException e) {
                    exchange.sendResponseHeaders(400, 0);
                }
            }
        }
    }
}

class StatsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String html = "<div class='stats-grid'>" +
                "<div class='stat-card'><h3>" + WebService.mineralService.getCollectionSize() + "</h3><p>Всего минералов</p></div>" +
                "<div class='stat-card'><h3>" + WebService.mineralService.getAllMineralClasses().size() + "</h3><p>Разных классов</p></div>" +
                "</div>";

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, html.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(html.getBytes());
        os.close();
    }
}

class ExportHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String format = "html";
        if (query != null && query.startsWith("format=")) {
            format = query.substring(7);
        }

        List<Mineral> minerals = WebService.mineralService.getAllMinerals();
        String content = "";
        String contentType = "text/html";
        String filename = "minerals." + format;

        switch (format) {
            case "html":
                content = exportToHTML(minerals);
                break;
            case "csv":
                content = exportToCSV(minerals);
                contentType = "text/csv";
                break;
            case "txt":
                content = exportToText(minerals);
                contentType = "text/plain";
                break;
            default:
                content = "Неверный формат";
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exchange.sendResponseHeaders(200, content.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(content.getBytes());
        os.close();
    }

private String exportToHTML(List<Mineral> minerals) {
    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Экспорт минералов</title></head><body>");
    html.append("<h1>Каталог минералов</h1>");
    for (Mineral mineral : minerals) {
        String imageUrl = mineral.getImageUrl() != null && !mineral.getImageUrl().isEmpty() ? 
                mineral.getImageUrl() : "";
        
        html.append(String.format(
                "<div style='border:1px solid #ccc; padding:10px; margin:10px; display:flex; gap:20px;'>" +
                "    <div style='flex-shrink:0;'>" +
                "        <img src='%s' alt='%s' style='width:150px; height:150px; object-fit:cover; border-radius:5px;' onerror=\"this.style.display='none'\">" +
                "    </div>" +
                "    <div style='flex:1;'>" +
                "        <h3>%s</h3>" +
                "        <p><strong>Формула:</strong> %s</p>" +
                "        <p><strong>Класс:</strong> %s</p>" +
                "        <p><strong>Цвет:</strong> %s</p>" +
                "        <p><strong>Твердость:</strong> %s</p>" +
                "        <p><strong>Месторождение:</strong> %s</p>" +
                "        <p><strong>Применение:</strong> %s</p>" +
                "    </div>" +
                "</div>",
                imageUrl, mineral.getName(),
                mineral.getName(), mineral.getFormula(), mineral.getMineralClass(),
                mineral.getColor(), mineral.getHardness(), mineral.getLocation(),
                mineral.getApplication()
        ));
    }
    html.append("</body></html>");
    return html.toString();
}

    private String exportToCSV(List<Mineral> minerals) {
        StringBuilder csv = new StringBuilder();
        csv.append("Название,Формула,Класс,Цвет,Твердость,Месторождение,Применение\\n");
        for (Mineral mineral : minerals) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\\n",
                    mineral.getName(), mineral.getFormula(), mineral.getMineralClass(),
                    mineral.getColor(), mineral.getHardness(), mineral.getLocation(),
                    mineral.getApplication()
            ));
        }
        return csv.toString();
    }

    private String exportToText(List<Mineral> minerals) {
        StringBuilder text = new StringBuilder();
        text.append("КАТАЛОГ МИНЕРАЛОВ\\n");
        text.append("================\\n\\n");
        for (Mineral mineral : minerals) {
            text.append(String.format(
                    "Минерал: %s\\n" +
                            "Формула: %s\\n" +
                            "Класс: %s\\n" +
                            "Цвет: %s\\n" +
                            "Твердость: %s\\n" +
                            "Месторождение: %s\\n" +
                            "Применение: %s\\n\\n",
                    mineral.getName(), mineral.getFormula(), mineral.getMineralClass(),
                    mineral.getColor(), mineral.getHardness(), mineral.getLocation(),
                    mineral.getApplication()
            ));
        }
        return text.toString();
    }
}

class ApiMineralsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<Mineral> minerals = WebService.mineralService.getAllMinerals();
        String json = HandlerUtils.convertMineralsToJson(minerals);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}

class MineralDetailsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String idStr = query != null ? query.replace("id=", "") : null;

        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                Mineral mineral = WebService.mineralService.getMineralById(id);

                if (mineral != null) {
                    String html = generateMineralDetails(mineral);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(200, html.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(html.getBytes());
                    os.close();
                } else {
                    String response = "Минерал не найден";
                    exchange.sendResponseHeaders(404, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } catch (NumberFormatException e) {
                exchange.sendResponseHeaders(400, 0);
            }
        } else {
            exchange.sendResponseHeaders(400, 0);
        }
    }

    private String generateMineralDetails(Mineral mineral) {
    String imageUrl = mineral.getImageUrl() != null && !mineral.getImageUrl().isEmpty() ? 
            mineral.getImageUrl() : "images/no-image.png";
        return "<!DOCTYPE html>\n" +
                "<html lang=\"ru\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>" + mineral.getName() + " - Детальная информация</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body { \n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; \n" +
                "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                "            min-height: 100vh;\n" +
                "            color: #333;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .container { \n" +
                "            max-width: 1000px; \n" +
                "            margin: 0 auto; \n" +
                "            background: white;\n" +
                "            border-radius: 15px;\n" +
                "            box-shadow: 0 10px 30px rgba(0,0,0,0.1);\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "        .header { \n" +
                "            background: #2c3e50;\n" +
                "            color: white;\n" +
                "            padding: 2rem;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .header h1 { \n" +
                "            font-size: 2.5rem; \n" +
                "            margin-bottom: 0.5rem;\n" +
                "        }\n" +
                "        .mineral-id {\n" +
                "            background: #34495e;\n" +
                "            color: #ecf0f1;\n" +
                "            padding: 0.5rem 1rem;\n" +
                "            border-radius: 20px;\n" +
                "            display: inline-block;\n" +
                "            font-size: 0.9rem;\n" +
                "        }\n" +
                "        .content { padding: 2rem; }\n" +
                "        .info-grid {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));\n" +
                "            gap: 1.5rem;\n" +
                "            margin-bottom: 2rem;\n" +
                "        }\n" +
                "        .info-section {\n" +
                "            background: #f8f9fa;\n" +
                "            padding: 1.5rem;\n" +
                "            border-radius: 10px;\n" +
                "            border-left: 4px solid #3498db;\n" +
                "        }\n" +
                "        .info-section h3 {\n" +
                "            color: #2c3e50;\n" +
                "            margin-bottom: 1rem;\n" +
                "            border-bottom: 2px solid #ecf0f1;\n" +
                "            padding-bottom: 0.5rem;\n" +
                "        }\n" +
                "        .info-item {\n" +
                "            margin-bottom: 0.8rem;\n" +
                "            display: flex;\n" +
                "        }\n" +
                "        .info-label {\n" +
                "            font-weight: bold;\n" +
                "            color: #34495e;\n" +
                "            min-width: 180px;\n" +
                "        }\n" +
                "        .info-value {\n" +
                "            color: #2c3e50;\n" +
                "            flex: 1;\n" +
                "        }\n" +
                "        .text-section {\n" +
                "            background: #f8f9fa;\n" +
                "            padding: 1.5rem;\n" +
                "            border-radius: 10px;\n" +
                "            margin-bottom: 1.5rem;\n" +
                "            border-left: 4px solid #27ae60;\n" +
                "        }\n" +
                "        .text-section h3 {\n" +
                "            color: #2c3e50;\n" +
                "            margin-bottom: 1rem;\n" +
                "        }\n" +
                "        .text-content {\n" +
                "            line-height: 1.6;\n" +
                "            color: #2c3e50;\n" +
                "        }\n" +
                "        .back-button {\n" +
                "            background: #3498db;\n" +
                "            color: white;\n" +
                "            border: none;\n" +
                "            padding: 1rem 2rem;\n" +
                "            border-radius: 5px;\n" +
                "            font-size: 1.1rem;\n" +
                "            cursor: pointer;\n" +
                "            transition: background 0.3s;\n" +
                "            display: block;\n" +
                "            width: 250px;\n" +
                "            margin: 2rem auto 0;\n" +
                "            text-align: center;\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "        .back-button:hover { \n" +
                "            background: #2980b9; \n" +
                "        }\n" +
                "        .empty-field {\n" +
                "            color: #7f8c8d;\n" +
                "            font-style: italic;\n" +
                "        }\n" +
                "        .image-container {\n" +
"            text-align: center;\n" +
"            margin: 2rem 0;\n" +
"            padding: 1rem;\n" +
"            background: #f8f9fa;\n" +
"            border-radius: 10px;\n" +
"        }\n" +
"        .mineral-image-large {\n" +
"            max-width: 500px;\n" +
"            margin: 0 auto;\n" +
"            border-radius: 10px;\n" +
"            overflow: hidden;\n" +
"            box-shadow: 0 5px 15px rgba(0,0,0,0.2);\n" +
"            border: 3px solid white;\n" +
"        }\n" +
"        .mineral-image-large img {\n" +
"            width: 100%;\n" +
"            height: auto;\n" +
"            display: block;\n" +
"            max-height: 400px;\n" +
"            object-fit: contain;\n" +
"        }\n" +
"        .image-caption {\n" +
"            margin-top: 0.5rem;\n" +
"            color: #7f8c8d;\n" +
"            font-style: italic;\n" +
"            font-size: 0.9rem;\n" +
"        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "             <h1>💎 " + escapeHtml(mineral.getName()) + "</h1>\n" +
            "            <div class=\"mineral-id\">ID: " + mineral.getId() + "</div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"content\">\n" +
            "            <div class=\"image-container\">\n" +
            "                <div class=\"mineral-image-large\">\n" +
            "                    <img src='" + imageUrl + "' alt='" + escapeHtml(mineral.getName()) + "' onerror=\"this.src='images/no-image.png'\" />\n" +
            "                </div>\n" +
            "                <p class=\"image-caption\">Изображение: " + escapeHtml(mineral.getName()) + "</p>\n" +
            "            </div>\n" +
            "            \n" +
            "            <div class=\"info-grid\">\n" +
                "                <div class=\"info-section\">\n" +
                "                    <h3>🧪 Химические свойства</h3>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Химическая формула:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getFormula().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getFormula())) + "</span>\n" +
                "                    </div>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Класс минерала:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getMineralClass().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getMineralClass())) + "</span>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                \n" +
                "                <div class=\"info-section\">\n" +
                "                    <h3>🎨 Внешние характеристики</h3>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Цвет:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getColor().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getColor())) + "</span>\n" +
                "                    </div>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Цвет черты:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getStreakColor().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getStreakColor())) + "</span>\n" +
                "                    </div>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Блеск:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getLuster().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getLuster())) + "</span>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                \n" +
                "                <div class=\"info-section\">\n" +
                "                    <h3>💪 Физические свойства</h3>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Твердость по Моосу:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getHardness().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getHardness())) + "</span>\n" +
                "                    </div>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Удельный вес:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getSpecificGravity().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getSpecificGravity())) + "</span>\n" +
                "                    </div>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Спайность:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getCleavage().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getCleavage())) + "</span>\n" +
                "                    </div>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Излом:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getFracture().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getFracture())) + "</span>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                \n" +
                "                <div class=\"info-section\">\n" +
                "                    <h3>🌍 Происхождение</h3>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Генезис:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getGenesis().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getGenesis())) + "</span>\n" +
                "                    </div>\n" +
                "                    <div class=\"info-item\">\n" +
                "                        <span class=\"info-label\">Месторождение:</span>\n" +
                "                        <span class=\"info-value\">" + (mineral.getLocation().isEmpty() ? "<span class='empty-field'>не указано</span>" : escapeHtml(mineral.getLocation())) + "</span>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"text-section\">\n" +
                "                <h3>🏭 Применение</h3>\n" +
                "                <div class=\"text-content\">" + (mineral.getApplication().isEmpty() ? "<span class='empty-field'>Информация о применении не указана</span>" : escapeHtml(mineral.getApplication()).replace("\n", "<br>")) + "</div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"text-section\">\n" +
                "                <h3>🔬 Дополнительные свойства</h3>\n" +
                "                <div class=\"text-content\">" + (mineral.getAdditionalProperties().isEmpty() ? "<span class='empty-field'>Дополнительные свойства не указаны</span>" : escapeHtml(mineral.getAdditionalProperties()).replace("\n", "<br>")) + "</div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"text-section\">\n" +
                "                <h3>📚 Интересные факты</h3>\n" +
                "                <div class=\"text-content\">" + (mineral.getInterestingFacts().isEmpty() ? "<span class='empty-field'>Интересные факты не указаны</span>" : escapeHtml(mineral.getInterestingFacts()).replace("\n", "<br>")) + "</div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <button class=\"back-button\" onclick=\"window.location.href='/'\">⬅️ Назад к каталогу</button>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

class AuthService {
    private Map<String, String> users = new HashMap<>();
    private Map<String, Boolean> sessions = new HashMap<>();

    public AuthService() {
        users.put("admin", "admin123");
        users.put("user", "user123");
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        if (users.containsKey(username)) {
            return false;
        }

        users.put(username, password);
        return true;
    }

    public boolean authenticate(String username, String password) {
        String storedPassword = users.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    public boolean isAdmin(String username) {
        return "admin".equals(username);
    }

    public String createSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, isAdmin(username));
        return sessionId;
    }

    public boolean isValidSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public boolean isAdminSession(String sessionId) {
        return sessions.getOrDefault(sessionId, false);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}

class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            Map<String, String> params = HandlerUtils.parseQuery(requestBody);
            String username = params.get("username");
            String password = params.get("password");

            if (username != null && password != null && WebService.authService.authenticate(username, password)) {
                String sessionId = WebService.authService.createSession(username);

                exchange.getResponseHeaders().add("Set-Cookie", "session=" + sessionId + "; Path=/; HttpOnly");
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(302, 0);
                exchange.getResponseBody().close();
            } else {
                String response = "Неверное имя пользователя или пароль";
                exchange.sendResponseHeaders(401, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}

class LogoutHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String sessionId = HandlerUtils.getSessionId(exchange);
        if (sessionId != null) {
            WebService.authService.removeSession(sessionId);
        }

        exchange.getResponseHeaders().add("Set-Cookie", "session=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
        exchange.getResponseHeaders().set("Location", "/");
        exchange.sendResponseHeaders(302, 0);
        exchange.getResponseBody().close();
    }
}

class RegisterHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            Map<String, String> params = HandlerUtils.parseQuery(requestBody);
            String username = params.get("username");
            String password = params.get("password");

            if (username != null && password != null && WebService.authService.register(username, password)) {
                String sessionId = WebService.authService.createSession(username);

                exchange.getResponseHeaders().add("Set-Cookie", "session=" + sessionId + "; Path=/; HttpOnly");
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(302, 0);
                exchange.getResponseBody().close();
            } else {
                String response = "Ошибка регистрации. Возможно, пользователь уже существует.";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}

// ↓↓↓ ДОБАВЬТЕ ЭТОТ КЛАСС ПОСЛЕ RegisterHandler ↓↓↓
class StaticFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String filePath = path.substring(1); // Убираем первый слэш
        
        File file = new File(filePath);
        
        if (file.exists() && !file.isDirectory()) {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
            
            String contentType = "application/octet-stream";
            if (filePath.endsWith(".png")) {
                contentType = "image/png";
            } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filePath.endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        } else {
            // Если файл не найден, отправляем 404
            String response = "Файл не найден: " + filePath;
            exchange.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}