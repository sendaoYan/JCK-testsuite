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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testUsSymbols();
        testMutation();
        testZoneStrings();
        testEqualsCloneHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
