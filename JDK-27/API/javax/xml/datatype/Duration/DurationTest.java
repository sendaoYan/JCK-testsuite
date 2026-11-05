/*
 * @test
 * @summary javax.xml.datatype.Duration conformance: field access, arithmetic, comparison, equals/hashCode, sign, addTo.
 * @run main DurationTest
 */

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class DurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        DatatypeFactory f = DatatypeFactory.newInstance();

        // ---- field accessors ----
        Duration d = f.newDuration("P1Y2M3DT4H5M6S");
        check(d.getSign() == 1, "positive sign");
        check(d.getYears() == 1 && d.getMonths() == 2 && d.getDays() == 3, "year/month/day fields");
        check(d.getHours() == 4 && d.getMinutes() == 5 && d.getSeconds() == 6, "hour/minute/second fields");

        // ---- getField / isSet ----
        check(d.isSet(DatatypeConstants.YEARS) && d.isSet(DatatypeConstants.MONTHS)
              && d.isSet(DatatypeConstants.DAYS) && d.isSet(DatatypeConstants.HOURS)
              && d.isSet(DatatypeConstants.MINUTES) && d.isSet(DatatypeConstants.SECONDS),
              "all fields set");
        Duration d2 = f.newDuration("P2Y");
        check(!d2.isSet(DatatypeConstants.MONTHS), "unset field not set");
        check(d2.getField(DatatypeConstants.YEARS) != null, "getField set returns value");
        check(d2.getField(DatatypeConstants.MONTHS) == null, "getField unset returns null");

        // ---- negative sign ----
        Duration neg = f.newDuration("-P1Y2M");
        check(neg.getSign() == -1, "negative duration sign");

        // ---- arithmetic ----
        Duration sum = f.newDuration("P1Y2M").add(f.newDuration("P2Y3M"));
        check(sum.getYears() == 3 && sum.getMonths() == 5, "add");
        Duration diff = f.newDuration("P3Y").subtract(f.newDuration("P1Y"));
        check(diff.getYears() == 2, "subtract");
        Duration doubled = f.newDuration("P2Y").multiply(2);
        check(doubled.getYears() == 4, "multiply(int)");
        Duration negated = f.newDuration("P1Y").negate();
        check(negated.getSign() == -1 && negated.getYears() == 1, "negate");

        // ---- comparison ----
        Duration shorter = f.newDuration("P1Y");
        Duration longer = f.newDuration("P2Y");
        check(shorter.compare(longer) == DatatypeConstants.LESSER, "compare LESSER");
        check(longer.compare(shorter) == DatatypeConstants.GREATER, "compare GREATER");
        check(shorter.compare(f.newDuration("P1Y")) == DatatypeConstants.EQUAL, "compare EQUAL");
        check(shorter.isShorterThan(longer), "isShorterThan");
        check(longer.isLongerThan(shorter), "isLongerThan");

        // ---- equals / hashCode / toString ----
        check(d.equals(f.newDuration("P1Y2M3DT4H5M6S")), "equals same value");
        check(d.hashCode() == f.newDuration("P1Y2M3DT4H5M6S").hashCode(), "hashCode equal for equal value");
        check(!d.equals(longer), "equals false for different");
        check("P1Y2M3DT4H5M6S".equals(d.toString()), "toString lexical form");

        // ---- getXMLSchemaType ----
        check(DatatypeConstants.DURATION.equals(d.getXMLSchemaType()), "getXMLSchemaType DURATION");
        check(DatatypeConstants.DURATION_DAYTIME.equals(f.newDurationDayTime("P1DT2H3M4S").getXMLSchemaType()),
              "dayTime duration schema type");
        check(DatatypeConstants.DURATION_YEARMONTH.equals(f.newDurationYearMonth("P1Y2M").getXMLSchemaType()),
              "yearMonth duration schema type");

        // ---- addTo(Calendar) / addTo(Date) ----
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US);
        cal.clear();
        cal.set(2001, 0, 1, 0, 0, 0);
        f.newDuration("P1DT2H").addTo(cal);
        check(cal.get(java.util.Calendar.DAY_OF_MONTH) == 2 && cal.get(java.util.Calendar.HOUR_OF_DAY) == 2,
              "addTo(Calendar)");
        java.util.Date dt = new java.util.Date(0); // epoch, in UTC via getTimeInMillis semantics
        long before = dt.getTime();
        f.newDuration("PT1S").addTo(dt);
        check(dt.getTime() == before + 1000L, "addTo(Date) advances one second");

        testLexicalParseMatrix(f);
        testMillisFactory(f);
        testDayTimeYearMonthFactories(f);
        testArithmeticDeep(f);
        testCompareMatrix(f);
        testLengthInMillis(f);
        testRoundTripAndSchemaType(f);
        testAddToDateMonthEnd(f);

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: ISO 8601 lexical parse matrix =================
    static int jdkFeature() {
        String v = System.getProperty("java.specification.version"); // "1.8" or "27"
        int dot = v.indexOf('.');
        try {
            return dot < 0 ? Integer.parseInt(v) : Integer.parseInt(v.substring(dot + 1));
        } catch (RuntimeException e) {
            return -1;
        }
    }

    static void testLexicalParseMatrix(DatatypeFactory f) {
        // fractional second: getSeconds()==0, getField(SECONDS) carries the fraction
        Duration half = f.newDuration("PT0.5S");
        check(half.getSeconds() == 0, "PT0.5S getSeconds==0");
        check(half.getField(DatatypeConstants.SECONDS) != null
              && ((java.math.BigDecimal) half.getField(DatatypeConstants.SECONDS)).compareTo(new java.math.BigDecimal("0.5")) == 0,
              "PT0.5S fractional second 0.5 via getField");
        check(half.getSign() == 1, "PT0.5S positive");

        // pure time zero: sign==0, all numeric fields zero
        Duration zero = f.newDuration("PT0S");
        check(zero.getSign() == 0, "PT0S sign 0");
        check(zero.getYears() == 0 && zero.getMonths() == 0 && zero.getDays() == 0
              && zero.getHours() == 0 && zero.getMinutes() == 0 && zero.getSeconds() == 0,
              "PT0S all numeric fields zero");
        check("PT0S".equals(zero.toString()), "PT0S toString");

        // negative day-only
        Duration negDay = f.newDuration("-P1D");
        check(negDay.getSign() == -1 && negDay.getDays() == 1, "-P1D sign -1, day 1");
        check("-P1D".equals(negDay.toString()), "-P1D toString");

        // weeks are NOT supported by this implementation (recorded, both JDKs)
        boolean p1wIae = false;
        try { f.newDuration("P1W"); } catch (IllegalArgumentException e) { p1wIae = true; }
        catch (RuntimeException e) { /* unexpected type recorded by design */ }
        check(p1wIae, "P1W rejected with IllegalArgumentException (weeks unsupported)");

        // malformed lexicals -> IllegalArgumentException
        String[] bad = {"P", "PX", "P1X", "PT", "P1D1Y", "P1DT", "pt1h"};
        for (String s : bad) {
            boolean iae = false;
            try { f.newDuration(s); } catch (IllegalArgumentException e) { iae = true; }
            check(iae, "malformed '" + s + "' -> IllegalArgumentException");
        }

        // fractional non-second field: implementation reports NumberFormatException (recorded)
        boolean nfe = false;
        try { f.newDuration("P0.5Y"); } catch (NumberFormatException e) { nfe = true; }
        check(nfe, "P0.5Y -> NumberFormatException (impl quirk, locked)");

        // null lexical -> NullPointerException
        boolean npe = false;
        try { f.newDuration((String) null); } catch (NullPointerException e) { npe = true; }
        check(npe, "newDuration(null) -> NullPointerException");
    }

    static void testMillisFactory(DatatypeFactory f) {
        Duration oneDay = f.newDuration(86400000L);
        check(oneDay.getDays() == 1 && oneDay.getHours() == 0 && oneDay.getMinutes() == 0 && oneDay.getSeconds() == 0,
              "newDuration(86400000) == 1D0H0M0S");
        check(oneDay.getSign() == 1, "newDuration(86400000) positive");
        check("P0Y0M1DT0H0M0.000S".equals(oneDay.toString()), "newDuration(long) lexical keeps .000 fraction");

        Duration threeMin = f.newDuration(180000L);
        check(threeMin.getDays() == 0 && threeMin.getHours() == 0 && threeMin.getMinutes() == 3 && threeMin.getSeconds() == 0,
              "newDuration(180000) == 0D0H3M0S");

        Duration negHour = f.newDuration(-3600000L);
        check(negHour.getSign() == -1 && negHour.getHours() == 1, "newDuration(-3600000) negative sign, 1 hour");
        check(negHour.toString().startsWith("-P"), "negative millis lexical starts with -P");

        Duration zero = f.newDuration(0L);
        check(zero.getSign() == 0, "newDuration(0) sign 0");
    }

    static void testDayTimeYearMonthFactories(DatatypeFactory f) {
        // four-int dayTime factory; negative sign keeps year/month undefined
        Duration dt = f.newDurationDayTime(false, 1, 2, 3, 4);
        check(dt.getSign() == -1, "newDurationDayTime(false,...) sign -1");
        check(dt.getDays() == 1 && dt.getHours() == 2 && dt.getMinutes() == 3 && dt.getSeconds() == 4,
              "newDurationDayTime(false,1,2,3,4) field values");
        check(!dt.isSet(DatatypeConstants.YEARS) && !dt.isSet(DatatypeConstants.MONTHS),
              "dayTime duration leaves year/month unset");
        check(dt.getYears() == 0 && dt.getMonths() == 0, "unset fields read as 0");
        check("-P1DT2H3M4S".equals(dt.toString()), "newDurationDayTime(false,...) lexical");

        Duration ym = f.newDurationYearMonth(true, 2, 3);
        check(ym.getYears() == 2 && ym.getMonths() == 3 && ym.getSign() == 1, "newDurationYearMonth(true,2,3)");
        check(!ym.isSet(DatatypeConstants.DAYS) && !ym.isSet(DatatypeConstants.HOURS), "yearMonth leaves day/time unset");
        check("P2Y3M".equals(ym.toString()), "newDurationYearMonth(true,2,3) lexical");

        // long overloads
        check("P1DT1H1M1.000S".equals(f.newDurationDayTime(90061000L).toString()), "newDurationDayTime(long) field split");
        check("P1Y1M".equals(f.newDurationYearMonth(14L * 30 * 24 * 3600000L + 12345L).toString()),
              "newDurationYearMonth(long) field split");

        // BigInteger overloads
        java.math.BigInteger two = java.math.BigInteger.valueOf(2);
        Duration dtBi = f.newDurationDayTime(true, two, two, two, two);
        check("P2DT2H2M2S".equals(dtBi.toString()), "newDurationDayTime(BigInteger x4)");
        Duration ymBi = f.newDurationYearMonth(true, two, two);
        check("P2Y2M".equals(ymBi.toString()), "newDurationYearMonth(BigInteger x2)");

        // six-int newDuration
        Duration d6 = f.newDuration(true, 1, 2, 3, 4, 5, 6);
        check("P1Y2M3DT4H5M6S".equals(d6.toString()), "newDuration(boolean,int x6)");

        // invalid dayTime/yearMonth lexicals
        boolean iae1 = false, iae2 = false, iae3 = false;
        try { f.newDurationDayTime("P1Y"); } catch (IllegalArgumentException e) { iae1 = true; }
        try { f.newDurationYearMonth("P1D"); } catch (IllegalArgumentException e) { iae2 = true; }
        try { f.newDurationDayTime("PT1H"); } catch (IllegalArgumentException e) { iae3 = false; }
        check(iae1, "newDurationDayTime('P1Y') -> IAE (year not allowed)");
        check(iae2, "newDurationYearMonth('P1D') -> IAE (day not allowed)");
        check("PT1H".equals(f.newDurationDayTime("PT1H").toString()), "newDurationDayTime accepts pure time");
    }

    static void testArithmeticDeep(DatatypeFactory f) {
        // add: months and days accumulate INDEPENDENTLY, no carry between fields (semantics locked)
        Duration sum = f.newDuration("P1M25D").add(f.newDuration("P1M10D"));
        check(sum.getMonths() == 2 && sum.getDays() == 35, "P1M25D + P1M10D = P2M35D (month/day separation, no carry)");

        // adding the negation cancels per-field
        Duration cancel = f.newDuration("P1M25D").add(f.newDuration("-P1M10D"));
        check(cancel.getSign() == 1 && cancel.getMonths() == 0 && cancel.getDays() == 15,
              "P1M25D + (-P1M10D) = P0M15D");

        // subtract = add(negate)
        Duration diff = f.newDuration("P1M25D").subtract(f.newDuration("P1M10D"));
        check(diff.getMonths() == 0 && diff.getDays() == 15, "P1M25D - P1M10D = P0M15D");

        // per-field underflow makes the result sign-ambiguous -> IllegalStateException
        boolean ise = false;
        try { f.newDuration("P2M5D").subtract(f.newDuration("P1M10D")); }
        catch (IllegalStateException e) { ise = true; }
        check(ise, "P2M5D - P1M10D -> IllegalStateException (mixed-sign result)");

        // negate flips sign, double negate restores
        Duration neg = f.newDuration("-P1D");
        check("P1D".equals(neg.negate().toString()), "negate flips -P1D to P1D");
        check("-P1D".equals(neg.negate().negate().toString()), "negate().negate() restores -P1D");
        check(neg.negate().getSign() == 1, "negated sign +1");
        check(f.newDuration("P0Y").getSign() == 0, "zero duration sign 0");

        // multiply(int) and multiply(BigDecimal)
        check("P3D".equals(f.newDuration("P1D").multiply(3).toString()), "P1D * 3 = P3D");
        check("PT1.0S".equals(f.newDuration("PT0.5S").multiply(2).toString()), "PT0.5S * 2 = PT1.0S");
        check("PT0.25S".equals(f.newDuration("PT0.5S").multiply(new java.math.BigDecimal("0.5")).toString()),
              "PT0.5S * 0.5 = PT0.25S (BigDecimal)");
        check("P0DT6H".equals(f.newDuration("P1D").multiply(new java.math.BigDecimal("0.25")).toString()),
              "P1D * 0.25 = P0DT6H (sub-unit carry into hours)");
    }

    static void testCompareMatrix(DatatypeFactory f) {
        // cross-field equivalence: compare EQUAL
        check(f.newDuration("P1D").compare(f.newDuration("PT24H")) == DatatypeConstants.EQUAL,
              "P1D vs PT24H compare EQUAL");
        check(f.newDuration("PT1H").compare(f.newDuration("PT60M")) == DatatypeConstants.EQUAL,
              "PT1H vs PT60M compare EQUAL");
        check(f.newDuration("P1Y").compare(f.newDuration("P12M")) == DatatypeConstants.EQUAL,
              "P1Y vs P12M compare EQUAL");
        // this implementation: equals/hashCode follow compare for these pairs (locked on both JDKs)
        check(f.newDuration("P1D").equals(f.newDuration("PT24H")), "P1D equals PT24H (impl: equals follows compare)");
        check(f.newDuration("P1D").hashCode() == f.newDuration("PT24H").hashCode(),
              "P1D and PT24H share hashCode");
        check(f.newDuration("P1Y").equals(f.newDuration("P12M")), "P1Y equals P12M");

        // INDETERMINATE: month vs ambiguous day counts (no exception, returns 2)
        check(f.newDuration("P1M").compare(f.newDuration("P30D")) == DatatypeConstants.INDETERMINATE,
              "P1M vs P30D -> INDETERMINATE");
        check(f.newDuration("P1M").compare(f.newDuration("P29D")) == DatatypeConstants.INDETERMINATE,
              "P1M vs P29D -> INDETERMINATE");
        check(f.newDuration("P1Y").compare(f.newDuration("P366D")) == DatatypeConstants.INDETERMINATE,
              "P1Y vs P366D -> INDETERMINATE");
        check(f.newDuration("P1M").compare(f.newDuration("P32D")) == DatatypeConstants.LESSER,
              "P1M vs P32D -> LESSER (31 < 32 decided)");
        check(f.newDuration("PT1H").compare(f.newDuration("PT61M")) == DatatypeConstants.LESSER,
              "PT1H vs PT61M -> LESSER");
        check(f.newDuration("P1D").compare(f.newDuration("PT23H")) == DatatypeConstants.GREATER,
              "P1D vs PT23H -> GREATER");

        // JDK-8 vs JDK-27 divergence: legacy compare collapses P1M~P28D / P1Y~P365D to EQUAL
        boolean legacy = jdkFeature() <= 8;
        if (legacy) {
            check(f.newDuration("P1M").compare(f.newDuration("P28D")) == DatatypeConstants.EQUAL,
                  "JDK-8 legacy: P1M vs P28D compare EQUAL (bug preserved)");
            check(f.newDuration("P1M").equals(f.newDuration("P28D")),
                  "JDK-8 legacy: P1M equals P28D (but hashCodes differ, contract hole)");
            check(f.newDuration("P1M").hashCode() != f.newDuration("P28D").hashCode(),
                  "JDK-8 legacy: P1M/P28D equals-but-different-hashCode recorded");
            check(f.newDuration("P1Y").compare(f.newDuration("P365D")) == DatatypeConstants.EQUAL,
                  "JDK-8 legacy: P1Y vs P365D compare EQUAL");
        } else {
            check(f.newDuration("P1M").compare(f.newDuration("P28D")) == DatatypeConstants.INDETERMINATE,
                  "JDK-27: P1M vs P28D -> INDETERMINATE (compare fixed)");
            check(!f.newDuration("P1M").equals(f.newDuration("P28D")), "JDK-27: P1M !equals P28D");
            check(f.newDuration("P1Y").compare(f.newDuration("P365D")) == DatatypeConstants.INDETERMINATE,
                  "JDK-27: P1Y vs P365D -> INDETERMINATE (compare fixed)");
        }

        // compare(null) -> NPE
        boolean npe = false;
        try { f.newDuration("P1D").compare(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "Duration.compare(null) -> NullPointerException");
    }

    static void testLengthInMillis(DatatypeFactory f) {
        // pure day/time durations convert exactly
        check(f.newDuration("P1D").getTimeInMillis(new java.util.Date(0)) == 86400000L,
              "P1D getTimeInMillis == 86400000");
        check(f.newDuration("PT1H").getTimeInMillis(new java.util.Date(0)) == 3600000L,
              "PT1H getTimeInMillis == 3600000");
        check(f.newDuration(5000L).getTimeInMillis(new java.util.Date(0)) == 5000L,
              "newDuration(5000) getTimeInMillis == 5000");
        // durations with month/year are evaluated against the anchor date (epoch: Jan 1970),
        // no IllegalStateException on this implementation (recorded)
        check(f.newDuration("P1M").getTimeInMillis(new java.util.Date(0)) == 31L * 86400000L,
              "P1M from epoch == 31 days (Jan has 31 days)");
        check(f.newDuration("P1Y").getTimeInMillis(new java.util.Date(0)) == 365L * 86400000L,
              "P1Y from epoch == 365 days (1970 not a leap year)");
        // Calendar anchor gives the same magnitude
        java.util.Calendar cal = new java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US);
        cal.clear();
        cal.set(2024, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        check(f.newDuration("P1D").getTimeInMillis(cal) == 86400000L, "P1D getTimeInMillis(Calendar) == 86400000");
    }

    static void testRoundTripAndSchemaType(DatatypeFactory f) {
        // toString -> newDuration -> equals round-trip (core ISO 8601 closure)
        String[] forms = {"P1Y2M3DT4H5M6.7S", "-PT1H", "P1M", "PT0.5S", "-P1Y", "P0Y0M0DT0H0M0S",
                          "P2M35D", "-P1DT2H3M4S"};
        for (String s : forms) {
            Duration d = f.newDuration(s);
            Duration back = f.newDuration(d.toString());
            check(d.equals(back) && d.toString().equals(back.toString()),
                  "round-trip '" + s + "' -> '" + d + "' -> equals");
        }

        // getXMLSchemaType requires complete field groups
        check(DatatypeConstants.DURATION.equals(f.newDuration("P1Y2M3DT4H5M6S").getXMLSchemaType()),
              "full duration type DURATION");
        check(DatatypeConstants.DURATION_DAYTIME.equals(f.newDurationDayTime(true, 1, 2, 3, 4).getXMLSchemaType()),
              "dayTime factory yields DURATION_DAYTIME");
        check(DatatypeConstants.DURATION_YEARMONTH.equals(f.newDurationYearMonth(true, 1, 2).getXMLSchemaType()),
              "yearMonth factory yields DURATION_YEARMONTH");
        String[] partial = {"P1M", "P1D", "PT1H", "P1Y1D"};
        for (String s : partial) {
            boolean ise = false;
            try { f.newDuration(s).getXMLSchemaType(); } catch (IllegalStateException e) { ise = true; }
            check(ise, "getXMLSchemaType('" + s + "') -> IllegalStateException (partial field set)");
        }
    }

    static void testAddToDateMonthEnd(DatatypeFactory f) {
        // month-end clamping through addTo(Date) with a fixed GMT anchor
        java.util.GregorianCalendar gc = new java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US);
        gc.clear();
        gc.set(2023, java.util.Calendar.JANUARY, 31, 0, 0, 0);
        java.util.Date d = gc.getTime();
        f.newDuration("P1M").addTo(d);
        java.util.GregorianCalendar back = new java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US);
        back.setTime(d);
        check(back.get(java.util.Calendar.MONTH) == java.util.Calendar.FEBRUARY
              && back.get(java.util.Calendar.DAY_OF_MONTH) == 28,
              "2023-01-31 + P1M -> 2023-02-28 (non-leap clamp)");

        gc.clear();
        gc.set(2024, java.util.Calendar.JANUARY, 31, 0, 0, 0);
        d = gc.getTime();
        f.newDuration("P1M").addTo(d);
        back.setTime(d);
        check(back.get(java.util.Calendar.MONTH) == java.util.Calendar.FEBRUARY
              && back.get(java.util.Calendar.DAY_OF_MONTH) == 29,
              "2024-01-31 + P1M -> 2024-02-29 (leap-year clamp)");
    }
}
