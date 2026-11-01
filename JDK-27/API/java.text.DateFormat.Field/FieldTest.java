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

    public static void main(String[] args) {
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
