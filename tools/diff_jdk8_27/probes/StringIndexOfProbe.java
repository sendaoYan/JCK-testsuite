import java.util.Locale;
import java.util.TimeZone;

public class StringIndexOfProbe {
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
            emit("idx.empty", "abc".indexOf(""));
            emit("idx.miss", "abc".indexOf("z"));
            emit("idx.char", "abc".indexOf('b'));
            emit("idx.fromNeg", "abc".indexOf('a', -5));
            emit("idx.fromTooBig", "abc".indexOf('a', 100));
            emit("lastIdx.empty", "abc".lastIndexOf(""));
            emit("lastIdx.miss", "abc".lastIndexOf("z"));
            emit("lastIdx.fromTooBig", "abc".lastIndexOf('c', 100));
            emit("lastIdx.fromNeg", "abc".lastIndexOf('c', -1));
            emit("idxString.surrogate", "a😀b".indexOf("\uDE00"));
            emit("idxString.missing", "abc".indexOf("abcd"));
            emit("startsWith.prefix", "abc".startsWith("ab"));
            emit("startsWith.offset", "abc".startsWith("b", 1));
            emit("endsWith.suffix", "abc".endsWith("bc"));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
