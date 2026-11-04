/*
 * @test
 * @summary java.time.temporal.IsoFields conformance: QUARTER/DAY/WEEK based fields.
 * @run main IsoFieldsTest
 */
import java.time.LocalDate;
import java.time.temporal.IsoFields;

public class IsoFieldsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2020, 7, 1)) == 3, "QUARTER_OF_YEAR");
        check(IsoFields.DAY_OF_QUARTER.getFrom(LocalDate.of(2020, 1, 1)) == 1, "DAY_OF_QUARTER");
        check(IsoFields.WEEK_OF_WEEK_BASED_YEAR.getFrom(LocalDate.of(2020, 1, 1)) == 1, "WEEK_OF_WEEK_BASED_YEAR");
        check(IsoFields.WEEK_BASED_YEAR.getFrom(LocalDate.of(2020, 1, 1)) == 2020, "WEEK_BASED_YEAR");
        check(IsoFields.QUARTER_YEARS != null, "QUARTER_YEARS");
        check(IsoFields.WEEK_BASED_YEARS != null, "WEEK_BASED_YEARS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
