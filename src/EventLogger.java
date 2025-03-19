import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EventLogger {
    private SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss.SSS");;
    private List<String> logs = Collections.synchronizedList(new ArrayList<>());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int count;

    public EventLogger(){
        try {
            FileWriter writer = new FileWriter("docs/event_logs.txt", false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        scheduler.scheduleAtFixedRate(this::flush, 5, 2, TimeUnit.SECONDS);
    }

    // Event log: [time, entity, event code, ...additional data]
    public synchronized void logEvent(EventCode eventCode, Object entity, String additionalData) {
        count++;
        String log = "Event log: [" + timestamp.format(new Date()) + ", " + entity + ", " + eventCode + ", "  + additionalData + "]";
        logs.add(log);
    }

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

    public void closeLogger() {
        flush();
        scheduler.shutdown();
    }

}