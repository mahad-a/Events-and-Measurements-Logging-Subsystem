import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is a metric analysis that reads the event text file logs and prints:
 * 1. Response time for each producer/consumer: (E.g., how long between the time the agent
 * is notified and the time they finish placing ingredients, or between the time a chef notices
 * the missing ingredients are available and actually produces the roll.)
 * 2. Throughput (rolls per unit time): (E.g., how many rolls were completed over the total
 * execution time.)
 * 3. Utilization: (E.g., ratio of busy vs. waiting time for each thread.)
 */
public class Metrics {
    private static final String logFile = "docs/event_logs.txt";
    private Map<String, List<Long>> agentStartTimes, agentEndTimes, chefStartTimes, chefEndTimes;

    private int agentBusyCount = 0;
    private int agentWaitCount = 0;

    /**
     * Metrics constructor
     */
    public Metrics() {
    }

    /**
     * Duration of the Restaurant program
     * @return the duration of the execution
     */
    public static long duration(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        long firstTimestamp = Long.MAX_VALUE; // maximum long value
        long lastTimestamp = Long.MIN_VALUE; // minimum long value

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Event log:")) continue; // ensure its a log

                String[] parts = line.split("[\\[\\],]+");
                String timeStr = parts[1].trim();
                long timestamp = sdf.parse(timeStr).getTime();

                // get the first time stamp and last time stamp by checking the max and min values
                firstTimestamp = Math.min(firstTimestamp, timestamp);
                lastTimestamp = Math.max(lastTimestamp, timestamp);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        // the difference between last and first is duration
        return (lastTimestamp - firstTimestamp); // return the duration
    }

    /**
     * Analyzing the event logs txt and parsing them into hashmaps
     */
    public void analyzeLogs(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        // maps to track the start and end times for agent and chef threads
        agentStartTimes = new HashMap<>();
        agentEndTimes = new HashMap<>();

        chefStartTimes = new HashMap<>();
        chefEndTimes = new HashMap<>();


        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Event log:")) continue;

                String[] parts = line.split("[\\[\\],]+");

                String timeStr = parts[1].trim();
                String entity = parts[2].trim();
                String eventCodeStr = parts[3].trim();

                EventCode eventCode = EventCode.valueOf(eventCodeStr);

                long timestamp = sdf.parse(timeStr).getTime(); // convert timestamp to long

                // agent starts work once it selects its ingredients
                if (entity.equals("Agent") && eventCode == EventCode.SELECTED_INGREDIENTS) {
                    agentStartTimes.putIfAbsent(entity, new ArrayList<>());
                    agentStartTimes.get(entity).add(timestamp);
                    // agent ends work once it places its ingredients on the counter
                } else if (entity.startsWith("Counter") && eventCode == EventCode.PLACED_INGREDIENTS) {
                    agentEndTimes.putIfAbsent("Agent", new ArrayList<>());
                    agentEndTimes.get("Agent").add(timestamp);
                }

                // chef starts work when in waiting state for when correct ingredients come to table
                if (entity.startsWith("Chef-") && eventCode == EventCode.WAITING_FOR_CORRECT_INGREDIENTS) {
                    chefStartTimes.putIfAbsent(entity, new ArrayList<>());
                    chefStartTimes.get(entity).add(timestamp);
                    // chef ends work once it makes the sushi roll
                } else if (entity.startsWith("Counter") && eventCode == EventCode.ROLL_MADE) {
                    String chef = "Chef-" + parts[4].trim().split("=")[1].split(";")[0];
                    chefEndTimes.putIfAbsent(chef, new ArrayList<>());
                    chefEndTimes.get(chef).add(timestamp);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate the average response times of the threads (agent and chef)
     */
    public void responseTimes() {
        analyzeLogs();

        System.out.println("--------------------------------------------------------------");
        System.out.println("\nResponse times for each producer/consumer (in milliseconds):");

        // search through map to get the start and end times for the agent thread
        if (agentStartTimes.containsKey("Agent") && agentEndTimes.containsKey("Agent")) {
            List<Long> starts = agentStartTimes.get("Agent");
            List<Long> ends = agentEndTimes.get("Agent");

            // only get response time for start and end that are equal in size
            int foundTimes = Math.min(starts.size(), ends.size());
            long responseTime = 0;
            for (int i = 0; i < foundTimes; i++) { // loop through getting a total time
                 responseTime += ends.get(i) - starts.get(i);
            }
            System.out.println("Average response time for Agent = " + (responseTime / foundTimes) + " ms");
        }

        // search through map to get chef thread start and end times
        for (String chef : chefStartTimes.keySet()) {
            if (chefEndTimes.containsKey(chef)) {
                List<Long> starts = chefStartTimes.get(chef);
                List<Long> ends = chefEndTimes.get(chef);
                // same concept as agent thread
                int foundTimes = Math.min(starts.size(), ends.size());
                long responseTime = 0;
                for (int i = 0; i < foundTimes; i++) {
                    responseTime += ends.get(i) - starts.get(i);
                }
                System.out.println("Average response time for " + chef + " = " + (responseTime / foundTimes) + " ms");
            }
        }
    }

    /**
     * Calculate the throughput to determine how many rolls were completed over the total execution time
     */
    public void throughput(){
        System.out.println("--------------------------------------------------------------");
        System.out.println("\nThroughput Calculation: ");

        // get the duration of the execution
        double duration = ((double) duration() / 1000);
        System.out.println("Duration of execution: " + duration + " seconds");
        double rollsPerUnit = 20.0 / duration; // static value of 20 sushi rolls made
        // can easily subsititue for getRollsMade() method to scale it
        System.out.println("Throughput (rolls per unit time): " + String.format("%.2f", rollsPerUnit) + " rolls per sec");
    }

    /**
     * Calculate the utilization, otherwise the ratio between busy time and waiting time for each thread
     */
    public void utilization() {
        analyzeLogs();
        System.out.println("--------------------------------------------------------------");
        System.out.println("\nUtilization Calculation:");

        // maps to track counters for utilization
        Map<String, Integer> chefBusyCount = new HashMap<>();
        Map<String, Integer> chefWaitCount = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Event log:")) continue;

                String[] parts = line.split("[\\[\\],]+");

                String entity = parts[2].trim();
                String eventCodeStr = parts[3].trim();
                EventCode eventCode = EventCode.valueOf(eventCodeStr);

                // suggestion from TA Sana
                // have a counter that counts for each time threads enter busy state and enter wait state, then use formula

                // agent thread counter
                if (entity.equals("Agent")) {
                    if (eventCode == EventCode.WAITING_FOR_EMPTY_COUNTER) {
                        agentWaitCount++;
                    }
                }

                if (entity.startsWith("Counter")) {
                    if (eventCode == EventCode.COUNTER_IS_EMPTY) {
                        agentBusyCount++;
                    }
                }

                // chef thread counters
                if (entity.startsWith("Chef-") && eventCode == EventCode.WAITING_FOR_CORRECT_INGREDIENTS) {
                    chefWaitCount.putIfAbsent(entity, 0);
                    String chefName = "Chef-" + parts[4].trim().split("[=;]")[1].trim();
                    chefWaitCount.put(chefName, chefWaitCount.get(chefName) + 1);
                }
                if (entity.startsWith("Counter") && eventCode == EventCode.ROLL_MADE) {
                    String chefName = "Chef-" + parts[4].trim().split("=")[1].split(";")[0];
                    chefBusyCount.putIfAbsent(chefName, 0);
                    chefBusyCount.put(chefName, chefBusyCount.get(chefName) + 1);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get utilization for the agent thread
        double utilization = (double) agentBusyCount / (agentBusyCount + agentWaitCount);
        System.out.println("Agent utilization: " + String.format("%.2f", utilization * 100) + "%");

        // loop through chef threads to get utilization for each thread
        for (String chef : chefWaitCount.keySet()) {
            int busy = chefBusyCount.get(chef);
            int wait = chefWaitCount.get(chef);

            double chefUtilization = (double) busy / (busy + wait);
            System.out.println(chef + " utilization: " + String.format("%.2f", chefUtilization * 100) + "%");
        }
    }

}
