import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.TimeZone;

public class ValueRangeProbe {
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
            // min > smallestMaximum: JDK-8 IAE message is "Minimum value must be less
            // than maximum value"; JDK-27+ says "smallest maximum value".
            ex("of.3,1,2", () -> emit("of.3,1,2", ValueRange.of(3, 1, 2)));
            // min > largestMaximum (single-value overload).
            ex("of.3,2", () -> emit("of.3,2", ValueRange.of(3, 2)));
            // Valid ranges are consistent on both (control).
            emit("of.1,2,3", ValueRange.of(1, 2, 3).toString());
            emit("of.1,1,1", ValueRange.of(1, 1, 1).toString());
            emit("of.1,3", ValueRange.of(1, 3).toString());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
