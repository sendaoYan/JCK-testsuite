import java.util.Locale;
import java.util.TimeZone;

public class StringNewApiProbe {
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
            ex("isBlank.empty", () -> emit("isBlank.empty", "".isBlank()));
            ex("isBlank.space", () -> emit("isBlank.space", "  ".isBlank()));
            ex("isBlank.text", () -> emit("isBlank.text", " x ".isBlank()));
            ex("strip", () -> emit("strip", "  hi  ".strip()));
            ex("stripLeading", () -> emit("stripLeading", "  hi  ".stripLeading()));
            ex("stripTrailing", () -> emit("stripTrailing", "  hi  ".stripTrailing()));
            ex("repeat", () -> emit("repeat", "ab".repeat(3)));
            ex("indent", () -> emit("indent", "a\nb".indent(2)));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
