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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
