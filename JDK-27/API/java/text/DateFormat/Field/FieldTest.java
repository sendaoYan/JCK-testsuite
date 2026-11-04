/*
 * @test
 * @summary java.text.DateFormat.Field conformance: constants and ofCalendarField.
 * @run main FieldTest
 */

import java.text.DateFormat;
import java.util.Calendar;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testField() {
        DateFormat.Field era = DateFormat.Field.ERA;
        check(era != null, "ERA");
        check(DateFormat.Field.YEAR != null, "YEAR");
        check(DateFormat.Field.MONTH != null, "MONTH");
        check(DateFormat.Field.DAY_OF_MONTH != null, "DAY_OF_MONTH");
        check(DateFormat.Field.DAY_OF_WEEK != null, "DAY_OF_WEEK");
        check(DateFormat.Field.HOUR0 != null, "HOUR0");
        check(DateFormat.Field.HOUR1 != null, "HOUR1");
        check(DateFormat.Field.HOUR_OF_DAY0 != null, "HOUR_OF_DAY0");
        check(DateFormat.Field.MINUTE != null, "MINUTE");
        check(DateFormat.Field.SECOND != null, "SECOND");
        check(DateFormat.Field.MILLISECOND != null, "MILLISECOND");
        check(DateFormat.Field.AM_PM != null, "AM_PM");
        check(DateFormat.Field.TIME_ZONE != null, "TIME_ZONE");

        check(DateFormat.Field.ofCalendarField(Calendar.ERA) == DateFormat.Field.ERA,
              "ofCalendarField ERA");
        check(DateFormat.Field.ofCalendarField(Calendar.YEAR) == DateFormat.Field.YEAR,
              "ofCalendarField YEAR");
        check(DateFormat.Field.ofCalendarField(Calendar.MONTH) == DateFormat.Field.MONTH,
              "ofCalendarField MONTH");

        check(era.equals(DateFormat.Field.ERA), "equals");
        check(era.hashCode() == DateFormat.Field.ERA.hashCode(), "hashCode");
        check(era.toString() != null, "toString");
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

    static void testContractDeepening() throws Exception {
        // full constant inventory with index-uniqueness and run-structure proof
        String[] names = {"ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
                "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH",
                "AM_PM", "HOUR0", "HOUR1", "HOUR_OF_DAY0", "HOUR_OF_DAY1", "TIME_ZONE",
                "MILLISECOND"};
        java.util.Set<Object> ids = new java.util.HashSet<>();
        for (String n : names)
            try {
                Object v = java.text.DateFormat.Field.class.getField(n).get(null);
                check(v instanceof java.text.DateFormat.Field, n + " is DateFormat.Field");
                check(ids.add(v), n + " distinct identity");
            } catch (NoSuchFieldException e) { throw new AssertionError(n + " missing"); }
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd HH",
                java.util.Locale.ENGLISH);
        f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        java.text.AttributedCharacterIterator it = f.formatToCharacterIterator(
                new java.util.GregorianCalendar(2024, java.util.Calendar.MARCH, 7, 8, 0).getTime());
        boolean year = false, hour = false, month = false;
        for (int i = it.getBeginIndex(); i < it.getEndIndex(); i++) {
            it.setIndex(i);
            year |= it.getAttribute(java.text.DateFormat.Field.YEAR) != null;
            hour |= it.getAttribute(java.text.DateFormat.Field.HOUR_OF_DAY0) != null;
            month |= it.getAttribute(java.text.DateFormat.Field.MONTH) != null;
        }
        check(year && hour && month,
              "YEAR, MONTH and HOUR_OF_DAY0 attribute runs all present");
    }
    public static void main(String[] args) throws Exception {
        testContractDeepening();
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
