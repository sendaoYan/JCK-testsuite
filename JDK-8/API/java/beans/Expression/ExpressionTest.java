/*
 * @test
 * @summary java.beans.Expression conformance: getValue evaluates expression.
 * @run main ExpressionTest
 */

import java.beans.*;
public class ExpressionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Expression e = new Expression("hello", "length", new Object[0]);
        check(Integer.valueOf(5).equals(e.getValue()), "value");
        check("hello".equals(e.getTarget()), "target");
        check("length".equals(e.getMethodName()), "method name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
