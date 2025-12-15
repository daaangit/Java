import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Log {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private Log(){}

    public static void info(String tag, String msg)
    {
        String time = LocalTime.now().format(FMT);
        System.out.printf("%s  %-12s  %s%n", time, "[" + tag + "]", msg);
    }
}
