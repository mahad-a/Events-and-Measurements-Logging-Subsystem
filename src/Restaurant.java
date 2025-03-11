/**
 * Main application class for running a restaurant.
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, January 07th, 2025
 */

public class Restaurant {

    /**
     * Method used to create new Chef threads; keeps consistency of naming conventions between threads
     *
     * @param t     Common table between Chefs and Agent
     * @param i     Ingredient that Chef will have an infinite supply of
     * @return      Created Chef thread
     */
    private static Thread makeNewChef(Counter t, Ingredient i){
        return new Thread(new Chef(t, i), "Chef-" + i.toString());
    }

    /**
     * Method used to run the program. The program creates all threads and starts them
     *
     * @param args
     */
    public static void main (String[] args){

        Thread ChefRice, ChefNori, ChefFilling, agent;  //Threads for each Chef and the Agent
        Counter counter;                                            //Table

        counter = new Counter();                                                //Common Table for all Chefs and Agent
        agent = new Thread(new Agent(counter), "Agent");                //Agent thread created
        ChefRice = makeNewChef(counter, Ingredient.Rice);             //Beans Chef created
        ChefNori = makeNewChef(counter, Ingredient.Nori);             //Water Chef created
        ChefFilling = makeNewChef(counter, Ingredient.Filling);             //Sugar Chef created

        //Start all Chef and Agent threads
        ChefRice.start();
        ChefNori.start();
        ChefFilling.start();
        agent.start();
    }
}
