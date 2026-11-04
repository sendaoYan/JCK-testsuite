import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeSet;

public class PropertiesProbe {
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
            Properties p = new Properties();
            p.setProperty("plain", "hello world");
            p.setProperty("colon", "colon:value");
            p.setProperty("equals", "equals=value");
            p.setProperty("backslash", "back\\slash");
            p.setProperty("unicode", "café");
            p.setProperty("leadingSpace", " leading");
            p.setProperty("tab", "tab\tvalue");

            StringWriter sw = new StringWriter();
            p.store(sw, "c");
            TreeSet<String> sorted = new TreeSet<String>();
            for (String line : sw.toString().split("\\r?\\n")) {
                if (line.startsWith("#")) continue;   // skip comment + timestamp
                if (line.trim().isEmpty()) continue;
                sorted.add(line);
            }
            for (String line : sorted) {
                emit("store.line", line);
            }

            Properties r = new Properties();
            r.load(new StringReader(sw.toString()));
            for (String k : new TreeSet<String>(r.stringPropertyNames())) {
                emit("roundtrip." + k, r.getProperty(k));
            }

            Properties p2 = new Properties();
            p2.load(new StringReader("a=1\nb = 2\nc\\:colon=3\nd back\\=slash=4\nempty=\n"));
            emit("load.a", p2.getProperty("a"));
            emit("load.b", p2.getProperty("b"));
            emit("load.c:colon", p2.getProperty("c:colon"));
            emit("load.d back=slash", p2.getProperty("d back=slash"));
            emit("load.empty", p2.getProperty("empty"));

            ex("get.null", () -> emit("get.null", p2.getProperty(null)));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
