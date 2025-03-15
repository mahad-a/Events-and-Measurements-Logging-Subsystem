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
    private BufferedWriter writer;
    private List<String> logs = Collections.synchronizedList(new ArrayList<String>());
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public EventLogger(){
        try {
            this.writer = new BufferedWriter(new FileWriter("docs/event_logs.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        scheduler.schedule(this::flush, 3, TimeUnit.SECONDS);
    }

    // Event log: [time, entity, event code, ...additional data]
    public synchronized void logEvent(EventCode eventCode, Object entity, String additionalData) {
        this.timestamp = new SimpleDateFormat("HH:mm:ss.SSS");
        String log = "[" + timestamp.format(new Date()) + "," + eventCode + "," + entity + "," + additionalData + "]\n";
        logs.add(log);
    }

    public synchronized void flush() {
        try {
            if (logs.isEmpty()) return;
            for (String entry : logs) {
                System.out.println("DEBUG: ENTRY = " + entry);
                writer.write(entry + "\n");
            }
            writer.flush();
            logs.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeLogger() {
        flush();
        System.out.println("DEBUG - FLUSHING");
        try {
            System.out.println("DEBUG - CLOSING AND SHUTTING DOWN");
            writer.close();
            scheduler.shutdown();
        } catch (IOException e){
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