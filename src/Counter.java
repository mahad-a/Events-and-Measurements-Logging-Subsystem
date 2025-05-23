import java.io.IOException;

/**
 * This class is for the counter in this roll making system.
 * The counter serves as a common place where ingredients are placed by the Agent and taken by the Chef.
 * The counter accepts ingredients from the Agent and notifies all Chefs that they are available.
 * The counter determines when each Chef is allowed to take the ingredients, based on what their missing ingredients are.
 * The counter lets the right Chef make and serve a roll, then notifies the Agent that the counter is empty.
 * The counter will allow ingredients to be placed and taken until 20 rolls are served.
 *
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, January 07th, 2025
 */
public class Counter {

    private final int SIZE = 2;                                 //Capacity of table
    private Ingredient[] ingredients = new Ingredient[SIZE];    //List of ingredients on the table
    private boolean tableFull = false;                          //True if there is at least 1 ingredient on the table
    private int rollsMade = 0;                             //Running total of rolls made
    private EventLogger logger;

    public Counter(EventLogger logger) {
        this.logger = logger;
    }

    /**
     * Method used to allow an Agent to place ingredients on the table when table is empty
     *
     * @param ingredient1 First ingredient to be placed by Agent
     * @param ingredient2 Second ingredient to be placed by Agent
     */
    public synchronized void addIngredients(Ingredient ingredient1, Ingredient ingredient2) {
        while (tableFull) { //Makes agent wait until table is empty to place ingredients
            if (this.rollsMade == 20) { //Will exit if no more rolls are required to be made
                return;
            }
            // log that agent thread is waiting for a empty counter
            logger.logEvent(EventCode.WAITING_FOR_EMPTY_COUNTER, Thread.currentThread().getName(), (ingredient1 + ", " + ingredient2));
            try {
                wait(); //Tells agent to wait until notified
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.rollsMade == 20) { //Will exit if no more rolls are required to be made
            return;
        }

        //Ingredients are placed on table
        ingredients[0] = ingredient1;
        ingredients[1] = ingredient2;

        tableFull = true;   //Table is now full

        // log that agent added to counter
        logger.logEvent(EventCode.PLACED_INGREDIENTS, "Counter", (ingredient1 + " & " + ingredient2));
        System.out.println("[" + Thread.currentThread().getName() + "] " + ingredient1.toString() + " and " + ingredient2.toString() + " placed on the table.");

        notifyAll();    //Notify all Chefs that table is full
    }

    /**
     * Method used by Chefs to obtain ingredients on table and make and serve a roll
     *
     * @param ingredient The ingredient the Chef has an infinite supply of (Used to determine if Chef is eligible to take the ingredients on the table)
     */
    public synchronized void getIngredients(Ingredient ingredient) {
        while (!tableFull || ingredientsContains(ingredient)) { //Makes Chef wait until the table is full and until the two required ingredients from the Chef is available
            if (this.rollsMade == 20) { //If 20 rolls have been made, do not make another
                return;
            }
            try {
                // log that chef thread is waiting for the right ingredient
                logger.logEvent(EventCode.WAITING_FOR_CORRECT_INGREDIENTS, Thread.currentThread().getName(), ("Has=" + ingredient));
                wait(); //Make the Chef wait until notified that new ingredients are available
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[" + Thread.currentThread().getName() + "] Roll made and served.");
        System.out.println("[" + Thread.currentThread().getName() + "] Waiting for remaining ingredients...");
        this.rollsMade++;  //Increase running total of rolls made

        // log that roll is finished and chef now back to waiting
        logger.logEvent(EventCode.ROLL_MADE, "Counter", ("ChefHas=" + ingredient + ";rollsMade=" + this.rollsMade));
        logger.logEvent(EventCode.WAITING_FOR_CORRECT_INGREDIENTS, Thread.currentThread().getName(), ("Has=" + ingredient));

        System.out.println("[Counter] Rolls made: " + this.rollsMade);
        System.out.println("--------------------------------------------------------------");
        //Clear ingredients and set table to empty
        ingredients[0] = null;
        ingredients[1] = null;
        tableFull = false;

        // log that counter is now empty
        logger.logEvent(EventCode.COUNTER_IS_EMPTY, "Counter", ("Ready to place ingredients"));

        notifyAll();    //Notify Chefs and Agent that ingredients have changed
    }

    /**
     * Method used to check if the ingredient given is one of the two ingredients on the table
     *
     * @param ingredient Ingredient from Chef (used to check if Chef can accept the ingredients on the table)
     * @return True if ingredient is on the table, false otherwise
     */
    private boolean ingredientsContains(Ingredient ingredient) {
        //If there are no ingredients on the table, or one of the ingredients on the table is the same as the ingredient given from the Chef, return True; false otherwise
        return (this.ingredients[0] == null || this.ingredients[1] == null || (this.ingredients[0] == ingredient || this.ingredients[1] == ingredient));
    }

    /**
     * Getter method for getRollsMade.
     *
     * @return getRollsMade
     */
    public int getRollsMade() {
        return this.rollsMade;
    }
}