/*
 * @test
 * @summary javax.xml.datatype.XMLGregorianCalendar conformance: field access/mutation, timezone, comparison, normalize, equals, toXMLFormat, add, clone.
 * @run main XMLGregorianCalendarTest
 */

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import java.util.GregorianCalendar;
import java.util.TimeZone;

public class XMLGregorianCalendarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }

    public static void main(String[] args) throws Exception {
        DatatypeFactory f = DatatypeFactory.newInstance();

        // ---- parse with timezone offset ----
        XMLGregorianCalendar c = f.newXMLGregorianCalendar("2001-10-26T21:32:52+08:00");
        check(c.getYear() == 2001 && c.getMonth() == 10 && c.getDay() == 26, "date fields");
        check(c.getHour() == 21 && c.getMinute() == 32 && c.getSecond() == 52, "time fields");
        check(c.getTimezone() == 480, "timezone +08:00 == 480 minutes");
        check(c.isValid(), "isValid true");
        check("2001-10-26T21:32:52+08:00".equals(c.toXMLFormat()), "toXMLFormat round-trip");
        check(c.toXMLFormat().equals(c.toString()), "toString == toXMLFormat");

        // ---- timezone variants ----
        XMLGregorianCalendar utc = f.newXMLGregorianCalendar("2001-10-26T21:32:52Z");
        check(utc.getTimezone() == 0, "Z == 0 timezone");
        XMLGregorianCalendar neg = f.newXMLGregorianCalendar("2001-10-26T21:32:52-05:00");
        check(neg.getTimezone() == -300, "-05:00 == -300 minutes");
        XMLGregorianCalendar noTz = f.newXMLGregorianCalendar("2001-10-26T21:32:52");
        check(noTz.getTimezone() == DatatypeConstants.FIELD_UNDEFINED, "no timezone -> FIELD_UNDEFINED");

        // ---- field accessors ----
        XMLGregorianCalendar big = f.newXMLGregorianCalendar("2001-10-26T21:32:52.123456789+00:00");
        check(big.getEon() == null && big.getEonAndYear() != null
              && big.getEonAndYear().intValue() == 2001, "eon/year accessors");
        check(big.getMillisecond() == 123, "getMillisecond from fractional second");
        check(big.getFractionalSecond() != null
              && big.getFractionalSecond().compareTo(new java.math.BigDecimal("0.123456789")) == 0,
              "getFractionalSecond");

        // ---- field mutation ----
        XMLGregorianCalendar m = f.newXMLGregorianCalendar();
        m.setYear(2001); m.setMonth(10); m.setDay(26);
        m.setHour(21); m.setMinute(32); m.setSecond(52);
        m.setTimezone(480);
        check(m.getYear() == 2001 && m.getMonth() == 10 && m.getDay() == 26
              && m.getHour() == 21 && m.getMinute() == 32 && m.getSecond() == 52
              && m.getTimezone() == 480, "field setters");
        m.setMillisecond(250);
        check(m.getMillisecond() == 250, "setMillisecond");
        m.setFractionalSecond(new java.math.BigDecimal("0.5"));
        check(m.getFractionalSecond().compareTo(new java.math.BigDecimal("0.5")) == 0, "setFractionalSecond");
        m.setTime(10, 11, 12);
        check(m.getHour() == 10 && m.getMinute() == 11 && m.getSecond() == 12, "setTime(h,m,s)");
        check(isIae(() -> m.setMonth(13)), "setMonth(13) -> IAE");
        check(isIae(() -> m.setDay(0)), "setDay(0) -> IAE");
        check(isIae(() -> m.setHour(-1)), "setHour(-1) -> IAE");

        // ---- clear/reset ----
        m.clear();
        check(m.getYear() == DatatypeConstants.FIELD_UNDEFINED && m.getMonth() == DatatypeConstants.FIELD_UNDEFINED,
              "clear resets fields to FIELD_UNDEFINED");
        check(m.getTimezone() == DatatypeConstants.FIELD_UNDEFINED, "clear resets timezone");
        m.reset();
        check(m.getYear() == DatatypeConstants.FIELD_UNDEFINED, "reset resets fields");

        // ---- comparison ----
        XMLGregorianCalendar earlier = f.newXMLGregorianCalendar("2001-10-26T21:32:52+08:00");
        XMLGregorianCalendar later = f.newXMLGregorianCalendar("2001-10-26T22:32:52+08:00");
        check(earlier.compare(later) == DatatypeConstants.LESSER, "compare LESSER");
        check(later.compare(earlier) == DatatypeConstants.GREATER, "compare GREATER");
        check(earlier.compare(f.newXMLGregorianCalendar("2001-10-26T21:32:52+08:00")) == DatatypeConstants.EQUAL, "compare EQUAL");

        // ---- equals / hashCode ----
        XMLGregorianCalendar same = f.newXMLGregorianCalendar("2001-10-26T21:32:52+08:00");
        check(earlier.equals(same), "equals same value");
        check(earlier.hashCode() == same.hashCode(), "hashCode equal for equal value");
        check(!earlier.equals(later), "equals false for different");

        // ---- normalize ----
        XMLGregorianCalendar norm = f.newXMLGregorianCalendar("2001-10-26T24:00:00");
        XMLGregorianCalendar normalized = norm.normalize();
        check(normalized != null, "normalize returns instance");
        check(normalized.getHour() != 24 || normalized.getDay() != 26, "normalize carries overflow");

        // ---- add(Duration) ----
        XMLGregorianCalendar base = f.newXMLGregorianCalendar("2001-01-01T00:00:00Z");
        base.add(f.newDuration("P1DT2H"));
        check(base.getDay() == 2 && base.getHour() == 2, "add(Duration) advances day and hour");

        // ---- toGregorianCalendar ----
        GregorianCalendar gcal = earlier.toGregorianCalendar();
        check(gcal.getTimeZone().getRawOffset() == 480 * 60 * 1000, "toGregorianCalendar preserves timezone");
        GregorianCalendar gcal2 = earlier.toGregorianCalendar(TimeZone.getTimeZone("UTC"), java.util.Locale.US, null);
        check(gcal2.getTimeZone().equals(TimeZone.getTimeZone("UTC")), "toGregorianCalendar with explicit timezone");
        TimeZone tz = earlier.getTimeZone(DatatypeConstants.FIELD_UNDEFINED);
        check(tz != null, "getTimeZone non-null");

        // ---- clone ----
        XMLGregorianCalendar cloned = (XMLGregorianCalendar) earlier.clone();
        check(cloned.equals(earlier), "clone equals original");
        cloned.setYear(1999);
        check(cloned.getYear() == 1999 && earlier.getYear() == 2001, "clone is independent");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
