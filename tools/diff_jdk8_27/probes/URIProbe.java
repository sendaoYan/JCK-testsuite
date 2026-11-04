import java.net.URI;
import java.util.Locale;
import java.util.TimeZone;

public class URIProbe {
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
            URI u = new URI("https://user:pass@example.com:8443/a/b/c?x=1&y=2#frag");
            emit("scheme", u.getScheme());
            emit("host", u.getHost());
            emit("port", u.getPort());
            emit("path", u.getPath());
            emit("rawPath", u.getRawPath());
            emit("rawQuery", u.getRawQuery());
            emit("query", u.getQuery());
            emit("fragment", u.getFragment());
            emit("userInfo", u.getUserInfo());
            emit("authority", u.getRawAuthority());
            emit("toString", u.toString());
            emit("toASCIIString", u.toASCIIString());

            URI base = new URI("https://example.com/a/b/");
            emit("resolve", base.resolve("../c/d").toString());
            emit("relativize", new URI("https://example.com/a/b/c").relativize(new URI("https://example.com/a/b/d")).toString());

            ex("badIPv6", () -> emit("badIPv6", new URI("http://[::1")));
            ex("badSpace", () -> emit("badSpace", new URI("http://exa mple.com")));
            ex("opaque", () -> emit("opaque", new URI("mailto:user@example.com").isOpaque()));
            emit("opaque.schemeSpecific", new URI("mailto:user@example.com").getSchemeSpecificPart());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
