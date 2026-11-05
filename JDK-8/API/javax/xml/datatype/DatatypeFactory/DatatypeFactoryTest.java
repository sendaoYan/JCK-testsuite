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

        testFactoryConstantsAndDiscovery();
        testDurationOverloads(f);
        testExceptionChains();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: lookup mechanism + full overload family =================
    static void testFactoryConstantsAndDiscovery() throws Exception {
        check("javax.xml.datatype.DatatypeFactory".equals(DatatypeFactory.DATATYPEFACTORY_PROPERTY),
              "DATATYPEFACTORY_PROPERTY value");
        check("com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl"
              .equals(DatatypeFactory.DATATYPEFACTORY_IMPLEMENTATION_CLASS),
              "DATATYPEFACTORY_IMPLEMENTATION_CLASS value");

        // discovery negative: system property names a non-existent implementation class
        System.setProperty(DatatypeFactory.DATATYPEFACTORY_PROPERTY, "com.example.NoSuchDatatypeFactoryImpl");
        try {
            boolean dce = false;
            String msg = null;
            try { DatatypeFactory.newInstance(); }
            catch (DatatypeConfigurationException e) { dce = true; msg = e.getMessage(); }
            check(dce, "bogus system property -> DatatypeConfigurationException");
            check(msg != null && msg.contains("Provider") && msg.contains("not found"),
                  "DCE message mentions provider not found");
        } finally {
            System.clearProperty(DatatypeFactory.DATATYPEFACTORY_PROPERTY);
        }
        check(System.getProperty(DatatypeFactory.DATATYPEFACTORY_PROPERTY) == null, "system property cleared");
        check(DatatypeFactory.newInstance() != null, "newInstance recovers after property cleared");

        // named-class newInstance with unknown class
        boolean dce2 = false;
        Throwable cause2 = null;
        try { DatatypeFactory.newInstance("com.example.NoClass", DatatypeFactoryTest.class.getClassLoader()); }
        catch (DatatypeConfigurationException e) { dce2 = true; cause2 = e.getCause(); }
        check(dce2, "newInstance(unknown class, loader) -> DatatypeConfigurationException");
        check(cause2 instanceof ClassNotFoundException, "DCE cause is ClassNotFoundException");

        // subclass instance validates the abstract factory contract surface via reflection
        check(java.lang.reflect.Modifier.isPublic(DatatypeFactory.class.getModifiers()), "DatatypeFactory is public");
        check(java.lang.reflect.Modifier.isAbstract(DatatypeFactory.class.getModifiers()), "is abstract");
        check(java.lang.reflect.Modifier.isProtected(DatatypeFactory.class.getDeclaredConstructors()[0].getModifiers()),
              "sole constructor is protected");
    }

    static void testDurationOverloads(DatatypeFactory f) {
        // six-int newDuration(boolean,int,int,int,int,int,int)
        Duration d6 = f.newDuration(true, 1, 2, 3, 4, 5, 6);
        check("P1Y2M3DT4H5M6S".equals(d6.toString()) && d6.getSign() == 1, "newDuration(boolean,int x6)");

        // long overload field decomposition (deterministic split)
        check("P0Y0M1DT0H0M0.000S".equals(f.newDuration(86400000L).toString()), "newDuration(long 1 day) lexical");
        Duration threeMin = f.newDuration(180000L);
        check(threeMin.getDays() == 0 && threeMin.getHours() == 0 && threeMin.getMinutes() == 3 && threeMin.getSeconds() == 0,
              "newDuration(180000) -> 3 minutes");
        Duration negHour = f.newDuration(-3600000L);
        check(negHour.getSign() == -1 && negHour.getHours() == 1, "newDuration(-3600000) negative");

        // dayTime / yearMonth int + BigInteger + long + lexical overloads
        Duration dtNeg = f.newDurationDayTime(false, 1, 2, 3, 4);
        check(dtNeg.getSign() == -1 && "-P1DT2H3M4S".equals(dtNeg.toString()), "newDurationDayTime(false,1,2,3,4)");
        check(!dtNeg.isSet(javax.xml.datatype.DatatypeConstants.YEARS), "dayTime leaves years unset");
        Duration ym = f.newDurationYearMonth(true, 2, 3);
        check("P2Y3M".equals(ym.toString()) && !ym.isSet(javax.xml.datatype.DatatypeConstants.DAYS), "newDurationYearMonth(true,2,3)");
        java.math.BigInteger two = java.math.BigInteger.valueOf(2);
        check("P2DT2H2M2S".equals(f.newDurationDayTime(true, two, two, two, two).toString()), "newDurationDayTime(BigInteger x4)");
        check("P2Y2M".equals(f.newDurationYearMonth(true, two, two).toString()), "newDurationYearMonth(BigInteger x2)");
        check("P1DT1H1M1.000S".equals(f.newDurationDayTime(90061000L).toString()), "newDurationDayTime(long)");
        check("P1Y1M".equals(f.newDurationYearMonth(14L * 30 * 24 * 3600000L + 12345L).toString()), "newDurationYearMonth(long)");
        check("PT1H".equals(f.newDurationDayTime("PT1H").toString()), "newDurationDayTime(lexical)");
        check(isIae(() -> f.newDurationDayTime("P1Y")), "newDurationDayTime('P1Y') -> IAE");
        check(isIae(() -> f.newDurationYearMonth("P1D")), "newDurationYearMonth('P1D') -> IAE");
        check(isIae(() -> f.newDuration("P1W")), "newDuration('P1W') -> IAE (weeks unsupported)");

        // XMLGregorianCalendar overload family
        XMLGregorianCalendar c8 = f.newXMLGregorianCalendar(2024, 6, 15, 10, 30, 0, 0, javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED);
        check("2024-06-15T10:30:00.000".equals(c8.toXMLFormat()), "newXMLGregorianCalendar(int x8) ms=0 prints .000");
        XMLGregorianCalendar bi = f.newXMLGregorianCalendar(java.math.BigInteger.valueOf(2024), 6, 15, 10, 30, 0,
                new java.math.BigDecimal("0.500"), 480);
        check("2024-06-15T10:30:00.500+08:00".equals(bi.toXMLFormat()), "newXMLGregorianCalendar(BigInteger,BigDecimal,int)");
        GregorianCalendar gc = new GregorianCalendar(java.util.TimeZone.getTimeZone("GMT+08:00"), java.util.Locale.US);
        gc.clear();
        gc.set(2024, java.util.Calendar.JUNE, 15, 10, 30, 0);
        gc.set(java.util.Calendar.MILLISECOND, 500);
        check("2024-06-15T10:30:00.500+08:00".equals(f.newXMLGregorianCalendar(gc).toXMLFormat()),
              "newXMLGregorianCalendar(GregorianCalendar GMT+08:00) exact mapping");
        check("2024-06-15".equals(f.newXMLGregorianCalendarDate(2024, 6, 15, javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED).toXMLFormat()),
              "newXMLGregorianCalendarDate undefined tz");
        check("10:30:00Z".equals(f.newXMLGregorianCalendarTime(10, 30, 0, 0).toXMLFormat()),
              "newXMLGregorianCalendarTime int-tz");
        check("10:30:00.5Z".equals(f.newXMLGregorianCalendarTime(10, 30, 0, new java.math.BigDecimal("0.5"), 0).toXMLFormat()),
              "newXMLGregorianCalendarTime BigDecimal-tz");
        check("10:30:00.005Z".equals(f.newXMLGregorianCalendarTime(10, 30, 0, 5, 0).toXMLFormat()),
              "newXMLGregorianCalendarTime 5-int (milliseconds)");
        boolean iaeTz = false;
        String tzMsg = null;
        try { f.newXMLGregorianCalendar(2024, 6, 15, 10, 30, 0, 500, 100000); }
        catch (IllegalArgumentException e) { iaeTz = true; tzMsg = e.getMessage(); }
        check(iaeTz && tzMsg != null && tzMsg.contains("100,000"), "8-arg tz out of range -> IAE '100,000'");
    }

    static void testExceptionChains() {
        DatatypeConfigurationException e1 = new DatatypeConfigurationException("boom");
        check("boom".equals(e1.getMessage()) && e1.getCause() == null, "DCE(String)");
        RuntimeException root = new RuntimeException("root");
        DatatypeConfigurationException e2 = new DatatypeConfigurationException("boom", root);
        check("boom".equals(e2.getMessage()) && e2.getCause() == root, "DCE(String,Throwable)");
        DatatypeConfigurationException e3 = new DatatypeConfigurationException(root);
        check(e3.getCause() == root && e3.getMessage() != null, "DCE(Throwable) propagates cause, message from toString");
        DatatypeConfigurationException e4 = new DatatypeConfigurationException();
        check(e4.getMessage() == null && e4.getCause() == null, "DCE() no-arg");
        check(e1 instanceof Exception && e2 instanceof Exception, "DCE is an Exception");
    }
}
