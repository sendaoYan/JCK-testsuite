/*
 * @test
 * @summary javax.print.attribute.standard.DialogTypeSelection conformance: enum attribute.
 * @run main DialogTypeSelectionTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class DialogTypeSelectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        DialogTypeSelection v1 = DialogTypeSelection.NATIVE;
        check(v1 instanceof DialogTypeSelection, "NATIVE instanceof DialogTypeSelection");
        check(v1.getCategory() == DialogTypeSelection.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        DialogTypeSelection v2 = DialogTypeSelection.COMMON;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(DialogTypeSelection.NATIVE), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == DialogTypeSelection.NATIVE.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
