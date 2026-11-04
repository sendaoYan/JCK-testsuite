import java.util.Locale;
import java.util.TimeZone;

public class StringToLowerCaseProbe {
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
            emit("lower.ascii", "ABCxyz".toLowerCase(Locale.US));
            emit("upper.ascii", "ABCxyz".toUpperCase(Locale.US));
            emit("lower.tr.I", "I".toLowerCase(new Locale("tr")));
            emit("upper.tr.i", "i".toUpperCase(new Locale("tr")));
            emit("upper.de.ss", "ß".toUpperCase(Locale.GERMAN));
            emit("lower.de.ss", "ß".toLowerCase(Locale.GERMAN));
            emit("lower.US.dotless", "İ".toLowerCase(Locale.US));
            emit("upper.US.dotless", "ı".toUpperCase(Locale.US));
            emit("lower.lt", "İ".toLowerCase(new Locale("lt")));
            emit("lower.az", "İ".toLowerCase(new Locale("az")));
            emit("lower.georgian", "Ⴀ".toLowerCase(Locale.US));
            emit("upper.desz", "ſ".toUpperCase(Locale.US));
            emit("lower.stigma", "Ϛ".toLowerCase(Locale.US));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
