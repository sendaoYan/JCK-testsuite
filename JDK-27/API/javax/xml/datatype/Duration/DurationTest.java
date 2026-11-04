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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
