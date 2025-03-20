import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Metrics {
    private final String logFile = "docs/event_logs.txt";
    private Map<String, List<Long>> agentStartTimes, agentEndTimes, chefStartTimes, chefEndTimes;
    private Map<String, List<Long>> agentWaitingStartTimes, agentWaitingEndTimes, agentBusyStartTimes, agentBusyEndTimes;

    /**
     * Metrics constructor
     */
    public Metrics() {
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

        agentWaitingStartTimes = new HashMap<>();
        agentWaitingEndTimes = new HashMap<>();
        agentBusyStartTimes = new HashMap<>();
        agentBusyEndTimes = new HashMap<>();

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
                } else if (entity.startsWith("Chef-") && eventCode == EventCode.WAITING_FOR_CORRECT_INGREDIENTS) {
                    chefStartTimes.putIfAbsent(entity, new ArrayList<>());
                    chefStartTimes.get(entity).add(timestamp);
                } else if (entity.startsWith("Counter") && eventCode == EventCode.ROLL_MADE) {
                    String chef = "Chef-" + parts[4].trim().split("=")[1].split(";")[0];
                    chefEndTimes.putIfAbsent(chef, new ArrayList<>());
                    chefEndTimes.get(chef).add(timestamp);
                }

                if (entity.equals("Agent") && eventCode == EventCode.WAITING_FOR_EMPTY_COUNTER) {
                    agentWaitingStartTimes.putIfAbsent(entity, new ArrayList<>());
                    agentWaitingStartTimes.get(entity).add(timestamp);
                } else if (entity.startsWith("Counter") && eventCode == EventCode.COUNTER_IS_EMPTY) {
                    agentWaitingEndTimes.putIfAbsent("Agent", new ArrayList<>());
                    agentWaitingEndTimes.get("Agent").add(timestamp);

                    agentBusyStartTimes.putIfAbsent("Agent", new ArrayList<>());
                    agentBusyStartTimes.get("Agent").add(timestamp);
                } else if (entity.startsWith("Counter") && eventCode == EventCode.PLACED_INGREDIENTS) {
                    agentBusyEndTimes.putIfAbsent("Agent", new ArrayList<>());
                    agentBusyEndTimes.get("Agent").add(timestamp);
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

        System.out.println("\nResponse Times (in milliseconds):");

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

        long rollsPerUnit = (lastTimestamp - firstTimestamp) / 20;
        System.out.println("Throughput (rolls per unit time): " + rollsPerUnit + " ms per sushi roll made");
    }

    public void utilization() {
        System.out.println("\nUtilization Calculation:");

        if (!agentBusyStartTimes.containsKey("Agent") || !agentBusyEndTimes.containsKey("Agent") ||
                !agentWaitingStartTimes.containsKey("Agent") || !agentWaitingEndTimes.containsKey("Agent")) {
            System.out.println("Insufficient data for Agent utilization.");
            return;
        }

        List<Long> busyStart = agentBusyStartTimes.get("Agent");
        List<Long> busyEnd = agentBusyEndTimes.get("Agent");
        List<Long> waitStart = agentWaitingStartTimes.get("Agent");
        List<Long> waitEnd = agentWaitingEndTimes.get("Agent");

        System.out.println("DEBUG: Agent Busy Start Times: " + agentBusyStartTimes);
        System.out.println("DEBUG: Agent Busy End Times: " + agentBusyEndTimes);
        System.out.println("DEBUG: Agent Waiting Start Times: " + agentWaitingStartTimes);
        System.out.println("DEBUG: Agent Waiting End Times: " + agentWaitingEndTimes);


        long totalBusyTime = 0;
        long totalWaitTime = 0;

        int busyEvents = Math.min(busyStart.size(), busyEnd.size());
        for (int i = 0; i < busyEvents; i++) {
            if (busyEnd.get(i) > busyStart.get(i)) {
                totalBusyTime += busyEnd.get(i) - busyStart.get(i);
            }
        }

        int waitEvents = Math.min(waitStart.size(), waitEnd.size());
        for (int i = 0; i < waitEvents; i++) {
            if (waitEnd.get(i) > waitStart.get(i)) {
                totalWaitTime += waitEnd.get(i) - waitStart.get(i);
            }
        }

        double utilization = (double) totalBusyTime / (totalBusyTime + totalWaitTime);
        System.out.printf("Agent Utilization: %.2f%%\n", utilization * 100);
    }


}
