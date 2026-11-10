import java.time.Year;
import java.util.Locale;
import java.util.TimeZone;

public class YearParseProbe {
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
            // Leading '+' sign: rejected by JDK-8, accepted by JDK-27+.
            ex("parse.plus", () -> emit("parse.plus", Year.parse("+2023")));
            ex("parse.plusSmall", () -> emit("parse.plusSmall", Year.parse("+999")));
            ex("parse.plusMax", () -> emit("parse.plusMax", Year.parse("+999999999")));
            // Leading '-' is accepted by both (control).
            ex("parse.minus", () -> emit("parse.minus", Year.parse("-999999999")));
            // Plain digits are accepted by both (control).
            ex("parse.plain", () -> emit("parse.plain", Year.parse("2023")));
            ex("parse.plainMin", () -> emit("parse.plainMin", Year.parse("-2023")));
            ex("parse.plainMax", () -> emit("parse.plainMax", Year.parse("999999999")));
            ex("parse.doubleSign", () -> emit("parse.doubleSign", Year.parse("+-2023")));
            emit("range", Year.MIN_VALUE + ".." + Year.MAX_VALUE);
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
