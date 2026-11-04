import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexGroupNameProbe {
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
            Pattern p = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})");
            Matcher m = p.matcher("2024-03-15");
            emit("matches", m.matches());
            emit("group.year", m.group("year"));
            emit("group.month", m.group("month"));
            emit("group.day", m.group("day"));
            emit("group.byIndex", m.group(2));
            ex("group.missing", () -> emit("group.missing", m.group("hour")));
            ex("group.negIndex", () -> emit("group.negIndex", m.group(-1)));

            Pattern numbered = Pattern.compile("(a)(b)");
            Matcher mn = numbered.matcher("ab");
            mn.matches();
            emit("numbered.count", mn.groupCount());
            emit("numbered.group0", mn.group(0));
            emit("numbered.group2", mn.group(2));

            ex("duplicateName", () -> Pattern.compile("(?<x>a)(?<x>b)"));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
