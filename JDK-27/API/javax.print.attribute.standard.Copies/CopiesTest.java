/*
 * @test
 * @summary javax.print.attribute.standard.Copies conformance: integer attribute.
 * @run main CopiesTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class CopiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Copies v = new Copies(3);
        check(v.getValue() == 3, "getValue");
        check(v.getCategory() == Copies.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new Copies(3)), "equals");
        check(!v.equals(new Copies(5)), "not equals");
        check(v.hashCode() == new Copies(3).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
