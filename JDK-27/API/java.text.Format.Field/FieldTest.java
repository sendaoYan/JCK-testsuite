/*
 * @test
 * @summary java.text.Format.Field conformance: base class equality/hashCode/toString via
 *          concrete subclasses.
 * @run main FieldTest
 */

import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testField() {
        Format.Field era = DateFormat.Field.ERA;
        Format.Field integer = NumberFormat.Field.INTEGER;
        check(era != null, "ERA instance");
        check(integer != null, "INTEGER instance");
        check(era.equals(DateFormat.Field.ERA), "equals same field");
        check(!era.equals(integer), "distinct fields not equal");
        check(era.hashCode() == DateFormat.Field.ERA.hashCode(), "hashCode");
        check(era.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
