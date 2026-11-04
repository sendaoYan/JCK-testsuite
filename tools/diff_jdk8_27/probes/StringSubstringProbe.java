import java.util.Locale;
import java.util.TimeZone;

public class StringSubstringProbe {
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
            emit("sub.0.0", "abc".substring(0, 0));
            emit("sub.0.3", "abc".substring(0, 3));
            emit("sub.whole", "abc".substring(0));
            emit("sub.1", "abc".substring(1));
            ex("sub.neg1", () -> emit("sub.neg1", "abc".substring(-1)));
            ex("sub.beyond", () -> emit("sub.beyond", "abc".substring(4)));
            ex("sub.startGtEnd", () -> emit("sub.startGtEnd", "abc".substring(2, 1)));
            ex("sub.endBeyond", () -> emit("sub.endBeyond", "abc".substring(0, 4)));
            emit("charAt.0", "abc".charAt(0));
            ex("charAt.3", () -> emit("charAt.3", "abc".charAt(3)));
            ex("charAt.neg", () -> emit("charAt.neg", "abc".charAt(-1)));
            emit("length.surrogate", "a😀b".length());
            emit("codePointCount", "a😀b".codePointCount(0, 4));
            emit("codePointAt", "a😀b".codePointAt(1));
            emit("isEmpty.empty", "".isEmpty());
            emit("isEmpty.one", "x".isEmpty());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
