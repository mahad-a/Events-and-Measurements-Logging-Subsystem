/**
 * The possible event codes
 */
public enum EventCode {
    SELECTED_INGREDIENTS, // agent selected ingredients
    PLACED_INGREDIENTS, // agent placed ingredients on counter
    ROLL_MADE, // chef finished roll
    WAITING_FOR_CORRECT_INGREDIENTS, // chef waiting for specific ingredient
    WAITING_FOR_EMPTY_COUNTER, // agent waiting for empty counter
    COUNTER_IS_EMPTY, // chef waiting for a full counter
    DONE // thread completed its task
}
