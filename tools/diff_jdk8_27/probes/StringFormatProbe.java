import java.util.Locale;
import java.util.TimeZone;

public class StringFormatProbe {
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
            emit("fmt.hex", String.format(Locale.US, "%x", 255));
            emit("fmt.pad", String.format(Locale.US, "%05d", 42));
            emit("fmt.float", String.format(Locale.US, "%.2f", 1.005));
            emit("fmt.scientific", String.format(Locale.US, "%e", 12345.678));
            emit("fmt.string", String.format(Locale.US, "[%10s]", "hi"));
            emit("fmt.leftString", String.format(Locale.US, "[%-10s]", "hi"));
            emit("fmt.percent", String.format(Locale.US, "%.0f%%", 12.5));
            emit("fmt.unicode", String.format(Locale.US, "%c", 0x20AC));
            emit("fmt.octal", String.format(Locale.US, "%o", 8));
            emit("fmt.negHex", String.format(Locale.US, "%h", -1));
            emit("fmt.precisionStr", String.format(Locale.US, "%.3s", "abcdef"));
            ex("fmt.badSpec", () -> emit("fmt.badSpec", String.format(Locale.US, "%d", "notint")));
            ex("fmt.badType", () -> emit("fmt.badType", String.format(Locale.US, "%q", 5)));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
