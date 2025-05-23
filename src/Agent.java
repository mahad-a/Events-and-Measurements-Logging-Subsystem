import java.io.IOException;

/**
 * This class is for the Agent in this Sushi making system.
 * The Agent selects two random ingredients and places them on the common table.
 * The Agent will repeat this procedure until 20 sushi rolls are made in total
 *
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, January 07th, 2025
 */
public class Agent implements Runnable {

    private Counter counter;    //The common table between Agent and Chefs
    private EventLogger logger;

    /**
     * Constructor for Agent
     *
     * @param t     The common table between Agent and Chefs
     */
    public Agent(Counter t, EventLogger l){
        this.counter = t;
        this.logger = l;
    }
    
    /**
     * Method used when Agent thread is ran
     */
    public void run(){
        Ingredient ingredient1, ingredient2;
        System.out.println("[" + Thread.currentThread().getName() + "] Waiting to place first ingredients on the counter...");

        while (this.counter.getRollsMade() != 20){   //Will loop until 20 rolls have been made and served

            //Randomly selects two different ingredients
            ingredient1 = Ingredient.getRandomIngredient();
            ingredient2 = Ingredient.getRandomIngredient();
            while (ingredient1 == ingredient2){     //If ingredients are the same, select and new second ingredient
                ingredient2 = Ingredient.getRandomIngredient();
            }

            // log that agent has selected ingredients to place
            logger.logEvent(EventCode.SELECTED_INGREDIENTS, Thread.currentThread().getName(), (ingredient1 + ", " + ingredient2));
            this.counter.addIngredients(ingredient1, ingredient2);    //Places the two selected ingredients on the table

        }
        //All rolls have been made
        logger.logEvent(EventCode.DONE, Thread.currentThread().getName(), ("TotalTime=" + Metrics.duration() + "ms for producing 20 rolls"));
        System.out.println("[" + Thread.currentThread().getName() + "] 20 rolls made, ending...");
    }
}