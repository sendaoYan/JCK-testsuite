/*
 * @test
 * @summary java.text.NumberFormat.Field conformance: constants/equality/hashCode.
 * @run main FieldTest
 */

import java.text.NumberFormat;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testField() {
        NumberFormat.Field integer = NumberFormat.Field.INTEGER;
        check(integer != null, "INTEGER");
        check(NumberFormat.Field.FRACTION != null, "FRACTION");
        check(NumberFormat.Field.EXPONENT != null, "EXPONENT");
        check(NumberFormat.Field.SIGN != null, "SIGN");
        check(NumberFormat.Field.DECIMAL_SEPARATOR != null, "DECIMAL_SEPARATOR");
        check(NumberFormat.Field.GROUPING_SEPARATOR != null, "GROUPING_SEPARATOR");
        check(NumberFormat.Field.PERCENT != null, "PERCENT");
        check(NumberFormat.Field.PERMILLE != null, "PERMILLE");
        check(NumberFormat.Field.CURRENCY != null, "CURRENCY");

        check(integer.equals(NumberFormat.Field.INTEGER), "equals");
        check(integer.hashCode() == NumberFormat.Field.INTEGER.hashCode(), "hashCode");
        check(!integer.equals(NumberFormat.Field.FRACTION), "distinct fields not equal");
        check(integer.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
