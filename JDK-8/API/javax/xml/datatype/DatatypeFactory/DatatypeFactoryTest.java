/*
 * @test
 * @summary javax.xml.datatype.DatatypeFactory conformance: factory boundary, Duration/XMLGregorianCalendar production, precise null/invalid exceptions.
 * @run main DatatypeFactoryTest
 */

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;

public class DatatypeFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }

    public static void main(String[] args) throws Exception {
        // ---- factory boundary ----
        DatatypeFactory f = DatatypeFactory.newInstance();
        check(f != null, "newInstance non-null");
        ClassLoader cl = DatatypeFactory.class.getClassLoader();
        DatatypeFactory f2 = DatatypeFactory.newInstance(
                "com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl", cl);
        check(f2 != null, "newInstance(className, loader) non-null");

        // ---- newDuration(String) ----
        Duration d = f.newDuration("P1Y2M3DT4H5M6S");
        check(d.getYears() == 1 && d.getMonths() == 2 && d.getDays() == 3
              && d.getHours() == 4 && d.getMinutes() == 5 && d.getSeconds() == 6,
              "newDuration lexically parses all fields");
        check(d.getSign() == 1, "positive duration sign");
        check(isNpe(() -> f.newDuration((String) null)), "newDuration(null) -> NPE");
        check(isIae(() -> f.newDuration("not-a-duration")), "newDuration(invalid) -> IAE");

        // ---- newDuration(long) ----
        Duration dl = f.newDuration(5000L);
        check(dl.getSeconds() == 5, "newDuration(long) millis -> seconds");

        // ---- dayTime / yearMonth duration ----
        Duration dt = f.newDurationDayTime("P3DT4H");
        check(dt.getDays() == 3 && dt.getHours() == 4 && dt.getYears() == 0 && dt.getMonths() == 0,
              "newDurationDayTime restricts to day/time fields");
        Duration ym = f.newDurationYearMonth("P1Y2M");
        check(ym.getYears() == 1 && ym.getMonths() == 2 && ym.getDays() == 0,
              "newDurationYearMonth restricts to year/month fields");
        check(isIae(() -> f.newDurationDayTime("bad")), "newDurationDayTime(invalid) -> IAE");
        check(isIae(() -> f.newDurationYearMonth("bad")), "newDurationYearMonth(invalid) -> IAE");

        // ---- newDuration BigInteger overloads ----
        Duration big = f.newDuration(true, BigInteger.ONE, BigInteger.valueOf(2),
                BigInteger.valueOf(3), BigInteger.valueOf(4), BigInteger.valueOf(5),
                new BigDecimal("6.5"));
        check(big.getYears() == 1 && big.getMonths() == 2 && big.getDays() == 3
              && big.getHours() == 4 && big.getMinutes() == 5 && big.getSign() == 1,
              "newDuration BigInteger overload parses fields");

        // ---- newXMLGregorianCalendar ----
        XMLGregorianCalendar c = f.newXMLGregorianCalendar();
        check(c != null, "newXMLGregorianCalendar() non-null");
        XMLGregorianCalendar c2 = f.newXMLGregorianCalendar("2001-10-26T21:32:52Z");
        check(c2.getYear() == 2001 && c2.getMonth() == 10 && c2.getDay() == 26
              && c2.getHour() == 21 && c2.getMinute() == 32 && c2.getSecond() == 52
              && c2.getTimezone() == 0, "newXMLGregorianCalendar(String) parses fields");
        GregorianCalendar gc = new GregorianCalendar(2001, 9, 26, 21, 32, 52); // month is 0-based
        gc.clear(java.util.Calendar.MILLISECOND);
        XMLGregorianCalendar c3 = f.newXMLGregorianCalendar(gc);
        check(c3.getYear() == 2001 && c3.getMonth() == 10 && c3.getDay() == 26,
              "newXMLGregorianCalendar(GregorianCalendar)");
        check(isNpe(() -> f.newXMLGregorianCalendar((String) null)), "newXMLGregorianCalendar(null) -> NPE");
        check(isIae(() -> f.newXMLGregorianCalendar("not-a-date")), "newXMLGregorianCalendar(invalid) -> IAE");
        check(isNpe(() -> f.newXMLGregorianCalendar((GregorianCalendar) null)), "newXMLGregorianCalendar(null gc) -> NPE");

        // ---- convenience date/time factories ----
        XMLGregorianCalendar date = f.newXMLGregorianCalendarDate(2001, 10, 26, 0);
        check(date.getYear() == 2001 && date.getMonth() == 10 && date.getDay() == 26 && date.getTimezone() == 0,
              "newXMLGregorianCalendarDate");
        XMLGregorianCalendar time = f.newXMLGregorianCalendarTime(21, 32, 52, 0);
        check(time.getHour() == 21 && time.getMinute() == 32 && time.getSecond() == 52,
              "newXMLGregorianCalendarTime");
        check(isIae(() -> f.newXMLGregorianCalendarDate(2001, 13, 26, 0)), "newXMLGregorianCalendarDate(month 13) -> IAE");
        check(isIae(() -> f.newXMLGregorianCalendarTime(10, 60, 0, 0)), "newXMLGregorianCalendarTime(minute 60) -> IAE");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
