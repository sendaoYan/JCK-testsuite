import java.util.Locale;
import java.util.TimeZone;

public class BooleanProbe {
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
            emit("parse.true", Boolean.parseBoolean("true"));
            emit("parse.TRUE", Boolean.parseBoolean("TRUE"));
            emit("parse.garbage", Boolean.parseBoolean("yes"));
            emit("parse.empty", Boolean.parseBoolean(""));
            emit("parse.null", Boolean.parseBoolean(null));
            emit("valueOf.null", Boolean.valueOf(null));
            emit("valueOf.same", Boolean.valueOf(true) == Boolean.TRUE);
            emit("toString.true", Boolean.toString(true));
            emit("hashCode.true", Boolean.valueOf(true).hashCode());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
