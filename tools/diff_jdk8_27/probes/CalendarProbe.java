import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarProbe {
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
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
            c.setTimeInMillis(0L);
            emit("epoch.YEAR", c.get(Calendar.YEAR));
            emit("epoch.MONTH", c.get(Calendar.MONTH));
            emit("epoch.DAY", c.get(Calendar.DAY_OF_MONTH));
            emit("epoch.DAY_OF_WEEK", c.get(Calendar.DAY_OF_WEEK));
            emit("epoch.HOUR", c.get(Calendar.HOUR_OF_DAY));
            emit("epoch.firstDayOfWeek", c.getFirstDayOfWeek());
            emit("epoch.minimalDays", c.getMinimalDaysInFirstWeek());

            c.clear();
            c.set(2024, Calendar.FEBRUARY, 30);
            c.setLenient(false);
            ex("lenientFalse.compute", () -> emit("lenientFalse.compute", c.getTimeInMillis()));

            Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
            c2.clear();
            c2.set(2024, Calendar.JUNE, 1, 12, 0, 0);
            c2.set(Calendar.MILLISECOND, 0);
            emit("offsetMillis", c2.getTimeInMillis());
            emit("isLeap.2024", ((java.util.GregorianCalendar) c2).isLeapYear(2024));
            emit("isLeap.1900", ((java.util.GregorianCalendar) c2).isLeapYear(1900));

            Calendar c3 = Calendar.getInstance(TimeZone.getTimeZone("GMT+05:30"), Locale.US);
            c3.clear();
            c3.set(2024, Calendar.JUNE, 1, 12, 0, 0);
            c3.set(Calendar.MILLISECOND, 0);
            emit("offsetZone", c3.get(Calendar.ZONE_OFFSET));
            emit("offsetMillis2", c3.getTimeInMillis());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
