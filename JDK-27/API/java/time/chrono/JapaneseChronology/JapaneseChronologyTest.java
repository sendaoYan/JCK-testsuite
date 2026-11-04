/*
 * @test
 * @summary java.time.chrono.JapaneseChronology conformance: white-box structure + black-box behavior.
 * @run main JapaneseChronologyTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.chrono.AbstractChronology;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.JapaneseEra;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.TextStyle;

public class JapaneseChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name, e); }
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try { r.run(); } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.JapaneseChronology");
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
        JapaneseChronology jc = JapaneseChronology.INSTANCE;
        check("Japanese".equals(jc.getId()), "getId");
        check("japanese".equals(jc.getCalendarType()), "getCalendarType");
        check(jc.isIsoBased(), "isIsoBased");

        // Chronology.getAvailableChronologies() sorted, contains the concrete calendars
        List<String> ids = new ArrayList<>();
        for (Chronology ch : Chronology.getAvailableChronologies()) ids.add(ch.getId());
        java.util.Collections.sort(ids);
        check(ids.contains("Japanese"), "available: Japanese");
        check(ids.contains("Minguo"), "available: Minguo");
        check(ids.contains("ThaiBuddhist"), "available: ThaiBuddhist");
        check(ids.contains("Hijrah-umalqura"), "available: Hijrah-umalqura");
        check(ids.contains("ISO"), "available: ISO");

        List<Era> eras = jc.eras();
        check(eras.contains(JapaneseEra.HEISEI), "eras contains HEISEI");
        check(eras.contains(JapaneseEra.REIWA), "eras contains REIWA");
        check(eras.size() >= 4, "at least 4 eras");

        check(jc.eraOf(-1) == JapaneseEra.MEIJI, "eraOf(-1) MEIJI");
        check(jc.eraOf(0) == JapaneseEra.TAISHO, "eraOf(0) TAISHO");
        check(jc.eraOf(1) == JapaneseEra.SHOWA, "eraOf(1) SHOWA");
        check(jc.eraOf(2) == JapaneseEra.HEISEI, "eraOf(2) HEISEI");
        check(jc.eraOf(3) == JapaneseEra.REIWA, "eraOf(3) REIWA");

        check(jc.prolepticYear(JapaneseEra.HEISEI, 6) == 1994, "prolepticYear HEISEI 6 = 1994");
        check(jc.prolepticYear(JapaneseEra.SHOWA, 1) == 1926, "prolepticYear SHOWA 1 = 1926");

        check(jc.isLeapYear(2020), "isLeapYear(2020) true");
        check(!jc.isLeapYear(2021), "isLeapYear(2021) false");

        check(jc.range(ChronoField.MONTH_OF_YEAR).getMinimum() == 1
                && jc.range(ChronoField.MONTH_OF_YEAR).getMaximum() == 12, "MONTH_OF_YEAR range 1..12");

        JapaneseDate d = jc.date(JapaneseEra.HEISEI, 6, 1, 1);
        check(d.getEra() == JapaneseEra.HEISEI, "date era");
        check(jc.dateEpochDay(d.toEpochDay()).equals(d), "date -> toEpochDay -> dateEpochDay round-trip");
        check(jc.date(1994, 1, 1).equals(d), "date(1994,1,1) equals date(HEISEI,6,1,1)");

        check(jc.dateNow() != null, "dateNow non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepJapaneseChronology();
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


    static void testDeepJapaneseChronology() {
        xeq(JapaneseChronology.INSTANCE.getId() + "|" + JapaneseChronology.INSTANCE.getCalendarType(),
            "Japanese|japanese", "ids");
        check(JapaneseChronology.INSTANCE.isLeapYear(2024), "leap 2024");
        check(!JapaneseChronology.INSTANCE.isLeapYear(2023), "not leap 2023");
        xeq(JapaneseChronology.INSTANCE.period(1, 2, 3).toString(), "Japanese P1Y2M3D", "period toString");
        xeq(JapaneseChronology.INSTANCE.eraOf(-1).toString(), "Meiji", "eraOf -1");
        xeq(JapaneseChronology.INSTANCE.eraOf(0).toString(), "Taisho", "eraOf 0");
        xeq(JapaneseChronology.INSTANCE.eraOf(3).toString(), "Reiwa", "eraOf 3");
        xact(() -> JapaneseChronology.INSTANCE.eraOf(6), java.time.DateTimeException.class, "Invalid era: 6", "eraOf 6");
        xact(() -> JapaneseChronology.INSTANCE.eraOf(99), java.time.DateTimeException.class, "Invalid era: 99", "eraOf 99");
        check(JapaneseChronology.INSTANCE.eras().size() == 5, "five eras");
        xeq(JapaneseChronology.INSTANCE.dateEpochDay(19889L).toString(), "Japanese Reiwa 6-06-15", "dateEpochDay");
        xeq(JapaneseChronology.INSTANCE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Japanese Calendar", "display name");
        java.time.Clock fixed = java.time.Clock.fixed(java.time.Instant.parse("2024-06-15T10:15:30Z"), java.time.ZoneOffset.UTC);
        xeq(JapaneseChronology.INSTANCE.dateNow(fixed).toString(), "Japanese Reiwa 6-06-15", "dateNow fixed clock");
        check(JapaneseDate.of(2024, 6, 15).range(java.time.temporal.ChronoField.YEAR_OF_ERA).toString().equals("1 - 292276976"),
            "reiwa year range");
        check(JapaneseDate.of(JapaneseEra.SHOWA, 2, 1, 8).range(java.time.temporal.ChronoField.YEAR_OF_ERA).toString()
            .equals("1 - 63"), "showa year range");
    }

}
