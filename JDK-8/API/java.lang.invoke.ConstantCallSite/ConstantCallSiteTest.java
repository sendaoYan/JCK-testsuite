/*
 * @test
 * @summary java.lang.invoke.ConstantCallSite conformance: target immutability.
 * @run main ConstantCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class ConstantCallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstantCallSite() throws Throwable {
        MethodHandle target = MethodHandles.constant(String.class, "x");
        ConstantCallSite cs = new ConstantCallSite(target);
        check(cs instanceof CallSite, "is CallSite");
        check(cs.getTarget() == target, "getTarget");
        try {
            cs.setTarget(MethodHandles.constant(String.class, "y"));
            check(false, "setTarget should throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "setTarget throws UnsupportedOperationException");
        }
        check("x".equals((String) cs.dynamicInvoker().invoke()), "dynamicInvoker");
    }

    public static void main(String[] args) throws Throwable {
        testConstantCallSite();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
