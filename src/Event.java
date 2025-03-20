import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
    private SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss.SSS");
    private Object entity;
    private EventCode eventCode;
    private String additionalData;

    /**
     * Event constructor
     * @param eventCode the code of event
     * @param entity the thread responsible for event
     * @param additionalData any information regarding event
     */
    public Event(EventCode eventCode, Object entity, String additionalData) {
        this.eventCode = eventCode;
        this.entity = entity;
        this.additionalData = additionalData;
    }

    @Override
    public String toString(){
        return "Event log: [" + timestamp.format(new Date()) + ", " + entity + ", " + eventCode + ", "  + additionalData + "]";
    }
}
