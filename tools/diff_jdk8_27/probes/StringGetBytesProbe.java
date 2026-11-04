import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class StringGetBytesProbe {
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
            emit("bytes.utf8", Arrays.toString("héllo".getBytes("UTF-8")));
            emit("bytes.iso", Arrays.toString("héllo".getBytes("ISO-8859-1")));
            emit("bytes.utf16", Arrays.toString("A".getBytes("UTF-16")));
            emit("bytes.empty", Arrays.toString("".getBytes("UTF-8")));
            ex("bytes.bad", () -> emit("bytes.bad", Arrays.toString("x".getBytes("NO-SUCH"))));
            ex("bytes.nullName", () -> emit("bytes.nullName", Arrays.toString("x".getBytes((String) null))));
            emit("newString.utf8", new String(new byte[]{104, -61, -87}, "UTF-8"));
            ex("newString.bad", () -> emit("newString.bad", new String(new byte[]{1}, "NO-SUCH")));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
