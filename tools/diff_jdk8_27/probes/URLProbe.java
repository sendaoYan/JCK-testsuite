import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

public class URLProbe {
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
            URL url = new URL("https://example.com:8443/a/b?q=1#f");
            emit("protocol", url.getProtocol());
            emit("host", url.getHost());
            emit("port", url.getPort());
            emit("defaultPort", url.getDefaultPort());
            emit("path", url.getPath());
            emit("query", url.getQuery());
            emit("file", url.getFile());
            emit("ref", url.getRef());
            emit("externalForm", url.toExternalForm());
            emit("authority", url.getAuthority());

            URL file = new URL("file:/tmp/x y");
            emit("file.protocol", file.getProtocol());
            emit("file.path", file.getPath());
            emit("file.file", file.getFile());
            emit("file.host", file.getHost());

            ex("badProtocol", () -> emit("badProtocol", new URL("ht tp://x")));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
