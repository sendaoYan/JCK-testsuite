/*
 * @test
 * @summary javax.management.BadBinaryOpValueExpException conformance: full
 *          contract coverage. getExp returns the problematic ValueExp, toString
 *          includes it, and Exception hierarchy.
 * @run main BadBinaryOpValueExpExceptionTest
 */

import javax.management.BadBinaryOpValueExpException;
import javax.management.Query;
import javax.management.ValueExp;

public class BadBinaryOpValueExpExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        ValueExp exp = Query.value(7);
        BadBinaryOpValueExpException x = new BadBinaryOpValueExpException(exp);
        check(x instanceof Exception, "extends Exception");
        check(x.getExp() == exp, "getExp returns exp");
        check(x.toString().contains(exp.toString()), "toString includes exp");

        BadBinaryOpValueExpException n = new BadBinaryOpValueExpException(null);
        check(n.getExp() == null, "null exp allowed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
