import java.util.Locale;
import java.util.TimeZone;

public class ThreadRemovedApiProbe {
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
            Thread t = new Thread();
            ex("destroy", () -> { t.destroy(); emit("destroy", "returned"); });
            ex("stop", () -> { t.stop(); emit("stop", "returned"); });
            ex("suspend", () -> { t.suspend(); emit("suspend", "returned"); });
            ex("resume", () -> { t.resume(); emit("resume", "returned"); });
            ex("countStackFrames", () -> { t.countStackFrames(); emit("countStackFrames", "returned"); });
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
