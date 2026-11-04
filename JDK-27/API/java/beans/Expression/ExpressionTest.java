/*
 * @test
 * @summary java.beans.Expression conformance: white-box structure + black-box behavior.
 * @run main ExpressionTest
 */

import java.beans.Expression;
import java.beans.Statement;

public class ExpressionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Expression");
        check(Statement.class.isAssignableFrom(c), "extends Statement");
    }

    static void testBlackBox() throws Exception {
        Expression e = new Expression("hello", "toUpperCase", new Object[0]);
        check("hello".equals(e.getTarget()), "target");
        check("toUpperCase".equals(e.getMethodName()), "methodName");
        check("HELLO".equals(e.getValue()), "getValue computes");

        Expression pre = new Expression("cached", "hello", "toUpperCase", new Object[0]);
        check("cached".equals(pre.getValue()), "pre-set value");
        check("cached".equals(pre.getValue()), "pre-set value stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
