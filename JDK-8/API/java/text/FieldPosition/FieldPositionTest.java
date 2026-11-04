/*
 * @test
 * @summary java.text.FieldPosition conformance: field/index/attribute constructors.
 * @run main FieldPositionTest
 */

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;

public class FieldPositionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFieldPosition() {
        FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD);
        check(fp.getField() == NumberFormat.INTEGER_FIELD, "getField");
        check(fp.getBeginIndex() == 0 && fp.getEndIndex() == 0, "default indexes zero");
        fp.setBeginIndex(1);
        fp.setEndIndex(3);
        check(fp.getBeginIndex() == 1 && fp.getEndIndex() == 3, "set/get indexes");

        FieldPosition fpAttr = new FieldPosition(DateFormat.Field.ERA);
        check(fpAttr.getFieldAttribute() == DateFormat.Field.ERA, "getFieldAttribute");
        check(fpAttr.getField() == -1, "getField -1 for attribute constructor");

        FieldPosition a = new FieldPosition(NumberFormat.INTEGER_FIELD);
        FieldPosition b = new FieldPosition(NumberFormat.INTEGER_FIELD);
        check(a.equals(b), "equals");
        check(a.hashCode() == b.hashCode(), "hashCode");
        check(fp.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testFieldPosition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
