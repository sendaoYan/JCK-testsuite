import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexLookbehindProbe {
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
            emit("lookbehind", Pattern.compile("(?<=a)b").matcher("ab").find());
            emit("lookbehind.mid", Pattern.compile("(?<=a)b").matcher("cbab").find());
            emit("lookahead", Pattern.compile("a(?=b)").matcher("ab").find());
            emit("negLookbehind", Pattern.compile("(?<!a)b").matcher("cb").find());
            emit("negLookahead", Pattern.compile("a(?!b)").matcher("ac").find());
            emit("boundedLookbehind", Pattern.compile("(?<=a{1,2})b").matcher("aab").find());
            emit("boundedLookbehind2", Pattern.compile("(?<=a{0,2})b").matcher("b").find());
            ex("unboundedLookbehind", () -> emit("unboundedLookbehind", Pattern.compile("(?<=a+)b")));
            ex("unboundedLookbehindStar", () -> emit("unboundedLookbehindStar", Pattern.compile("(?<=a*)b")));
            ex("lookbehind.nullChar", () -> emit("lookbehind.nullChar", Pattern.compile("(?<=\\x00)a").matcher("\u0000a").find()));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
