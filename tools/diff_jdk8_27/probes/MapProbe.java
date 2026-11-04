import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MapProbe {
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
            LinkedHashMap<String, Integer> m = new LinkedHashMap<String, Integer>();
            m.put("b", 2);
            m.put("a", 1);
            m.put("c", 3);
            emit("linked.keySet", m.keySet().toString());
            emit("linked.get", m.get("a"));
            m.put("a", 10);
            emit("linked.overwrite", m.get("a"));
            emit("linked.size", m.size());
            emit("linked.contains", m.containsKey("b"));
            emit("linked.removed", m.remove("b"));
            emit("linked.getRemoved", m.get("b"));

            HashMap<String, Integer> h = new HashMap<String, Integer>();
            h.put("x", 1);
            emit("hash.get", h.get("x"));
            emit("hash.contains", h.containsKey("x"));
            emit("hash.size", h.size());
            emit("hash.nullKey", h.get(null));
            h.put(null, 42);
            emit("hash.nullKeyAfter", h.get(null));
            h.remove("x");
            emit("hash.afterRemove", h.size());
            emit("hash.getOrDefault", h.getOrDefault("missing", -1));

            Map<String, Integer> um = java.util.Collections.unmodifiableMap(h);
            ex("unmodifiable.put", () -> um.put("y", 2));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
