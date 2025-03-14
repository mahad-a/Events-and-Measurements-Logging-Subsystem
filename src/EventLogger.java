import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class EventLogger {
    private SimpleDateFormat timestamp;
    private EventCode eventCode;
    private Object entity;
    private String additionalData;
    private BufferedWriter writer;
    private List<String> logs = Collections.synchronizedList(new ArrayList<String>());

    public EventLogger(){
        try {
            this.writer = new BufferedWriter(new FileWriter("docs/event_logs.txt", true));
            this.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Event log: [time, entity, event code, ...additional data]
    public synchronized void logEvent(EventCode eventCode, Object entity, String additionalData) {
        this.timestamp = new SimpleDateFormat("HH:mm:ss.SSS");
        String log = "[" + timestamp.format(new Date()) + "," + eventCode + "," + entity + "," + additionalData + "]\n";
        logs.add(log);
    }

    public synchronized void flush() {
        try {
            for (String entry : logs) {
                writer.write(entry + "\n");
            }
            writer.flush();
            logs.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeLogger() throws IOException {
        flush();
        try {
            writer.close();
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