/**
 * This class is for the Chefs in this roll making system.
 * The Chef has an infinite supply of one of the three ingredients.
 * The Chef will wait at the table until the other two ingredients are placed, and will then make a roll and serve it.
 * The Chef will repeat this procedure until 20 rolls are made in total between all Chefs.
 *
 *
 * @author Dr. Rami Sabouni,
 * Systems and Computer Engineering,
 * Carleton University
 * @version 1.0, January 07th, 2025
 */
public class Chef implements Runnable {
    private Counter counter;                //The common table between Agent and Chefs
    private Ingredient ingredient;      //The only ingredient each instance of Chef has an infinite supply of (this ingredient is different between all three Chefs)

    /**
     * Constructor for Chef
     *
     * @param t     //The common table between Agent and Chefs
     * @param i     //The ingredient this Chef has an infinite supply of
     */
    public Chef(Counter t, Ingredient i){
        this.counter = t;
        this.ingredient = i;
    }

    /**
     * Method used for each Chef thread when ran
     */
    public void run(){
        System.out.println("[" + Thread.currentThread().getName() + "] Waiting for remaining ingredients...");
        while (this.counter.getRollsMade() != 20){   //Will loop until 20 rolls have been made and served
            this.counter.getIngredients(this.ingredient); //Attempts to obtain the missing ingredients of the Chef (if obtained, roll is made and served)
            // Sleep for between 0 and 2 seconds before calculating n!
            try {
                Thread.sleep((int)(Math.random() * 2000));
            } catch (InterruptedException e) {}
        }

        //All rolls have been made
        System.out.println("[" + Thread.currentThread().getName() + "] 20 rolls made, ending...");
    }
}