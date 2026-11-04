/*
 * @test
 * @summary javax.print.attribute.standard.NumberUp conformance: integer attribute.
 * @run main NumberUpTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class NumberUpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        NumberUp v = new NumberUp(2);
        check(v.getValue() == 2, "getValue");
        check(v.getCategory() == NumberUp.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new NumberUp(2)), "equals");
        check(!v.equals(new NumberUp(4)), "not equals");
        check(v.hashCode() == new NumberUp(2).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
