/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistChronology conformance: white-box structure + black-box behavior.
 * @run main ThaiBuddhistChronologyTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.AbstractChronology;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;
import java.time.chrono.ThaiBuddhistEra;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class ThaiBuddhistChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name, e); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.ThaiBuddhistChronology");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == AbstractChronology.class, "extends AbstractChronology");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(Modifier.isStatic(c.getField("INSTANCE").getModifiers()), "INSTANCE static");
        check(Modifier.isFinal(c.getField("INSTANCE").getModifiers()), "INSTANCE final");
        checkMethod(c, "getId");
        checkMethod(c, "getCalendarType");
        checkMethod(c, "eraOf", int.class);
        checkMethod(c, "eras");
        checkMethod(c, "isLeapYear", long.class);
        checkMethod(c, "prolepticYear", Era.class, int.class);
        checkMethod(c, "date", Era.class, int.class, int.class, int.class);
        checkMethod(c, "dateEpochDay", long.class);
    }

    static void testBlackBox() {
        ThaiBuddhistChronology tc = ThaiBuddhistChronology.INSTANCE;
        check("ThaiBuddhist".equals(tc.getId()), "getId");
        check("buddhist".equals(tc.getCalendarType()), "getCalendarType");
        check(tc.isIsoBased(), "isIsoBased");

        List<String> ids = new ArrayList<>();
        for (Chronology ch : Chronology.getAvailableChronologies()) ids.add(ch.getId());
        java.util.Collections.sort(ids);
        check(ids.contains("ThaiBuddhist"), "available: ThaiBuddhist");
        check(ids.contains("Japanese"), "available: Japanese");
        check(ids.contains("Minguo"), "available: Minguo");
        check(ids.contains("Hijrah-umalqura"), "available: Hijrah-umalqura");

        List<Era> eras = tc.eras();
        check(eras.size() == 2, "two eras");
        check(eras.contains(ThaiBuddhistEra.BE), "eras contains BE");
        check(eras.contains(ThaiBuddhistEra.BEFORE_BE), "eras contains BEFORE_BE");

        check(tc.eraOf(1) == ThaiBuddhistEra.BE, "eraOf(1) BE");
        check(tc.eraOf(0) == ThaiBuddhistEra.BEFORE_BE, "eraOf(0) BEFORE_BE");

        check(tc.prolepticYear(ThaiBuddhistEra.BE, 2563) == 2563, "prolepticYear BE 2563 = 2563");
        check(tc.prolepticYear(ThaiBuddhistEra.BEFORE_BE, 1) == 0, "prolepticYear BEFORE_BE 1 = 0");

        // ThaiBuddhist proleptic 2563 == ISO 2020 (leap)
        check(tc.isLeapYear(2563), "isLeapYear(2563) true");
        check(!tc.isLeapYear(2562), "isLeapYear(2562) false");

        check(tc.range(ChronoField.MONTH_OF_YEAR).getMinimum() == 1
                && tc.range(ChronoField.MONTH_OF_YEAR).getMaximum() == 12, "MONTH_OF_YEAR range 1..12");

        ThaiBuddhistDate d = tc.date(ThaiBuddhistEra.BE, 2563, 1, 1);
        check(d.getEra() == ThaiBuddhistEra.BE, "date era");
        check(tc.dateEpochDay(d.toEpochDay()).equals(d), "date -> toEpochDay -> dateEpochDay round-trip");
        check(tc.date(2563, 1, 1).equals(d), "date(2563,1,1) equals date(BE,2563,1,1)");

        check(tc.dateNow() != null, "dateNow non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
