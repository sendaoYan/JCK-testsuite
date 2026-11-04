import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class StringSplitProbe {
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
            emit("split.simple", Arrays.toString("a,b,c".split(",")));
            emit("split.trailing", Arrays.toString("a,b,".split(",")));
            emit("split.leading", Arrays.toString(",a,b".split(",")));
            emit("split.empty", Arrays.toString("".split(",")));
            emit("split.regexEmpty", Arrays.toString("abc".split("")));
            emit("split.emptyStringEmptyRegex", Arrays.toString("".split("")));
            emit("split.limitNeg", Arrays.toString("a,b,".split(",", -1)));
            emit("split.limit0", Arrays.toString("a,b,".split(",", 0)));
            emit("split.limit2", Arrays.toString("a,b,c".split(",", 2)));
            emit("split.limitBig", Arrays.toString("a,b,c".split(",", 10)));
            ex("split.nullRegex", () -> emit("split.nullRegex", Arrays.toString("ab".split(null))));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
