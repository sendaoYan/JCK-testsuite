/*
 * @test
 * @summary java.time.chrono.AbstractChronology conformance: resolveDate and compareTo.
 * @run main AbstractChronologyTest
 */

import java.time.LocalDate;
import java.time.chrono.AbstractChronology;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.HashMap;
import java.util.Map;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;
import java.time.chrono.HijrahChronology;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.MinguoChronology;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.temporal.TemporalAccessor;

public class AbstractChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractChronology() {
        AbstractChronology ac = IsoChronology.INSTANCE;
        check(IsoChronology.INSTANCE instanceof AbstractChronology, "extends AbstractChronology");
        check(ac.compareTo(IsoChronology.INSTANCE) == 0, "compareTo self");

        Map<TemporalField, Long> map = new HashMap<TemporalField, Long>();
        map.put(ChronoField.YEAR, 2020L);
        map.put(ChronoField.MONTH_OF_YEAR, 1L);
        map.put(ChronoField.DAY_OF_MONTH, 1L);
        ChronoLocalDate resolved = ac.resolveDate(map, ResolverStyle.STRICT);
        check(resolved.equals(LocalDate.of(2020, 1, 1)), "resolveDate");
    }

    public static void main(String[] args) {
        testAbstractChronology();
        testDeepAbstractChronology();
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


    static void testDeepAbstractChronology() {
        java.util.SortedSet<Chronology> all = new java.util.TreeSet<Chronology>(Chronology.getAvailableChronologies());
        xeq(all.toString(), "[Hijrah-umalqura, ISO, Japanese, Minguo, ThaiBuddhist]", "registry sorted order");
        check(HijrahChronology.INSTANCE.compareTo(IsoChronology.INSTANCE) < 0, "Hijrah before ISO");
        check(IsoChronology.INSTANCE.compareTo(HijrahChronology.INSTANCE) > 0, "ISO after Hijrah");
        check(JapaneseChronology.INSTANCE.compareTo(ThaiBuddhistChronology.INSTANCE) == -10, "Japanese vs ThaiBuddhist id compare");
        check(MinguoChronology.INSTANCE.compareTo(JapaneseChronology.INSTANCE) > 0, "Minguo after Japanese");
        check(IsoChronology.INSTANCE.equals(Chronology.of("ISO")), "ISO equals of(ISO)");
        check(IsoChronology.INSTANCE.equals(IsoChronology.INSTANCE), "ISO self equals");
        check(!IsoChronology.INSTANCE.equals(HijrahChronology.INSTANCE), "ISO not equals Hijrah");
        check(IsoChronology.INSTANCE.hashCode() == IsoChronology.INSTANCE.hashCode(), "hashCode stable");
        check(JapaneseChronology.INSTANCE.dateEpochDay(19889L).toString().equals("Japanese Reiwa 6-06-15"), "dateEpochDay 19889");
        check(HijrahChronology.INSTANCE.dateEpochDay(19557L).toString().equals("Hijrah-umalqura AH 1445-01-01"), "dateEpochDay 19557");
        check(MinguoChronology.INSTANCE.dateYearDay(113, 167).toString().equals("Minguo ROC 113-06-15"), "dateYearDay 167");
        check(HijrahChronology.INSTANCE.dateYearDay(1445, 345).toString().equals("Hijrah-umalqura AH 1445-12-21"), "hijrah dateYearDay");
        java.util.HashMap<java.time.temporal.TemporalField, Long> partial =
            new java.util.HashMap<java.time.temporal.TemporalField, Long>();
        partial.put(java.time.temporal.ChronoField.YEAR, 2567L);
        check(ThaiBuddhistChronology.INSTANCE.resolveDate(partial, java.time.format.ResolverStyle.STRICT) == null,
            "partial map unresolved");
        java.util.HashMap<java.time.temporal.TemporalField, Long> full =
            new java.util.HashMap<java.time.temporal.TemporalField, Long>();
        full.put(java.time.temporal.ChronoField.YEAR, 2567L);
        full.put(java.time.temporal.ChronoField.MONTH_OF_YEAR, 6L);
        full.put(java.time.temporal.ChronoField.DAY_OF_MONTH, 15L);
        check(ThaiBuddhistChronology.INSTANCE.resolveDate(full, java.time.format.ResolverStyle.STRICT) != null,
            "full map resolved");
        check(JapaneseChronology.INSTANCE.date(java.time.LocalDateTime.of(2024, 6, 15, 10, 0).atZone(java.time.ZoneOffset.UTC))
            .toString().equals("Japanese Reiwa 6-06-15"), "date from ZDT");
        xact(() -> JapaneseChronology.INSTANCE.date(java.time.Instant.parse("2024-06-15T10:15:30Z")),
            java.time.DateTimeException.class,
            "Unable to obtain LocalDate from TemporalAccessor: 2024-06-15T10:15:30Z of type java.time.Instant",
            "date from bare Instant");
        xact(() -> IsoChronology.INSTANCE.dateNow((java.time.ZoneId) null), NullPointerException.class, "zone", "dateNow null zone");
        xact(() -> IsoChronology.INSTANCE.dateNow((java.time.Clock) null), NullPointerException.class, "clock", "dateNow null clock");
        java.time.Clock fixed = java.time.Clock.fixed(java.time.Instant.parse("2024-06-15T10:15:30Z"), java.time.ZoneOffset.UTC);
        check(JapaneseChronology.INSTANCE.dateNow(fixed).toString().equals("Japanese Reiwa 6-06-15"), "jp dateNow fixed clock");
        check(HijrahChronology.INSTANCE.dateNow(fixed).toString().equals("Hijrah-umalqura AH 1445-12-09"), "hj dateNow fixed clock");
        check(MinguoChronology.INSTANCE.dateNow(fixed).toString().equals("Minguo ROC 113-06-15"), "mg dateNow fixed clock");
        check(ThaiBuddhistChronology.INSTANCE.dateNow(fixed).toString().equals("ThaiBuddhist BE 2567-06-15"), "tb dateNow fixed clock");
    }

}
