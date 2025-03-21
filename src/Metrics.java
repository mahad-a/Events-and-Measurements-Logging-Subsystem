import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
        long firstTimestamp = Long.MAX_VALUE;
        long lastTimestamp = Long.MIN_VALUE;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Event log:")) continue;

                String[] parts = line.split("[\\[\\],]+");
                if (parts.length < 5) continue;

                String timeStr = parts[1].trim();
                long timestamp = sdf.parse(timeStr).getTime();

                firstTimestamp = Math.min(firstTimestamp, timestamp);
                lastTimestamp = Math.max(lastTimestamp, timestamp);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return (lastTimestamp - firstTimestamp);
    }

    /**
     * Analyzing the event logs txt and parsing them into hashmaps
     */
    public void analyzeLogs(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        agentStartTimes = new HashMap<>();
        agentEndTimes = new HashMap<>();

        chefStartTimes = new HashMap<>();
        chefEndTimes = new HashMap<>();


        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Event log:")) continue;

                String[] parts = line.split("[\\[\\],]+");
                if (parts.length < 5) continue;

                String timeStr = parts[1].trim();
                String entity = parts[2].trim();
                String eventCodeStr = parts[3].trim();

                EventCode eventCode = EventCode.valueOf(eventCodeStr);

                long timestamp = sdf.parse(timeStr).getTime();

                if (entity.equals("Agent") && eventCode == EventCode.SELECTED_INGREDIENTS) {
                    agentStartTimes.putIfAbsent(entity, new ArrayList<>());
                    agentStartTimes.get(entity).add(timestamp);
                } else if (entity.startsWith("Counter") && eventCode == EventCode.PLACED_INGREDIENTS) {
                    agentEndTimes.putIfAbsent("Agent", new ArrayList<>());
                    agentEndTimes.get("Agent").add(timestamp);
                }

                if (entity.startsWith("Chef-") && eventCode == EventCode.WAITING_FOR_CORRECT_INGREDIENTS) {
                    chefStartTimes.putIfAbsent(entity, new ArrayList<>());
                    chefStartTimes.get(entity).add(timestamp);
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

        if (agentStartTimes.containsKey("Agent") && agentEndTimes.containsKey("Agent")) {
            List<Long> starts = agentStartTimes.get("Agent");
            List<Long> ends = agentEndTimes.get("Agent");

            int foundTimes = Math.min(starts.size(), ends.size());
            long responseTime = 0;
            for (int i = 0; i < foundTimes; i++) {
                 responseTime += ends.get(i) - starts.get(i);
            }
            System.out.println("Average response time for Agent = " + (responseTime / foundTimes) + " ms");
        }

        for (String chef : chefStartTimes.keySet()) {
            if (chefEndTimes.containsKey(chef)) {
                List<Long> starts = chefStartTimes.get(chef);
                List<Long> ends = chefEndTimes.get(chef);
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

        double duration = ((double) duration() / 1000);
        System.out.println("Duration of execution: " + duration + " seconds");
        double rollsPerUnit = 20.0 / duration;
        System.out.println("Throughput (rolls per unit time): " + String.format("%.2f", rollsPerUnit) + " rolls per sec");
    }

    /**
     * Calculate the utilization, otherwise the ratio between busy time and waiting time for each thread
     */
    public void utilization() {
        analyzeLogs();
        System.out.println("--------------------------------------------------------------");
        System.out.println("\nUtilization Calculation:");


        Map<String, Integer> chefBusyCount = new HashMap<>();
        Map<String, Integer> chefWaitCount = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Event log:")) continue;

                String[] parts = line.split("[\\[\\],]+");
                if (parts.length < 5) continue;

                String entity = parts[2].trim();
                String eventCodeStr = parts[3].trim();
                EventCode eventCode = EventCode.valueOf(eventCodeStr);

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

        double utilization = (double) agentBusyCount / (agentBusyCount + agentWaitCount);
        System.out.println("Agent utilization: " + String.format("%.2f", utilization * 100) + "%");

        for (String chef : chefWaitCount.keySet()) {
            int busy = chefBusyCount.get(chef);
            int wait = chefWaitCount.get(chef);

            double chefUtilization = (double) busy / (busy + wait);
            System.out.println(chef + " utilization: " + String.format("%.2f", chefUtilization * 100) + "%");
        }
    }

}
