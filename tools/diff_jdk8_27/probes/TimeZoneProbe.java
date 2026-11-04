import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneProbe {
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
            emit("defaultId", TimeZone.getDefault().getID());
            emit("gmtId", TimeZone.getTimeZone("GMT").getID());
            emit("utcId", TimeZone.getTimeZone("UTC").getID());
            emit("plus530.rawOffset", TimeZone.getTimeZone("GMT+05:30").getRawOffset());
            emit("plus530.dstSavings", TimeZone.getTimeZone("GMT+05:30").getDSTSavings());
            emit("ny.rawOffset", TimeZone.getTimeZone("America/New_York").getRawOffset());
            emit("ny.useDaylight", TimeZone.getTimeZone("America/New_York").useDaylightTime());
            emit("hasSameRules", TimeZone.getTimeZone("GMT+05:30").hasSameRules(TimeZone.getTimeZone("GMT+0530")));
            emit("availableIds.count", TimeZone.getAvailableIDs().length);
            ex("nullZone", () -> emit("nullZone", TimeZone.getTimeZone((String) null).getID()));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
