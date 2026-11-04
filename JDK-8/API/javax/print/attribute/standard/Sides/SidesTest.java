/*
 * @test
 * @summary javax.print.attribute.standard.Sides conformance: enum attribute.
 * @run main SidesTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class SidesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Sides v1 = Sides.ONE_SIDED;
        check(v1 instanceof Sides, "ONE_SIDED instanceof Sides");
        check(v1.getCategory() == Sides.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        Sides v2 = Sides.TWO_SIDED_LONG_EDGE;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(Sides.ONE_SIDED), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == Sides.ONE_SIDED.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
