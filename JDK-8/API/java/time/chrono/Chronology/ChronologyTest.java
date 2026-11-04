/*
 * @test
 * @summary java.time.chrono.Chronology conformance: via IsoChronology.
 * @run main ChronologyTest
 */

import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.DateTimeException;
import java.time.chrono.HijrahChronology;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.MinguoChronology;
import java.time.chrono.ThaiBuddhistChronology;

public class ChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronology() {
        Chronology c = IsoChronology.INSTANCE;
        check("ISO".equals(c.getId()), "getId");
        check("iso8601".equals(c.getCalendarType()), "getCalendarType");
        check(c.isLeapYear(2020), "isLeapYear 2020");
        check(!c.isLeapYear(2019), "isLeapYear 2019 false");
        check(c.prolepticYear(IsoEra.CE, 2020) == 2020, "prolepticYear CE");
        check(c.prolepticYear(IsoEra.BCE, 1) == 0, "prolepticYear BCE");
        check(c.eraOf(1) == IsoEra.CE, "eraOf 1");
        check(c.eras().size() == 2, "two eras");
        check(c.date(2020, 1, 1).equals(LocalDate.of(2020, 1, 1)), "date");
        check(c.dateEpochDay(0).equals(LocalDate.of(1970, 1, 1)), "dateEpochDay");
        check(c.dateNow() != null, "dateNow");
        check(c.getDisplayName(TextStyle.FULL, Locale.ENGLISH) != null, "getDisplayName");
        check(c.period(1, 2, 3) != null, "period");
        check(c.compareTo(IsoChronology.INSTANCE) == 0, "compareTo");
        check(Chronology.of("ISO") == IsoChronology.INSTANCE, "Chronology.of ISO");
    }

    public static void main(String[] args) {
        testChronology();
        testDeepChronology();
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


    static void testDeepChronology() {
        java.util.SortedSet<Chronology> all = new java.util.TreeSet<Chronology>(Chronology.getAvailableChronologies());
        check(all.size() == 5, "five chronologies");
        check(Chronology.of("ISO").equals(IsoChronology.INSTANCE), "of ISO");
        check(Chronology.of("japanese").equals(JapaneseChronology.INSTANCE), "of japanese case-insensitive");
        check(Chronology.of("Minguo").equals(MinguoChronology.INSTANCE), "of Minguo");
        check(Chronology.of("ThaiBuddhist").equals(ThaiBuddhistChronology.INSTANCE), "of ThaiBuddhist");
        check(Chronology.of("Hijrah-umalqura").equals(HijrahChronology.INSTANCE), "of Hijrah-umalqura");
        xeq(JapaneseChronology.INSTANCE.getId() + "|" + JapaneseChronology.INSTANCE.getCalendarType(), "Japanese|japanese", "jp ids");
        xeq(MinguoChronology.INSTANCE.getId() + "|" + MinguoChronology.INSTANCE.getCalendarType(), "Minguo|roc", "mg ids");
        xeq(ThaiBuddhistChronology.INSTANCE.getId() + "|" + ThaiBuddhistChronology.INSTANCE.getCalendarType(),
            "ThaiBuddhist|buddhist", "tb ids");
        xeq(HijrahChronology.INSTANCE.getId() + "|" + HijrahChronology.INSTANCE.getCalendarType(),
            "Hijrah-umalqura|islamic-umalqura", "hj ids");
        xeq(IsoChronology.INSTANCE.getId() + "|" + IsoChronology.INSTANCE.getCalendarType(), "ISO|iso8601", "iso ids");
        check(Chronology.ofLocale(java.util.Locale.JAPAN).equals(IsoChronology.INSTANCE), "ofLocale ja-JP");
        check(Chronology.ofLocale(new java.util.Locale("th", "TH")).equals(IsoChronology.INSTANCE),
            "ofLocale th-TH plain is ISO");
        check(Chronology.ofLocale(java.util.Locale.forLanguageTag("th-TH-u-ca-buddhist")).equals(ThaiBuddhistChronology.INSTANCE),
            "ofLocale u-ca-buddhist");
        check(Chronology.ofLocale(java.util.Locale.forLanguageTag("ja-JP-u-ca-japanese")).equals(JapaneseChronology.INSTANCE),
            "ofLocale u-ca-japanese");
        check(Chronology.ofLocale(java.util.Locale.forLanguageTag("en-US-u-ca-islamic-umalqura")).equals(HijrahChronology.INSTANCE),
            "ofLocale u-ca-islamic-umalqura");
        check(Chronology.ofLocale(new java.util.Locale("ja", "JP", "JP")).equals(JapaneseChronology.INSTANCE),
            "ofLocale ja-JP-JP variant");
        check(Chronology.ofLocale(java.util.Locale.US).equals(IsoChronology.INSTANCE), "ofLocale US");
        xact(() -> Chronology.of(null), NullPointerException.class, "id", "of null");
        xact(() -> Chronology.of("Bogus"), java.time.DateTimeException.class, "Unknown chronology: Bogus", "of Bogus");
        xact(() -> Chronology.of(""), java.time.DateTimeException.class, "Unknown chronology: ", "of empty");
        xact(() -> Chronology.of("iso"), java.time.DateTimeException.class, "Unknown chronology: iso", "of lowercase iso");
        xact(() -> Chronology.ofLocale(null), NullPointerException.class, "locale", "ofLocale null");
        xact(() -> IsoChronology.INSTANCE.eraOf(5), java.time.DateTimeException.class, "Invalid era: 5", "iso eraOf 5");
        xeq(IsoChronology.INSTANCE.eraOf(0).toString(), "BCE", "iso eraOf 0");
        xeq(IsoChronology.INSTANCE.eraOf(1).toString(), "CE", "iso eraOf 1");
    }

}
