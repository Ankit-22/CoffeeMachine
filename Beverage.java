import java.util.List;

// POJO Class to hold data of a Beverage
public class Beverage {
    private List<Ingredient> ingredients;
    private String name;

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
