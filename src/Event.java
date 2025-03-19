import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
    private SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss.SSS");
    private Object entity;
    private EventCode eventCode;
    private String additionalData;

    public Event(EventCode eventCode, Object entity, String additionalData) {
        this.eventCode = eventCode;
        this.entity = entity;
        this.additionalData = additionalData;
    }

    public Object getEntity() {
        return entity;
    }

    public EventCode getEventCode() {
        return eventCode;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public SimpleDateFormat getTimestamp() {
        return timestamp;
    }

    public long dateToLong(String time)  {
        try{
            Date date = timestamp.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String toString(){
        return "Event log: [" + timestamp.format(new Date()) + ", " + entity + ", " + eventCode + ", "  + additionalData + "]";
    }
}
