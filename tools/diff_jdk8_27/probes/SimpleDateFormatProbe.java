import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SimpleDateFormatProbe {
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            emit("format.epoch", sdf.format(new Date(0)));
            emit("format.day1", sdf.format(new Date(86400000L)));
            emit("format.neg", sdf.format(new Date(-1L)));

            sdf.setLenient(false);
            ex("parse.lenientFalse.bad", () -> emit("parse.lenientFalse.bad", sdf.parse("2024-02-30 00:00:00.000")));
            ex("parse.lenientFalse.ok", () -> emit("parse.lenientFalse.ok", sdf.parse("2024-03-15 01:02:03.004").getTime()));

            sdf.setLenient(true);
            emit("parse.lenient.rollover", sdf.parse("2024-02-30 00:00:00.000").getTime());

            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
            emit("parse.basic", sdf2.parse("2024-03-15").getTime());
            ex("parse.garbage", () -> emit("parse.garbage", sdf2.parse("not a date")));

            SimpleDateFormat sdf3 = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US);
            sdf3.setTimeZone(TimeZone.getTimeZone("GMT"));
            emit("format.named", sdf3.format(new Date(0)));

            SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy", Locale.US);
            sdf4.setTimeZone(TimeZone.getTimeZone("GMT"));
            sdf4.set2DigitYearStart(new Date(0L));
            emit("parse.2digit", sdf4.parse("24").getTime());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
