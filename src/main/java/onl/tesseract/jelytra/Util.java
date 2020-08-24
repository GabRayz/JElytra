package onl.tesseract.jelytra;

import java.time.Duration;

public class Util {
    public static String durationToString(Duration duration)
    {
        if (duration.toHours() > 0)
            return String.format("%dh %dm %ds:%dms", (int)duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
        else
            return String.format("%dm %ds:%dms", duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
    }
}
