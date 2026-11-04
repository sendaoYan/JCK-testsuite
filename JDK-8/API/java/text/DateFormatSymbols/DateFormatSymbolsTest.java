/*
 * @test
 * @summary java.text.DateFormatSymbols conformance: era/month/weekday/amPm arrays and clone.
 * @run main DateFormatSymbolsTest
 */

import java.text.DateFormatSymbols;
import java.util.Locale;

public class DateFormatSymbolsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateFormatSymbols() {
        DateFormatSymbols s = DateFormatSymbols.getInstance(Locale.US);
        check(s.getEras().length == 2, "getEras length");
        check(s.getMonths().length == 13, "getMonths length");
        check(s.getShortMonths().length == 13, "getShortMonths length");
        check(s.getWeekdays().length == 8, "getWeekdays length");
        check(s.getShortWeekdays().length == 8, "getShortWeekdays length");
        check(s.getAmPmStrings().length == 2, "getAmPmStrings length");
        check(s.getZoneStrings().length > 0, "getZoneStrings non-empty");
        check(s.getLocalPatternChars().length() > 0, "getLocalPatternChars");

        s.setEras(new String[]{"BC", "AD"});
        check("AD".equals(s.getEras()[1]), "setEras");
        s.setAmPmStrings(new String[]{"am", "pm"});
        check("pm".equals(s.getAmPmStrings()[1]), "setAmPmStrings");
        s.setLocalPatternChars("GyMdkHmsSEDFwWahKzZ");
        check(s.getLocalPatternChars().length() > 0, "setLocalPatternChars");

        DateFormatSymbols s2 = (DateFormatSymbols) s.clone();
        check(s.equals(s2), "clone equals");
        check(s.hashCode() == s2.hashCode(), "clone hashCode");

        check(DateFormatSymbols.getInstance(Locale.US)
                .equals(DateFormatSymbols.getInstance(Locale.US)), "getInstance equals");
        check(DateFormatSymbols.getAvailableLocales().length > 0, "getAvailableLocales");
    }

    public static void main(String[] args) {
        testDateFormatSymbols();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
