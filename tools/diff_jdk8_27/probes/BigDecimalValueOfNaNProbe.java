import java.math.BigDecimal;
import java.util.Locale;
import java.util.TimeZone;

public class BigDecimalValueOfNaNProbe {
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
            // valueOf(double) on NaN / infinities: JDK-8 throws NFE with a null
            // message; JDK-19+ throws NFE with "Infinite or NaN".
            ex("valueOf.NaN", () -> emit("valueOf.NaN", BigDecimal.valueOf(Double.NaN)));
            ex("valueOf.POSINF", () -> emit("valueOf.POSINF", BigDecimal.valueOf(Double.POSITIVE_INFINITY)));
            ex("valueOf.NEGINF", () -> emit("valueOf.NEGINF", BigDecimal.valueOf(Double.NEGATIVE_INFINITY)));
            // new BigDecimal(double) on the same inputs is consistent on both (control).
            ex("ctor.NaN", () -> emit("ctor.NaN", new BigDecimal(Double.NaN)));
            ex("ctor.POSINF", () -> emit("ctor.POSINF", new BigDecimal(Double.POSITIVE_INFINITY)));
            // finite values are consistent on both (control).
            emit("valueOf.0.1", BigDecimal.valueOf(0.1).toString());
            emit("valueOf.zero", BigDecimal.valueOf(0.0).toString());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
