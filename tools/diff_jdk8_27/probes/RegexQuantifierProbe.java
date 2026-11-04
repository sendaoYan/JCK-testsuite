import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class RegexQuantifierProbe {
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
            emit("greedy", Pattern.compile("a+").matcher("aaa").matches());
            emit("reluctant", Pattern.compile("a+?").matcher("aaa").matches());
            emit("possessive", Pattern.compile("a++").matcher("aaa").matches());
            emit("exact", Pattern.compile("a{3}").matcher("aaa").matches());
            emit("range", Pattern.compile("a{2,3}").matcher("aaa").matches());
            emit("rangeOpen", Pattern.compile("a{2,}").matcher("aaa").matches());
            emit("nestedPlus.backtrack", Pattern.compile("(a+)+b").matcher("aaaaaaaaaaaaaaaac").matches());
            emit("emptyMatch", Pattern.compile("a*").matcher("").matches());
            emit("dotAll.newline", Pattern.compile(".", Pattern.DOTALL).matcher("\n").matches());
            emit("dot.noNewline", Pattern.compile(".").matcher("\n").matches());
            emit("wordBoundary", Pattern.compile("\\bfoo\\b").matcher("foo bar").find());
            ex("quantifier.loneBrace", () -> Pattern.compile("a{"));
            ex("quantifier.badOrder", () -> Pattern.compile("a{3,2}"));
            ex("quantifier.zeroExact", () -> Pattern.compile("a{0}"));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
