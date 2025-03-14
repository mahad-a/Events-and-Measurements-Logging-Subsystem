import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class EventLogger {
    private SimpleDateFormat timestamp;
    private EventCode eventCode;
    private Object entity;
    private String additionalData;
    private BufferedWriter writer;
    private ScheduledExecutorService scheduler;

    public EventLogger(){
        try {
            this.writer = new BufferedWriter(new FileWriter("logs.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    // Event log: [time, entity, event code, ...additional data]
    public void logEvent(EventCode eventCode, Object entity, String additionalData) {
        this.timestamp = new SimpleDateFormat("HH:mm:ss.SSS");
        scheduler = Executors.newScheduledThreadPool(1);
        String log = "[" + timestamp.format(new Date()) + "," + eventCode + "," + entity + "," + additionalData + "]\n";
        try {
            writer.write(log);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
    }

    public void closeLogger() throws IOException {
        writer.close();
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