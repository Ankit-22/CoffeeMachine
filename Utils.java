import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Utils {

    // Utility to extract ingredient details from generic json object
    public static List<Ingredient> createIngredients(Map<String, Object> ingredientData) {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredientData.forEach((name, amount) -> {
            Ingredient ingredient = new Ingredient();
            ingredient.setName(name);
            ingredient.setAmount((Integer) amount);
            ingredients.add(ingredient);
        });
        return ingredients;
    }

    // Utility to extract beverage details from generic json object
    public static List<Beverage> createBeverages(Map<String, Object> beverageData) {
        List<Beverage> beverages = new ArrayList<>();
        beverageData.forEach((name, ingredients) -> {
            Beverage beverage = new Beverage();
            beverage.setName(name);
            beverage.setIngredients(createIngredients((Map<String, Object>) ingredients));
            beverages.add(beverage);
        });
        return beverages;
    }
}
