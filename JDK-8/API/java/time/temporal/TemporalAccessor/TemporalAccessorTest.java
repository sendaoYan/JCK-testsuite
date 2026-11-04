/*
 * @test
 * @summary java.time.temporal.TemporalAccessor API conformance: field support and getLong.
 * @run main TemporalAccessorTest
 */

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.LocalTime;
import java.time.Year;
import java.time.chrono.IsoChronology;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class TemporalAccessorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalAccessor() {
        TemporalAccessor ta = LocalDate.of(2024, 6, 15);
        check(ta instanceof LocalDate, "LocalDate is a TemporalAccessor");
        check(ta.isSupported(ChronoField.DAY_OF_MONTH), "supports day of month");
        check(ta.getLong(ChronoField.YEAR) == 2024, "getLong year");
    }

    public static void main(String[] args) {
        testTemporalAccessor();
        testDeepTemporalAccessor();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepTemporalAccessor() {
        TemporalAccessor t = LocalTime.of(10, 15, 30);
        check(t.get(ChronoField.HOUR_OF_DAY) == 10, "get");
        check(t.getLong(ChronoField.SECOND_OF_DAY) == 36930L, "getLong");
        check(t.isSupported(ChronoField.MINUTE_OF_HOUR), "isSupported true");
        check(!t.isSupported(ChronoField.DAY_OF_MONTH), "isSupported false");
        xeq(t.range(ChronoField.HOUR_OF_DAY).toString(), "0 - 23", "range");
        xeq(t.query(TemporalQueries.precision()).toString(), "Nanos", "query");
        xact(() -> t.get(ChronoField.DAY_OF_MONTH), UnsupportedTemporalTypeException.class,
            "Unsupported field: DayOfMonth", "get unsupported");
        xact(() -> t.getLong(ChronoField.YEAR), UnsupportedTemporalTypeException.class,
            "Unsupported field: Year", "getLong unsupported");
        TemporalAccessor d = LocalDate.of(2024, 2, 29);
        check(d.get(ChronoField.DAY_OF_MONTH) == 29, "date get");
        check(d.getLong(ChronoField.EPOCH_DAY) == 19782L, "epochDay long");
        check(d.query(TemporalQueries.chronology()) == IsoChronology.INSTANCE, "query default chronology");
        check(TemporalAccessor.class.isAssignableFrom(Temporal.class), "Temporal extends TemporalAccessor");
        check(TemporalAccessor.class.isAssignableFrom(LocalDate.class), "value classes implement");
        xact(() -> LocalTime.NOON.get(null), NullPointerException.class, null, "get null field");
    }

}
