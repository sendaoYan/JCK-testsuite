/*
 * @test
 * @summary java.time.chrono.HijrahChronology conformance: white-box structure + black-box behavior.
 * @run main HijrahChronologyTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.AbstractChronology;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.chrono.HijrahEra;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class HijrahChronologyTest {
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
        Class<?> c = Class.forName("java.time.chrono.HijrahChronology");
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
        HijrahChronology hc = HijrahChronology.INSTANCE;
        check("Hijrah-umalqura".equals(hc.getId()), "getId");
        check("islamic-umalqura".equals(hc.getCalendarType()), "getCalendarType");

        List<String> ids = new ArrayList<>();
        for (Chronology ch : Chronology.getAvailableChronologies()) ids.add(ch.getId());
        java.util.Collections.sort(ids);
        check(ids.contains("Hijrah-umalqura"), "available: Hijrah-umalqura");
        check(ids.contains("Japanese"), "available: Japanese");
        check(ids.contains("Minguo"), "available: Minguo");
        check(ids.contains("ThaiBuddhist"), "available: ThaiBuddhist");

        List<Era> eras = hc.eras();
        check(eras.size() == 1, "one era");
        check(eras.contains(HijrahEra.AH), "eras contains AH");
        check(hc.eraOf(1) == HijrahEra.AH, "eraOf(1) AH");

        check(hc.prolepticYear(HijrahEra.AH, 1437) == 1437, "prolepticYear AH 1437 = 1437");

        // deterministic for this JDK (Umm al-Qura data)
        check(!hc.isLeapYear(1437), "isLeapYear(1437) false");

        check(hc.range(ChronoField.MONTH_OF_YEAR).getMinimum() == 1
                && hc.range(ChronoField.MONTH_OF_YEAR).getMaximum() == 12, "MONTH_OF_YEAR range 1..12");

        HijrahDate d = hc.date(HijrahEra.AH, 1437, 1, 1);
        check(d.getEra() == HijrahEra.AH, "date era");
        // round-trip only, no hard-coded epoch-day (Umm al-Qura data)
        check(hc.dateEpochDay(d.toEpochDay()).equals(d), "date -> toEpochDay -> dateEpochDay round-trip");
        check(hc.date(1437, 1, 1).equals(d), "date(1437,1,1) equals date(AH,1437,1,1)");

        check(hc.dateNow() != null, "dateNow non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
