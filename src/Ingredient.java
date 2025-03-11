import java.util.Random;

/**
 * Ingredient enums
 *
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, January 07th, 2025
 */
public enum Ingredient {
    Rice,
    Nori,
    Filling;

    /**
     * Pick a random value of the Ingredient enum.
     * @return a random Ingredient.
     */
    public static Ingredient getRandomIngredient() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}