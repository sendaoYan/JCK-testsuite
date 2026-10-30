/*
 * @test
 * @summary JLS 15.20/15.21 (Java SE 8): relational operators compare numbers or
 *          references, instanceof tests the runtime type, and ==/!= compare
 *          primitives by value or references by identity.
 * @run main RelationalEqualityTest
 */

public class RelationalEqualityTest {
    public static void main(String[] args) {
        if (!(1 < 2) || !(2 <= 2) || !(3 > 2) || !(2 >= 2)) throw new AssertionError("numeric relational");
        if (!("a" == "a")) throw new AssertionError("string literal identity");
        if (new Object() == new Object()) throw new AssertionError("distinct objects differ");
        if (!new String("x").equals("x")) throw new AssertionError("equals vs ==");
        if (!(new Object() instanceof Object)) throw new AssertionError("instanceof");
        if (!("s" instanceof String)) throw new AssertionError("instanceof String");
        if (!(new int[1] instanceof Object)) throw new AssertionError("instanceof array");

        System.out.println("PASS");
    }
}
