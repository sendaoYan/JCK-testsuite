/*
 * @test
 * @summary java.lang.invoke.MutableCallSite conformance: mutable target.
 * @run main MutableCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MutableCallSite;

public class MutableCallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMutableCallSite() throws Throwable {
        MethodHandle first = MethodHandles.constant(String.class, "a");
        MutableCallSite cs = new MutableCallSite(first);
        check(cs instanceof CallSite, "is CallSite");
        check(cs.getTarget() == first, "getTarget");
        MethodHandle second = MethodHandles.constant(String.class, "b");
        cs.setTarget(second);
        check(cs.getTarget() == second, "setTarget");
        check("b".equals((String) cs.dynamicInvoker().invoke()), "dynamicInvoker");
    }

    public static void main(String[] args) throws Throwable {
        testMutableCallSite();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
