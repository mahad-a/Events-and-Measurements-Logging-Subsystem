import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EventLogger {
    private SimpleDateFormat timestamp;
    private EventCode eventCode;
    private Object entity;
    private String additionalData;
    private FileWriter writer;
    private ArrayList<String> logs = new ArrayList<String>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public EventLogger(){
        try {
            this.writer = new FileWriter("docs/event_logs.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        scheduler.scheduleAtFixedRate(this::flush, 2, 2, TimeUnit.SECONDS);
    }

    // Event log: [time, entity, event code, ...additional data]
    public synchronized void logEvent(EventCode eventCode, Object entity, String additionalData) {
        this.timestamp = new SimpleDateFormat("HH:mm:ss.SSS");
        String log = "Event log: [" + timestamp.format(new Date()) + ", " + eventCode + ", " + entity + ", " + additionalData + "]";
        System.out.println("DEBUG: Logging event -> " + log);
        logs.add(log);
        System.out.println("logs size after logging -> " + logs.size());
    }

    public synchronized void flush() {
        if (logs.isEmpty()) return;
        try {
            for (String entry : logs) {
                System.out.println("DEBUG: ENTRY = " + entry);
                writer.write(entry + "\n");
            }
            writer.flush();
            System.out.println("DEBUG: logs size before clearing -> " + logs.size());
            logs.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeLogger() {
        System.out.println("DEBUG - FLUSHING");
        synchronized (logs) {
            flush();
        }

        System.out.println("DEBUG - CLOSING AND SHUTTING DOWN");
        scheduler.shutdown();

        try {
            writer.close(); // Close the file writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SimpleDateFormat getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(SimpleDateFormat timestamp) {
        this.timestamp = timestamp;
    }

    public EventCode getEventCode() {
        return eventCode;
    }

    public void setEventCode(EventCode eventCode) {
        this.eventCode = eventCode;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
}