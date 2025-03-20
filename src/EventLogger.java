import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class EventLogger {
    private List<String> logs = Collections.synchronizedList(new ArrayList<>());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Event logger constructor
     */
    public EventLogger(){
        try {
            FileWriter writer = new FileWriter("docs/event_logs.txt", false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // schedule to clean logs in memory and write to text file every 5 seconds
        scheduler.scheduleAtFixedRate(this::flush, 5, 2, TimeUnit.SECONDS);
    }

    /**
     * Logs the event in memory
     * @param eventCode the event code
     * @param entity the thread responsible for the event
     * @param additionalData any extra information regarding the event
     */
    public synchronized void logEvent(EventCode eventCode, Object entity, String additionalData) {
        // Event log: [time, entity, event code, ...additional data]
        logs.add(new Event(eventCode, entity, additionalData).toString());
    }

    /**
     * Flush the logs from memory and write into logs text file
     */
    public synchronized void flush() {
        synchronized (logs) {
            if (logs.isEmpty()) return;
            try (FileWriter writer = new FileWriter("docs/event_logs.txt", true)) {
                for (String entry : logs) {
                    writer.write(entry + "\n");
                }
                writer.flush();
                logs.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the logger and shuts down the scheduler daemon thread
     */
    public void closeLogger() {
        flush(); // get the last logs in memory
        scheduler.shutdown();
    }

}