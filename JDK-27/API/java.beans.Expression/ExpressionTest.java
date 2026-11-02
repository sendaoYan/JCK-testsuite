/*
 * @test
 * @summary java.beans.Expression conformance: execute and get value.
 * @run main ExpressionTest
 */

import java.beans.*;

public class ExpressionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        Expression e = new Expression("hello", "length", new Object[0]);
        e.execute();
        check(e.getValue() instanceof Integer, "value type");
        check(((Integer) e.getValue()).intValue() == 5, "length 5");
        Expression e2 = new Expression(null, System.class, "currentTimeMillis", new Object[0]);
        e2.execute();
        check(e2.getValue() instanceof Long, "currentTimeMillis");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
