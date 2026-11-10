import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.TimeZone;

public class ChronoFieldRangeProbe {
    interface Fn { void run() throws Exception; }
    static void emit(String k, Object v) { System.out.println(k + "=" + v); }
    static void ex(String k, Fn f) {
        try { f.run(); }
        catch (Throwable t) { System.out.println(k + "=" + t.getClass().getName() + "|msg=" + t.getMessage()); }
    }
    public static void main(String[] a) {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            // JDK-8 reports INSTANT_SECONDS as the full long range; JDK-9+ bounds it
            // to the actual Instant range.
            ex("INSTANT_SECONDS", () -> emit("INSTANT_SECONDS", ChronoField.INSTANT_SECONDS.range()));
            // JDK-8 reports a symmetric EPOCH_DAY range; JDK-9+ bounds it to the
            // actual LocalDate range.
            ex("EPOCH_DAY", () -> emit("EPOCH_DAY", ChronoField.EPOCH_DAY.range()));
            // Controls: ranges that agree across both JDKs.
            ex("YEAR", () -> emit("YEAR", ChronoField.YEAR.range()));
            ex("SECOND_OF_DAY", () -> emit("SECOND_OF_DAY", ChronoField.SECOND_OF_DAY.range()));
            ex("NANO_OF_SECOND", () -> emit("NANO_OF_SECOND", ChronoField.NANO_OF_SECOND.range()));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
