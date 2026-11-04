/*
 * @test
 * @summary java.time.chrono.AbstractChronology conformance: white-box structure + black-box behavior.
 * @run main AbstractChronologyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.chrono.AbstractChronology;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.MinguoChronology;
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
import java.time.chrono.HijrahChronology;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.temporal.TemporalAccessor;

public class AbstractChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.AbstractChronology");
        check(Modifier.isAbstract(c.getModifiers()), "AbstractChronology is abstract");
        check(!c.isInterface(), "AbstractChronology is a class, not an interface");
        check(!Modifier.isFinal(c.getModifiers()), "AbstractChronology is not final");
        check(c.getSuperclass() == Object.class, "AbstractChronology directly extends Object");
        check(Chronology.class.isAssignableFrom(c), "AbstractChronology implements Chronology");
        check(hasMethod(c, "resolveDate"), "has resolveDate");
        check(hasMethod(c, "compareTo"), "has compareTo");
        check(hasMethod(c, "equals"), "has equals");
        check(hasMethod(c, "hashCode"), "has hashCode");
        check(hasMethod(c, "toString"), "has toString");
    }

    static void testBlackBox() {
        // IsoChronology is the canonical concrete AbstractChronology.
        AbstractChronology ac = IsoChronology.INSTANCE;
        check(ac instanceof AbstractChronology, "IsoChronology extends AbstractChronology");
        check(ac.compareTo(IsoChronology.INSTANCE) == 0, "compareTo self == 0");
        check(ac.compareTo(Chronology.of("ThaiBuddhist")) < 0, "compareTo ThaiBuddhist < 0");
        check(ac.compareTo(Chronology.of("Hijrah-umalqura")) > 0, "compareTo Hijrah > 0");

        // equals/hashCode are id-based.
        check(ac.equals(Chronology.of("ISO")), "equals Chronology.of(ISO)");
        check(!ac.equals(Chronology.of("Minguo")), "not equals Minguo");
        check(ac.hashCode() == Chronology.of("ISO").hashCode(), "hashCode consistent with equals");
        check("ISO".equals(ac.toString()), "toString == ISO");

        // resolveDate (STRICT) with year/month/day.
        Map<TemporalField, Long> map = new HashMap<TemporalField, Long>();
        map.put(ChronoField.YEAR, 2020L);
        map.put(ChronoField.MONTH_OF_YEAR, 1L);
        map.put(ChronoField.DAY_OF_MONTH, 1L);
        ChronoLocalDate resolved = ac.resolveDate(map, ResolverStyle.STRICT);
        check(resolved.equals(LocalDate.of(2020, 1, 1)), "resolveDate STRICT 2020-01-01");

        // resolveDate with day-of-year (leap year day 60 -> Feb 29).
        Map<TemporalField, Long> doy = new HashMap<TemporalField, Long>();
        doy.put(ChronoField.YEAR, 2020L);
        doy.put(ChronoField.DAY_OF_YEAR, 60L);
        check(ac.resolveDate(doy, ResolverStyle.STRICT).equals(LocalDate.of(2020, 2, 29)),
              "resolveDate STRICT day-of-year 60 -> Feb 29");

        // resolveDate with an invalid date (Feb 30) must throw DateTimeException.
        Map<TemporalField, Long> invalid = new HashMap<TemporalField, Long>();
        invalid.put(ChronoField.YEAR, 2020L);
        invalid.put(ChronoField.MONTH_OF_YEAR, 2L);
        invalid.put(ChronoField.DAY_OF_MONTH, 30L);
        expectThrows(java.time.DateTimeException.class,
              () -> ac.resolveDate(invalid, ResolverStyle.STRICT),
              "resolveDate invalid Feb 30 throws DateTimeException");

        // compareTo consistency across a known set (ISO id sorts between Hijrah and Minguo).
        check(ac.compareTo(MinguoChronology.INSTANCE) < 0, "ISO < Minguo by id");
        check(ac.compareTo(JapaneseChronology.INSTANCE) < 0, "ISO < Japanese by id");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
