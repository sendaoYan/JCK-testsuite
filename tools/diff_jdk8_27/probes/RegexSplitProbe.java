import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class RegexSplitProbe {
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
            emit("split.trailingEmpty", Arrays.toString(Pattern.compile(",").split("a,b,")));
            emit("split.limitNeg", Arrays.toString(Pattern.compile(",").split("a,b,", -1)));
            emit("split.limit2", Arrays.toString(Pattern.compile(",").split("a,b,c", 2)));
            emit("split.zeroWidth", Arrays.toString(Pattern.compile("").split("ab")));
            emit("split.zeroWidthEmpty", Arrays.toString(Pattern.compile("").split("")));
            emit("split.word", Arrays.toString(Pattern.compile("\\s+").split(" a  b ")));
            ex("split.nullInput", () -> Pattern.compile(",").split((CharSequence) null));
            ex("split.nullPattern", () -> Pattern.compile(null));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
