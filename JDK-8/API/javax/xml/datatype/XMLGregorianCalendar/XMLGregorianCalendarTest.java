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

        testFullParseMatrix(f);
        testToXMLFormatMatrix(f);
        testInvalidLexicals(f);
        testFactoryOverloads(f);
        testSameInstantCompare(f);
        testAddMonthEndClamp(f);
        testNormalizeDeep(f);
        testValidityMatrix(f);
        testFractionalInterplay(f);
        testNegativeYearAndEon(f);
        testEmptyAndNull(f);
        testGregorianRoundTrip(f);

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: lexical parse + getter matrix =================
    static void testFullParseMatrix(DatatypeFactory f) {
        XMLGregorianCalendar c = f.newXMLGregorianCalendar("2024-06-15T10:30:00.500+08:00");
        check(c.getYear() == 2024 && c.getMonth() == 6 && c.getDay() == 15, "full parse date fields");
        check(c.getHour() == 10 && c.getMinute() == 30 && c.getSecond() == 0, "full parse time fields (second 0 with fraction)");
        check(c.getMillisecond() == 500, "full parse getMillisecond 500");
        check(c.getFractionalSecond() != null
              && c.getFractionalSecond().compareTo(new java.math.BigDecimal("0.5")) == 0,
              "full parse fractional second 0.500");
        check(c.getTimezone() == 480, "full parse timezone 480 (+08:00)");
        check(c.getEon() == null && c.getEonAndYear().intValue() == 2024, "full parse eon/year");
        check(DatatypeConstants.DATETIME.equals(c.getXMLSchemaType()), "full parse schema type dateTime");
    }

    static void testToXMLFormatMatrix(DatatypeFactory f) {
        String[][] cases = {
            {"2024-06-15", "2024-06-15"},
            {"2024-06-15T10:30:00", "2024-06-15T10:30:00"},
            {"2024-06-15T10:30:00.5", "2024-06-15T10:30:00.5"},
            {"2024-06-15T10:30:00.500+08:00", "2024-06-15T10:30:00.500+08:00"},
            {"2024-06-15T10:30:00Z", "2024-06-15T10:30:00Z"},
            {"2024-06-15T10:30:00-05:00", "2024-06-15T10:30:00-05:00"},
            {"10:30:00", "10:30:00"},
            {"10:30:00.123", "10:30:00.123"},
            {"2024-06", "2024-06"},
            {"--06-15", "--06-15"},
            {"---15", "---15"},
            {"2024", "2024"},
            {"2024Z", "2024Z"},
            {"2024-06-15T10:30:00+14:00", "2024-06-15T10:30:00+14:00"},
        };
        for (String[] c : cases) {
            XMLGregorianCalendar x = f.newXMLGregorianCalendar(c[0]);
            check(c[1].equals(x.toXMLFormat()), "toXMLFormat('" + c[0] + "') == '" + c[1] + "'");
            check(x.toString().equals(x.toXMLFormat()), "toString==toXMLFormat for '" + c[0] + "'");
        }

        // schema type per lexical shape
        check(DatatypeConstants.DATE.equals(f.newXMLGregorianCalendar("2024-06-15").getXMLSchemaType()), "gtype date");
        check(DatatypeConstants.TIME.equals(f.newXMLGregorianCalendar("10:30:00").getXMLSchemaType()), "gtype time");
        check(DatatypeConstants.GYEARMONTH.equals(f.newXMLGregorianCalendar("2024-06").getXMLSchemaType()), "gtype gYearMonth");
        check(DatatypeConstants.GMONTHDAY.equals(f.newXMLGregorianCalendar("--06-15").getXMLSchemaType()), "gtype gMonthDay");
        check(DatatypeConstants.GDAY.equals(f.newXMLGregorianCalendar("---15").getXMLSchemaType()), "gtype gDay");
        check(DatatypeConstants.GYEAR.equals(f.newXMLGregorianCalendar("2024").getXMLSchemaType()), "gtype gYear");

        // unset timezone drops the suffix entirely
        XMLGregorianCalendar u = f.newXMLGregorianCalendar("2024-06-15T10:30:00+08:00");
        u.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
        check(u.getTimezone() == DatatypeConstants.FIELD_UNDEFINED, "setTimezone(FIELD_UNDEFINED) unsets tz");
        check("2024-06-15T10:30:00".equals(u.toXMLFormat()), "unset tz -> no suffix");

        // 24:00:00 lexical normalizes to midnight of the next day
        XMLGregorianCalendar t24 = f.newXMLGregorianCalendar("2024-06-15T24:00:00Z");
        check("2024-06-16T00:00:00Z".equals(t24.toXMLFormat()), "T24:00:00 normalizes to next-day 00:00:00");
        check(t24.getDay() == 16 && t24.getHour() == 0, "24:00 -> day+1 hour 0");

        // offset minutes >= 60 normalize (+08:60 -> +09:00)
        XMLGregorianCalendar tz60 = f.newXMLGregorianCalendar("2024-06-15T10:30:00.500+08:60");
        check(tz60.getTimezone() == 540, "+08:60 parsed as 540 minutes");
        check("2024-06-15T10:30:00.500+09:00".equals(tz60.toXMLFormat()), "+08:60 lexical -> +09:00");

        // gMonth lexical '--06--' is rejected by this implementation (recorded, both JDKs)
        boolean iae = false;
        try { f.newXMLGregorianCalendar("--06--"); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "gMonth lexical '--06--' rejected with IAE (impl quirk, locked)");
    }

    static void testInvalidLexicals(DatatypeFactory f) {
        // (input, expected message fragment)
        String[][] cases = {
            {"2024-13-01", "Month"},
            {"2024-06-32", "Day"},
            {"2024-06-15T25:00:00", "Hour"},
            {"2024-06-15T10:61:00", "Minute"},
            {"2024-06-15T10:30:00+99:00", "5,940"},
            {"2024-6-5", "2024-6-5"},
            {"garbage", "garbage"},
        };
        for (String[] c : cases) {
            boolean iae = false;
            String msg = null;
            try { f.newXMLGregorianCalendar(c[0]); }
            catch (IllegalArgumentException e) { iae = true; msg = e.getMessage(); }
            check(iae, "lexical '" + c[0] + "' -> IllegalArgumentException");
            check(msg != null && msg.contains(c[1]), "message contains '" + c[1] + "' for '" + c[0] + "'");
        }
        // Feb 30 fails schema date validity with the "not a valid representation" shape
        boolean iaeFeb = false;
        String febMsg = null;
        try { f.newXMLGregorianCalendar("2024-02-30"); }
        catch (IllegalArgumentException e) { iaeFeb = true; febMsg = e.getMessage(); }
        check(iaeFeb, "2024-02-30 -> IllegalArgumentException");
        check(febMsg != null && febMsg.contains("not a valid representation"), "Feb-30 message shape");
    }

    static void testFactoryOverloads(DatatypeFactory f) {
        // GregorianCalendar mapping with fixed GMT+08:00 zone
        GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT+08:00"), java.util.Locale.US);
        gc.clear();
        gc.set(2024, java.util.Calendar.JUNE, 15, 10, 30, 0);
        gc.set(java.util.Calendar.MILLISECOND, 500);
        XMLGregorianCalendar mc = f.newXMLGregorianCalendar(gc);
        check("2024-06-15T10:30:00.500+08:00".equals(mc.toXMLFormat()), "GregorianCalendar mapping exact");
        check(mc.getTimezone() == 480, "GregorianCalendar timezone mapping 480");

        // 8-arg int factory: ms=0 prints ".000" because the field is set
        XMLGregorianCalendar c8 = f.newXMLGregorianCalendar(2024, 6, 15, 10, 30, 0, 0, DatatypeConstants.FIELD_UNDEFINED);
        check("2024-06-15T10:30:00.000".equals(c8.toXMLFormat()), "8-arg with ms=0 keeps .000");

        // 8-arg int factory IAEs with precise messages
        String[][] bad = {
            {"MONTH13", "Month"},
            {"DAY32", "Day"},
            {"HOUR25", "Hour"},
            {"TZ100000", "100,000"},
        };
        for (String[] b : bad) {
            boolean iae = false;
            String msg = null;
            try {
                if (b[0].equals("MONTH13")) f.newXMLGregorianCalendar(2024, 13, 15, 10, 30, 0, 500, 480);
                else if (b[0].equals("DAY32")) f.newXMLGregorianCalendar(2024, 6, 32, 10, 30, 0, 500, 480);
                else if (b[0].equals("HOUR25")) f.newXMLGregorianCalendar(2024, 6, 15, 25, 30, 0, 500, 480);
                else f.newXMLGregorianCalendar(2024, 6, 15, 10, 30, 0, 500, 100000);
            } catch (IllegalArgumentException e) { iae = true; msg = e.getMessage(); }
            check(iae, "8-arg factory " + b[0] + " -> IAE");
            check(msg != null && msg.contains(b[1]), "8-arg " + b[0] + " message contains '" + b[1] + "'");
        }

        // BigInteger year + BigDecimal fraction overload
        XMLGregorianCalendar bi = f.newXMLGregorianCalendar(java.math.BigInteger.valueOf(2024), 6, 15, 10, 30, 0,
                new java.math.BigDecimal("0.500"), 480);
        check("2024-06-15T10:30:00.500+08:00".equals(bi.toXMLFormat()), "BigInteger/BigDecimal ctor exact");

        // date-only / time-only factories
        check("2024-06-15".equals(f.newXMLGregorianCalendarDate(2024, 6, 15, DatatypeConstants.FIELD_UNDEFINED).toXMLFormat()),
              "newXMLGregorianCalendarDate undefined tz");
        check("10:30:00Z".equals(f.newXMLGregorianCalendarTime(10, 30, 0, 0).toXMLFormat()),
              "newXMLGregorianCalendarTime(int tz)");
        check("10:30:00.5Z".equals(f.newXMLGregorianCalendarTime(10, 30, 0, new java.math.BigDecimal("0.5"), 0).toXMLFormat()),
              "newXMLGregorianCalendarTime(BigDecimal) fraction");
        XMLGregorianCalendar tms = f.newXMLGregorianCalendarTime(10, 30, 0, 5, 0);
        check("10:30:00.005Z".equals(tms.toXMLFormat()) && tms.getMillisecond() == 5,
              "newXMLGregorianCalendarTime 5-arg (4th int is milliseconds)");
    }

    static void testSameInstantCompare(DatatypeFactory f) {
        XMLGregorianCalendar plus8 = f.newXMLGregorianCalendar("2024-06-15T10:30:00+08:00");
        XMLGregorianCalendar utc = f.newXMLGregorianCalendar("2024-06-15T02:30:00Z");
        check(plus8.compare(utc) == DatatypeConstants.EQUAL, "+08:00 vs Z same instant compare EQUAL");
        // implementation: equals/hashCode follow compare for same-instant values (locked, both JDKs)
        check(plus8.equals(utc), "same instant different tz spelling equals (impl follows compare)");
        check(plus8.hashCode() == utc.hashCode(), "same instant hashCode equal");
        // strict lexical equality is field-based
        check(plus8.getTimezone() == 480 && utc.getTimezone() == 0, "tz fields keep their offsets");

        // partial (undefined-field) lexicals -> INDETERMINATE against fuller values
        check(f.newXMLGregorianCalendar("2024").compare(f.newXMLGregorianCalendar("2024-06-15")) == DatatypeConstants.INDETERMINATE,
              "gYear vs date -> INDETERMINATE");
        check(f.newXMLGregorianCalendar("2024-06").compare(f.newXMLGregorianCalendar("2024-06-15")) == DatatypeConstants.INDETERMINATE,
              "gYearMonth vs date -> INDETERMINATE");
        check(f.newXMLGregorianCalendar("2024-06").compare(f.newXMLGregorianCalendar("2023-06")) == DatatypeConstants.GREATER,
              "gYearMonth ordering GREATER");
        check(f.newXMLGregorianCalendar("2024-06").compare(f.newXMLGregorianCalendar("2024-07")) == DatatypeConstants.LESSER,
              "gYearMonth ordering LESSER");
        // no-tz vs tz: same fields, timezone unknown -> INDETERMINATE
        check(f.newXMLGregorianCalendar("2024-06-15T10:30:00").compare(f.newXMLGregorianCalendar("2024-06-15T10:30:00Z")) == DatatypeConstants.INDETERMINATE,
              "undefined tz vs Z -> INDETERMINATE");
    }

    static void testAddMonthEndClamp(DatatypeFactory f) {
        XMLGregorianCalendar e1 = f.newXMLGregorianCalendar("2023-01-31");
        e1.add(f.newDuration("P1M"));
        check("2023-02-28".equals(e1.toXMLFormat()), "2023-01-31 + P1M -> 2023-02-28 (non-leap clamp)");

        XMLGregorianCalendar e2 = f.newXMLGregorianCalendar("2024-01-31");
        e2.add(f.newDuration("P1M"));
        check("2024-02-29".equals(e2.toXMLFormat()), "2024-01-31 + P1M -> 2024-02-29 (leap-year clamp)");

        XMLGregorianCalendar e3 = f.newXMLGregorianCalendar("2024-01-31");
        e3.add(f.newDuration("-P1M"));
        check("2023-12-31".equals(e3.toXMLFormat()), "2024-01-31 - P1M -> 2023-12-31");

        XMLGregorianCalendar e4 = f.newXMLGregorianCalendar("2024-03-31");
        e4.add(f.newDuration("P1D"));
        check("2024-04-01".equals(e4.toXMLFormat()), "2024-03-31 + P1D -> 2024-04-01 (month rollover)");

        XMLGregorianCalendar e5 = f.newXMLGregorianCalendar("2024-06-15");
        e5.add(f.newDuration("PT25H"));
        check("2024-06-16".equals(e5.toXMLFormat()), "2024-06-15 + PT25H -> 2024-06-16 (date-only gains day)");
    }

    static void testNormalizeDeep(DatatypeFactory f) {
        XMLGregorianCalendar n1 = f.newXMLGregorianCalendar("2024-06-15T10:30:00+08:00");
        XMLGregorianCalendar r1 = n1.normalize();
        check("2024-06-15T02:30:00Z".equals(r1.toXMLFormat()), "normalize(+08:00) -> same instant as Z");
        check(n1.compare(r1) == DatatypeConstants.EQUAL, "normalized value same instant as original");
        check(!n1.toXMLFormat().equals(r1.toXMLFormat()), "normalize changes representation");

        XMLGregorianCalendar n2 = f.newXMLGregorianCalendar("2024-06-15T10:30:00-12:00");
        check("2024-06-15T22:30:00Z".equals(n2.normalize().toXMLFormat()), "normalize(-12:00) -> same instant as Z");

        XMLGregorianCalendar n3 = f.newXMLGregorianCalendar("2024-06-15T10:30:00");
        check("2024-06-15T10:30:00".equals(n3.normalize().toXMLFormat()), "normalize without tz returns equivalent value");
    }

    static void testValidityMatrix(DatatypeFactory f) {
        XMLGregorianCalendar feb30 = f.newXMLGregorianCalendar();
        feb30.setYear(2024); feb30.setMonth(2); feb30.setDay(30);
        check(!feb30.isValid(), "Feb 30 invalid");
        check("2024-02-30".equals(feb30.toXMLFormat()), "invalid value still formats lexically");

        XMLGregorianCalendar feb29 = f.newXMLGregorianCalendar();
        feb29.setYear(2024); feb29.setMonth(2); feb29.setDay(29);
        check(feb29.isValid(), "Feb 29 valid in leap year");

        XMLGregorianCalendar feb29nl = f.newXMLGregorianCalendar();
        feb29nl.setYear(2023); feb29nl.setMonth(2); feb29nl.setDay(29);
        check(!feb29nl.isValid(), "Feb 29 invalid in non-leap year");

        XMLGregorianCalendar y0 = f.newXMLGregorianCalendar();
        y0.setYear(0); y0.setMonth(1); y0.setDay(1);
        check(!y0.isValid(), "year 0 invalid under XML Schema 1.0");
        check("0000-01-01".equals(y0.toXMLFormat()), "year 0 formats as 0000");

        // day-only value is considered valid (fields independent)
        XMLGregorianCalendar dayOnly = f.newXMLGregorianCalendar();
        dayOnly.setDay(15);
        check(dayOnly.isValid(), "day-only value valid");
    }

    static void testFractionalInterplay(DatatypeFactory f) {
        XMLGregorianCalendar fr = f.newXMLGregorianCalendar("2024-06-15T10:30:00Z");
        fr.setMillisecond(250);
        check(fr.getMillisecond() == 250, "setMillisecond(250)");
        check(fr.getFractionalSecond() != null
              && fr.getFractionalSecond().compareTo(new java.math.BigDecimal("0.25")) == 0,
              "setMillisecond reflected in getFractionalSecond");
        check("2024-06-15T10:30:00.250Z".equals(fr.toXMLFormat()), "millisecond 250 lexical");

        fr.setFractionalSecond(new java.math.BigDecimal("0.123456"));
        check(fr.getMillisecond() == 123, "fraction .123456 -> getMillisecond 123 (truncation)");
        check("2024-06-15T10:30:00.123456Z".equals(fr.toXMLFormat()), "sub-millisecond fraction kept in lexical");

        fr.setFractionalSecond(null);
        check(fr.getMillisecond() == DatatypeConstants.FIELD_UNDEFINED, "setFractionalSecond(null) clears ms");
        check("2024-06-15T10:30:00Z".equals(fr.toXMLFormat()), "cleared fraction drops from lexical");

        fr.setMillisecond(60);
        check("2024-06-15T10:30:00.060Z".equals(fr.toXMLFormat()), "setTime(h,m,s,ms) 4th arg is milliseconds");

        fr.setTime(23, 59, 59, new java.math.BigDecimal("0.999"));
        check("2024-06-15T23:59:59.999Z".equals(fr.toXMLFormat()), "setTime with BigDecimal fraction");
        check(fr.getHour() == 23 && fr.getMinute() == 59 && fr.getSecond() == 59, "setTime fields");
    }

    static void testNegativeYearAndEon(DatatypeFactory f) {
        XMLGregorianCalendar neg = f.newXMLGregorianCalendar();
        neg.setYear(-500); neg.setMonth(1); neg.setDay(1);
        check(neg.getYear() == -500, "setYear(-500) preserved");
        check("-0500-01-01".equals(neg.toXMLFormat()), "negative year lexical -0500-01-01");
        check(neg.isValid(), "negative year is valid");
        check(neg.getEon() == null, "getEon null for small year");
        check(neg.getEonAndYear() != null && neg.getEonAndYear().intValue() == -500, "getEonAndYear -500");

        XMLGregorianCalendar big = f.newXMLGregorianCalendar();
        big.setYear(java.math.BigInteger.valueOf(10000));
        big.setMonth(1); big.setDay(1);
        check(big.getYear() == 10000, "setYear(BigInteger 10000) getYear 10000");
        check(big.isValid(), "5-digit year valid");
        check("10000-01-01".equals(big.toXMLFormat()), "5-digit year lexical");
    }

    static void testEmptyAndNull(DatatypeFactory f) {
        XMLGregorianCalendar empty = f.newXMLGregorianCalendar();
        check(empty.isValid(), "all-undefined calendar isValid true (recorded)");
        check(empty.getYear() == DatatypeConstants.FIELD_UNDEFINED
              && empty.getMonth() == DatatypeConstants.FIELD_UNDEFINED
              && empty.getDay() == DatatypeConstants.FIELD_UNDEFINED
              && empty.getHour() == DatatypeConstants.FIELD_UNDEFINED
              && empty.getTimezone() == DatatypeConstants.FIELD_UNDEFINED,
              "all-undefined getters");
        boolean ise1 = false, ise2 = false;
        try { empty.toXMLFormat(); } catch (IllegalStateException e) { ise1 = true; }
        try { empty.getXMLSchemaType(); } catch (IllegalStateException e) { ise2 = true; }
        check(ise1, "all-undefined toXMLFormat -> IllegalStateException");
        check(ise2, "all-undefined getXMLSchemaType -> IllegalStateException");

        boolean npe1 = false, npe2 = false;
        try { f.newXMLGregorianCalendar("2024-06-15").compare(null); } catch (NullPointerException e) { npe1 = true; }
        try { f.newXMLGregorianCalendar("2024-06-15").add(null); } catch (NullPointerException e) { npe2 = true; }
        check(npe1, "compare(null) -> NullPointerException");
        check(npe2, "add(null) -> NullPointerException");
    }

    static void testGregorianRoundTrip(DatatypeFactory f) {
        XMLGregorianCalendar c = f.newXMLGregorianCalendar("2024-06-15T10:30:00+08:00");
        GregorianCalendar gc = c.toGregorianCalendar();
        check(gc.getTimeZone().getRawOffset() == 480 * 60 * 1000, "toGregorianCalendar keeps +08:00 raw offset");
        // explicit UTC target gives the exact epoch millis of the instant
        GregorianCalendar utc = c.toGregorianCalendar(TimeZone.getTimeZone("GMT"), java.util.Locale.US, null);
        check(utc.getTimeInMillis() == 1718447400000L, "toGregorianCalendar(UTC) exact epoch (2024-06-15T02:30:00Z)");
        check(c.getTimeZone(DatatypeConstants.FIELD_UNDEFINED).getRawOffset() == 480 * 60 * 1000,
              "getTimeZone(FIELD_UNDEFINED) falls back to own offset");
        // when this calendar HAS a timezone the argument is ignored (recorded, both JDKs)
        check(c.getTimeZone(0).getRawOffset() == 480 * 60 * 1000, "defined tz: getTimeZone(0) still returns own +08:00");
        // with no timezone set, the argument is honored
        XMLGregorianCalendar noTz = f.newXMLGregorianCalendar("2024-06-15T10:30:00");
        check(noTz.getTimeZone(0).getRawOffset() == 0, "undefined tz: getTimeZone(0) is GMT+00:00");
        check(noTz.getTimeZone(60).getRawOffset() == 60 * 60 * 1000, "undefined tz: getTimeZone(60) is GMT+01:00");
    }
}
