import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File; 
public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:minerals.db";
    
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("✅ SQLite драйвер загружен");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ SQLite драйвер не найден!");
            e.printStackTrace();
        }
    }
    
    /**
     * Создать все таблицы (если их нет)
     */
    public static void createTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Включаем поддержку внешних ключей
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // 1. Таблица месторождений
            String createLocalities = "CREATE TABLE IF NOT EXISTS localities (\n" +
                                     "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                                     "name TEXT NOT NULL UNIQUE\n" +
                                     ")";
            stmt.execute(createLocalities);
            
            // 2. Таблица минералов - ВАЖНО: колонка называется 'class' как в PostgreSQL!
         String createMinerals = "CREATE TABLE IF NOT EXISTS minerals (\n" +
                               "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                               "name TEXT NOT NULL,\n" +
                               "formula TEXT,\n" +
                               "class TEXT,\n" +  // ← ВАЖНО: class, а не mineral_class
                               "color TEXT,\n" +
                               "streak_color TEXT,\n" +
                               "luster TEXT,\n" +
                               "hardness TEXT,\n" +
                               "specific_gravity TEXT,\n" +
                               "cleavage TEXT,\n" +
                               "fracture TEXT,\n" +
                               "genesis TEXT,\n" +
                               "application TEXT,\n" +
                               "additional_properties TEXT,\n" +
                               "interesting_facts TEXT,\n" +
                               "value_category TEXT,\n" +  // ЗАПЯТАЯ добавлена
                               "image_url TEXT\n" +  // ← ДОБАВЬТЕ ЭТУ СТРОЧКУ
                               ")";
        stmt.execute(createMinerals);
            
            // 3. Таблица связи минерал-месторождение - БЕЗ ПОДЧЕРКИВАНИЯ как в PostgreSQL!
            String createLinks = "CREATE TABLE IF NOT EXISTS minerallocalities (\n" +  // ← БЕЗ ПОДЧЕРКИВАНИЯ
                                "mineral_id INTEGER,\n" +
                                "locality_id INTEGER,\n" +
                                "FOREIGN KEY (mineral_id) REFERENCES minerals(id) ON DELETE CASCADE,\n" +
                                "FOREIGN KEY (locality_id) REFERENCES localities(id) ON DELETE CASCADE,\n" +
                                "PRIMARY KEY (mineral_id, locality_id)\n" +
                                ")";
            stmt.execute(createLinks);
            
            System.out.println("✅ 3 таблицы созданы: minerals, localities, minerallocalities");
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка создания таблиц: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Загрузить все минералы с их месторождениями
     */
    public static List<Mineral> loadAllFromDatabase() {
    List<Mineral> minerals = new ArrayList<>();
    
    // ДИАГНОСТИКА: Путь к БД
    System.out.println("📁 Путь к SQLite БД: jdbc:sqlite:minerals.db");
    File dbFile = new File("minerals.db");
    System.out.println("📂 Файл БД существует: " + dbFile.exists() + 
                      ", размер: " + dbFile.length() + " байт");
    
    try (Connection conn = DriverManager.getConnection(DB_URL);
         Statement stmt = conn.createStatement()) {
        
        // Проверяем структуру таблицы minerals
        System.out.println("🔍 Проверка структуры таблицы minerals:");
        ResultSet rsMeta = conn.getMetaData().getColumns(null, null, "minerals", null);
        while (rsMeta.next()) {
            String colName = rsMeta.getString("COLUMN_NAME");
            String colType = rsMeta.getString("TYPE_NAME");
            System.out.println("  - " + colName + " (" + colType + ")");
        }
        
        // Включаем внешние ключи
        stmt.execute("PRAGMA foreign_keys = ON");
        
        // Проверяем, есть ли данные
        ResultSet rsCount = stmt.executeQuery("SELECT COUNT(*) as cnt FROM minerals");
        if (rsCount.next()) {
            System.out.println("📊 Записей в minerals: " + rsCount.getInt("cnt"));
        }
        
        // Загружаем все минералы
        String sql = "SELECT * FROM minerals ORDER BY name";
        ResultSet rs = stmt.executeQuery(sql);
        
        // Получаем метаданные ResultSet
        ResultSetMetaData metaData = rs.getMetaData();
        System.out.println("📋 Столбцы в ResultSet:");
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            System.out.println("  " + i + ". " + metaData.getColumnName(i));
        }
        
        while (rs.next()) {
            try {
                Mineral mineral = new Mineral();
                
                // Безопасное чтение полей
                mineral.setId(rs.getInt("id"));
                mineral.setName(rs.getString("name"));
                mineral.setFormula(rs.getString("formula"));
                
                // Пробуем разные варианты для поля class
                String mineralClass = "";
                try {
                    mineralClass = rs.getString("class");
                } catch (SQLException e) {
                    try {
                        // Пробуем по индексу
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            if ("class".equalsIgnoreCase(metaData.getColumnName(i))) {
                                mineralClass = rs.getString(i);
                                break;
                            }
                        }
                    } catch (SQLException e2) {
                        mineralClass = "";
                    }
                }
                mineral.setMineralClass(mineralClass);
                
                // Остальные поля
                mineral.setColor(rs.getString("color"));
                mineral.setStreakColor(rs.getString("streak_color"));
                mineral.setLuster(rs.getString("luster"));
                mineral.setHardness(rs.getString("hardness"));
                mineral.setSpecificGravity(rs.getString("specific_gravity"));
                mineral.setCleavage(rs.getString("cleavage"));
                mineral.setFracture(rs.getString("fracture"));
                mineral.setGenesis(rs.getString("genesis"));
                mineral.setApplication(rs.getString("application"));
                mineral.setAdditionalProperties(rs.getString("additional_properties"));
                mineral.setInterestingFacts(rs.getString("interesting_facts"));
                mineral.setValueCategory(rs.getString("value_category"));
                mineral.setImageUrl(rs.getString("image_url")); 
                
                // Получаем месторождения
                String locations = getLocationsForMineral(conn, mineral.getId());
                mineral.setLocation(locations);
                
                minerals.add(mineral);
                
            } catch (SQLException e) {
                System.out.println("⚠️ Ошибка чтения строки: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Загружено " + minerals.size() + " минералов из БД");
        
    } catch (SQLException e) {
        System.out.println("❌ Ошибка загрузки минералов: " + e.getMessage());
        e.printStackTrace();
    }
    
    return minerals;
}
    /**
     * Получить список месторождений для минерала
     */
    private static String getLocationsForMineral(Connection conn, int mineralId) throws SQLException {
        StringBuilder locations = new StringBuilder();
        
        // ВАЖНО: таблица называется minerallocalities (без подчеркивания)
        String sql = "SELECT l.name FROM localities l " +
                    "JOIN minerallocalities ml ON l.id = ml.locality_id " +  // ← БЕЗ ПОДЧЕРКИВАНИЯ
                    "WHERE ml.mineral_id = ? " +
                    "ORDER BY l.name";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mineralId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                if (locations.length() > 0) {
                    locations.append(", ");
                }
                locations.append(rs.getString("name"));
            }
        }
        
        return locations.toString();
    }
    
    /**
     * Проверить, пустая ли база данных
     */
    public static boolean isDatabaseEmpty() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM minerals");
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
            return true;
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка проверки БД: " + e.getMessage());
            return true; // если ошибка - считаем пустой
        }
    }
    
    /**
     * Сохранить все минералы в базу данных
     */
    public static void saveAllMineralsToDatabase(List<Mineral> minerals) {
        if (minerals == null || minerals.isEmpty()) {
            System.out.println("⚠️ Нет минералов для сохранения");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false); // начинаем транзакцию
            
            // Включаем внешние ключи
            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Очищаем таблицы (в правильном порядке из-за внешних ключей)
            stmt.execute("DELETE FROM minerallocalities");  // ← БЕЗ ПОДЧЕРКИВАНИЯ
            stmt.execute("DELETE FROM minerals");
            stmt.execute("DELETE FROM localities");
            
            System.out.println("🗑️ Очищены старые данные");
            
            int savedCount = 0;
            for (Mineral mineral : minerals) {
                // Сохраняем минерал с его месторождениями
                saveMineralToDatabase(conn, mineral);
                savedCount++;
                
                if (savedCount % 10 == 0) {
                    System.out.println("💾 Сохранено " + savedCount + " минералов...");
                }
            }
            
            conn.commit(); // завершаем транзакцию
            System.out.println("✅ Всего сохранено: " + savedCount + " минералов");
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сохранения всех минералов: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Сохранить один минерал (внутренний метод для транзакции)
     */
    private static void saveMineralToDatabase(Connection conn, Mineral mineral) throws SQLException {
        // 1. Сохраняем минерал - ВАЖНО: колонка называется 'class'
        String insertMineral = "INSERT INTO minerals (name, formula, class, color, " +  // ← class
                              "streak_color, luster, hardness, specific_gravity, cleavage, " +
                              "fracture, genesis, application, additional_properties, " +
                              "interesting_facts, value_category, image_url) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmtMin = conn.prepareStatement(insertMineral, Statement.RETURN_GENERATED_KEYS);
        pstmtMin.setString(1, mineral.getName());
        pstmtMin.setString(2, mineral.getFormula());
        pstmtMin.setString(3, mineral.getMineralClass());  // ← сохраняем в колонку 'class'
        pstmtMin.setString(4, mineral.getColor());
        pstmtMin.setString(5, mineral.getStreakColor());
        pstmtMin.setString(6, mineral.getLuster());
        pstmtMin.setString(7, mineral.getHardness());
        pstmtMin.setString(8, mineral.getSpecificGravity());
        pstmtMin.setString(9, mineral.getCleavage());
        pstmtMin.setString(10, mineral.getFracture());
        pstmtMin.setString(11, mineral.getGenesis());
        pstmtMin.setString(12, mineral.getApplication());
        pstmtMin.setString(13, mineral.getAdditionalProperties());
        pstmtMin.setString(14, mineral.getInterestingFacts());
        pstmtMin.setString(15, mineral.getValueCategory());
        pstmtMin.setString(16, mineral.getImageUrl());
        
        pstmtMin.executeUpdate();
        
        // Получаем ID нового минерала
        ResultSet rs = pstmtMin.getGeneratedKeys();
        int mineralId = -1;
        if (rs.next()) {
            mineralId = rs.getInt(1);
        }
        pstmtMin.close();
        
        // 2. Сохраняем месторождения минерала
        if (mineralId != -1 && mineral.getLocation() != null && !mineral.getLocation().isEmpty()) {
            String[] locationArray = mineral.getLocation().split(",");
            
            for (String loc : locationArray) {
                String locationName = loc.trim();
                if (!locationName.isEmpty()) {
                    // Находим или создаем месторождение
                    int localityId = findOrCreateLocality(conn, locationName);
                    
                    // Создаем связь между минералом и месторождением
                    if (localityId != -1) {
                        // ВАЖНО: таблица называется minerallocalities (без подчеркивания)
                        String insertLink = "INSERT OR IGNORE INTO minerallocalities (mineral_id, locality_id) VALUES (?, ?)";
                        PreparedStatement pstmtLink = conn.prepareStatement(insertLink);
                        pstmtLink.setInt(1, mineralId);
                        pstmtLink.setInt(2, localityId);
                        pstmtLink.executeUpdate();
                        pstmtLink.close();
                    }
                }
            }
        }
    }
    
    /**
     * Найти или создать месторождение
     */
    private static int findOrCreateLocality(Connection conn, String locationName) throws SQLException {
        // Сначала пробуем найти существующее месторождение
        String findSql = "SELECT id FROM localities WHERE name = ?";
        PreparedStatement findStmt = conn.prepareStatement(findSql);
        findStmt.setString(1, locationName);
        ResultSet rs = findStmt.executeQuery();
        
        if (rs.next()) {
            int id = rs.getInt("id");
            findStmt.close();
            return id;
        }
        findStmt.close();
        
        // Если не нашли - создаем новое
        String insertSql = "INSERT INTO localities (name) VALUES (?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, locationName);
        insertStmt.executeUpdate();
        
        ResultSet newRs = insertStmt.getGeneratedKeys();
        if (newRs.next()) {
            int id = newRs.getInt(1);
            insertStmt.close();
            return id;
        }
        
        insertStmt.close();
        return -1;
    }
    
    /**
     * Добавить новый минерал через веб-форму
     */
    public static int addMineralToDatabase(Mineral mineral) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            
            // Включаем внешние ключи
            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Сохраняем минерал
            saveMineralToDatabase(conn, mineral);
            
            // Получаем ID последнего добавленного минерала
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid() as id");
            int mineralId = -1;
            if (rs.next()) {
                mineralId = rs.getInt("id");
            }
            
            conn.commit();
            
            if (mineralId != -1) {
                System.out.println("✅ Добавлен минерал: " + mineral.getName() + " (ID: " + mineralId + ")");
            }
            
            return mineralId;
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка добавления минерала: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Удалить минерал по ID
     */
    public static boolean deleteMineral(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM minerals WHERE id = ?")) {
            
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            
            // Благодаря ON DELETE CASCADE, связи удалятся автоматически
            // из таблицы minerallocalities
            
            if (rows > 0) {
                System.out.println("🗑️ Удален минерал ID=" + id);
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка удаления минерала: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Вывести статистику базы данных
     */
    public static void printDatabaseStats() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Включаем внешние ключи
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Количество минералов
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) as count FROM minerals");
            if (rs1.next()) {
                System.out.println("📊 Минералов: " + rs1.getInt("count"));
            }
            
            // Количество месторождений
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) as count FROM localities");
            if (rs2.next()) {
                System.out.println("📍 Месторождений: " + rs2.getInt("count"));
            }
            
            // Количество связей - ВАЖНО: таблица minerallocalities (без подчеркивания)
            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) as count FROM minerallocalities");
            if (rs3.next()) {
                System.out.println("🔗 Связей минерал-месторождение: " + rs3.getInt("count"));
            }
            
            // Статистика по категориям ценности
            ResultSet rs4 = stmt.executeQuery(
                "SELECT value_category, COUNT(*) as count " +
                "FROM minerals " +
                "WHERE value_category IS NOT NULL AND value_category != '' " +
                "GROUP BY value_category " +
                "ORDER BY count DESC");
            
            System.out.println("💰 Распределение по категориям:");
            boolean hasCategories = false;
            while (rs4.next()) {
                String category = rs4.getString("value_category");
                if (category == null || category.trim().isEmpty()) {
                    category = "(пусто)";
                }
                System.out.println("  " + category + ": " + rs4.getInt("count"));
                hasCategories = true;
            }
            if (!hasCategories) {
                System.out.println("  Категории не указаны");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения статистики: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Получить список всех месторождений
     */
    public static List<String> getAllLocalities() {
        List<String> localities = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM localities ORDER BY name")) {
            
            while (rs.next()) {
                localities.add(rs.getString("name"));
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения месторождений: " + e.getMessage());
        }
        
        return localities;
    }
    
    /**
     * Получить список уникальных категорий ценности
     */
    public static List<String> getAllValueCategories() {
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT DISTINCT value_category FROM minerals " +
                "WHERE value_category IS NOT NULL AND value_category != '' " +
                "ORDER BY value_category")) {
            
            while (rs.next()) {
                categories.add(rs.getString("value_category"));
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения категорий: " + e.getMessage());
        }
        
        return categories;
    }
    
    /**
     * Тестовое подключение к базе
     */
    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("✅ Подключение к SQLite успешно");
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Не удалось подключиться к SQLite: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Сбросить базу данных (удалить все данные)
     * Использовать осторожно!
     */
    public static void resetDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Включаем внешние ключи
            stmt.execute("PRAGMA foreign_keys = ON");
            
            stmt.execute("DELETE FROM minerallocalities");  // ← БЕЗ ПОДЧЕРКИВАНИЯ
            stmt.execute("DELETE FROM minerals");
            stmt.execute("DELETE FROM localities");
            
            // Сбрасываем автоинкремент
            stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('minerals', 'localities')");
            
            System.out.println("♻️ База данных полностью очищена");
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сброса БД: " + e.getMessage());
        }
    }
    
    /**
     * Проверить структуру таблиц (для отладки)
     */
    public static void checkTableStructure() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("\n🔍 Проверка структуры таблиц:");
            
            // minerals
            System.out.println("\nТаблица 'minerals':");
            ResultSet rs1 = stmt.executeQuery("PRAGMA table_info(minerals)");
            while (rs1.next()) {
                System.out.println("  " + rs1.getInt("cid") + ". " + 
                                 rs1.getString("name") + " (" + 
                                 rs1.getString("type") + ")");
            }
            
            // localities
            System.out.println("\nТаблица 'localities':");
            ResultSet rs2 = stmt.executeQuery("PRAGMA table_info(localities)");
            while (rs2.next()) {
                System.out.println("  " + rs2.getInt("cid") + ". " + 
                                 rs2.getString("name") + " (" + 
                                 rs2.getString("type") + ")");
            }
            
            // minerallocalities
            System.out.println("\nТаблица 'minerallocalities':");
            ResultSet rs3 = stmt.executeQuery("PRAGMA table_info(minerallocalities)");
            while (rs3.next()) {
                System.out.println("  " + rs3.getInt("cid") + ". " + 
                                 rs3.getString("name") + " (" + 
                                 rs3.getString("type") + ")");
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Ошибка проверки структуры: " + e.getMessage());
        }
    }
}