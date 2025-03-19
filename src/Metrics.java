import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Metrics {

    public Metrics() {
    }

    public void responseTimes(String logFile){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            ArrayList<Event> events = new ArrayList<>();
            while ((line = reader.readLine()) != null){
                String[] parts = line.split(",");

                String threadName = parts[1];
                String eventType = parts[2].trim();


                Event event = new Event(EventCode.valueOf(eventType), threadName, "");
                events.add(event);
            }

            Map<String, Long> responseTimes = new HashMap<>();
            Map<String, Long> startTimes = new HashMap<>();

            for (Event event : events) {
                long eventTimestamp = event.dateToLong(event.getTimestamp().format(new Date()));
                String eventName = event.getEntity().toString();
//                System.out.println("DEBUG: Processing Event: " + event + "\n FOUND CODE = " + event.getEventCode() + "\n MUST BE = " + EventCode.SELECTED_INGREDIENTS);
                if (event.getEventCode() == EventCode.SELECTED_INGREDIENTS) {
                    startTimes.put(eventName, eventTimestamp);
                }
                System.out.println("DEBUG: start times = " + startTimes);
                if (event.getEventCode() == EventCode.WAITING_FOR_CORRECT_INGREDIENTS) {
                    Long startTime = startTimes.get(eventName);
                    System.out.println("DEBUG: EVENT NAME = " + eventName );
                    System.out.println("DEBUG: START TIME = " + startTime);
                    if (startTime != null) {
                        long responseTime = eventTimestamp - startTime;
                        System.out.println("DEBUG: RESPONSE = " + responseTime);
                        responseTimes.put(eventName, responseTime);
                        System.out.println("DEBUG: Response Time for " + eventName + ": " + responseTime);
                    }
                }
            }

            if (responseTimes.isEmpty()) {
                System.out.println("No responses");
            } else {
                System.out.println("Response Times (in milliseconds):");
                for (Map.Entry<String, Long> entry : responseTimes.entrySet()) {
                    System.out.println("Entity: " + entry.getKey() + ", Response Time: " + entry.getValue() + " ms");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
