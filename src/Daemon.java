import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Daemon {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startPeriodicFlush(EventLogger eventLogger) {
        scheduler.scheduleAtFixedRate(eventLogger::flush, 3, 3, TimeUnit.SECONDS);
    }

    public static void stopFlusher() {
        scheduler.shutdown();
    }
}
