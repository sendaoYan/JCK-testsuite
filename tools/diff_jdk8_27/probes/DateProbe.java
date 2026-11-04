import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateProbe {
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
            Date d = new Date(0L);
            emit("toString.epoch", d.toString());
            emit("getTime.epoch", d.getTime());
            Date d2 = new Date(-86400000L);
            emit("before", d2.before(d));
            emit("after", d.after(d2));
            emit("compareTo", d2.compareTo(d));
            emit("equals", d.equals(new Date(0L)));
            emit("equals.sameMillis", d.equals(new Date(0L)));
            Date d3 = new Date(86400000L);
            emit("compareTo.equal", d3.compareTo(new Date(86400000L)));
            emit("hashCode", d.hashCode());
            ex("compareTo.null", () -> d.compareTo(null));
            Date before = new Date(1234567890123L);
            emit("toString.2009", before.toString());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
