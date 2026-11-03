/*
 * @test
 * @summary java.time.chrono.MinguoChronology conformance: white-box structure + black-box behavior.
 * @run main MinguoChronologyTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.AbstractChronology;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;
import java.time.chrono.MinguoEra;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class MinguoChronologyTest {
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
        Class<?> c = Class.forName("java.time.chrono.MinguoChronology");
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
        MinguoChronology mc = MinguoChronology.INSTANCE;
        check("Minguo".equals(mc.getId()), "getId");
        check("roc".equals(mc.getCalendarType()), "getCalendarType");
        check(mc.isIsoBased(), "isIsoBased");

        List<String> ids = new ArrayList<>();
        for (Chronology ch : Chronology.getAvailableChronologies()) ids.add(ch.getId());
        java.util.Collections.sort(ids);
        check(ids.contains("Minguo"), "available: Minguo");
        check(ids.contains("Japanese"), "available: Japanese");
        check(ids.contains("ThaiBuddhist"), "available: ThaiBuddhist");
        check(ids.contains("Hijrah-umalqura"), "available: Hijrah-umalqura");

        List<Era> eras = mc.eras();
        check(eras.size() == 2, "two eras");
        check(eras.contains(MinguoEra.ROC), "eras contains ROC");
        check(eras.contains(MinguoEra.BEFORE_ROC), "eras contains BEFORE_ROC");

        check(mc.eraOf(1) == MinguoEra.ROC, "eraOf(1) ROC");
        check(mc.eraOf(0) == MinguoEra.BEFORE_ROC, "eraOf(0) BEFORE_ROC");

        check(mc.prolepticYear(MinguoEra.ROC, 1) == 1, "prolepticYear ROC 1 = 1");
        check(mc.prolepticYear(MinguoEra.BEFORE_ROC, 1) == 0, "prolepticYear BEFORE_ROC 1 = 0");

        // Minguo proleptic 109 == ISO 2020 (leap), 108 == ISO 2019 (not leap)
        check(mc.isLeapYear(109), "isLeapYear(109) true");
        check(!mc.isLeapYear(108), "isLeapYear(108) false");

        check(mc.range(ChronoField.MONTH_OF_YEAR).getMinimum() == 1
                && mc.range(ChronoField.MONTH_OF_YEAR).getMaximum() == 12, "MONTH_OF_YEAR range 1..12");

        MinguoDate d = mc.date(MinguoEra.ROC, 109, 1, 1);
        check(d.getEra() == MinguoEra.ROC, "date era");
        check(mc.dateEpochDay(d.toEpochDay()).equals(d), "date -> toEpochDay -> dateEpochDay round-trip");
        check(mc.date(109, 1, 1).equals(d), "date(109,1,1) equals date(ROC,109,1,1)");

        check(mc.dateNow() != null, "dateNow non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
