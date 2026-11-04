/*
 * @test
 * @summary java.text.DateFormatSymbols conformance: white-box structure + black-box locale symbol access/mutation.
 * @run main DateFormatSymbolsTest
 */

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class DateFormatSymbolsTest {
    static int passed;
    static final Locale US = Locale.US;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.DateFormatSymbols");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(Locale.class) != null, "ctor(Locale)");
        check(DateFormatSymbols.getInstance() instanceof DateFormatSymbols, "getInstance() non-null");
        check(DateFormatSymbols.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testUsSymbols() {
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(US);
        check("January".equals(dfs.getMonths()[0]), "months[0]=January");
        check("December".equals(dfs.getMonths()[11]), "months[11]=December");
        check(dfs.getMonths().length == 13, "months length 13 (with empty 13th)");
        check("Jan".equals(dfs.getShortMonths()[0]), "shortMonths[0]=Jan");
        check("Sunday".equals(dfs.getWeekdays()[Calendar.SUNDAY]), "weekdays[SUNDAY]=Sunday");
        check("Thursday".equals(dfs.getWeekdays()[Calendar.THURSDAY]), "weekdays[THURSDAY]=Thursday");
        check("Sun".equals(dfs.getShortWeekdays()[Calendar.SUNDAY]), "shortWeekdays[SUNDAY]=Sun");
        check("BC".equals(dfs.getEras()[0]) && "AD".equals(dfs.getEras()[1]), "eras BC/AD");
        check("AM".equals(dfs.getAmPmStrings()[0]) && "PM".equals(dfs.getAmPmStrings()[1]), "amPm AM/PM");
        check("GyMdkHmsSEDFwWahKzZ".equals(dfs.getLocalPatternChars()), "localPatternChars");
    }

    static void testMutation() {
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(US);

        dfs.setMonths(new String[]{"M1","M2","M3","M4","M5","M6","M7","M8","M9","M10","M11","M12","M13"});
        check("M1".equals(dfs.getMonths()[0]), "setMonths/getMonths");

        dfs.setShortMonths(new String[]{"s1","s2","s3","s4","s5","s6","s7","s8","s9","s10","s11","s12","s13"});
        check("s1".equals(dfs.getShortMonths()[0]), "setShortMonths/getShortMonths");

        dfs.setEras(new String[]{"B","A"});
        check("A".equals(dfs.getEras()[1]), "setEras/getEras");

        dfs.setAmPmStrings(new String[]{"am","pm"});
        check("am".equals(dfs.getAmPmStrings()[0]), "setAmPmStrings/getAmPmStrings");

        dfs.setWeekdays(new String[]{"","W1","W2","W3","W4","W5","W6","W7"});
        check("W1".equals(dfs.getWeekdays()[Calendar.SUNDAY]), "setWeekdays/getWeekdays");

        dfs.setShortWeekdays(new String[]{"","w1","w2","w3","w4","w5","w6","w7"});
        check("w1".equals(dfs.getShortWeekdays()[Calendar.SUNDAY]), "setShortWeekdays/getShortWeekdays");

        dfs.setLocalPatternChars("abc");
        check("abc".equals(dfs.getLocalPatternChars()), "setLocalPatternChars/getLocalPatternChars");
    }

    static void testZoneStrings() {
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(US);
        check(dfs.getZoneStrings() != null && dfs.getZoneStrings().length > 0, "default zone strings non-empty");
        String[][] custom = {{"My/Zone", "My Zone Standard Time", "MZS", "My Zone Daylight Time", "MZD"}};
        dfs.setZoneStrings(custom);
        String[][] got = dfs.getZoneStrings();
        check("My/Zone".equals(got[0][0]) && "MZS".equals(got[0][2]), "setZoneStrings/getZoneStrings");
    }

    static void testEqualsCloneHashCode() {
        DateFormatSymbols a = DateFormatSymbols.getInstance(US);
        DateFormatSymbols b = DateFormatSymbols.getInstance(US);
        check(a.equals(b) && b.equals(a), "two US instances equal");
        check(a.hashCode() == b.hashCode(), "equal hashCode");
        DateFormatSymbols c = (DateFormatSymbols) a.clone();
        check(a.equals(c) && c.equals(a), "clone equals");
        check(a.hashCode() == c.hashCode(), "clone hashCode");
        DateFormatSymbols d = (DateFormatSymbols) a.clone();
        d.setMonths(new String[]{"X1","X2","X3","X4","X5","X6","X7","X8","X9","X10","X11","X12","X13"});
        check(!a.equals(d), "mutated clone not equal");
    }

    static void testBlackBox() {
        DateFormatSymbols us = new DateFormatSymbols(Locale.US);
        check("January".equals(us.getMonths()[0]), "ctor(Locale.US) months[0]");
        check("Sunday".equals(us.getWeekdays()[Calendar.SUNDAY]), "ctor(Locale.US) weekdays[SUNDAY]");
        check("AD".equals(us.getEras()[1]), "ctor(Locale.US) eras[1]");
        check("AM".equals(us.getAmPmStrings()[0]), "ctor(Locale.US) ampm[0]");

        DateFormatSymbols def = new DateFormatSymbols();
        check(def.getMonths() != null && def.getMonths().length == 13, "no-arg ctor months length 13");
        check(def.getWeekdays() != null && def.getWeekdays().length == 8, "no-arg ctor weekdays length 8");
        check(def.getEras() != null && def.getEras().length == 2, "no-arg ctor eras length 2");
        check(def.getAmPmStrings() != null && def.getAmPmStrings().length == 2, "no-arg ctor ampm length 2");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testEnglishDataContract() {
        DateFormatSymbols s = new DateFormatSymbols(Locale.ENGLISH);
        check("January".equals(s.getMonths()[0]) && "March".equals(s.getMonths()[2])
              && "December".equals(s.getMonths()[11]) && "".equals(s.getMonths()[12]),
              "en months table (13th slot empty)");
        check("Jan".equals(s.getShortMonths()[0]) && "Dec".equals(s.getShortMonths()[11])
              && "".equals(s.getShortMonths()[12]), "en shortMonths table");
        check("Sunday".equals(s.getWeekdays()[1]) && "Saturday".equals(s.getWeekdays()[7])
              && "".equals(s.getWeekdays()[0]), "en weekdays table (index 0 empty)");
        check("Sun".equals(s.getShortWeekdays()[1]) && "Sat".equals(s.getShortWeekdays()[7]),
              "en shortWeekdays table");
        check("AM".equals(s.getAmPmStrings()[0]) && "PM".equals(s.getAmPmStrings()[1]),
              "en am/pm table");
        check("BC".equals(s.getEras()[0]) && "AD".equals(s.getEras()[1]), "en eras table");
        check("GyMdkHmsSEDFwWahKzZ".equals(s.getLocalPatternChars()),
              "en local pattern chars are the standard set");
        check(java.util.Arrays.equals(new DateFormatSymbols(Locale.ENGLISH).getMonths(),
              DateFormatSymbols.getInstance(Locale.ENGLISH).getMonths()),
              "ctor(Locale) equals getInstance(Locale) data");
    }

    static void testSettersDriveFormatContract() {
        Calendar c = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        c.clear(); c.set(2024, Calendar.MARCH, 7, 14, 5, 9);
        java.util.Date d = c.getTime();
        DateFormatSymbols s = new DateFormatSymbols(Locale.ENGLISH);
        String[] months = s.getMonths().clone();
        months[2] = "MARCHXYZ";
        s.setMonths(months);
java.text.SimpleDateFormat f1 = new java.text.SimpleDateFormat("MMMM", s);
        f1.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        check("MARCHXYZ".equals(f1.format(d)), "setMonths drives MMMM output");
        s.setShortMonths(new String[]{"J","F","M","A","M","J","J","A","S","O","N","D",""});
java.text.SimpleDateFormat f2 = new java.text.SimpleDateFormat("MMM", s);
        f2.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        check("M".equals(f2.format(d)), "setShortMonths drives MMM output");
        s.setAmPmStrings(new String[]{"ante", "post"});
java.text.SimpleDateFormat f3 = new java.text.SimpleDateFormat("a", s);
        f3.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        check("post".equals(f3.format(d)), "setAmPmStrings drives a output");
        s.setEras(new String[]{"BCE", "CE"});
java.text.SimpleDateFormat f4 = new java.text.SimpleDateFormat("G", s);
        f4.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        check("CE".equals(f4.format(d)), "setEras drives G output");
        s.setWeekdays(new String[]{"", "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"});
java.text.SimpleDateFormat f5 = new java.text.SimpleDateFormat("EEEE", s);
        f5.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        check("THU".equals(f5.format(d)), "setWeekdays drives EEEE output");
        s.setShortWeekdays(new String[]{"", "Sn", "Mo", "Tu", "We", "Th", "Fr", "Sa"});
java.text.SimpleDateFormat f6 = new java.text.SimpleDateFormat("EEE", s);
        f6.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        check("Th".equals(f6.format(d)), "setShortWeekdays drives EEE output");
    }

    static void testZoneStringsContract() {
        String[][] zs = new DateFormatSymbols(Locale.ENGLISH).getZoneStrings();
        check(zs != null && zs.length > 0, "getZoneStrings non-empty");
        boolean found = false;
        for (String[] row : zs) if ("Asia/Shanghai".equals(row[0])) { found = true; break; }
        check(found, "zone table contains Asia/Shanghai");
    }

    static void testEqualsHashCodeCloneContract() {
        DateFormatSymbols a = new DateFormatSymbols(Locale.ENGLISH);
        DateFormatSymbols b = new DateFormatSymbols(Locale.ENGLISH);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "same-locale equals/hashCode");
        check(a.clone().equals(a), "clone equals");
        DateFormatSymbols m = (DateFormatSymbols) a.clone();
        m.setAmPmStrings(new String[]{"x", "y"});
        check(!m.equals(a), "mutation breaks equality");
        check(!a.equals(new DateFormatSymbols(Locale.GERMAN)), "different locale not equal");
        expectX(NullPointerException.class, () -> new DateFormatSymbols(null),
                "ctor(null) -> NPE");
        expectX(NullPointerException.class, () -> DateFormatSymbols.getInstance(null),
                "getInstance(null) -> NPE");
    }

    static void testVersionLockedLocaleDataContract() {
        // JDK-27 CLDR locale data
        DateFormatSymbols de = new DateFormatSymbols(Locale.GERMAN);
        check("M\u00E4rz".equals(de.getMonths()[2]), "de March (CLDR)");
        check(java.util.Arrays.equals(de.getShortMonths(),
              new String[]{"Jan.","Feb.","M\u00E4rz","Apr.","Mai","Juni","Juli","Aug.","Sept.","Okt.","Nov.","Dez.",""}),
              "de shortMonths end with periods (CLDR)");
        check(java.util.Arrays.equals(de.getShortWeekdays(),
              new String[]{"","So.","Mo.","Di.","Mi.","Do.","Fr.","Sa."}),
              "de shortWeekdays end with periods (CLDR)");
        check("v. Chr.".equals(de.getEras()[0]) && "n. Chr.".equals(de.getEras()[1]), "de eras (CLDR)");
        check("GyMdkHmsSEDFwWahKzZ".equals(de.getLocalPatternChars()),
              "de local pattern chars equal standard set (CLDR)");
        DateFormatSymbols fr = new DateFormatSymbols(Locale.FRENCH);
        check("av. J.-C.".equals(fr.getEras()[0]), "fr BC era localized (CLDR)");
        check("GyMdkHmsSEDFwWahKzZ".equals(fr.getLocalPatternChars()), "fr local pattern chars (CLDR)");
        DateFormatSymbols ja = new DateFormatSymbols(Locale.JAPANESE);
        check("1\u6708".equals(ja.getShortMonths()[0]), "ja shortMonths carry month suffix (CLDR)");
        check("\u7D00\u5143\u524D".equals(ja.getEras()[0]), "ja era (CLDR)");
        DateFormatSymbols root = new DateFormatSymbols(Locale.ROOT);
        check("Jan".equals(root.getMonths()[0]), "ROOT months are short names (CLDR)");
        check("BCE".equals(root.getEras()[0]) && "CE".equals(root.getEras()[1]), "ROOT eras BCE/CE (CLDR)");
        check(new DateFormatSymbols(Locale.ENGLISH).getZoneStrings().length == 632,
              "en zone table size (CLDR data, JDK-27.0.1)");
    }
    public static void main(String[] args) throws Exception {
        testEnglishDataContract();
        testSettersDriveFormatContract();
        testZoneStringsContract();
        testEqualsHashCodeCloneContract();
        testVersionLockedLocaleDataContract();
        testWhiteBox();
        testUsSymbols();
        testMutation();
        testZoneStrings();
        testEqualsCloneHashCode();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
