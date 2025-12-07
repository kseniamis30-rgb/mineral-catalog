import java.io.Serializable;
import java.util.Objects;

/**
 * Класс-сущность: представляет минерал с полной характеристикой
 */
public class Mineral implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String formula;
    private String mineralClass; // поле называется mineralClass
    private String color;
    private String streakColor;
    private String luster;
    private String hardness;
    private String specificGravity;
    private String cleavage;
    private String fracture;
    private String genesis;
    private String application;
    private String additionalProperties;
    private String interestingFacts;
    private String location;
    private String valueCategory;
    private String imageUrl;
   

    /**
     * Основной конструктор с ID
     * Параметр для класса минерала называется mineralClassFromDB (не class!)
     */
    public Mineral(int id, String name, String formula, String mineralClassFromDB, String color,
                   String streakColor, String luster, String hardness, String specificGravity,
                   String cleavage, String fracture, String genesis, String application,
                   String additionalProperties, String interestingFacts, String location, String valueCategory, String imageUrl) {
        this.id = id;
        setName(name);
        setFormula(formula);
        setMineralClass(mineralClassFromDB); // mineralClassFromDB → mineralClass
        setColor(color);
        setStreakColor(streakColor);
        setLuster(luster);
        setHardness(hardness);
        setSpecificGravity(specificGravity);
        setCleavage(cleavage);
        setFracture(fracture);
        setGenesis(genesis);
        setApplication(application);
        setAdditionalProperties(additionalProperties);
        setInterestingFacts(interestingFacts);
        setLocation(location);
        setValueCategory(valueCategory);
        setImageUrl(imageUrl);
    }

    /**
     * Конструктор без ID (для создания новых минералов)
     * ID будет сгенерирован в базе данных
     */
    public Mineral(String name, String formula, String mineralClassFromDB, String color,
                   String streakColor, String luster, String hardness, String specificGravity,
                   String cleavage, String fracture, String genesis, String application,
                   String additionalProperties, String interestingFacts, String location, String valueCategory, String imageUrl) {
        this.id = -1; // Временный ID, будет заменен при сохранении в БД
        setName(name);
        setFormula(formula);
        setMineralClass(mineralClassFromDB); // mineralClassFromDB → mineralClass
        setColor(color);
        setStreakColor(streakColor);
        setLuster(luster);
        setHardness(hardness);
        setSpecificGravity(specificGravity);
        setCleavage(cleavage);
        setFracture(fracture);
        setGenesis(genesis);
        setApplication(application);
        setAdditionalProperties(additionalProperties);
        setInterestingFacts(interestingFacts);
        setLocation(location);
        setValueCategory(valueCategory); 
        setImageUrl(imageUrl);
    }

    /**
     * Пустой конструктор (для сериализации/десериализации)
     */
    public Mineral() {
        this.id = -1;
        this.name = "";
        this.formula = "";
        this.mineralClass = "";
        this.color = "";
        this.streakColor = "";
        this.luster = "";
        this.hardness = "";
        this.specificGravity = "";
        this.cleavage = "";
        this.fracture = "";
        this.genesis = "";
        this.application = "";
        this.additionalProperties = "";
        this.interestingFacts = "";
        this.location = "";
        this.valueCategory = "";
        this.imageUrl = "";
    }

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getFormula() { return formula; }
    public String getMineralClass() { return mineralClass; }
    public String getColor() { return color; }
    public String getStreakColor() { return streakColor; }
    public String getLuster() { return luster; }
    public String getHardness() { return hardness; }
    public String getSpecificGravity() { return specificGravity; }
    public String getCleavage() { return cleavage; }
    public String getFracture() { return fracture; }
    public String getGenesis() { return genesis; }
    public String getApplication() { return application; }
    public String getAdditionalProperties() { return additionalProperties; }
    public String getInterestingFacts() { return interestingFacts; }
    public String getLocation() { return location; }
    public String getValueCategory() {return valueCategory; }
    public String getImageUrl() { return imageUrl; }
    

    // Сеттеры
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public void setFormula(String formula) {
        this.formula = formula != null ? formula.trim() : "";
    }

    public void setMineralClass(String mineralClass) {
        this.mineralClass = mineralClass != null ? mineralClass.trim() : "";
    }

    public void setColor(String color) {
        this.color = color != null ? color.trim() : "";
    }

    public void setStreakColor(String streakColor) {
        this.streakColor = streakColor != null ? streakColor.trim() : "";
    }

    public void setLuster(String luster) {
        this.luster = luster != null ? luster.trim() : "";
    }

    public void setHardness(String hardness) {
        this.hardness = hardness != null ? hardness.trim() : "";
    }

    public void setSpecificGravity(String specificGravity) {
        this.specificGravity = specificGravity != null ? specificGravity.trim() : "";
    }

    public void setCleavage(String cleavage) {
        this.cleavage = cleavage != null ? cleavage.trim() : "";
    }

    public void setFracture(String fracture) {
        this.fracture = fracture != null ? fracture.trim() : "";
    }

    public void setGenesis(String genesis) {
        this.genesis = genesis != null ? genesis.trim() : "";
    }

    public void setApplication(String application) {
        this.application = application != null ? application.trim() : "";
    }

    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties != null ? additionalProperties.trim() : "";
    }

    public void setInterestingFacts(String interestingFacts) {
        this.interestingFacts = interestingFacts != null ? interestingFacts.trim() : "";
    }

    public void setLocation(String location) {
        this.location = location != null ? location.trim() : "";
    }

    public void setValueCategory(String valueCategory) {
    this.valueCategory = valueCategory != null ? valueCategory.trim() : "";
    }

        public void setImageUrl(String imageUrl) { // Добавили сеттер для imageUrl
        this.imageUrl = imageUrl != null ? imageUrl.trim() : "";
    }

    
    /**
     * Метод для создания копии с новым ID
     */
    public Mineral withId(int newId) {
    return new Mineral(
        newId, 
        this.name, 
        this.formula, 
        this.mineralClass, 
        this.color, 
        this.streakColor,
        this.luster, 
        this.hardness, 
        this.specificGravity, 
        this.cleavage, 
        this.fracture, 
        this.genesis,
        this.application, 
        this.additionalProperties, 
        this.interestingFacts, 
        this.location, 
        this.valueCategory,
        this.imageUrl 
    );
}
@Override
public String toString() {
    return String.format(
        "====== %s (ID: %d) ======\n" +
        "Формула: %s\n" +
        "Класс: %s\n" +
        "Цвет: %s\n" +
        "Цвет черты: %s\n" +
        "Блеск: %s\n" +
        "Твердость: %s\n" +
        "Удельный вес: %s\n" +
        "Спайность: %s\n" +
        "Излом: %s\n" +
        "Генезис: %s\n" +
        "Применение: %s\n" +
        "Дополнительные свойства: %s\n" +
        "Интересные факты: %s\n" +
        "Месторождение: %s\n" +
        "Категория ценности: %s\n" +
        "URL изображения: %s\n" +
        "===========================\n",
        name, id, formula, mineralClass, color, streakColor, luster, hardness,
        specificGravity, cleavage, fracture, genesis, application,
        additionalProperties, interestingFacts, location, valueCategory,
        imageUrl != null && !imageUrl.isEmpty() ? imageUrl : "не указано"
    );
}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mineral mineral = (Mineral) o;
        return id == mineral.id &&
                Objects.equals(name, mineral.name) &&
                Objects.equals(formula, mineral.formula) &&
                Objects.equals(mineralClass, mineral.mineralClass) &&
                Objects.equals(imageUrl, mineral.imageUrl); // Добавили imageUrl
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, formula, mineralClass, imageUrl); // Добавили imageUrl
    }
}