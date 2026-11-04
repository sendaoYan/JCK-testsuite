import java.util.Locale;
import java.util.TimeZone;

public class LongParseProbe {
    interface Fn { void run() throws Exception; }
    static void emit(String k, Object v) { System.out.println(k + "=" + v); }
    static void ex(String k, Fn f) {
        try { f.run(); }
        catch (Throwable t) { System.out.println(k + "=exception=" + t.getClass().getName()); }
    }
    public static void main(String[] a) {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            ex("parse.MAX", () -> emit("parse.MAX", Long.parseLong("9223372036854775807")));
            ex("parse.MIN", () -> emit("parse.MIN", Long.parseLong("-9223372036854775808")));
            ex("parse.MAXp1", () -> emit("parse.MAXp1", Long.parseLong("9223372036854775808")));
            ex("parse.MINm1", () -> emit("parse.MINm1", Long.parseLong("-9223372036854775809")));
            ex("parse.plus", () -> emit("parse.plus", Long.parseLong("+42")));
            ex("parse.empty", () -> emit("parse.empty", Long.parseLong("")));
            ex("parse.radix36", () -> emit("parse.radix36", Long.parseLong("zzzz", 36)));
            ex("parse.radix37", () -> emit("parse.radix37", Long.parseLong("1", 37)));
            ex("parse.hexMin", () -> emit("parse.hexMin", Long.parseLong("-8000000000000000", 16)));
            ex("parse.hexOverflow", () -> emit("parse.hexOverflow", Long.parseLong("8000000000000000", 16)));
            emit("toString.MIN", Long.toString(Long.MIN_VALUE));
            emit("toHexString.MIN", Long.toHexString(Long.MIN_VALUE));
            emit("toOctalString.MIN", Long.toOctalString(Long.MIN_VALUE));
            emit("toBinaryString.MIN", Long.toBinaryString(Long.MIN_VALUE));
            emit("toString.radix36.MAX", Long.toString(Long.MAX_VALUE, 36));
            ex("parse.null", () -> emit("parse.null", Long.parseLong(null)));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
