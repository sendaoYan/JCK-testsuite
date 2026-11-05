/*
 * @test
 * @summary javax.xml.datatype.DatatypeConstants conformance: months, comparison, FIELD_UNDEFINED, schema QNames, timezone limits, Field identity.
 * @run main DatatypeConstantsTest
 */

import javax.xml.datatype.DatatypeConstants;
import javax.xml.namespace.QName;

public class DatatypeConstantsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        // ---- months 1..12 ----
        check(DatatypeConstants.JANUARY == 1, "JANUARY == 1");
        check(DatatypeConstants.FEBRUARY == 2, "FEBRUARY == 2");
        check(DatatypeConstants.MARCH == 3, "MARCH == 3");
        check(DatatypeConstants.APRIL == 4, "APRIL == 4");
        check(DatatypeConstants.MAY == 5, "MAY == 5");
        check(DatatypeConstants.JUNE == 6, "JUNE == 6");
        check(DatatypeConstants.JULY == 7, "JULY == 7");
        check(DatatypeConstants.AUGUST == 8, "AUGUST == 8");
        check(DatatypeConstants.SEPTEMBER == 9, "SEPTEMBER == 9");
        check(DatatypeConstants.OCTOBER == 10, "OCTOBER == 10");
        check(DatatypeConstants.NOVEMBER == 11, "NOVEMBER == 11");
        check(DatatypeConstants.DECEMBER == 12, "DECEMBER == 12");

        // ---- comparison results ----
        check(DatatypeConstants.LESSER == -1, "LESSER == -1");
        check(DatatypeConstants.EQUAL == 0, "EQUAL == 0");
        check(DatatypeConstants.GREATER == 1, "GREATER == 1");
        check(DatatypeConstants.INDETERMINATE == 2, "INDETERMINATE == 2");

        // ---- FIELD_UNDEFINED ----
        check(DatatypeConstants.FIELD_UNDEFINED == Integer.MIN_VALUE, "FIELD_UNDEFINED == Integer.MIN_VALUE");

        // ---- schema QName constants ----
        check(DatatypeConstants.DATETIME.equals(new QName("http://www.w3.org/2001/XMLSchema", "dateTime")), "DATETIME");
        check(DatatypeConstants.TIME.equals(new QName("http://www.w3.org/2001/XMLSchema", "time")), "TIME");
        check(DatatypeConstants.DATE.equals(new QName("http://www.w3.org/2001/XMLSchema", "date")), "DATE");
        check(DatatypeConstants.GYEARMONTH.equals(new QName("http://www.w3.org/2001/XMLSchema", "gYearMonth")), "GYEARMONTH");
        check(DatatypeConstants.GMONTHDAY.equals(new QName("http://www.w3.org/2001/XMLSchema", "gMonthDay")), "GMONTHDAY");
        check(DatatypeConstants.GYEAR.equals(new QName("http://www.w3.org/2001/XMLSchema", "gYear")), "GYEAR");
        check(DatatypeConstants.GMONTH.equals(new QName("http://www.w3.org/2001/XMLSchema", "gMonth")), "GMONTH");
        check(DatatypeConstants.GDAY.equals(new QName("http://www.w3.org/2001/XMLSchema", "gDay")), "GDAY");
        check(DatatypeConstants.DURATION.equals(new QName("http://www.w3.org/2001/XMLSchema", "duration")), "DURATION");
        check(DatatypeConstants.DURATION_DAYTIME.equals(new QName("http://www.w3.org/2003/11/xpath-datatypes", "dayTimeDuration")), "DURATION_DAYTIME QName");
        check(DatatypeConstants.DURATION_YEARMONTH.equals(new QName("http://www.w3.org/2003/11/xpath-datatypes", "yearMonthDuration")), "DURATION_YEARMONTH QName");

        // ---- timezone limits (W3C schema convention: east is negative) ----
        check(DatatypeConstants.MAX_TIMEZONE_OFFSET == -14 * 60, "MAX_TIMEZONE_OFFSET == -840");
        check(DatatypeConstants.MIN_TIMEZONE_OFFSET == 14 * 60, "MIN_TIMEZONE_OFFSET == 840");

        // ---- Field identity ----
        check(DatatypeConstants.YEARS == DatatypeConstants.YEARS, "YEARS identity");
        check(DatatypeConstants.MONTHS == DatatypeConstants.MONTHS, "MONTHS identity");
        check(DatatypeConstants.DAYS == DatatypeConstants.DAYS, "DAYS identity");
        check(DatatypeConstants.HOURS == DatatypeConstants.HOURS, "HOURS identity");
        check(DatatypeConstants.MINUTES == DatatypeConstants.MINUTES, "MINUTES identity");
        check(DatatypeConstants.SECONDS == DatatypeConstants.SECONDS, "SECONDS identity");
        check(DatatypeConstants.YEARS != DatatypeConstants.MONTHS, "distinct Field objects");
        check("YEARS".equals(DatatypeConstants.YEARS.toString()), "Field toString");

        testClassShape();
        testFieldConstantFamily();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: class shape + full Field constant family =================
    static void testClassShape() throws Exception {
        check(java.lang.reflect.Modifier.isFinal(DatatypeConstants.class.getModifiers()), "DatatypeConstants is final");
        check(java.lang.reflect.Modifier.isPublic(DatatypeConstants.class.getModifiers()), "is public");
        java.lang.reflect.Constructor<?>[] ctors = DatatypeConstants.class.getDeclaredConstructors();
        check(ctors.length == 1 && java.lang.reflect.Modifier.isPrivate(ctors[0].getModifiers()),
              "sole constructor is private (constant-only class)");
    }

    static void testFieldConstantFamily() {
        DatatypeConstants.Field[] fields = {
            DatatypeConstants.YEARS, DatatypeConstants.MONTHS, DatatypeConstants.DAYS,
            DatatypeConstants.HOURS, DatatypeConstants.MINUTES, DatatypeConstants.SECONDS,
        };
        String[] names = {"YEARS", "MONTHS", "DAYS", "HOURS", "MINUTES", "SECONDS"};
        for (int i = 0; i < fields.length; i++) {
            check(fields[i] != null, names[i] + " constant non-null");
            check(fields[i].getId() == i, names[i] + " getId()==" + i);
            check(names[i].equals(fields[i].toString()), names[i] + " toString exact");
        }
        // all six are mutually distinct singletons
        for (int i = 0; i < fields.length; i++)
            for (int j = i + 1; j < fields.length; j++)
                check(fields[i] != fields[j], names[i] + " distinct from " + names[j]);
        // Field class itself is final
        check(java.lang.reflect.Modifier.isFinal(DatatypeConstants.Field.class.getModifiers()),
              "DatatypeConstants.Field is final");
    }
}
